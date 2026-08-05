package com.kolaysoft.projecttracking.controller;

import com.kolaysoft.projecttracking.dto.AssignableUserResponse;
import com.kolaysoft.projecttracking.service.UserLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "Kullanıcı Seçim Listeleri",
        description = "Atama işlemlerinde kullanılabilecek aktif kullanıcıları listeler"
)
public class UserLookupController {

    private final UserLookupService userLookupService;

    @Operation(
            summary = "Atanabilir aktif kullanıcıları listeler"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    @GetMapping("/assignable")
    public ResponseEntity<List<AssignableUserResponse>> getAssignableUsers() {
        return ResponseEntity.ok(
                userLookupService.getAssignableUsers()
        );
    }
}
