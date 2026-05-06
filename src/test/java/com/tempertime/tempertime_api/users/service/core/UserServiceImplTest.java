package com.tempertime.tempertime_api.users.service.core;

import com.tempertime.tempertime_api.common.normalizer.InputNormalizer;
import com.tempertime.tempertime_api.common.validator.InvalidPasswordFormatException;
import com.tempertime.tempertime_api.common.validator.PasswordValidator;
import com.tempertime.tempertime_api.events.service.access.EventAccessService;
import com.tempertime.tempertime_api.security.refresh.RefreshTokenService;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.dto.request.UserDeleteAccountRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdatePasswordRequest;
import com.tempertime.tempertime_api.users.dto.request.UserUpdateProfileRequest;
import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.exception.InvalidPasswordException;
import com.tempertime.tempertime_api.users.exception.UserNotFoundException;
import com.tempertime.tempertime_api.users.mapper.UserMapper;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceOwnerExistsException;
import com.tempertime.tempertime_api.workspaces.service.authorization.WorkspaceAccessService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    // Repositories
    @Mock
    private UserRepository userRepository;

    // Security / Encoder
    @Mock
    private PasswordEncoder passwordEncoder;

    // Services
    @Mock
    private WorkspaceAccessService workspaceAccessService;
    @Mock
    private EventAccessService eventAccessService;
    @Mock
    private RefreshTokenService refreshTokenService;

    // Loaders
    @Mock
    private UserLoader userLoader;

    // Mappers
    @Mock
    private UserMapper userMapper;

    // Validators / Normalizers
    @Mock
    private PasswordValidator passwordValidator;
    @Mock
    private InputNormalizer inputNormalizer;

    // Class under test
    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    class GetProfileTests {

        @Test
        void shouldReturnUserProfile_whenUserExists() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            UserProfileResponse response = UserTestDataProvider.userProfileResponse(user);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(userMapper.toUserProfileResponse(user)).thenReturn(response);

            UserProfileResponse result = userService.getProfile(userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(userLoader).loadUserOrThrow(userId);
            verify(userMapper).toUserProfileResponse(user);
        }

        @Test
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {

            Long userId = 99L;

            when(userLoader.loadUserOrThrow(userId))
                    .thenThrow(new UserNotFoundException());

            assertThatThrownBy(() -> userService.getProfile(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userLoader).loadUserOrThrow(userId);
            verify(userMapper, never()).toUserProfileResponse(any());
        }
    }

    @Nested
    class UpdateProfileTests {

        @Test
        void shouldUpdateBothFields_whenValidDataProvided() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("  FirstUpdated  ", "  LastUpdated  ");

            User savedUser = UserTestDataProvider.user(userId);
            savedUser.setFirstName("FirstUpdated");
            savedUser.setLastName("LastUpdated");

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(savedUser);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(inputNormalizer.normalize("  FirstUpdated  ")).thenReturn("FirstUpdated");
            when(inputNormalizer.normalize("  LastUpdated  ")).thenReturn("LastUpdated");

            when(userRepository.save(user)).thenReturn(savedUser);
            when(userMapper.toUserProfileResponse(savedUser)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result).isNotNull();
            assertThat(user.getFirstName()).isEqualTo("FirstUpdated");
            assertThat(user.getLastName()).isEqualTo("LastUpdated");

            verify(userLoader).loadUserOrThrow(userId);
            verify(inputNormalizer).normalize("  FirstUpdated  ");
            verify(inputNormalizer).normalize("  LastUpdated  ");
            verify(userRepository).save(user);
            verify(userMapper).toUserProfileResponse(savedUser);
        }

        @Test
        void shouldUpdateOnlyFirstName_whenLastNameIsNull() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String originalLastName = user.getLastName();

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("FirstUpdated", null);

            User savedUser = UserTestDataProvider.user(userId);
            savedUser.setFirstName("FirstUpdated");

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(savedUser);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(inputNormalizer.normalize("FirstUpdated"))
                    .thenReturn("FirstUpdated");

            when(userRepository.save(user)).thenReturn(savedUser);
            when(userMapper.toUserProfileResponse(savedUser)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result).isNotNull();

            assertThat(user.getFirstName()).isEqualTo("FirstUpdated");
            assertThat(user.getLastName()).isEqualTo(originalLastName);

            verify(userLoader).loadUserOrThrow(userId);
            verify(inputNormalizer).normalize("FirstUpdated");
            verify(userRepository).save(user);
            verify(userMapper).toUserProfileResponse(savedUser);
        }

        @Test
        void shouldUpdateOnlyLastName_whenFirstNameIsNull() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String originalFirstName = user.getFirstName();

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest(null, "LastUpdated");

            User savedUser = UserTestDataProvider.user(userId);
            savedUser.setLastName("LastUpdated");

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(savedUser);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(inputNormalizer.normalize("LastUpdated"))
                    .thenReturn("LastUpdated");

            when(userRepository.save(user)).thenReturn(savedUser);
            when(userMapper.toUserProfileResponse(savedUser)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result).isNotNull();

            assertThat(user.getFirstName()).isEqualTo(originalFirstName);
            assertThat(user.getLastName()).isEqualTo("LastUpdated");

            verify(userLoader).loadUserOrThrow(userId);
            verify(inputNormalizer).normalize("LastUpdated");
            verify(userRepository).save(user);
            verify(userMapper).toUserProfileResponse(savedUser);
        }

        @Test
        void shouldUpdateOnlyFirstName_whenLastNameIsBlank() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String originalLastName = user.getLastName();

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("FirstUpdated", "   ");

            User savedUser = UserTestDataProvider.user(userId);
            savedUser.setFirstName("FirstUpdated");

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(savedUser);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(inputNormalizer.normalize("FirstUpdated")).thenReturn("FirstUpdated");

            when(userRepository.save(user)).thenReturn(savedUser);
            when(userMapper.toUserProfileResponse(savedUser)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result).isNotNull();

            assertThat(user.getFirstName()).isEqualTo("FirstUpdated");
            assertThat(user.getLastName()).isEqualTo(originalLastName);

            verify(userLoader).loadUserOrThrow(userId);
            verify(inputNormalizer).normalize("FirstUpdated");
            verify(userRepository).save(user);
            verify(userMapper).toUserProfileResponse(savedUser);
        }

        @Test
        void shouldUpdateOnlyLastName_whenFirstNameIsBlank() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String originalFirstName = user.getFirstName();

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("   ", "LastUpdated");

            User savedUser = UserTestDataProvider.user(userId);
            savedUser.setLastName("LastUpdated");

            UserProfileResponse response =
                    UserTestDataProvider.userProfileResponse(savedUser);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(inputNormalizer.normalize("LastUpdated")).thenReturn("LastUpdated");

            when(userRepository.save(user)).thenReturn(savedUser);
            when(userMapper.toUserProfileResponse(savedUser)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result).isNotNull();

            assertThat(user.getFirstName()).isEqualTo(originalFirstName);
            assertThat(user.getLastName()).isEqualTo("LastUpdated");

            verify(userLoader).loadUserOrThrow(userId);
            verify(inputNormalizer).normalize("LastUpdated");
            verify(userRepository).save(user);
            verify(userMapper).toUserProfileResponse(savedUser);
        }

        @Test
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {

            Long userId = 99L;

            UserUpdateProfileRequest request =
                    new UserUpdateProfileRequest("FirstUpdated", "LastUpdated");

            when(userLoader.loadUserOrThrow(userId))
                    .thenThrow(new UserNotFoundException());

            assertThatThrownBy(() -> userService.updateProfile(userId, request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userLoader).loadUserOrThrow(userId);
            verify(inputNormalizer, never()).normalize(any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserProfileResponse(any());
        }
    }

    @Nested
    class UpdatePasswordTests {

        @Test
        void shouldUpdatePasswordAndRevokeTokens_whenValidDataProvided() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String oldPasswordHash = user.getPasswordHash();

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(
                            UserTestDataProvider.PASSWORD,
                            "NewPassword123"
                    );

            String currentPassword = request.currentPassword();

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(passwordEncoder.matches(currentPassword, oldPasswordHash))
                    .thenReturn(true);

            doNothing().when(passwordValidator).validate(request.newPassword());
            when(passwordEncoder.encode(request.newPassword()))
                    .thenReturn("encodedNewPassword");

            userService.updatePassword(userId, request);

            assertThat(user.getPasswordHash()).isEqualTo("encodedNewPassword");

            verify(passwordEncoder).matches(currentPassword, oldPasswordHash);
            verify(passwordValidator).validate(request.newPassword());
            verify(passwordEncoder).encode(request.newPassword());
            verify(userRepository).save(user);
            verify(refreshTokenService).revokeAllRefreshTokensForUser(user);
        }

        @Test
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {

            Long userId = 99L;

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(
                            UserTestDataProvider.PASSWORD,
                            "NewPassword123"
                    );

            when(userLoader.loadUserOrThrow(userId))
                    .thenThrow(new UserNotFoundException());

            assertThatThrownBy(() -> userService.updatePassword(userId, request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userLoader).loadUserOrThrow(userId);

            verify(passwordEncoder, never()).matches(any(), any());
            verify(passwordValidator, never()).validate(any());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
            verify(refreshTokenService, never()).revokeAllRefreshTokensForUser(any());
        }

        @Test
        void shouldThrowInvalidPasswordException_whenCurrentPasswordIsIncorrect() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String oldPasswordHash = user.getPasswordHash();

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(
                            "WrongPassword",
                            "NewPassword123"
                    );

            String currentPassword = request.currentPassword();

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(passwordEncoder.matches(currentPassword, oldPasswordHash))
                    .thenReturn(false);

            assertThatThrownBy(() -> userService.updatePassword(userId, request))
                    .isInstanceOf(InvalidPasswordException.class);

            verify(passwordEncoder).matches(currentPassword, oldPasswordHash);

            verify(passwordValidator, never()).validate(any());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
            verify(refreshTokenService, never()).revokeAllRefreshTokensForUser(any());
        }

        @Test
        void shouldThrowInvalidPasswordFormatException_whenNewPasswordIsInvalid() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            String oldPasswordHash = user.getPasswordHash();

            UserUpdatePasswordRequest request =
                    new UserUpdatePasswordRequest(
                            UserTestDataProvider.PASSWORD,
                            "BadPassword"
                    );

            String currentPassword = request.currentPassword();

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            when(passwordEncoder.matches(currentPassword, oldPasswordHash))
                    .thenReturn(true);

            doThrow(new InvalidPasswordFormatException())
                    .when(passwordValidator).validate(request.newPassword());

            assertThatThrownBy(() -> userService.updatePassword(userId, request))
                    .isInstanceOf(InvalidPasswordFormatException.class);

            verify(passwordValidator).validate(request.newPassword());

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
            verify(refreshTokenService, never()).revokeAllRefreshTokensForUser(any());
        }
    }

    @Nested
    class DeleteAccountTests {
        @Test
        void shouldDeleteUserAccount_whenValidRequest() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest(UserTestDataProvider.PASSWORD);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            doNothing().when(workspaceAccessService)
                    .requireNoOwnedWorkspaces(userId);

            when(passwordEncoder.matches(request.currentPassword(), user.getPasswordHash()))
                    .thenReturn(true);

            doNothing().when(eventAccessService).removeUserFromAllEvents(userId);
            doNothing().when(userRepository).delete(user);

            userService.deleteAccount(userId, request);

            verify(userLoader).loadUserOrThrow(userId);
            verify(workspaceAccessService).requireNoOwnedWorkspaces(userId);
            verify(passwordEncoder).matches(request.currentPassword(), user.getPasswordHash());
            verify(eventAccessService).removeUserFromAllEvents(userId);
            verify(userRepository).delete(user);
        }

        @Test
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {

            Long userId = 99L;

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest(UserTestDataProvider.PASSWORD);

            when(userLoader.loadUserOrThrow(userId))
                    .thenThrow(new UserNotFoundException());

            assertThatThrownBy(() -> userService.deleteAccount(userId, request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userLoader).loadUserOrThrow(userId);

            verify(workspaceAccessService, never()).requireNoOwnedWorkspaces(any());
            verify(passwordEncoder, never()).matches(any(), any());
            verify(eventAccessService, never()).removeUserFromAllEvents(any());
            verify(userRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceOwnerExistsException_whenUserHasOwnedWorkspaces() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest(UserTestDataProvider.PASSWORD);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            doThrow(new WorkspaceOwnerExistsException())
                    .when(workspaceAccessService)
                    .requireNoOwnedWorkspaces(userId);

            assertThatThrownBy(() -> userService.deleteAccount(userId, request))
                    .isInstanceOf(WorkspaceOwnerExistsException.class);

            verify(userLoader).loadUserOrThrow(userId);
            verify(workspaceAccessService).requireNoOwnedWorkspaces(userId);

            verify(passwordEncoder, never()).matches(any(), any());
            verify(eventAccessService, never()).removeUserFromAllEvents(any());
            verify(userRepository, never()).delete(any());
        }

        @Test
        void shouldThrowInvalidPasswordException_whenPasswordIsIncorrect() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);

            UserDeleteAccountRequest request =
                    new UserDeleteAccountRequest("WrongPassword");

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);

            doNothing().when(workspaceAccessService)
                    .requireNoOwnedWorkspaces(userId);

            when(passwordEncoder.matches(request.currentPassword(), user.getPasswordHash()))
                    .thenReturn(false);

            assertThatThrownBy(() -> userService.deleteAccount(userId, request))
                    .isInstanceOf(InvalidPasswordException.class);

            verify(userLoader).loadUserOrThrow(userId);
            verify(workspaceAccessService).requireNoOwnedWorkspaces(userId);
            verify(passwordEncoder).matches(request.currentPassword(), user.getPasswordHash());

            verify(eventAccessService, never()).removeUserFromAllEvents(any());
            verify(userRepository, never()).delete(any());
        }
    }
}
