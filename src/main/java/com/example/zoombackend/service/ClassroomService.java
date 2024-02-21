package com.example.zoombackend.service;

import com.example.zoombackend.model.User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassroomService {

    private final Map<Integer, List<User>> classrooms = new HashMap<>();

    public void addUserToClassroom(int roomNumber, User newUser) {
        if (!classrooms.containsKey(roomNumber)) {
            classrooms.put(roomNumber, new ArrayList<>());
        }
        classrooms.get(roomNumber).add(newUser);
    }

    public List<User> getAllUsers(int roomNumber) {
        return classrooms.getOrDefault(roomNumber, Collections.emptyList());
    }
}
