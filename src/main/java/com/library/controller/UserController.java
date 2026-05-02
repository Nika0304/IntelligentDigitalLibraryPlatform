package com.library.controller;

import com.library.dto.UserRequest;
import com.library.dto.UserResponse;
import com.library.dto.UserStatusUpdateRequest;
import com.library.model.UserStatus;
import com.library.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers()
    {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(userService.getUserById(id));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUsersByStatus(@PathVariable UserStatus status)
    {
        try
        {
            return ResponseEntity.ok(userService.getUsersByStatus(status));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest userRequestDto)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDto));
        }
        catch (RuntimeException e)
        {
            return handleUserException(e);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserRequest request)
    {
        try
        {
            return ResponseEntity.ok(userService.updateUser(userId, request));
        }
        catch (RuntimeException e)
        {
            return handleUserException(e);
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody UserStatusUpdateRequest request)
    {
        try
        {
            return ResponseEntity.ok(userService.updateUserStatus(userId, request.getStatus()));
        }
        catch (RuntimeException e)
        {
            return handleUserException(e);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId)
    {
        try
        {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleUserException(e);
        }
    }

    private ResponseEntity<String> handleUserException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("email already exists"))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}