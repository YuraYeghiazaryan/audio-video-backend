package com.example.zoombackend.controller;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import com.example.zoombackend.service.ChimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chime")
@RequiredArgsConstructor
public class ChimeController {

    private final ChimeService chimeService;

    @GetMapping("/attendee")
    @CrossOrigin
    public Map<String, AmazonWebServiceResult<ResponseMetadata>> createAttendee(
            @RequestParam int roomNumber,
            @RequestParam String username
    ) {
        CreateMeetingResult createMeetingResult = chimeService.createMeeting(roomNumber).getCreateMeetingResult();
        CreateAttendeeResult createAttendeeResult = chimeService.createAttendee(roomNumber, username).getCreateAttendeeResult();

        return Map.of(
                "meeting", createMeetingResult,
                "attendee", createAttendeeResult
        );
    }
}
