package com.example.zoombackend.controller;

import com.example.zoombackend.model.connectionOptions.ConnectionOptions;
import com.example.zoombackend.service.audioVideo.AudioVideoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
