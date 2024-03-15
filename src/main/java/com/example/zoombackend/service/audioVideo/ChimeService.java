package com.example.zoombackend.service.audioVideo;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.amazonaws.services.chimesdkmeetings.model.GetMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.NotFoundException;
import com.example.zoombackend.model.Group;
import com.example.zoombackend.model.connectionOptions.ChimeConnectionOptions;
import com.example.zoombackend.model.connectionOptions.ConnectionOptions;
import com.example.zoombackend.persistance.entity.ChimeMeeting;
import com.example.zoombackend.persistance.entity.ChimeRoom;
import com.example.zoombackend.persistance.repository.AmazonMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChimeService implements AudioVideoService {

   private final AmazonChimeSDKMeetings chimeClient;
   private final AmazonMeetingRepository amazonMeetingRepository;

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
      groups.forEach((Group group) -> {
//         this.amazonMeetingRepository.findByRoomNumberAndGroupId(roomNumber, group.id())
//                 .map(
//                         (AmazonMeetingEntity existingAmazonMeeting) -> {
//                            String meetingId = existingAmazonMeeting.getCreateMeetingResult().getMeeting().getMeetingId();
//                            ListAttendeesRequest listAttendeesRequest = new ListAttendeesRequest();
//                            listAttendeesRequest.setMeetingId(meetingId);
//
//                            this.chimeClient
//                                    .listAttendees(listAttendeesRequest)
//                                    .getAttendees()
//                                    .forEach((Attendee attendee) -> {
//                                       DeleteAttendeeRequest deleteAttendeeRequest = new DeleteAttendeeRequest();
//                                       deleteAttendeeRequest.setMeetingId(meetingId);
//                                       deleteAttendeeRequest.setAttendeeId(attendee.getAttendeeId());
//                                       this.chimeClient.deleteAttendee(deleteAttendeeRequest);
//                                    });
//
//                            existingAmazonMeeting.getAmazonAttendees().clear();
//                            return existingAmazonMeeting;
//                         }
//                 ).orElseGet(() -> createMainMeeting(roomNumber, group.id()))
      });
   }

   private ChimeMeeting createMeeting(int roomNumber) {
       return this.amazonMeetingRepository.findByRoomNumber(roomNumber)
               .orElseGet(() -> {
                   ChimeRoom mainRoom = this.buildMainRoom(roomNumber);
                   ChimeMeeting chimeMeeting = new ChimeMeeting(
                           roomNumber,
                           mainRoom
                   );

                   return this.amazonMeetingRepository.save(chimeMeeting);
               });
   }

   private ChimeMeeting addSubRoomToMeeting(int roomNumber, long groupId) {
       return this.amazonMeetingRepository.findByRoomNumber(roomNumber)
               .map((ChimeMeeting chimeMeeting) -> {
                   ChimeRoom subRoom = this.buildSubRoom(roomNumber, groupId);
                   this.amazonMeetingRepository.addSubRoom(roomNumber, groupId, subRoom);
                   return chimeMeeting;
               })
               .orElseThrow();
   }

    private ChimeRoom buildMainRoom(int roomNumber) {
        return this.amazonMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseGet(() -> this.createChimeRoom(String.valueOf(roomNumber)));
    }

    private ChimeRoom buildSubRoom(int roomNumber, long groupId) {
        return this.amazonMeetingRepository.findByRoomNumber(roomNumber)
                .map((ChimeMeeting chimeMeeting) -> chimeMeeting.getSubRooms().get(groupId))
                .orElseGet(() -> this.createChimeRoom(roomNumber + "_" + groupId));
    }

    private CreateAttendeeResult attendeeToMainRoom(int roomNumber, String username) {
        return this.amazonMeetingRepository.findByRoomNumber(roomNumber)
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

//    private CreateAttendeeResult attendeeToSubRoom(int roomNumber, long groupId) {
//
//    }

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
        ChimeRoom mainRoom = this.amazonMeetingRepository.findByRoomNumber(roomNumber)
                .map(ChimeMeeting::getMainRoom)
                .orElseThrow();

        try {
            GetMeetingRequest getMeetingRequest = new GetMeetingRequest();
            getMeetingRequest.setMeetingId(mainRoom.getCreateMeetingResult().getMeeting().getMeetingId());
            //* if meeting exists, available and not ended *//
            this.chimeClient.getMeeting(getMeetingRequest);
        } catch (NotFoundException exception) {
            mainRoom = this.createChimeRoom(String.valueOf(roomNumber));
            this.amazonMeetingRepository.setMainRoom(roomNumber, mainRoom);
        }

        String meetingId = mainRoom.getCreateMeetingResult().getMeeting().getMeetingId();
        CreateAttendeeResult createAttendeeResult = createChimeAttendee(meetingId, username);

        this.amazonMeetingRepository.addAttendee(roomNumber, createAttendeeResult);

        return createAttendeeResult;
    }

    private CreateAttendeeResult createChimeAttendee(int roomNumber, long groupId, String username) {
        ChimeRoom subRoom = this.amazonMeetingRepository.findByRoomNumber(roomNumber)
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
            this.amazonMeetingRepository.addSubRoom(roomNumber, groupId, subRoom);
        }

        String meetingId = subRoom.getCreateMeetingResult().getMeeting().getMeetingId();
        CreateAttendeeResult createAttendeeResult = createChimeAttendee(meetingId, username);

        this.amazonMeetingRepository.addAttendee(roomNumber, createAttendeeResult);

        return createAttendeeResult;
    }
