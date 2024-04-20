package com.example.audioVideo.persistance.audioVideo.chime.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChimeSession {
    private final Map<String, ChimeRoom> roomMap = new HashMap<>();

    public Optional<ChimeRoom> getRoomByName(String roomName) {
        return Optional.ofNullable(roomMap.get(roomName));
    }

    public void addRoom(String roomName, ChimeRoom chimeRoom) {
        roomMap.put(roomName, chimeRoom);
    }
}
