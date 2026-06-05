package com.library.controller;

import com.library.model.ChatQuestion;
import com.library.model.FaqEntry;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController
{
    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository)
    {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @GetMapping("/faq")
    public ResponseEntity<List<FaqEntry>> getAllFaq()
    {
        return ResponseEntity.ok(chatService.getAllFaq());
    }

    @PostMapping("/match")
    public ResponseEntity<Map<String, Object>> match(@RequestBody Map<String, String> body)
    {
        return ResponseEntity.ok(chatService.match(body.getOrDefault("text", "")));
    }

    @PostMapping("/questions")
    public ResponseEntity<?> submitQuestion(@RequestBody Map<String, String> body)
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.submitQuestion(u.getUserId(), body.get("message")));
    }

    @GetMapping("/questions/me")
    public ResponseEntity<?> myQuestions()
    {
        User u = currentUser();
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(chatService.getUserQuestions(u.getUserId()));
    }

    @GetMapping("/questions/pending")
    public ResponseEntity<List<ChatQuestion>> pending()
    {
        return ResponseEntity.ok(chatService.getPendingQuestions());
    }

    @PutMapping("/questions/{id}/answer")
    public ResponseEntity<?> answer(@PathVariable Long id, @RequestBody Map<String, String> body)
    {
        return ResponseEntity.ok(chatService.answerQuestion(id, body.get("answer")));
    }

    @PostMapping("/faq")
    public ResponseEntity<FaqEntry> createFaq(@RequestBody Map<String, String> body)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                chatService.createFaq(body.get("question"), body.get("answer"), body.get("keywords")));
    }

    @PutMapping("/faq/{id}")
    public ResponseEntity<FaqEntry> updateFaq(@PathVariable Long id, @RequestBody Map<String, String> body)
    {
        return ResponseEntity.ok(
                chatService.updateFaq(id, body.get("question"), body.get("answer"), body.get("keywords")));
    }

    @DeleteMapping("/faq/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id)
    {
        chatService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    private User currentUser()
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepository.findByEmail((String) auth.getPrincipal()).orElse(null);
    }
}