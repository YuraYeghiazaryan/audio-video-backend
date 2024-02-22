package com.example.zoombackend.controller;

import com.example.zoombackend.model.User;
import com.example.zoombackend.service.ClassroomService;
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

    @PostMapping("/{roomNumber}/user-joined")
    @CrossOrigin
    public void userAdded(@PathVariable int roomNumber, @RequestBody User user) {
        User savedUser = classroomService.addUserToClassroom(roomNumber, user);

        simpMessagingTemplate.convertAndSend("/topic/" + roomNumber + "/user-joined", savedUser);
    }
}
