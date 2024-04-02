package com.example.audioVideo.service.audioVideo;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.model.Attendee;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequestItem;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingWithAttendeesRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingWithAttendeesResult;
import com.amazonaws.services.chimesdkmeetings.model.DeleteMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.GetMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.Meeting;
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
import java.util.List;
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

            Meeting meeting = chimeMeeting.getMainRoom().getMeeting();
            Attendee attendee = createChimeAttendee(roomNumber, username);
            return new ChimeConnectionOptions(meeting, attendee);
        }
    }

    @Override
    public ConnectionOptions getConnectionOptions(int roomNumber, long groupId, String username) {
        synchronized (this) {
            ChimeMeeting chimeMeeting = this.addSubRoomToMeeting(roomNumber, groupId);

            Meeting meeting = chimeMeeting.getSubRooms().get(groupId).getMeeting();
            Attendee attendee = createChimeAttendee(roomNumber, groupId, username);

            return new ChimeConnectionOptions(meeting, attendee);
        }
   }

    @Override
    public void createMeetings(int roomNumber, Groups groups) {
        if (groups.main() != null) {
            List<String> usernames = userService
                    .getUsersByIds(roomNumber, groups.main().userIds())
                    .stream()
                    .map(User::getUsername)
                    .toList();
            this.createMeetingWithAttendees(roomNumber, usernames);
        }

        if (groups.privateTalk() != null && !groups.privateTalk().userIds().isEmpty()) {
            List<String> usernames = userService
                    .getUsersByIds(roomNumber, groups.privateTalk().userIds())
                    .stream()
                    .map(User::getUsername)
                    .toList();
            this.addSubRoomToMeetingWithAttendees(roomNumber, groups.privateTalk().id(), usernames);
        }

        if (groups.teamTalk() != null) {
            groups.teamTalk().forEach((Group group) -> {
                if (group.userIds().isEmpty()) {
                    return;
                }

                List<String> usernames = userService
                        .getUsersByIds(roomNumber, group.userIds())
                        .stream()
                        .map(User::getUsername)
                        .toList();
                this.addSubRoomToMeetingWithAttendees(roomNumber, group.id(), usernames);
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

   private ChimeMeeting createMeetingWithAttendees(int roomNumber, List<String> usernames) {
       return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
               .orElseGet(() -> {
                   ChimeRoom mainRoom = this.buildMainRoomWithAttendees(roomNumber, usernames);
                   ChimeMeeting chimeMeeting = new ChimeMeeting(
                           roomNumber,
                           mainRoom
                   );

                   return this.chimeMeetingRepository.save(chimeMeeting);
               });
   }

    private ChimeMeeting addSubRoomToMeetingWithAttendees(int roomNumber, long groupId, List<String> usernames) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map((ChimeMeeting chimeMeeting) -> {
                    ChimeRoom subRoom = this.buildSubRoomWithAttendees(roomNumber, groupId, usernames);
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

    private ChimeRoom buildMainRoomWithAttendees(int roomNumber, List<String> usernames) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseGet(() -> this.createChimeRoomWithAttendees(String.valueOf(roomNumber), usernames));
    }

    private ChimeRoom buildSubRoomWithAttendees(int roomNumber, long groupId, List<String> usernames) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseGet(() -> this.createChimeRoomWithAttendees(roomNumber + "_" + groupId, usernames));
    }

    private ChimeRoom buildSubRoom(int roomNumber, long groupId) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map((ChimeMeeting chimeMeeting) -> chimeMeeting.getSubRooms().get(groupId))
                .orElseGet(() -> this.createChimeRoom(roomNumber + "_" + groupId));
    }

    private Attendee attendeeToMainRoom(int roomNumber, String username) {
        return this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .map(ChimeRoom::getAttendee)
                .orElseThrow()
                .stream()
                .filter((Attendee attendee) ->
                        attendee.getExternalUserId().equals(username)
                )
                .findAny()
                .orElseGet(() -> this.createChimeAttendee(roomNumber, username));
    }

    private ChimeRoom createChimeRoom(String roomName) {
        CreateMeetingRequest request = new CreateMeetingRequest();
        request.setExternalMeetingId(roomName);
        request.setMediaRegion("us-east-1");

        Meeting meeting = chimeClient.createMeeting(request).getMeeting();

        return new ChimeRoom(
                meeting,
                new ArrayList<>()
        );
    }

    private ChimeRoom createChimeRoomWithAttendees(String roomName, List<String> usernames) {
        List<CreateAttendeeRequestItem> attendeeRequestItems = usernames.stream()
                .map((String username) -> {
                    CreateAttendeeRequestItem  createAttendeeRequestItem=  new CreateAttendeeRequestItem();
                    createAttendeeRequestItem.setExternalUserId(username);
                    return createAttendeeRequestItem;
                })
                .toList();

        CreateMeetingWithAttendeesRequest request = new CreateMeetingWithAttendeesRequest();
        request.setExternalMeetingId(roomName);
        request.withAttendees(attendeeRequestItems);
        request.setMediaRegion("us-east-1");

        CreateMeetingWithAttendeesResult createMeetingWithAttendeesResult = chimeClient.createMeetingWithAttendees(request);
        Meeting meeting = createMeetingWithAttendeesResult.getMeeting();
        List<Attendee> attendees = createMeetingWithAttendeesResult.getAttendees();

        return new ChimeRoom(
                meeting,
                attendees
        );
    }

    private CreateAttendeeResult createChimeAttendee(String meetingId, String username) {
        CreateAttendeeRequest request = new CreateAttendeeRequest();
        request.setExternalUserId(username);
        request.setMeetingId(meetingId);

        return chimeClient.createAttendee(request);
    }

    private Attendee createChimeAttendee(int roomNumber, String username) {
        ChimeRoom mainRoom = this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseThrow();

        try {
            GetMeetingRequest getMeetingRequest = new GetMeetingRequest();
            getMeetingRequest.setMeetingId(mainRoom.getMeeting().getMeetingId());
            //* if meeting exists, available and not ended *//
            this.chimeClient.getMeeting(getMeetingRequest);
        } catch (NotFoundException exception) {
            mainRoom = this.createChimeRoom(String.valueOf(roomNumber));
            this.chimeMeetingRepository.setMainRoom(roomNumber, mainRoom);
        }

        String meetingId = mainRoom.getMeeting().getMeetingId();
        Attendee attendee = createChimeAttendee(meetingId, username).getAttendee();

        this.chimeMeetingRepository.addAttendee(roomNumber, attendee);

        return attendee;
    }

    private Attendee createChimeAttendee(int roomNumber, long groupId, String username) {
        ChimeRoom subRoom = this.chimeMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getSubRooms)
                .map((Map<Long, ChimeRoom> subRooms) -> subRooms.get(groupId))
                .orElseThrow();

        try {
            GetMeetingRequest getMeetingRequest = new GetMeetingRequest();
            getMeetingRequest.setMeetingId(subRoom.getMeeting().getMeetingId());
            //* if meeting exists, available and not ended *//
            this.chimeClient.getMeeting(getMeetingRequest);
        } catch (NotFoundException exception) {
            subRoom = this.createChimeRoom(String.valueOf(roomNumber));
            this.chimeMeetingRepository.addSubRoom(roomNumber, groupId, subRoom);
        }

        String meetingId = subRoom.getMeeting().getMeetingId();
        Attendee attendee = createChimeAttendee(meetingId, username).getAttendee();

        this.chimeMeetingRepository.addAttendee(roomNumber, attendee);

        return attendee;
    }

    private void deleteRoom(ChimeRoom room) {
        String meetingId = room.getMeeting().getMeetingId();

        DeleteMeetingRequest deleteMeetingRequest = new DeleteMeetingRequest();
        deleteMeetingRequest.setMeetingId(meetingId);

        this.chimeClient.deleteMeeting(deleteMeetingRequest);
    }
}
