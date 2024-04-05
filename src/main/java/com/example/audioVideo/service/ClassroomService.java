package com.example.audioVideo.service;

import com.example.audioVideo.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassroomService {

    private final Map<Integer, Map<Long, User>> classrooms = new HashMap<>();

    public User addUserToClassroom(int roomNumber, User newUser) {
        if (!classrooms.containsKey(roomNumber)) {
            classrooms.put(roomNumber, new HashMap<>());
        }

        classrooms.get(roomNumber).put(newUser.getId(), newUser);

        return newUser;
    }

    public Set<User> getAllUsers(int roomNumber) {
        return new HashSet<>(classrooms.getOrDefault(roomNumber, Collections.emptyMap()).values());
    }

    public Optional<User> getUserById(int roomNumber, long userId) {
        return Optional.ofNullable(classrooms.get(roomNumber))
                .map((Map<Long, User> users) -> users.get(userId));
    }

    public Optional<User> getUserByUsername(int roomNumber, String username) {
        return Optional.ofNullable(classrooms.get(roomNumber))
                .flatMap((Map<Long, User> users) ->
                        users.values()
                                .stream()
                                .filter((User user) -> user.getUsername().equals(username)).findAny()
                );
    }

    public Set<User> getUsersByIds(int roomNumber, Set<Long> userIds) {
        return Optional.ofNullable(classrooms.get(roomNumber))
                .map((Map<Long, User> users) ->
                    users
                            .values()
                            .stream()
                            .filter((User user) -> userIds.contains(user.getId()))
                            .collect(Collectors.toSet())
                )
                .orElse(new HashSet<>());
    }
}
