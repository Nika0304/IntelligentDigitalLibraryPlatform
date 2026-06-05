package com.library.controller;

import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.service.BookGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class BookGroupController
{
    private final BookGroupService groupService;
    private final UserRepository userRepository;

    public BookGroupController(BookGroupService groupService, UserRepository userRepository)
    {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> listApproved() { return ResponseEntity.ok(groupService.listApproved()); }

    @GetMapping("/pending")
    public ResponseEntity<?> listPending() { return ResponseEntity.ok(groupService.listPending()); }

    @GetMapping("/me")
    public ResponseEntity<?> myGroups()
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(groupService.myGroups(u.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id)
    {
        User u = currentUser();
        return ResponseEntity.ok(groupService.getDetails(id, u == null ? null : u.getUserId()));
    }

    @PostMapping
    public ResponseEntity<?> propose(@RequestBody Map<String, String> body)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    groupService.proposeGroup(u.getUserId(),
                            body.get("name"), body.get("theme"), body.get("description")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id)
    {
        try { return ResponseEntity.ok(groupService.approveGroup(id)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id)
    {
        try { return ResponseEntity.ok(groupService.rejectGroup(id)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> archive(@PathVariable Long id)
    {
        groupService.archiveGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable Long id)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { groupService.joinGroup(id, u.getUserId()); return ResponseEntity.ok().build(); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable Long id)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { groupService.leaveGroup(id, u.getUserId()); return ResponseEntity.ok().build(); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/{id}/featured-book")
    public ResponseEntity<?> setBook(@PathVariable Long id, @RequestBody Map<String, Long> body)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { return ResponseEntity.ok(groupService.setFeaturedBook(id, u.getUserId(), body.get("bookId"))); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable Long id)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Autentificare necesară.");
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            return ResponseEntity.ok(groupService.listMessages(id, u.getUserId(), isAdmin));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> postMessage(@PathVariable Long id, @RequestBody Map<String, String> body)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(groupService.postMessage(id, u.getUserId(), body.get("content")));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/messages/{mid}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long mid)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try { groupService.deleteMessage(mid, u.getUserId(), isAdmin); return ResponseEntity.ok().build(); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/top")
    public ResponseEntity<?> topActive()
    {
        return ResponseEntity.ok(groupService.topActive(3));
    }

    private User currentUser()
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepository.findByEmail((String) auth.getPrincipal()).orElse(null);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<?> castVote(@PathVariable Long id, @RequestBody Map<String, Long> body)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { return ResponseEntity.ok(groupService.castVote(id, u.getUserId(), body.get("bookId"))); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/{id}/vote")
    public ResponseEntity<?> getVote(@PathVariable Long id)
    {
        User u = currentUser();
        return ResponseEntity.ok(groupService.getVoteTally(id, u == null ? null : u.getUserId()));
    }

    @PostMapping("/{id}/vote/apply")
    public ResponseEntity<?> applyWinner(@PathVariable Long id)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try { return ResponseEntity.ok(groupService.applyVoteWinner(id, u.getUserId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/{id}/toggle-mute")
    public ResponseEntity<?> toggleMute(@PathVariable Long id)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            boolean muted = groupService.toggleMute(id, u.getUserId());
            return ResponseEntity.ok(Map.of("muted", muted));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

}