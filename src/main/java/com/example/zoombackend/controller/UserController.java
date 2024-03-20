package com.example.zoombackend.controller;

import com.example.zoombackend.model.AudioState;
import com.example.zoombackend.model.Role;
import com.example.zoombackend.model.User;
import com.example.zoombackend.model.VideoState;
import com.example.zoombackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/login")
    @CrossOrigin
    public User login(@RequestParam String username, @RequestParam Role role) {
        return userService.login(username, role);
    }

    @PostMapping("/{roomNumber}/user-video-state-changed")
    @CrossOrigin
    public void userVideoStateChanged(@PathVariable int roomNumber, @RequestBody VideoState videoState) {
        userService.setUserVideoState(roomNumber, videoState.userId(), videoState.isOn());
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/user-video-state-changed", videoState);
    }

    @PostMapping("/{roomNumber}/user-audio-state-changed")
    @CrossOrigin
    public void userAudioStateChanged(@PathVariable int roomNumber, @RequestBody AudioState audioState) {
        userService.setUserAudioState(roomNumber, audioState.userId(), audioState.isOn());
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/user-audio-state-changed", audioState);
    }
}
