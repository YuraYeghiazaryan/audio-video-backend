package com.example.zoombackend.controller;

import com.example.zoombackend.model.ConnectionOptions;
import com.example.zoombackend.service.ZoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zoom")
@RequiredArgsConstructor
public class ZoomController {

    private final ZoomService zoomService;

    @GetMapping("/connection-options")
    @CrossOrigin
    public ConnectionOptions getConnectionOptions(@RequestParam String sessionName, @RequestParam String username) {
        return zoomService.getConnectionOptions(sessionName, username);
    }
}
