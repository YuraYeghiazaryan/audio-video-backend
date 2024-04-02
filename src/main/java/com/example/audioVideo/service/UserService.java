package com.example.audioVideo.service;

import com.example.audioVideo.model.Role;
import com.example.audioVideo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ClassroomService classroomService;

    public Optional<User> getUserById(int roomNumber, long userId) {
        return classroomService.getUserById(roomNumber, userId);
    }

    public List<User> getUsersByIds(int roomNumber, Set<Long> userIds) {
        return classroomService.getUsersByIds(roomNumber, userIds);
    }

    public User login(String username, Role role) {
        return new User(username, role);
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
