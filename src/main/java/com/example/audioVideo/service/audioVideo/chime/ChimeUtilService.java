package com.example.audioVideo.service.audioVideo.chime;

import org.springframework.stereotype.Service;

@Service
public class ChimeUtilService {

    public String buildMainRoomName(int roomNumber) {
        return roomNumber + "_main";
    }

    public String buildPrivateTalkRoomName(int roomNumber) {
        return roomNumber + "_private-talk";
    }

    public String buildTeamTalkRoomName(int roomNumber, long teamId) {
        return roomNumber + "_team-talk_" + teamId;
    }
}
