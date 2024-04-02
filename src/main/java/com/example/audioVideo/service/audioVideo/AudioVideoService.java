package com.example.audioVideo.service.audioVideo;

import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import org.springframework.stereotype.Service;

@Service
public interface AudioVideoService {
    ConnectionOptions getConnectionOptions(int roomNumber, String username);
    ConnectionOptions getConnectionOptions(int roomNumber, long groupId, String username);

    void createMeetings(int roomNumber, Groups groups);
}
