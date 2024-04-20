package com.example.audioVideo.persistance.audioVideo.chime.entity;

import com.amazonaws.services.chimesdkmeetings.model.Attendee;
import com.amazonaws.services.chimesdkmeetings.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ChimeRoom {
    private Meeting meeting;
    private Map<String, Attendee> attendees;
}
