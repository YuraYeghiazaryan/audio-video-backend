package com.example.audioVideo.model.connectionOptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class OpenTokConnectionOptions extends ConnectionOptions {
    private int apiKey;
    private String sessionId;
    private String token;
}