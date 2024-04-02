package com.example.audioVideo.persistance.entity;

import com.amazonaws.services.chimesdkmeetings.model.Attendee;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.amazonaws.services.chimesdkmeetings.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChimeRoom {
    private Meeting meeting;
    private final List<Attendee> attendee;
}
