package com.example.zoombackend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionOptions {
    private String videoSDKJWT;
    private String username;
    private String sessionName;
    private String sessionPasscode;
}
