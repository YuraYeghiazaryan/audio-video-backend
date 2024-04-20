package com.example.audioVideo.service.audioVideo;

import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import org.springframework.stereotype.Service;

@Service
public interface AudioVideoService {
    ConnectionOptions getConnectionOptions(int roomNumber, String roomName, String username) throws Exception;

    void breakRoomIntoGroups(int roomNumber, Groups groups);
}
