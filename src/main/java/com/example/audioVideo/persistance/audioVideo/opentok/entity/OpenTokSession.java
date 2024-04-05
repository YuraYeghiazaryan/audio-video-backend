package com.example.audioVideo.persistance.audioVideo.opentok.entity;

import com.opentok.Session;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OpenTokSession {
    private int roomNumber;
    private Session session;
}
