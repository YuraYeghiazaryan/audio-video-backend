package com.example.audioVideo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AudioVideoUser {
    private String id;
    private boolean joined;
    @JsonProperty(value="isVideoOn")
    private boolean isVideoOn;
    @JsonProperty(value="isAudioOn")
    private boolean isAudioOn;
}
