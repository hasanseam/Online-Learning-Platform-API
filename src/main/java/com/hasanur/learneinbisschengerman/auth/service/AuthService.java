package com.hasanur.learneinbisschengerman.auth.service;

import com.hasanur.learneinbisschengerman.auth.Dtos.LoginRequest;
import com.hasanur.learneinbisschengerman.auth.Dtos.LoginResponse;
import com.hasanur.learneinbisschengerman.auth.Dtos.LogoutRequest;
import com.hasanur.learneinbisschengerman.auth.Dtos.RefreshRequest;
import com.hasanur.learneinbisschengerman.auth.Dtos.RefreshResponse;
import com.hasanur.learneinbisschengerman.auth.JwtUtil;
import com.hasanur.learneinbisschengerman.auth.RefreshToken;
import com.hasanur.learneinbisschengerman.user.User;
import com.hasanur.learneinbisschengerman.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String accessToken = jwtUtil.generateToken(user);
        RefreshToken createdRefreshToken = refreshTokenService.createRefreshToken(request.email());

        return new LoginResponse(accessToken, createdRefreshToken.getToken());
    }

    public RefreshResponse refreshToken(RefreshRequest request) {

        RefreshToken token = refreshTokenService.verifyToken(request.refreshToken());

        User user = token.getUser();

        String newAccessToken = jwtUtil.generateToken(user);

        return new RefreshResponse(newAccessToken, token.getToken());
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.invalidateRefreshToken(request.refreshToken());
    }

}
