package com.example.audioVideo.controller;

import com.example.audioVideo.model.Group;
import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import com.example.audioVideo.model.payload.BreakRoomPayload;
import com.example.audioVideo.persistance.entity.ChimeMeeting;
import com.example.audioVideo.persistance.repository.ChimeMeetingRepository;
import com.example.audioVideo.service.audioVideo.AudioVideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/audio-video")
public class AudioVideoController {

    private final AudioVideoService audioVideoService;

    public AudioVideoController(
            @Qualifier("chimeService")
//            @Qualifier("zoomService")
            AudioVideoService audioVideoService
    ) {
        this.audioVideoService = audioVideoService;
    }

    @PostMapping("/{roomNumber}/break-room-into-groups")
    @CrossOrigin
    public void breakRoomIntoGroups(@PathVariable int roomNumber, @RequestBody BreakRoomPayload payload) {
        Groups groups = payload.groups();
        this.audioVideoService.createMeetings(roomNumber, groups);
    }

    @GetMapping("/main-session/connection-options")
    @CrossOrigin
    public ConnectionOptions createAttendee(
            @RequestParam int roomNumber,
            @RequestParam String username
    ) {
        return this.audioVideoService.getConnectionOptions(roomNumber, username);
    }

    @GetMapping("/sub-session/connection-options")
    @CrossOrigin
    public ConnectionOptions createAttendee(
            @RequestParam int roomNumber,
            @RequestParam long groupId,
            @RequestParam String username
    ) {
        return this.audioVideoService.getConnectionOptions(roomNumber, groupId, username);
    }
}
