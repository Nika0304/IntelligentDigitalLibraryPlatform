package com.library.controller;

import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.service.WrappedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Year;

@RestController
@RequestMapping("/api/wrapped")
public class WrappedController
{
    private final WrappedService wrappedService;
    private final UserRepository userRepository;

    public WrappedController(WrappedService wrappedService, UserRepository userRepository)
    {
        this.wrappedService = wrappedService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> myWrapped(@RequestParam(required = false) Integer year)
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User u = userRepository.findByEmail((String) auth.getPrincipal()).orElse(null);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        int y = year != null ? year : Year.now().getValue();
        return ResponseEntity.ok(wrappedService.getWrappedForUser(u.getUserId(), y));
    }
}