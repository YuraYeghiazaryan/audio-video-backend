package com.example.zoombackend.persistance.entity;

import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmazonAttendeeEntity {
    private AmazonMeetingEntity amazonMeeting;
    private CreateAttendeeResult createAttendeeResult;
}
