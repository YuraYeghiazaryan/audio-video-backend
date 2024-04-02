package com.example.audioVideo.configuartion;

import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetings;
import com.amazonaws.services.chimesdkmeetings.AmazonChimeSDKMeetingsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public AmazonChimeSDKMeetings getAmazonChimeSDKMeetings() {
        return AmazonChimeSDKMeetingsClient.builder().build();
    }
}
