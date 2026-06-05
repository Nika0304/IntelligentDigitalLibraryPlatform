package com.library.controller;

import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController
{
    private final RecommendationService service;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService service, UserRepository userRepository)
    {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> myRecommendations(@RequestParam(defaultValue = "6") int limit)
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User u = userRepository.findByEmail((String) auth.getPrincipal())
                .orElse(null);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(service.recommendForUser(u.getUserId(), limit));
    }
}