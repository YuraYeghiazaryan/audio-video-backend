package com.example.zoombackend.service;

import com.example.zoombackend.model.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ClassroomService {

    private final Map<Integer, Set<User>> classrooms = new HashMap<>();

    public User addUserToClassroom(int roomNumber, User newUser) {
        if (!classrooms.containsKey(roomNumber)) {
            classrooms.put(roomNumber, new HashSet<>());
        }

        classrooms.get(roomNumber).add(newUser);

        return newUser;
    }

    public Set<User> getAllUsers(int roomNumber) {
        return classrooms.getOrDefault(roomNumber, Collections.emptySet());
    }
}
