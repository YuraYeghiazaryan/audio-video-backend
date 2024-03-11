package com.example.zoombackend.service;

import com.example.zoombackend.model.Role;
import com.example.zoombackend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ClassroomService classroomService;

    public User login(String username, Role role) {
        return new User(username, role);
    }

    public void setUserVideoState(int roomNumber, long userId, boolean isOn) {
        classroomService.getAllUsers(roomNumber)
                .stream()
                .filter((User user) -> user.getId() == userId)
                .findFirst()
                .ifPresent((User user) -> user.getZoomUser().setVideoOn(isOn));
    }
}
