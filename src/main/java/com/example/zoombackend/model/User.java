package com.example.zoombackend.model;

import lombok.Data;

@Data
public class User {
    private long id;
    private String username;
    private String role;
    private ZoomUser zoomUser;
}
