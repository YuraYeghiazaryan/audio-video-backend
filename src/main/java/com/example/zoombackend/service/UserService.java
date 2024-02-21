package com.example.zoombackend.service;

import com.example.zoombackend.model.Role;
import com.example.zoombackend.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User login(String username, Role role) {
        return new User(username, role);
    }
}
