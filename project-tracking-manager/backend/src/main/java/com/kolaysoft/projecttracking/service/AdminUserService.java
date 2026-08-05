package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.CreateUserRequest;
import com.kolaysoft.projecttracking.dto.UpdateUserRoleRequest;
import com.kolaysoft.projecttracking.dto.UpdateUserStatusRequest;
import com.kolaysoft.projecttracking.dto.UserResponse;
import com.kolaysoft.projecttracking.exception.BusinessRuleException;
import com.kolaysoft.projecttracking.exception.UserNotFoundException;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.security.AppUserPrincipal;
import com.kolaysoft.projecttracking.security.CurrentUserService;
import com.kolaysoft.projecttracking.user.AppUser;
import com.kolaysoft.projecttracking.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final AppUserRepository
            appUserRepository;

    private final CurrentUserService
            currentUserService;

    private final AccountActivationService
            accountActivationService;

    @Transactional
    public UserResponse createUser(
            CreateUserRequest request
    ) {
        String normalizedUsername =
                normalizeRequiredText(
                        request.getUsername(),
                        "Kullanıcı adı"
                );

        String normalizedFullName =
                normalizeRequiredText(
                        request.getFullName(),
                        "Ad soyad"
                );

        String normalizedEmail =
                normalizeRequiredText(
                        request.getEmail(),
                        "E-posta adresi"
                ).toLowerCase(Locale.ROOT);

        String normalizedPhoneNumber =
                normalizeOptionalPhoneNumber(
                        request.getPhoneNumber()
                );

        if (appUserRepository
                .existsByUsernameIgnoreCase(
                        normalizedUsername
                )) {

            throw new BusinessRuleException(
                    "Bu kullanıcı adıyla daha önce bir kullanıcı oluşturulmuştur."
            );
        }

        if (appUserRepository
                .existsByEmailIgnoreCase(
                        normalizedEmail
                )) {

            throw new BusinessRuleException(
                    "Bu e-posta adresiyle daha önce bir kullanıcı oluşturulmuştur."
            );
        }

        AppUser appUser =
                new AppUser();

        appUser.setUsername(
                normalizedUsername
        );

        appUser.setFullName(
                normalizedFullName
        );

        appUser.setEmail(
                normalizedEmail
        );

        appUser.setPhoneNumber(
                normalizedPhoneNumber
        );

        appUser.setRole(
                request.getRole()
        );

        appUser.setPassword(null);
        appUser.setActive(false);
        appUser.setEmailVerified(false);
        appUser.setActivationCompleted(false);
        appUser.setPasswordChangedAt(null);

        AppUser savedUser =
                appUserRepository.save(
                        appUser
                );

        accountActivationService
                .issueActivation(
                        savedUser
                );

        return toResponse(savedUser);
    }

    public List<UserResponse> getUsers() {
        return appUserRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(
            Long id
    ) {
        return toResponse(
                getUser(id)
        );
    }

    @Transactional
    public UserResponse updateUserRole(
            Long id,
            UpdateUserRoleRequest request
    ) {
        AppUser appUser =
                getUser(id);

        AppUserPrincipal currentUser =
                currentUserService
                        .getCurrentUser();

        if (appUser
                .getId()
                .equals(currentUser.getId())
                && request.getRole()
                != UserRole.ADMIN) {

            throw new BusinessRuleException(
                    "Kendi hesabınızın ADMIN rolünü kaldıramazsınız."
            );
        }

        appUser.setRole(
                request.getRole()
        );

        AppUser updatedUser =
                appUserRepository.save(
                        appUser
                );

        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateUserStatus(
            Long id,
            UpdateUserStatusRequest request
    ) {
        AppUser appUser =
                getUser(id);

        AppUserPrincipal currentUser =
                currentUserService
                        .getCurrentUser();

        if (appUser
                .getId()
                .equals(currentUser.getId())
                && Boolean.FALSE.equals(
                request.getActive()
        )) {

            throw new BusinessRuleException(
                    "Kendi kullanıcı hesabınızı pasife alamazsınız."
            );
        }

        if (Boolean.TRUE.equals(
                request.getActive()
        )
                && !appUser
                .isActivationCompleted()) {

            throw new BusinessRuleException(
                    "Aktivasyonu tamamlanmamış kullanıcı manuel olarak aktifleştirilemez."
            );
        }

        appUser.setActive(
                request.getActive()
        );

        AppUser updatedUser =
                appUserRepository.save(
                        appUser
                );

        return toResponse(updatedUser);
    }

    @Transactional
    public void resendActivation(
            Long id
    ) {
        AppUser appUser =
                getUser(id);

        if (appUser.isActivationCompleted()) {
            throw new BusinessRuleException(
                    "Aktivasyonu tamamlanmış kullanıcıya yeniden aktivasyon maili gönderilemez."
            );
        }

        if (appUser.getEmail() == null
                || appUser
                .getEmail()
                .isBlank()) {

            throw new BusinessRuleException(
                    "Kullanıcının kayıtlı e-posta adresi bulunmamaktadır."
            );
        }

        accountActivationService
                .issueActivation(
                        appUser
                );
    }

    private AppUser getUser(
            Long id
    ) {
        return appUserRepository
                .findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id)
                );
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.isBlank()) {

            throw new BusinessRuleException(
                    fieldName
                            + " boş bırakılamaz."
            );
        }

        return value.trim();
    }

    private String normalizeOptionalPhoneNumber(
            String phoneNumber
    ) {
        if (phoneNumber == null
                || phoneNumber.isBlank()) {

            return null;
        }

        String normalizedPhoneNumber =
                phoneNumber.trim();

        if (!normalizedPhoneNumber.matches(
                "\\+?[0-9]{10,15}"
        )) {
            throw new BusinessRuleException(
                    "Telefon numarası 10 ile 15 rakam arasında olmalıdır."
            );
        }

        return normalizedPhoneNumber;
    }

    private UserResponse toResponse(
            AppUser appUser
    ) {
        return new UserResponse(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getFullName(),
                appUser.getEmail(),
                appUser.getPhoneNumber(),
                appUser.getRole(),
                appUser.isActive(),
                appUser.isEmailVerified(),
                appUser.isActivationCompleted(),
                appUser.getPasswordChangedAt(),
                appUser.getCreatedAt(),
                appUser.getUpdatedAt()
        );
    }
}