package com.example.audioVideo.service.audioVideo;

import com.example.audioVideo.model.Groups;
import com.example.audioVideo.model.connectionOptions.ConnectionOptions;
import com.example.audioVideo.model.connectionOptions.ZoomConnectionOptions;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Map;

@Service
public class ZoomService implements AudioVideoService {
    @Value("${zoom.api.app.key}")
    private String key;
    @Value("${zoom.api.app.secret}")
    private String secret;
    @Value("${zoom.api.session.passcode}")
    private String sessionPasscode;

    @Override
    public ZoomConnectionOptions getConnectionOptions(int roomNumber, String username) {
        Date currentDate = new Date();
        String sessionName = String.valueOf(roomNumber);

        SecretKey hmacKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        long iat = currentDate.getTime() / 1000;
        String jwt = Jwts.builder()
                .header()
                .add(Map.of("alg", "HS256", "typ", "JWT"))
                .and()
                .claim("app_key", key)
                .claim("role_type", 1)
                .claim("tpc", sessionName)
                .claim("version", 1)
                .claim("iat", iat)
                .claim("exp", iat + 60*60)
                .encodePayload(true)
                .signWith(hmacKey)
                .compact();

        return ZoomConnectionOptions
                .builder()
                .videoSDKJWT(jwt)
                .username(username)
                .sessionName(sessionName)
                .sessionPasscode(sessionPasscode)
                .build();
    }

    @Override
    public ConnectionOptions getConnectionOptions(int roomNumber, long groupId, String username) {
        return null;
    }

    @Override
    public void createMeetings(int roomNumber, Groups groups) {

    }
}
