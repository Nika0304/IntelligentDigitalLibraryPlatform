package com.library.service;

import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WrappedService
{
    private final UserRepository userRepository;
    private final DownloadHistoryRepository downloadRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final BookService bookService;

    private static final Locale RO = new Locale("ro", "RO");

    public WrappedService(UserRepository userRepository,
                          DownloadHistoryRepository downloadRepository,
                          ReservationRepository reservationRepository,
                          ReviewRepository reviewRepository,
                          WishlistRepository wishlistRepository,
                          BookService bookService)
    {
        this.userRepository = userRepository;
        this.downloadRepository = downloadRepository;
        this.reservationRepository = reservationRepository;
        this.reviewRepository = reviewRepository;
        this.wishlistRepository = wishlistRepository;
        this.bookService = bookService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWrappedForUser(Long userId, int year)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // === 1. Colectare evenimente din anul respectiv ===
        Set<Long> consumedBookIds = new HashSet<>();
        Map<Long, Book> bookCache = new HashMap<>();
        Map<Integer, Integer> monthlyActivity = new TreeMap<>();
        Set<LocalDate> activeDays = new TreeSet<>();

        int totalDownloads = 0;
        int totalReservations = 0;

        // downloads
        for (DownloadHistory d : downloadRepository.findByUserOrderByDownloadDateDesc(user)) {
            if (d.getDownloadDate() == null || d.getDownloadDate().getYear() != year) continue;
            if (d.getBook() == null) continue;
            consumedBookIds.add(d.getBook().getBookId());
            bookCache.put(d.getBook().getBookId(), d.getBook());
            monthlyActivity.merge(d.getDownloadDate().getMonthValue(), 1, Integer::sum);
            activeDays.add(d.getDownloadDate().toLocalDate());
            totalDownloads++;
        }

        // reservations
        for (Reservation r : reservationRepository.findByUser(user)) {
            if (r.getReservationDate() == null || r.getReservationDate().getYear() != year) continue;
            if (r.getBook() == null) continue;
            consumedBookIds.add(r.getBook().getBookId());
            bookCache.put(r.getBook().getBookId(), r.getBook());
            monthlyActivity.merge(r.getReservationDate().getMonthValue(), 1, Integer::sum);
            activeDays.add(r.getReservationDate().toLocalDate());
            totalReservations++;
        }

        // reviews — pentru activitate și pentru carte favorită
        List<Review> userReviewsThisYear = new ArrayList<>();
        for (Review rv : reviewRepository.findByUser(user)) {
            if (rv.getCreatedAt() == null || rv.getCreatedAt().getYear() != year) continue;
            userReviewsThisYear.add(rv);
            activeDays.add(rv.getCreatedAt().toLocalDate());
        }

        // === 2. Top categorii ===
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> authorCount = new HashMap<>();
        for (Long bookId : consumedBookIds) {
            Book b = bookCache.get(bookId);
            if (b == null) continue;
            if (b.getCategory() != null) {
                categoryCount.merge(b.getCategory().getName(), 1, Integer::sum);
            }
            if (b.getAuthors() != null) {
                for (Author a : b.getAuthors()) {
                    authorCount.merge(a.getName(), 1, Integer::sum);
                }
            }
        }

        List<Map<String, Object>> topCategories = categoryCount.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .toList();

        Map.Entry<String, Integer> topAuthorEntry = authorCount.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);

        Map<String, Object> topAuthor = null;
        if (topAuthorEntry != null) {
            topAuthor = new LinkedHashMap<>();
            topAuthor.put("name", topAuthorEntry.getKey());
            topAuthor.put("count", topAuthorEntry.getValue());
        }

        // === 3. Luna cea mai activă ===
        Map<String, Object> mostActiveMonth = null;
        Map.Entry<Integer, Integer> topMonth = monthlyActivity.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);
        if (topMonth != null) {
            mostActiveMonth = new LinkedHashMap<>();
            mostActiveMonth.put("monthNumber", topMonth.getKey());
            mostActiveMonth.put("monthName",
                    capitalize(Month.of(topMonth.getKey()).getDisplayName(TextStyle.FULL, RO)));
            mostActiveMonth.put("count", topMonth.getValue());
        }

        // === 4. Reading streak (zile consecutive cu activitate) ===
        int streak = longestStreak(activeDays);

        // === 5. Cartea favorită (cel mai mare rating dat de utilizator anul ăsta) ===
        Map<String, Object> favoriteBook = null;
        Review topRated = userReviewsThisYear.stream()
                .max(Comparator.comparingInt(Review::getRating))
                .orElse(null);
        if (topRated != null && topRated.getBook() != null) {
            Book b = topRated.getBook();
            favoriteBook = new LinkedHashMap<>();
            favoriteBook.put("bookId", b.getBookId());
            favoriteBook.put("title", b.getTitle());
            favoriteBook.put("coverImageURL", b.getCoverImageURL());
            favoriteBook.put("userRating", topRated.getRating());
            favoriteBook.put("authors", b.getAuthors() == null ? List.of()
                    : b.getAuthors().stream().map(Author::getName).toList());
        }

        // === 6. Activitate lunară pentru chart (12 luni, cu 0 unde lipsesc) ===
        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", m);
            entry.put("monthShort", capitalize(Month.of(m).getDisplayName(TextStyle.SHORT, RO)));
            entry.put("count", monthlyActivity.getOrDefault(m, 0));
            monthlyChart.add(entry);
        }

        // === 7. Comparație cu platforma ===
        long totalUsers = Math.max(userRepository.count(), 1);
        long platformBooksThisYear = downloadRepository.findAll().stream()
                .filter(d -> d.getDownloadDate() != null && d.getDownloadDate().getYear() == year)
                .count()
                + reservationRepository.findAll().stream()
                .filter(r -> r.getReservationDate() != null && r.getReservationDate().getYear() == year)
                .count();
        double avgBooksPerUser = (double) platformBooksThisYear / totalUsers;

        // === 8. Rating mediu acordat ===
        double avgRatingGiven = userReviewsThisYear.isEmpty() ? 0
                : Math.round(userReviewsThisYear.stream().mapToInt(Review::getRating).average().orElse(0) * 10.0) / 10.0;

        // === 9. Personalitate cititor ===
        String personality = derivePersonality(
                consumedBookIds.size(),
                userReviewsThisYear.size(),
                categoryCount.size(),
                topAuthorEntry == null ? 0 : topAuthorEntry.getValue(),
                totalDownloads, totalReservations
        );

        // === 10. Wishlist count (carti dorite anul ăsta — proxy, dacă nu există dată) ===
        long wishlistCount = wishlistRepository.findByUser(user).size();

        // === Răspuns ===
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("fullName", user.getFullName());
        result.put("totalBooks", consumedBookIds.size());
        result.put("totalDownloads", totalDownloads);
        result.put("totalReservations", totalReservations);
        result.put("totalReviews", userReviewsThisYear.size());
        result.put("avgRatingGiven", avgRatingGiven);
        result.put("wishlistCount", wishlistCount);
        result.put("readingStreak", streak);
        result.put("topCategories", topCategories);
        result.put("topAuthor", topAuthor);
        result.put("mostActiveMonth", mostActiveMonth);
        result.put("favoriteBook", favoriteBook);
        result.put("monthlyActivity", monthlyChart);
        result.put("platformAverageBooks", Math.round(avgBooksPerUser * 10.0) / 10.0);
        result.put("personality", personality);
        return result;
    }

    /* === Helpers === */

    private int longestStreak(Set<LocalDate> daysSet)
    {
        if (daysSet.isEmpty()) return 0;
        List<LocalDate> days = new ArrayList<>(daysSet);
        Collections.sort(days);
        int best = 1, cur = 1;
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).minusDays(1).equals(days.get(i - 1))) {
                cur++;
                best = Math.max(best, cur);
            } else {
                cur = 1;
            }
        }
        return best;
    }

    private String derivePersonality(int books, int reviews, int categories,
                                     int topAuthorBooks, int downloads, int reservations)
    {
        if (books == 0) return "Cititor Tăcut";
        if (books >= 20) return "Cititor Vorace";
        if (reviews >= Math.max(5, books * 0.6)) return "Critic Atent";
        if (categories >= 5) return "Explorator de Genuri";
        if (topAuthorBooks >= 3) return "Fan Devotat";
        if (downloads > reservations * 2) return "Cititor Digital";
        if (reservations > downloads * 2) return "Fan al Hârtiei";
        return "Cititor Eclectic";
    }

    private String capitalize(String s)
    {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(RO) + s.substring(1);
    }
}