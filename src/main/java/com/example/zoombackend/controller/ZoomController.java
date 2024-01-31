package com.example.zoombackend.controller;

import com.example.zoombackend.model.ConnectionOptions;
import com.example.zoombackend.service.ZoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zoom")
@RequiredArgsConstructor
public class ZoomController {

    private final ZoomService zoomService;

    @GetMapping("/connection-options")
    public ConnectionOptions getConnectionOptions(@RequestParam String sessionName, @RequestParam String username) {
        return zoomService.getConnectionOptions(sessionName, username);
    }
}
