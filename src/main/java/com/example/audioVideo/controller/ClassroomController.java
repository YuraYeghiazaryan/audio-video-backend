package com.example.audioVideo.controller;

import com.example.audioVideo.model.User;
import com.example.audioVideo.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/{roomNumber}/users")
    @CrossOrigin
    public Set<User> getAllUsers(@PathVariable int roomNumber) {
        return classroomService.getAllUsers(roomNumber);
    }

    @PostMapping("/{roomNumber}/game-mode")
    @CrossOrigin
    public void gameModeStateChanged(@PathVariable int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/game-mode", body);
    }

    @PostMapping("/{roomNumber}/team-talk")
    @CrossOrigin
    public void teamTalkStateChanged(@PathVariable int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/team-talk", body);
    }

    @PostMapping("/{roomNumber}/private-talk")
    @CrossOrigin
    public void privateTalkStateChanged(@PathVariable int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/private-talk", body);
    }

    @PostMapping("/{roomNumber}/add-user-to-private-talk")
    @CrossOrigin
    public void addUserToPrivateTalk(@PathVariable int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/add-user-to-private-talk", body);
    }

    @PostMapping("/{roomNumber}/remove-user-from-private-talk")
    @CrossOrigin
    public void removeUserFromPrivateTalk(@PathVariable int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/remove-user-from-private-talk", body);
    }

    @PostMapping("/{roomNumber}/user-joined")
    @CrossOrigin
    public void userAdded(@PathVariable int roomNumber, @RequestBody User user) {
        User savedUser = classroomService.addUserToClassroom(roomNumber, user);

        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/user-joined", savedUser);
    }
}
