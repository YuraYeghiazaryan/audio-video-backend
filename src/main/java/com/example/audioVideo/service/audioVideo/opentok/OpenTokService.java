package com.example.audioVideo.service.audioVideo.opentok;

import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import com.example.audioVideo.model.connectionOptions.OpenTokConnectionOptions;
import com.example.audioVideo.persistance.audioVideo.opentok.entity.OpenTokSession;
import com.example.audioVideo.persistance.audioVideo.opentok.repository.OpenTokSessionRepository;
import com.example.audioVideo.service.audioVideo.AudioVideoService;
import com.opentok.MediaMode;
import com.opentok.OpenTok;
import com.opentok.Session;
import com.opentok.SessionProperties;
import com.opentok.TokenOptions;
import com.opentok.exception.OpenTokException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Log4j2
public class OpenTokService implements AudioVideoService {
    @Value("${opentok.api.app.key}")
    private int appKey;
    @Value("${opentok.api.app.secret}")
    private String secret;

    private final OpenTokSessionRepository openTokSessionRepository;

    @Override
    public OpenTokConnectionOptions getConnectionOptions(int roomNumber, String roomName, String username) throws OpenTokException {
        OpenTokSession openTokSession = openTokSessionRepository
                .findSessionByRoomNumber(roomNumber)
                .orElseGet(() -> {
                    try {
                        return openTokSessionRepository.saveSession(roomNumber, createSession());
                    } catch (OpenTokException e) {
                        log.warn("Can't create opentok session because of following exception: ", e);
                        return null;
                    }
                });

        assert openTokSession != null;

        Session session = openTokSession.getSession();

        String sessionId = session.getSessionId();
        String token = createToken(session, username);

        return new OpenTokConnectionOptions(appKey, sessionId, token);
    }

    @Override
    public void breakRoomIntoGroups(int roomNumber, Groups groups) {}

    private Session createSession() throws OpenTokException {
        SessionProperties sessionProperties = new SessionProperties.Builder()
                .mediaMode(MediaMode.ROUTED)
                .build();

        return new OpenTok(appKey, secret).createSession(sessionProperties);
    }

    private String createToken(Session session, String username) throws OpenTokException {
        TokenOptions tokenOptions = new TokenOptions
                .Builder()
                .expireTime(new Date().getTime() / 1000 + 60*60) /* 1 hour */
                .data(username)
                .build();

        return new OpenTok(appKey, secret).generateToken(session.getSessionId(), tokenOptions);
    }
}
