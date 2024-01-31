package com.example.zoombackend.service;

import com.example.zoombackend.model.ConnectionOptions;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class ZoomService {
    @Value("${zoom.api.app.key}")
    private String key;
    @Value("${zoom.api.app.secret}")
    private String secret;
    @Value("${zoom.api.session.passcode}")
    private String sessionPasscode;

    public ConnectionOptions getConnectionOptions(String sessionName, String username) {
        Date currentDate = new Date();


        SecretKey hmacKey = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
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

        return ConnectionOptions
                .builder()
                .videoSDKJWT(jwt)
                .username(username)
                .sessionName(sessionName)
                .sessionPasscode(sessionPasscode)
                .build();
    }
}
