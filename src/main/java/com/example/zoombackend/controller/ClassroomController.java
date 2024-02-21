package com.example.zoombackend.controller;

import com.example.zoombackend.model.User;
import com.example.zoombackend.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping("/{roomNumber}/users")
    @CrossOrigin
    public Set<User> getAllUsers(@PathVariable int roomNumber) {
        return classroomService.getAllUsers(roomNumber);
    }

    @PostMapping("/{roomNumber}/user-added")
    @CrossOrigin
    public void userAdded(@PathVariable int roomNumber, @RequestBody User user) {
        classroomService.addUserToClassroom(roomNumber, user);
    }
}
