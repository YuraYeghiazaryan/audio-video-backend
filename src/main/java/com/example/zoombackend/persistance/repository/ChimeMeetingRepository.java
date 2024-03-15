package com.example.zoombackend.persistance.repository;

import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.example.zoombackend.persistance.entity.ChimeMeeting;
import com.example.zoombackend.persistance.entity.ChimeRoom;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ChimeMeetingRepository {
    private final Map<Integer, ChimeMeeting> chimeMeetingsMap = new HashMap<>();

    public ChimeMeeting save(ChimeMeeting chimeMeeting) {
        this.chimeMeetingsMap.put(chimeMeeting.getRoomNumber(), chimeMeeting);
        return chimeMeeting;
    }

    public void deleteByRoomNumber(int roomNumber) {
        this.chimeMeetingsMap.remove(roomNumber);
    }

    public Optional<ChimeMeeting> findByRoomNumber(int roomNumber) {
        return Optional.ofNullable(this.chimeMeetingsMap.get(roomNumber));
    }

    public void addSubRoom(int roomNumber, long groupId, ChimeRoom chimeRoom) {
        Optional.ofNullable(this.chimeMeetingsMap.get(roomNumber))
                .ifPresentOrElse(
                        (ChimeMeeting chimeMeeting) -> chimeMeeting.getSubRooms().put(groupId, chimeRoom),
                        () -> {
                            throw new IllegalStateException("Can't set main room. Room " + roomNumber + " not found.");
                        }
                );
    }

    public void setMainRoom(int roomNumber, ChimeRoom chimeRoom) {
        Optional.ofNullable(this.chimeMeetingsMap.get(roomNumber))
                .ifPresentOrElse(
                        (ChimeMeeting chimeMeeting) -> chimeMeeting.setMainRoom(chimeRoom),
                        () -> {
                            throw new IllegalStateException("Can't set main room. Room " + roomNumber + " not found.");
                        }
                );
    }

    public void addAttendee(int roomNumber, CreateAttendeeResult createAttendeeResult) {
        ChimeRoom mainRoom = this.findRoom(roomNumber);
        this.addAttendeeToRoom(mainRoom, createAttendeeResult);
    }
    public void addAttendee(int roomNumber, long groupId, CreateAttendeeResult createAttendeeResult) {
        ChimeRoom subRoom = this.findRoom(roomNumber, groupId);
        this.addAttendeeToRoom(subRoom, createAttendeeResult);
    }

    private void addAttendeeToRoom(ChimeRoom mainRoom, CreateAttendeeResult createAttendeeResult) {
        boolean exists = mainRoom
                .getCreateAttendeeRequests()
                .stream()
                .anyMatch((CreateAttendeeResult foundCreateAttendeeResult) ->
                        Objects.equals(
                                foundCreateAttendeeResult.getAttendee().getExternalUserId(),
                                createAttendeeResult.getAttendee().getExternalUserId()
                        )
                );
        if (!exists) {
            mainRoom.getCreateAttendeeRequests().add(createAttendeeResult);
        }
    }

    private ChimeRoom findRoom(int roomNumber) {
        return Optional.ofNullable(this.chimeMeetingsMap.get(roomNumber))
                .map(ChimeMeeting::getMainRoom)
                .orElseThrow();
    };
    private ChimeRoom findRoom(int roomNumber, long groupId) {
        return Optional.ofNullable(this.chimeMeetingsMap.get(roomNumber))
                .map(ChimeMeeting::getSubRooms)
                .map((Map<Long, ChimeRoom> subRooms) -> subRooms.get(groupId))
                .orElseThrow();
    };
}
