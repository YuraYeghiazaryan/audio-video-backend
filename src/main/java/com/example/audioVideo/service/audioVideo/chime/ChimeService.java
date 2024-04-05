package com.example.audioVideo.service.audioVideo.chime;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.model.Attendee;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequestItem;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingWithAttendeesRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingWithAttendeesResult;
import com.amazonaws.services.chimesdkmeetings.model.Meeting;
import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.Role;
import com.example.audioVideo.model.Team;
import com.example.audioVideo.model.User;
import com.example.audioVideo.model.connectionOptions.ChimeConnectionOptions;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import com.example.audioVideo.persistance.entity.ChimeRoom;
import com.example.audioVideo.persistance.repository.ChimeSessionRepository;
import com.example.audioVideo.service.UserService;
import com.example.audioVideo.service.audioVideo.AudioVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChimeService implements AudioVideoService {
    private final AmazonChimeSDKMeetings chimeClient;
    private final ChimeSessionRepository chimeSessionRepository;
    private final UserService userService;
    private final ChimeUtilService chimeUtilService;

    @Override
    public ConnectionOptions getConnectionOptions(int roomNumber, String roomName, String username) {
        User user = userService.getUserByUsername(roomNumber, username).orElseThrow();

        ChimeRoom room = chimeSessionRepository
                .findRoomByName(roomNumber, roomName)
                .orElseGet(() -> {
                    if (user.getRole() != Role.TEACHER) {
                        throw new RuntimeException("Non teacher user can't create meeting");
                    }
                    Meeting meeting = this.createMeeting(roomName);

                    return chimeSessionRepository.createRoomForSession(roomNumber, roomName, new ChimeRoom(meeting, new HashMap<>()));
                });

        Meeting meeting = room.getMeeting();
        Attendee attendee = room.getAttendees().get(username);

        if (attendee == null) {
            Attendee newAttendee = this.createAttendee(meeting, username);
            attendee = chimeSessionRepository.addAttendeeToRoom(roomNumber, roomName, newAttendee);
        }

        return new ChimeConnectionOptions(meeting, attendee);
    }

    @Override
    public void breakRoomIntoGroups(int roomNumber, Groups groups) {
        if (groups.main() != null) {
            Set<String> usernames = userService.getUsernames(roomNumber, groups.main().getUserIds());
            String roomName = chimeUtilService.buildMainRoomName(roomNumber);

            synchronizeRoomAttendees(roomNumber, roomName, usernames);
        }

        if (groups.privateTalk() != null) {
            Set<String> usernames = userService.getUsernames(roomNumber, groups.privateTalk().getUserIds());
            String roomName = chimeUtilService.buildPrivateTalkRoomName(roomNumber);

            synchronizeRoomAttendees(roomNumber, roomName, usernames);
        }

        if (groups.teamTalk() != null) {
            groups.teamTalk().forEach((Team team) -> {
                Set<String> usernames = userService.getUsernames(roomNumber, team.getUserIds());
                String roomName = chimeUtilService.buildTeamTalkRoomName(roomNumber, team.getId());

                synchronizeRoomAttendees(roomNumber, roomName, usernames);
            });
        }
    }

    /**
    * remove all attendees that are not in given meeting and add absent attendees
    * */
    private void synchronizeRoomAttendees(int roomNumber, String roomName, Set<String> usernames) {
        ChimeRoom room = chimeSessionRepository.findRoomByName(roomNumber, roomName)
                .orElseGet(() -> {
                    ChimeRoom newRoom = this.createMeetingWithAttendees(roomName, usernames);

                    return chimeSessionRepository.createRoomForSession(roomNumber, roomName, newRoom);
                });

        Map<String, Attendee> attendees = room.getAttendees().values().stream().filter((Attendee attendee) -> {
            boolean shouldBeRemoved = !usernames.contains(attendee.getExternalUserId());

//            if (shouldBeRemoved) {
//                DeleteAttendeeRequest deleteAttendeeRequest = new DeleteAttendeeRequest();
//                deleteAttendeeRequest.setMeetingId(room.getMeeting().getMeetingId());
//                deleteAttendeeRequest.setAttendeeId(attendee.getAttendeeId());
//
//                chimeClient.deleteAttendee(deleteAttendeeRequest);
//            }

            return !shouldBeRemoved;
        }).collect(
                Collectors.toMap(Attendee::getExternalUserId, Function.identity())
        );

        usernames.forEach((String username) -> {
            if (attendees.get(username) != null) {
                return;
            }

            Attendee newAttendee = createAttendee(room.getMeeting(), username);
            newAttendee = chimeSessionRepository.addAttendeeToRoom(roomNumber, roomName, newAttendee);

            attendees.put(username, newAttendee);
        });

        room.setAttendees(attendees);
    }

    private Meeting createMeeting(String roomName) {
        CreateMeetingRequest request = new CreateMeetingRequest();
        request.setExternalMeetingId(roomName);
        request.setMediaRegion("us-east-1");

        return chimeClient.createMeeting(request).getMeeting();
    }

    private ChimeRoom createMeetingWithAttendees(String roomName, Set<String> usernames) {
        List<CreateAttendeeRequestItem> createAttendeeRequestItems = usernames.stream()
                .map((String username) -> {
                    CreateAttendeeRequestItem createAttendeeRequestItem = new CreateAttendeeRequestItem();
                    createAttendeeRequestItem.setExternalUserId(username);
                    return createAttendeeRequestItem;
                }).toList();

        CreateMeetingWithAttendeesRequest request = new CreateMeetingWithAttendeesRequest();
        request.setExternalMeetingId(roomName);
        request.withAttendees(createAttendeeRequestItems);
        request.setMediaRegion("us-east-1");

        CreateMeetingWithAttendeesResult createMeetingWithAttendeesResult = chimeClient.createMeetingWithAttendees(request);

        Meeting meeting = createMeetingWithAttendeesResult.getMeeting();
        Map<String, Attendee> attendees = createMeetingWithAttendeesResult
                .getAttendees()
                .stream()
                .collect(
                        Collectors.toMap(Attendee::getExternalUserId, Function.identity())
                );

        return new ChimeRoom(meeting, attendees);
    }

    private Attendee createAttendee(Meeting meeting, String username) {
        CreateAttendeeRequest request = new CreateAttendeeRequest();
        request.setExternalUserId(username);
        request.setMeetingId(meeting.getMeetingId());

        return chimeClient.createAttendee(request).getAttendee();
    }
}
