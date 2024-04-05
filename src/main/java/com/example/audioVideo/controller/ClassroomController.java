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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/users")
    @CrossOrigin
    public Set<User> getAllUsers(@RequestParam int roomNumber) {
        return classroomService.getAllUsers(roomNumber);
    }

    @PostMapping("/game-mode")
    @CrossOrigin
    public void gameModeStateChanged(@RequestParam int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/game-mode", body);
    }

    @PostMapping("/team-talk")
    @CrossOrigin
    public void teamTalkStateChanged(@RequestParam int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/team-talk", body);
    }

    @PostMapping("/private-talk")
    @CrossOrigin
    public void privateTalkStateChanged(@RequestParam int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/private-talk", body);
    }

    @PostMapping("/add-user-to-private-talk")
    @CrossOrigin
    public void addUserToPrivateTalk(@RequestParam int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/add-user-to-private-talk", body);
    }

    @PostMapping("/remove-user-from-private-talk")
    @CrossOrigin
    public void removeUserFromPrivateTalk(@RequestParam int roomNumber, @RequestBody Object body) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/remove-user-from-private-talk", body);
    }

    @PostMapping("/user-joined")
    @CrossOrigin
    public void userAdded(@RequestParam int roomNumber, @RequestBody User user) {
        User savedUser = classroomService.addUserToClassroom(roomNumber, user);

        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/user-joined", savedUser);
    }
}
