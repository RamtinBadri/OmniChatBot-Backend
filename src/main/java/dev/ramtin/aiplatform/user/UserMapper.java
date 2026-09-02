package dev.ramtin.aiplatform.user;

import dev.ramtin.aiplatform.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static UserDTO toDTO(User user) {
        if(user == null) {
            return null;
        }
        return UserDTO.builder().
                id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
