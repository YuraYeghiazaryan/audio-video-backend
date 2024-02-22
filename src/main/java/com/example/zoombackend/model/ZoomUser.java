package com.example.zoombackend.model;

public record ZoomUser (
        long id,
        boolean isVideoOn,
        boolean isAudioOn
) {}
