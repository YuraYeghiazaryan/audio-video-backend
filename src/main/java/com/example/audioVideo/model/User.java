package com.example.audioVideo.model;

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
    private AudioVideoUser audioVideoUser;

    public User(String username, Role role) {
        this.id = Objects.hashCode(username + role);
        this.username = username;
        this.role = role;
    }
}
