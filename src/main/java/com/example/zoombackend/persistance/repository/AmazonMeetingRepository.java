package com.example.zoombackend.persistance.repository;

import com.example.zoombackend.persistance.entity.AmazonMeetingEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class AmazonMeetingRepository {
    private final Map<Integer, AmazonMeetingEntity> amnazonMeetingEntityMap = new HashMap<>();

    public AmazonMeetingEntity save(AmazonMeetingEntity amazonMeetingEntity) {
        this.amnazonMeetingEntityMap.put(amazonMeetingEntity.getRoomNumber(), amazonMeetingEntity);
        return amazonMeetingEntity;
    }

    public Optional<AmazonMeetingEntity> findByRoomNumber(int roomNumber) {
        AmazonMeetingEntity amazonMeeting = this.amnazonMeetingEntityMap.get(roomNumber);

        return Optional.ofNullable(amazonMeeting);
    }
}
