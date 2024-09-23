package com.example.audioVideo.service;

import com.example.audioVideo.model.Role;
import com.example.audioVideo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ClassroomService classroomService;

    public Optional<User> getUserByUsername(int roomNumber, String username) {
        return classroomService.getUserByUsername(roomNumber, username);
    }

    public Set<User> getUsersByIds(int roomNumber, Set<Long> userIds) {
        return classroomService.getUsersByIds(roomNumber, userIds);
    }

    public Set<String> getUsernames(int roomNumber, Set<Long> userIds) {
        return this.getUsersByIds(roomNumber, userIds)
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
    }

    public User login(int roomNumber, String username, Role role) {
        return this.classroomService.addUserToClassroom(roomNumber, new User(username, role));
    }

    public void setUserVideoState(int roomNumber, long userId, boolean isOn) {
        classroomService.getAllUsers(roomNumber)
                .stream()
                .filter((User user) -> user.getId() == userId)
                .findFirst()
                .ifPresent((User user) -> user.getAudioVideoUser().setVideoOn(isOn));
    }

    public void setUserAudioState(int roomNumber, long userId, boolean isOn) {
        classroomService.getAllUsers(roomNumber)
                .stream()
                .filter((User user) -> user.getId() == userId)
                .findFirst()
                .ifPresent((User user) -> user.getAudioVideoUser().setAudioOn(isOn));
    }
}
