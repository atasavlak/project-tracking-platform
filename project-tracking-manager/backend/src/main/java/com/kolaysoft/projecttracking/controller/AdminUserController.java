package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.CreateUserRequest;
import com.kolaysoft.projecttracking.dto.MessageResponse;
import com.kolaysoft.projecttracking.dto.UpdateUserRoleRequest;
import com.kolaysoft.projecttracking.dto.UpdateUserStatusRequest;
import com.kolaysoft.projecttracking.dto.UserResponse;
import com.kolaysoft.projecttracking.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Admin Kullanıcı Yönetimi",
        description = "Kullanıcı oluşturma, listeleme, rol, durum ve aktivasyon işlemleri"
)
public class AdminUserController {

    private final AdminUserService
            adminUserService;

    @Operation(
            summary = "Yeni kullanıcı oluşturur ve aktivasyon maili gönderir"
    )
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid
            @RequestBody
            CreateUserRequest request
    ) {
        UserResponse response =
                adminUserService.createUser(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Bütün kullanıcıları listeler"
    )
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(
                adminUserService.getUsers()
        );
    }

    @Operation(
            summary = "ID bilgisine göre kullanıcıyı getirir"
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                adminUserService.getUserById(id)
        );
    }

    @Operation(
            summary = "Kullanıcının rolünü günceller"
    )
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(
                adminUserService.updateUserRole(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Kullanıcının aktiflik durumunu günceller"
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(
                adminUserService.updateUserStatus(
                        id,
                        request
                )
        );
    }

    @Operation(
            summary = "Aktivasyonu tamamlanmamış kullanıcıya yeni aktivasyon maili gönderir"
    )
    @PostMapping("/{id}/resend-activation")
    public ResponseEntity<MessageResponse> resendActivation(
            @PathVariable
            Long id
    ) {
        adminUserService
                .resendActivation(id);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Aktivasyon maili yeniden gönderildi."
                )
        );
    }
}