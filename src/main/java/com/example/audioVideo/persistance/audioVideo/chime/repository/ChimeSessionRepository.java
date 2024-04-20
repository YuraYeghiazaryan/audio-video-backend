package com.example.audioVideo.persistance.audioVideo.chime.repository;

import com.amazonaws.services.chimesdkmeetings.model.Attendee;
import com.example.audioVideo.persistance.audioVideo.chime.entity.ChimeRoom;
import com.example.audioVideo.persistance.audioVideo.chime.entity.ChimeSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ChimeSessionRepository {
    private final Map<Integer, ChimeSession> sessions = new HashMap<>();

    public ChimeRoom addRoomToSession(int roomNumber, String roomName, ChimeRoom room) {
        ChimeSession session = sessions.get(roomNumber);
        if (session == null) {
            session = new ChimeSession();
            sessions.put(roomNumber, session);
        }

        session.addRoom(roomName, room);
        return room;
    }

    public Attendee addAttendeeToRoom(int roomNumber, String roomName, Attendee attendee) {
        ChimeSession session = sessions.get(roomNumber);
        if (session == null) {
            session = new ChimeSession();
            sessions.put(roomNumber, session);
        }

        ChimeRoom room = session.getRoomByName(roomName).orElseThrow();
        room.getAttendees().put(attendee.getExternalUserId(), attendee);

        return attendee;
    }


    public Optional<ChimeSession> findSessionByRoomNumber(int roomNumber) {
        return Optional.ofNullable(sessions.get(roomNumber));
    }

    public Optional<ChimeRoom> findRoomByName(int roomNumber, String roomName) {
        return this.findSessionByRoomNumber(roomNumber)
                .flatMap((ChimeSession chimeSession) -> chimeSession.getRoomByName(roomName));
    }
}
