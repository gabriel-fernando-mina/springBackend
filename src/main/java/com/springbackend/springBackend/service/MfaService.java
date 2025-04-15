package com.springbackend.springBackend.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator;

    public MfaService() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30000)
                .setWindowSize(5)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
    }

    public boolean verifyCode(String secret, String code) {
        return googleAuthenticator.authorize(secret, Integer.parseInt(code));
    }
}