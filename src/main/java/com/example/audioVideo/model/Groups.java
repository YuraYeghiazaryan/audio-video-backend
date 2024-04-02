package com.example.audioVideo.model;

import java.util.List;

public record Groups(
        Group main,
        List<Group> teamTalk,
        Group privateTalk
) {}
