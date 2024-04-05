package com.example.audioVideo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    protected Set<Long> userIds;
    protected boolean isAudioAvailableForLocalUser;
    protected boolean isVideoAvailableForLocalUse;
}
