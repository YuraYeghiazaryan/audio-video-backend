package com.example.zoombackend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    private long id;
    private Role role;
    private String username;
    private RoomConnection roomConnection;
    private ZoomUser zoomUser;

    public User(String username, Role role) {
        this.id = Objects.hashCode(username + role);
        this.username = username;
        this.role = role;
    }
}
