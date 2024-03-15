package com.example.zoombackend.model;

import java.util.Set;

public record Group(
        long id,
        Set<Long> userIds,
        boolean isAudioAvailableForLocalUser,
        boolean isVideoAvailableForLocalUser
) {}
