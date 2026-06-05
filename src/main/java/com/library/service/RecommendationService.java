package com.library.service;

import com.library.dto.BookResponse;
import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService
{
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final WishlistRepository wishlistRepository;
    private final DownloadHistoryRepository downloadRepository;
    private final ReservationRepository reservationRepository;
    private final BookService bookService;

    public RecommendationService(UserRepository userRepository,
                                 BookRepository bookRepository,
                                 WishlistRepository wishlistRepository,
                                 DownloadHistoryRepository downloadRepository,
                                 ReservationRepository reservationRepository,
                                 BookService bookService)
    {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.wishlistRepository = wishlistRepository;
        this.downloadRepository = downloadRepository;
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> recommendForUser(Long userId, int limit)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Long> seenBookIds = new HashSet<>();
        Map<Long, Integer> categoryScore = new HashMap<>();
        Map<Long, Category> categoryCache = new HashMap<>();

        // 1. wishlist → +3 puncte / carte
        wishlistRepository.findByUser(user).forEach(w -> {
            Book b = w.getBook();
            if (b == null) return;
            seenBookIds.add(b.getBookId());
            if (b.getCategory() != null) {
                categoryScore.merge(b.getCategory().getCategoryId(), 3, Integer::sum);
                categoryCache.putIfAbsent(b.getCategory().getCategoryId(), b.getCategory());
            }
        });

        // 2. descărcări → +2 puncte / carte
        downloadRepository.findByUserOrderByDownloadDateDesc(user).forEach(d -> {
            Book b = d.getBook();
            if (b == null) return;
            seenBookIds.add(b.getBookId());
            if (b.getCategory() != null) {
                categoryScore.merge(b.getCategory().getCategoryId(), 2, Integer::sum);
                categoryCache.putIfAbsent(b.getCategory().getCategoryId(), b.getCategory());
            }
        });

        // 3. rezervări → +1 punct / carte
        reservationRepository.findByUser(user).forEach(r -> {
            Book b = r.getBook();
            if (b == null) return;
            seenBookIds.add(b.getBookId());
            if (b.getCategory() != null) {
                categoryScore.merge(b.getCategory().getCategoryId(), 1, Integer::sum);
                categoryCache.putIfAbsent(b.getCategory().getCategoryId(), b.getCategory());
            }
        });

        // 4. selectează top 3 categorii preferate
        List<Category> topCategories = categoryScore.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(e -> categoryCache.get(e.getKey()))
                .toList();

        // 5. dacă userul nu are istoric → recomandă cele mai noi cărți
        List<Book> candidates;
        boolean isCold = topCategories.isEmpty();

        if (isCold) {
            candidates = bookRepository.findAll().stream()
                    .sorted((a, b) -> Integer.compare(
                            b.getPublicationYear() == null ? 0 : b.getPublicationYear(),
                            a.getPublicationYear() == null ? 0 : a.getPublicationYear()))
                    .limit(limit)
                    .toList();
        } else {
            candidates = topCategories.stream()
                    .flatMap(c -> bookRepository.findByCategory(c).stream())
                    .filter(b -> !seenBookIds.contains(b.getBookId()))
                    .distinct()
                    .limit(limit)
                    .toList();

            // dacă în categoriile preferate sunt prea puține → completează cu cărți populare
            if (candidates.size() < limit) {
                final List<Book> currentCandidates = candidates;
                List<Book> filler = bookRepository.findAll().stream()
                        .filter(b -> !seenBookIds.contains(b.getBookId()))
                        .filter(b -> !currentCandidates.contains(b))
                        .limit(limit - currentCandidates.size())
                        .toList();
                List<Book> combined = new ArrayList<>(currentCandidates);
                combined.addAll(filler);
                candidates = combined;
            }
        }

        List<BookResponse> books = candidates.stream()
                .map(bookService::toResponse)
                .toList();

        String reason = isCold
                ? "Cărți noi din biblioteca noastră"
                : "Pe baza istoricului tău: " + topCategories.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));

        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("reason", reason);
        result.put("isPersonalized", !isCold);
        return result;
    }
}