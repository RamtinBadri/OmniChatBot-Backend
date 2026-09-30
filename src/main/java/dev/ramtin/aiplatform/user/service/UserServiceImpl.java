package dev.ramtin.aiplatform.user.service;

import dev.ramtin.aiplatform.exception.ResourceConflictException;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.user.*;
import dev.ramtin.aiplatform.user.dto.UpdateUserRequest;
import dev.ramtin.aiplatform.user.dto.UserDTO;
import dev.ramtin.aiplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toDTO(user);
    }

    @Override
    public Page<UserDTO> getUsers(String search, String role, UserStatus status, Pageable pageable) {
        return userRepository.search(search, role, status, pageable).map(UserMapper::toDTO);
    }

    @Override
    public UserDTO updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceConflictException("Email already exists: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return UserMapper.toDTO(updatedUser);
    }

    @Override
    public void updateUserRole(String userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        user.setUserRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void updateCreditSource(String userId, CreditSource creditSource) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setCreditSource(creditSource);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}