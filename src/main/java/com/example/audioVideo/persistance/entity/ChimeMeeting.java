package com.example.audioVideo.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ChimeMeeting {
    private int roomNumber;
    private ChimeRoom mainRoom;
    private final Map<Long, ChimeRoom> subRooms = new HashMap<>();
}
