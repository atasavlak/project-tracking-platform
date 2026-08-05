package com.kolaysoft.projecttracking.entity;

import com.kolaysoft.projecttracking.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activation_tokens")
@Getter
@Setter
@NoArgsConstructor
public class UserActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private AppUser appUser;

    @Column(
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public UserActivationToken(
            AppUser appUser,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        this.appUser = appUser;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}