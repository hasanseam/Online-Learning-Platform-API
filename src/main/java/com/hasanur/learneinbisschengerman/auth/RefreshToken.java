package com.hasanur.learneinbisschengerman.auth;

import com.hasanur.learneinbisschengerman.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    private String token;

    private Instant expiryDate;

    @ManyToOne
    private User user;
}
