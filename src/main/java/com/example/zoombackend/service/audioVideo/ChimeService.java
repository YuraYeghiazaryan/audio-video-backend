package com.example.zoombackend.service.audioVideo;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.amazonaws.services.chimesdkmeetings.model.DeleteMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.GetMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.NotFoundException;
import com.example.zoombackend.model.Group;
import com.example.zoombackend.model.connectionOptions.ChimeConnectionOptions;
import com.example.zoombackend.model.connectionOptions.ConnectionOptions;
import com.example.zoombackend.persistance.entity.ChimeMeeting;
import com.example.zoombackend.persistance.entity.ChimeRoom;
import com.example.zoombackend.persistance.repository.ChimeMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChimeService implements AudioVideoService {

   private final AmazonChimeSDKMeetings chimeClient;
   private final ChimeMeetingRepository chimeMeetingRepository;

    @Override
    public ConnectionOptions getConnectionOptions(int roomNumber, String username) {
        ChimeMeeting chimeMeeting = this.createMeeting(roomNumber);

        CreateMeetingResult createMeetingResult = chimeMeeting.getMainRoom().getCreateMeetingResult();
        CreateAttendeeResult createAttendeeResult = createChimeAttendee(roomNumber, username);

        return new ChimeConnectionOptions(createMeetingResult, createAttendeeResult);
    }

    @Override
   public ConnectionOptions getConnectionOptions(int roomNumber, long groupId, String username) {
        ChimeMeeting chimeMeeting = this.addSubRoomToMeeting(roomNumber, groupId);

        CreateMeetingResult createMeetingResult = chimeMeeting.getMainRoom().getCreateMeetingResult();
        CreateAttendeeResult createAttendeeResult = createChimeAttendee(roomNumber, groupId, username);

        return new ChimeConnectionOptions(createMeetingResult, createAttendeeResult);
   }

    @Override
   public void audioVideoGroupsChanged(int roomNumber, List<Group> groups) {
        this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .ifPresentOrElse((ChimeMeeting chimeMeeting) -> {
                    chimeMeeting.getSubRooms().values().forEach(this::deleteRoom);
                }, () -> {
                    throw new IllegalStateException();
                });

        groups.forEach((Group group) -> this.addSubRoomToMeeting(roomNumber, group.id()));
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

        CreateMeetingResult createMeetingResult = chimeClient.createMeeting(request);;

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
