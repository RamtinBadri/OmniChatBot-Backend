package dev.ramtin.aiplatform.user;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.user.dto.UpdateUserRequest;
import dev.ramtin.aiplatform.user.dto.UserDTO;
import dev.ramtin.aiplatform.user.service.UserServiceImpl;
import dev.ramtin.aiplatform.exception.ResourceConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;


    // getUserById()
    @Test
    void getUserById_whenUserExists_returnsUserDTO() {
        User user = User.builder().id("user123").username("ramtin").email("ramtin@example.com").build();

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById("user123");

        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("ramtin@example.com", result.getEmail());

        verify(userRepository).findById("user123");
    }


    @Test
    void getUserById_whenUserDoesNotExist_throwsResourceNotFoundException() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("user123"));
        verify(userRepository).findById("user123");
    }


    // updateUser()
    @Test
    void updateUser_whenUserExists_updatesAndReturnsUserDTO() {
        User user = User.builder().id("user123").username("ramtin").email("old@example.com")
                .firstName("Ramtin").lastName("OldName").build();

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");
        request.setFirstName("Ramtin");
        request.setLastName("NewName");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.updateUser("user123", request);

        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Ramtin", result.getFirstName());
        assertEquals("NewName", result.getLastName());

        verify(userRepository).findById("user123");
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).save(user);
    }


    @Test
    void updateUser_whenUserDoesNotExist_throwsResourceNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");
        request.setFirstName("Ramtin");
        request.setLastName("NewName");

        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser("user123", request));
        verify(userRepository).findById("user123");
    }


    @Test
    void updateUser_whenEmailAlreadyExists_throwsConflictException() {
        User user = User.builder().id("user123").email("old@example.com").build();
        User existingUser = User.builder().id("user456").email("existing@example.com").build();

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("existing@example.com");
        request.setFirstName("Ramtin");
        request.setLastName("NewName");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(ResourceConflictException.class, () -> userService.updateUser("user123", request));

        verify(userRepository).findById("user123");
        verify(userRepository).findByEmail("existing@example.com");
    }

    // updateUserRole()
    @Test
    void updateUserRole_whenUserExists_updatesRole() {
        User user = User.builder().id("user123").username("ramtin").userRole(UserRole.USER).build();

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        userService.updateUserRole("user123", UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getUserRole());

        verify(userRepository).findById("user123");
        verify(userRepository).save(user);
    }


    @Test
    void updateUserRole_whenUserDoesNotExist_throwsResourceNotFoundException() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole("user123", UserRole.ADMIN));
        verify(userRepository).findById("user123");
    }

    @Test
    void shouldUpdateCreditSourceSuccessfully() {
        User user = User.builder()
                .id("user-1")
                .creditSource(CreditSource.SUBSCRIPTION)
                .build();

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        userService.updateCreditSource(
                "user-1",
                CreditSource.PAY_AS_YOU_GO
        );

        assertEquals(
                CreditSource.PAY_AS_YOU_GO,
                user.getCreditSource()
        );

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCreditSourceForNonExistingUser() {
        when(userRepository.findById("user-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateCreditSource(
                        "user-1",
                        CreditSource.PAY_AS_YOU_GO
                )
        );
    }
}