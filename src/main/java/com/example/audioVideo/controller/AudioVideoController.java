package com.example.audioVideo.controller;

import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import com.example.audioVideo.model.payload.BreakRoomPayload;
import com.example.audioVideo.service.audioVideo.AudioVideoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audio-video")
public class AudioVideoController {

    private final AudioVideoService audioVideoService;

    public AudioVideoController(
            @Qualifier("chimeService")
//            @Qualifier("openTokService")
//            @Qualifier("zoomService")
            AudioVideoService audioVideoService
    ) {
        this.audioVideoService = audioVideoService;
    }

    @PostMapping("/break-room-into-groups")
    @CrossOrigin
    public void breakRoomIntoGroups(@RequestParam int roomNumber, @RequestBody BreakRoomPayload payload) {
        Groups groups = payload.groups();
        this.audioVideoService.breakRoomIntoGroups(roomNumber, groups);
    }

    /*
    * All parameters of this endpoint should be fetched using spring security
    * */
    @GetMapping("/connection-options")
    @CrossOrigin
    public ConnectionOptions getConnectionOptions(
            @RequestParam int roomNumber,
            @RequestParam String roomName,
            @RequestParam String username
    ) throws Exception {
        return this.audioVideoService.getConnectionOptions(roomNumber, roomName, username);
    }
}
