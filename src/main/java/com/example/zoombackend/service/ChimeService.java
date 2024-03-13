package com.example.zoombackend.service;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.example.zoombackend.persistance.entity.AmazonAttendeeEntity;
import com.example.zoombackend.persistance.entity.AmazonMeetingEntity;
import com.example.zoombackend.persistance.repository.AmazonMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChimeService {

   private final AmazonChimeSDKMeetings chimeClient;
   private final AmazonMeetingRepository amazonMeetingRepository;

   /**
    * Creates new meeting or gets already existing one by roomNumber
    * */
   public AmazonMeetingEntity createMeeting(int roomNumber) {
      return amazonMeetingRepository.findByRoomNumber(roomNumber).orElseGet(() -> {
         CreateMeetingRequest request = new CreateMeetingRequest();
         request.setExternalMeetingId(String.valueOf(roomNumber));
         request.setMediaRegion("us-east-1");

         CreateMeetingResult createMeetingResult = chimeClient.createMeeting(request);

         AmazonMeetingEntity amazonMeeting = AmazonMeetingEntity
                 .builder()
                 .roomNumber(roomNumber)
                 .createMeetingResult(createMeetingResult)
                 .build();

         return this.amazonMeetingRepository.save(amazonMeeting);
      });
   }

   /**
    * Creates new attendee or gets already existing one by roomNumber and username
    * */
   public AmazonAttendeeEntity createAttendee(int roomNumber, String username) {
      AmazonMeetingEntity amazonMeeting = this.amazonMeetingRepository.findByRoomNumber(roomNumber)
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
}
