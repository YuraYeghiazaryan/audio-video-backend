package com.example.audioVideo.model;

import java.util.List;

public record Groups(
        Group main,
        Group privateTalk,
        List<Team> teamTalk
) {}
