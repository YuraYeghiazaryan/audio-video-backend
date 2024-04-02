package com.example.audioVideo.model.connectionOptions;

import com.amazonaws.services.chimesdkmeetings.model.Attendee;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.amazonaws.services.chimesdkmeetings.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class ChimeConnectionOptions extends ConnectionOptions {
    private Meeting meeting;
    private Attendee attendee;
}