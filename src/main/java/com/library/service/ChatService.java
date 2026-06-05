package com.library.service;

import com.library.dto.BookResponse;
import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService
{
    private final FaqEntryRepository faqRepository;
    private final ChatQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookService bookService;
    private final NotificationService notificationService;

    public ChatService(FaqEntryRepository faqRepository,
                       ChatQuestionRepository questionRepository,
                       UserRepository userRepository,
                       BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       BookService bookService,
                       NotificationService notificationService)
    {
        this.faqRepository = faqRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookService = bookService;
        this.notificationService = notificationService;
    }

    public List<FaqEntry> getAllFaq() { return faqRepository.findAll(); }

    @Transactional
    public FaqEntry createFaq(String q, String a, String k)
    {
        return faqRepository.save(new FaqEntry(q, a, normalize(k)));
    }

    @Transactional
    public FaqEntry updateFaq(Long id, String q, String a, String k)
    {
        FaqEntry f = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        f.setQuestion(q);
        f.setAnswer(a);
        f.setKeywords(normalize(k));
        return faqRepository.save(f);
    }

    @Transactional
    public void deleteFaq(Long id) { faqRepository.deleteById(id); }

    /**
     * Returnează un map cu cheile: type, answer, books
     * type: "FAQ" | "BOOKS" | "NONE"
     */
    public Map<String, Object> match(String text)
    {
        Map<String, Object> result = new HashMap<>();
        String normalized = normalize(text);

        // 1. FAQ keyword match
        for (FaqEntry f : faqRepository.findAll())
        {
            for (String kw : f.getKeywords().split(","))
            {
                String trimmed = kw.trim();
                if (!trimmed.isEmpty() && normalized.contains(trimmed))
                {
                    result.put("type", "FAQ");
                    result.put("question", f.getQuestion());
                    result.put("answer", f.getAnswer());
                    return result;
                }
            }
        }

        // 2. Category match
        List<Book> books = new ArrayList<>();
        for (Category c : categoryRepository.findAll())
        {
            if (normalized.contains(normalize(c.getName())))
            {
                books.addAll(bookRepository.findByCategory(c));
                break;
            }
        }

        // 3. Title fuzzy match (dacă încă n-am găsit)
        if (books.isEmpty())
        {
            for (Book b : bookRepository.findAll())
            {
                if (normalize(b.getTitle()).contains(normalized) && normalized.length() >= 3)
                {
                    books.add(b);
                }
            }
        }

        if (!books.isEmpty())
        {
            List<BookResponse> top = books.stream()
                    .limit(4)
                    .map(bookService::toResponse)
                    .toList();
            result.put("type", "BOOKS");
            result.put("books", top);
            return result;
        }

        result.put("type", "NONE");
        return result;
    }

    @Transactional
    public ChatQuestion submitQuestion(Long userId, String message)
    {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return questionRepository.save(new ChatQuestion(u, message));
    }

    public List<ChatQuestion> getUserQuestions(Long userId)
    {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return questionRepository.findByUserOrderByCreatedAtDesc(u);
    }

    public List<ChatQuestion> getPendingQuestions()
    {
        return questionRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Transactional
    public ChatQuestion answerQuestion(Long questionId, String answer)
    {
        ChatQuestion q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        q.setAnswer(answer);
        q.setStatus("ANSWERED");
        q.setAnsweredAt(LocalDateTime.now());
        ChatQuestion saved = questionRepository.save(q);

        // notifică user-ul (apare în Profil + email automat)
        notificationService.createAutomaticNotification(
                q.getUser(),
                "Adminul ți-a răspuns la întrebare: \"" + answer + "\"",
                NotificationType.SYSTEM
        );

        return saved;
    }

    private static String normalize(String s)
    {
        if (s == null) return "";
        String n = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.trim();
    }
}