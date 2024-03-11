package com.example.zoombackend.model;

import lombok.Data;

@Data
public class ZoomUser {
    private long id;
    private boolean isVideoOn;
    private boolean isAudioO;
}
