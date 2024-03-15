package com.example.zoombackend.persistance.entity;

import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeRequest;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChimeRoom {
    private CreateMeetingResult createMeetingResult;
    private final List<CreateAttendeeResult> createAttendeeRequests;
}
