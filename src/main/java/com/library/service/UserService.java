package com.library.service;

import com.library.dto.UserRequest;
import com.library.dto.UserResponse;
import com.library.model.Role;
import com.library.model.RoleType;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService
{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository)
    {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserResponse> getAllUsers()
    {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users)
        {
            responses.add(mapToResponse(user));
        }

        return responses;
    }

    public UserResponse getUserById(Long userId)
    {
        User user = getUserEntityById(userId);
        return mapToResponse(user);
    }

    public List<UserResponse> getUsersByStatus(UserStatus status)
    {
        List<User> users = userRepository.findByStatus(status);
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users)
        {
            responses.add(mapToResponse(user));
        }

        return responses;
    }

    public UserResponse createUser(UserRequest request)
    {
        validateUserRequest(request, true);

        if (userRepository.existsByEmail(request.getEmail()))
        {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public UserResponse updateUser(Long userId, UserRequest request)
    {
        User user = getUserEntityById(userId);

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty())
        {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty())
        {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }

            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty())
        {
            user.setPassword(request.getPassword());
        }

        if (request.getRoleId() != null)
        {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));

            user.setRole(role);
        }

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public UserResponse updateUserStatus(Long userId, UserStatus status)
    {
        if (status == null)
        {
            throw new RuntimeException("User status is required");
        }

        User user = getUserEntityById(userId);
        user.setStatus(status);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public void deleteUser(Long userId)
    {
        User user = getUserEntityById(userId);
        userRepository.delete(user);
    }

    private User getUserEntityById(Long userId)
    {
        if (userId == null)
        {
            throw new RuntimeException("User id is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private void validateUserRequest(UserRequest request, boolean requirePassword)
    {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty())
        {
            throw new RuntimeException("Full name is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty())
        {
            throw new RuntimeException("Email is required");
        }

        if (requirePassword && (request.getPassword() == null || request.getPassword().trim().isEmpty()))
        {
            throw new RuntimeException("Password is required");
        }

        if (request.getRoleId() == null)
        {
            throw new RuntimeException("Role id is required");
        }
    }

    private UserResponse mapToResponse(User user)
    {
        Long roleId = null;
        String roleName = null;

        if (user.getRole() != null)
        {
            roleId = user.getRole().getRoleId();

            if (user.getRole().getRoleName() != null)
            {
                roleName = user.getRole().getRoleName().name();
            }
        }

        return new UserResponse(user.getUserId(), user.getFullName(), user.getEmail(), user.getStatus(), roleId, roleName);
    }
}