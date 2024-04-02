package com.example.audioVideo.model;

import lombok.Data;

@Data
public class AudioVideoUser {
    private String id;
    private boolean joined;
    private boolean isVideoOn;
    private boolean isAudioOn;
}
