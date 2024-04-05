package com.example.audioVideo.persistance.audioVideo.opentok.repository;

import com.example.audioVideo.persistance.audioVideo.opentok.entity.OpenTokSession;
import com.opentok.Session;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class OpenTokSessionRepository {
    private final Map<Integer, OpenTokSession> sessions = new HashMap<>();

    public OpenTokSession saveSession(int roomNumber, Session session) {
        OpenTokSession openTokSession = OpenTokSession
                .builder()
                .roomNumber(roomNumber)
                .session(session)
                .build();

        sessions.put(roomNumber, openTokSession);
        return openTokSession;
    }

    public Optional<OpenTokSession> findSessionByRoomNumber(int roomNumber) {
        return Optional.ofNullable(sessions.get(roomNumber));
    }
}
