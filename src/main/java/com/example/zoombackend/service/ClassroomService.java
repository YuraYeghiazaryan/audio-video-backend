package com.example.zoombackend.service;

import com.example.zoombackend.model.User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassroomService {

    private final Map<Integer, Set<User>> classrooms = new HashMap<>();

    public void addUserToClassroom(int roomNumber, User newUser) {
        if (!classrooms.containsKey(roomNumber)) {
            classrooms.put(roomNumber, new HashSet<>());
        }

        classrooms.get(roomNumber).add(newUser);
    }

    public Set<User> getAllUsers(int roomNumber) {
        return classrooms.getOrDefault(roomNumber, Collections.emptySet());
    }
}
