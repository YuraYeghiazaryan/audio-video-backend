package com.example.zoombackend.controller;

import com.example.zoombackend.model.Group;
import com.example.zoombackend.model.connectionOptions.ConnectionOptions;
import com.example.zoombackend.service.audioVideo.AudioVideoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audio-video")
public class AudioVideoController {

    private final AudioVideoService audioVideoService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public AudioVideoController(
            @Qualifier("chimeService")
//            @Qualifier("zoomService")
            AudioVideoService audioVideoService,
            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.audioVideoService = audioVideoService;
        this.simpMessagingTemplate = simpMessagingTemplate;
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

    @PostMapping("/{roomNumber}/audio-video-groups-changed")
    @CrossOrigin
    public void audioVideoGroupsChanged(@PathVariable int roomNumber, @RequestBody List<Group> groups) {
        this.audioVideoService.audioVideoGroupsChanged(roomNumber, groups);

        this.simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/audio-video-groups-changed", groups);
    }
}
