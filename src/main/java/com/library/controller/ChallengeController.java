package com.library.controller;

import com.library.dto.ChallengeRequest;
import com.library.dto.ChallengeResponse;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.service.ChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final ChallengeService service;
    private final UserRepository userRepo;

    public ChallengeController(ChallengeService service, UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    /* ===== PUBLIC ===== */
    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> list() {
        return ResponseEntity.ok(service.listActive(currentUser()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> myChallenges() {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(service.myChallenges(u));
    }

    /* ===== USER ===== */
    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable("id") Long id) {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { return ResponseEntity.ok(service.join(id, u)); }
        catch (RuntimeException e) { return handle(e); }
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable("id") Long id) {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { service.leave(id, u); return ResponseEntity.noContent().build(); }
        catch (RuntimeException e) { return handle(e); }
    }

    /* ===== ADMIN ===== */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ChallengeRequest req) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req)); }
        catch (RuntimeException e) { return handle(e); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody ChallengeRequest req) {
        try { return ResponseEntity.ok(service.update(id, req)); }
        catch (RuntimeException e) { return handle(e); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> archive(@PathVariable("id") Long id) {
        try { service.archive(id); return ResponseEntity.noContent().build(); }
        catch (RuntimeException e) { return handle(e); }
    }

    /* ===== Helpers ===== */
    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepo.findByEmail((String) auth.getPrincipal()).orElse(null);
    }

    private ResponseEntity<String> handle(RuntimeException e) {
        String m = e.getMessage();
        if (m != null && m.toLowerCase().contains("not found"))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(m);
        if (m != null && m.toLowerCase().contains("already"))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(m);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
    }
}