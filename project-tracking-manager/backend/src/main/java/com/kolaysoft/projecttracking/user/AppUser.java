package com.kolaysoft.projecttracking.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_app_users_username",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "uk_app_users_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 100
    )
    private String username;

    @Column(
            length = 255
    )
    private String password;

    @Column(
            nullable = false,
            length = 150
    )
    private String fullName;

    @Column(
            nullable = false,
            length = 150
    )
    private String email;

    @Column(
            name = "phone_number",
            length = 20
    )
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private UserRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean activationCompleted;

    private LocalDateTime passwordChangedAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt =
                LocalDateTime.now();
    }
}