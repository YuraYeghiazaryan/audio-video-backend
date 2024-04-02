package com.example.audioVideo.service.audioVideo;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.amazonaws.services.chimesdkmeetings.model.DeleteMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.GetMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.NotFoundException;
import com.example.audioVideo.model.Group;
import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.User;
import com.example.audioVideo.model.connectionOptions.ChimeConnectionOptions;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import com.example.audioVideo.persistance.entity.ChimeMeeting;
import com.example.audioVideo.persistance.entity.ChimeRoom;
import com.example.audioVideo.persistance.repository.ChimeMeetingRepository;
import com.example.audioVideo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChimeService implements AudioVideoService {
    private final AmazonChimeSDKMeetings chimeClient;
    private final ChimeMeetingRepository chimeMeetingRepository;
    private final UserService userService;

    @Override
    public ConnectionOptions getConnectionOptions(int roomNumber, String username) {
        synchronized (this) {
            ChimeMeeting chimeMeeting = this.createMeeting(roomNumber);

            CreateMeetingResult createMeetingResult = chimeMeeting.getMainRoom().getCreateMeetingResult();
            CreateAttendeeResult createAttendeeResult = createChimeAttendee(roomNumber, username);
            return new ChimeConnectionOptions(createMeetingResult, createAttendeeResult);
        }
    }

    @Override
    public ConnectionOptions getConnectionOptions(int roomNumber, long groupId, String username) {
        synchronized (this) {
            ChimeMeeting chimeMeeting = this.addSubRoomToMeeting(roomNumber, groupId);

            CreateMeetingResult createMeetingResult = chimeMeeting.getSubRooms().get(groupId).getCreateMeetingResult();
            CreateAttendeeResult createAttendeeResult = createChimeAttendee(roomNumber, groupId, username);

            return new ChimeConnectionOptions(createMeetingResult, createAttendeeResult);
        }
   }

    @Override
    public void createMeetings(int roomNumber, Groups groups) {
        if (groups.main() != null) {
            this.createMeeting(roomNumber);
            userService
                    .getUsersByIds(roomNumber, groups.main().userIds())
                    .forEach((User user) -> this.createChimeAttendee(roomNumber, user.getUsername()));
        }

        if (groups.privateTalk() != null && !groups.privateTalk().userIds().isEmpty()) {
            this.addSubRoomToMeeting(roomNumber, groups.privateTalk().id());
            userService
                    .getUsersByIds(roomNumber, groups.privateTalk().userIds())
                    .forEach((User user) -> this.createChimeAttendee(roomNumber, groups.privateTalk().id(), user.getUsername()));
        }

        if (groups.teamTalk() != null) {
            groups.teamTalk().forEach((Group group) -> {
                if (group.userIds().isEmpty()) {
                    return;
                }

                this.addSubRoomToMeeting(roomNumber, group.id());
                userService
                        .getUsersByIds(roomNumber, group.userIds())
                        .forEach((User user) -> this.createChimeAttendee(roomNumber, group.id(), user.getUsername()));
            });
        }
    }

    private ChimeMeeting createMeeting(int roomNumber) {
       return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
               .orElseGet(() -> {
                   ChimeRoom mainRoom = this.buildMainRoom(roomNumber);
                   ChimeMeeting chimeMeeting = new ChimeMeeting(
                           roomNumber,
                           mainRoom
                   );

                   return this.chimeMeetingRepository.save(chimeMeeting);
               });
   }

    private ChimeMeeting addSubRoomToMeeting(int roomNumber, long groupId) {
       return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
               .map((ChimeMeeting chimeMeeting) -> {
                   ChimeRoom subRoom = this.buildSubRoom(roomNumber, groupId);
                   this.chimeMeetingRepository.addSubRoom(roomNumber, groupId, subRoom);
                   return chimeMeeting;
               })
               .orElseThrow();
   }

    private ChimeRoom buildMainRoom(int roomNumber) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseGet(() -> this.createChimeRoom(String.valueOf(roomNumber)));
    }

    private ChimeRoom buildSubRoom(int roomNumber, long groupId) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map((ChimeMeeting chimeMeeting) -> chimeMeeting.getSubRooms().get(groupId))
                .orElseGet(() -> this.createChimeRoom(roomNumber + "_" + groupId));
    }

    private CreateAttendeeResult attendeeToMainRoom(int roomNumber, String username) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .map(ChimeRoom::getCreateAttendeeRequests)
                .orElseThrow()
                .stream()
                .filter((CreateAttendeeResult createAttendeeResult) ->
                        createAttendeeResult.getAttendee().getExternalUserId().equals(username)
                )
                .findAny()
                .orElseGet(() -> this.createChimeAttendee(roomNumber, username));
    }

    private ChimeRoom createChimeRoom(String roomName) {
        CreateMeetingRequest request = new CreateMeetingRequest();
        request.setExternalMeetingId(roomName);
        request.setMediaRegion("us-east-1");

        CreateMeetingResult createMeetingResult = chimeClient.createMeeting(request);

        return new ChimeRoom(
                createMeetingResult,
                new ArrayList<>()
        );
    }

    private CreateAttendeeResult createChimeAttendee(String meetingId, String username) {
        CreateAttendeeRequest request = new CreateAttendeeRequest();
        request.setExternalUserId(username);
        request.setMeetingId(meetingId);

        return chimeClient.createAttendee(request);
    }

    private CreateAttendeeResult createChimeAttendee(int roomNumber, String username) {
        ChimeRoom mainRoom = this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseThrow();

        try {
            GetMeetingRequest getMeetingRequest = new GetMeetingRequest();
            getMeetingRequest.setMeetingId(mainRoom.getCreateMeetingResult().getMeeting().getMeetingId());
            //* if meeting exists, available and not ended *//
            this.chimeClient.getMeeting(getMeetingRequest);
        } catch (NotFoundException exception) {
            mainRoom = this.createChimeRoom(String.valueOf(roomNumber));
            this.chimeMeetingRepository.setMainRoom(roomNumber, mainRoom);
        }

        String meetingId = mainRoom.getCreateMeetingResult().getMeeting().getMeetingId();
        CreateAttendeeResult createAttendeeResult = createChimeAttendee(meetingId, username);

        this.chimeMeetingRepository.addAttendee(roomNumber, createAttendeeResult);

        return createAttendeeResult;
    }

    private CreateAttendeeResult createChimeAttendee(int roomNumber, long groupId, String username) {
        ChimeRoom subRoom = this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getSubRooms)
                .map((Map<Long, ChimeRoom> subRooms) -> subRooms.get(groupId))
                .orElseThrow();

        try {
            GetMeetingRequest getMeetingRequest = new GetMeetingRequest();
            getMeetingRequest.setMeetingId(subRoom.getCreateMeetingResult().getMeeting().getMeetingId());
            //* if meeting exists, available and not ended *//
            this.chimeClient.getMeeting(getMeetingRequest);
        } catch (NotFoundException exception) {
            subRoom = this.createChimeRoom(String.valueOf(roomNumber));
            this.chimeMeetingRepository.addSubRoom(roomNumber, groupId, subRoom);
        }

        String meetingId = subRoom.getCreateMeetingResult().getMeeting().getMeetingId();
        CreateAttendeeResult createAttendeeResult = createChimeAttendee(meetingId, username);

        this.chimeMeetingRepository.addAttendee(roomNumber, createAttendeeResult);

        return createAttendeeResult;
    }

    private void deleteRoom(ChimeRoom room) {
        String meetingId = room.getCreateMeetingResult().getMeeting().getMeetingId();

        DeleteMeetingRequest deleteMeetingRequest = new DeleteMeetingRequest();
        deleteMeetingRequest.setMeetingId(meetingId);

        this.chimeClient.deleteMeeting(deleteMeetingRequest);
    }
}
