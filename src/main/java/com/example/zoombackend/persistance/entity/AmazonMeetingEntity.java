package com.example.zoombackend.persistance.entity;

import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AmazonMeetingEntity {
    private int roomNumber;
    private CreateMeetingResult createMeetingResult;
    private final List<AmazonAttendeeEntity> amazonAttendees = new ArrayList<>();
}
