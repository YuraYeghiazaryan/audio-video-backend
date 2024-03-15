package com.example.zoombackend.service.audioVideo;

import com.example.zoombackend.model.Group;
import com.example.zoombackend.model.connectionOptions.ConnectionOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AudioVideoService {
    ConnectionOptions getConnectionOptions(int roomNumber, String username);
    ConnectionOptions getConnectionOptions(int roomNumber, long groupId, String username);

    void audioVideoGroupsChanged(int roomNumber, List<Group> groups);
}
