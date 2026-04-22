package com.hasanur.learneinbisschengerman.auth.Dtos;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {}
