package com.example.zoombackend.controller;

import com.example.zoombackend.model.Role;
import com.example.zoombackend.model.User;
import com.example.zoombackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    @CrossOrigin
    public User login(@RequestParam String username, @RequestParam Role role) {
        return userService.login(username, role);
    }

}
