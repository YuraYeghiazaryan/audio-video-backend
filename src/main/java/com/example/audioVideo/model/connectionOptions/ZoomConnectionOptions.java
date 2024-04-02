package com.example.audioVideo.model.connectionOptions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ZoomConnectionOptions extends ConnectionOptions {
    private String videoSDKJWT;
    private String username;
    private String sessionName;
    private String sessionPasscode;
}
