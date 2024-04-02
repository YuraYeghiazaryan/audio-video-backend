package com.example.audioVideo.model.payload;

import com.example.audioVideo.model.Groups;

public record BreakRoomPayload(
        long senderId,
        Groups groups
) {}
