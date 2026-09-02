package dev.ramtin.aiplatform.user.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.UserMapper;
import dev.ramtin.aiplatform.user.UserRepository;
import dev.ramtin.aiplatform.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    public Page<UserDTO> getUsers(String search, String role, String status, Pageable pageable) {
        return userRepository.search(search, role, status, pageable).map(UserMapper::toDTO);
    }
}