/*
   *//**
    * Creates new meeting or gets already existing one by roomNumber
    * *//*
   private AmazonMeetingEntity createMeeting(int roomNumber, long groupId) {
      return this.amazonMeetingRepository.findByRoomNumberAndGroupId(roomNumber, groupId).orElseGet(() -> {
         CreateMeetingRequest request = new CreateMeetingRequest();
         request.setExternalMeetingId(String.valueOf(roomNumber));
         request.setMediaRegion("us-east-1");

         CreateMeetingResult createMeetingResult = chimeClient.createMeeting(request);

         AmazonMeetingEntity amazonMeeting = AmazonMeetingEntity
                 .builder()
                 .roomNumber(roomNumber)
                 .createMeetingResult(createMeetingResult)
                 .build();

         return this.amazonMeetingRepository.save(amazonMeeting, groupId);
      });
   }

   *//**
    * Creates new attendee or gets already existing one by roomNumber, groupId and username
    * *//*
   private AmazonAttendeeEntity createAttendee(int roomNumber, long groupId, String username) {
      AmazonMeetingEntity amazonMeeting = this.amazonMeetingRepository.findByRoomNumberAndGroupId(roomNumber, groupId)
              .map((AmazonMeetingEntity foundAmazonMeeting) -> {
                 try {
                    GetMeetingRequest getMeetingRequest = new GetMeetingRequest();
                    getMeetingRequest.setMeetingId(foundAmazonMeeting.getCreateMeetingResult().getMeeting().getMeetingId());
                    *//* if meeting exists, available and not ended *//*
                    this.chimeClient.getMeeting(getMeetingRequest);
                    return foundAmazonMeeting;
                 } catch (NotFoundException exception) {
                    return this.updateMeeting(roomNumber, groupId);
                 }
              })
              .orElseThrow(
                      () -> new IllegalArgumentException("Room " + roomNumber + " not found")
              );

      return amazonMeeting.getAmazonAttendees()
              .stream()
              .filter((AmazonAttendeeEntity amazonAttendee) ->
                      amazonAttendee.getCreateAttendeeResult()
                              .getAttendee()
                              .getExternalUserId()
                              .equals(username)
              ).findAny()
              .orElseGet(() -> this.createAttendee(amazonMeeting, username));
   }

   private AmazonAttendeeEntity createAttendee(AmazonMeetingEntity amazonMeeting, String username) {
      String meetingId = amazonMeeting.getCreateMeetingResult().getMeeting().getMeetingId();

      CreateAttendeeRequest request = new CreateAttendeeRequest();
      request.setExternalUserId(username);
      request.setMeetingId(meetingId);

      CreateAttendeeResult createAttendeeResult = chimeClient.createAttendee(request);

      AmazonAttendeeEntity amazonAttendee = AmazonAttendeeEntity
              .builder()
              .amazonMeeting(amazonMeeting)
              .createAttendeeResult(createAttendeeResult)
              .build();

      amazonMeeting.getAmazonAttendees().add(amazonAttendee);

      return amazonAttendee;
   }

   private AmazonMeetingEntity updateMeeting(int roomNumber, long groupId) {
      this.amazonMeetingRepository.remove(roomNumber);
      return this.createMeeting(roomNumber, groupId);
   }*/
}
