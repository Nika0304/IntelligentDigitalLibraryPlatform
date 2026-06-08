package com.library.service;

import com.library.dto.BookResponse;
import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ReviewRepository reviewRepository;
    private final BookService bookService;

    // --- ponderi pentru semnale ---
    private static final int W_WISHLIST       = 3;
    private static final int W_DOWNLOAD       = 2;
    private static final int W_RESERVATION    = 1;
    private static final int W_REVIEW_LIKED   = 4;   // rating >= 4
    private static final int W_REVIEW_DISLIKE = -3;  // rating <= 2
    private static final double AUTHOR_WEIGHT_MULT = 1.5; // autorii contează mai mult
    private static final int RECENT_DAYS = 30;

    public RecommendationService(UserRepository userRepository,
                                 BookRepository bookRepository,
                                 WishlistRepository wishlistRepository,
                                 DownloadHistoryRepository downloadRepository,
                                 ReservationRepository reservationRepository,
                                 ReviewRepository reviewRepository,
                                 BookService bookService)
    {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.wishlistRepository = wishlistRepository;
        this.downloadRepository = downloadRepository;
        this.reservationRepository = reservationRepository;
        this.reviewRepository = reviewRepository;
        this.bookService = bookService;
    }

    /* ===================================================================
       ENDPOINT VECHI — păstrat pentru compatibilitate cu frontend-ul
       =================================================================== */
    @Transactional(readOnly = true)
    public Map<String, Object> recommendForUser(Long userId, int limit)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = buildProfile(user);

        List<Book> recs = scoreAndPick(profile, limit, null);

        List<BookResponse> books = recs.stream().map(bookService::toResponse).toList();

        String reason;
        boolean personalized;
        if (profile.isCold()) {
            personalized = false;
            reason = "Cărți noi din biblioteca noastră";
        } else {
            personalized = true;
            List<String> topCats = profile.topCategoryNames(3);
            reason = "Pe baza istoricului tău: " + String.join(", ", topCats);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("reason", reason);
        result.put("isPersonalized", personalized);
        return result;
    }

    /* ===================================================================
       ENDPOINT NOU — secțiuni multiple (For You, Trending, etc.)
       =================================================================== */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> recommendSections(Long userId, int limitPerSection)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = buildProfile(user);
        List<Map<String, Object>> sections = new ArrayList<>();

        // 1. For You – personalizat (sau new arrivals pentru cold start)
        List<Book> forYou = scoreAndPick(profile, limitPerSection, null);
        if (!forYou.isEmpty()) {
            sections.add(buildSection(
                    "forYou",
                    profile.isCold() ? "Cărți noi pentru tine" : "Pentru tine",
                    profile.isCold()
                            ? "Selecție din cele mai recente apariții"
                            : "Pe baza categoriilor și autorilor preferați",
                    forYou
            ));
        }

        // 2. Trending – cele mai descărcate/rezervate în ultimele 30 de zile
        List<Book> trending = trendingBooks(profile, limitPerSection);
        if (!trending.isEmpty()) {
            sections.add(buildSection(
                    "trending",
                    "În tendințe acum",
                    "Cele mai populare cărți din ultimele " + RECENT_DAYS + " de zile",
                    trending
            ));
        }

        // 3. Bestsellers cu rating mare
        List<Book> bestRated = topRatedBooks(profile, limitPerSection);
        if (!bestRated.isEmpty()) {
            sections.add(buildSection(
                    "bestRated",
                    "Bestselleri apreciați",
                    "Cele mai bine cotate cărți din bibliotecă",
                    bestRated
            ));
        }

        // 4. De la autori favoriți (doar dacă userul ARE autori preferați)
        if (!profile.isCold() && !profile.authorScore.isEmpty()) {
            List<Book> fromAuthors = booksFromFavoriteAuthors(profile, limitPerSection);
            if (!fromAuthors.isEmpty()) {
                String topAuthorsStr = profile.topAuthorNames(2).stream()
                        .collect(Collectors.joining(" și "));
                sections.add(buildSection(
                        "byFavoriteAuthors",
                        "De la autorii tăi preferați",
                        "Continuă cu " + topAuthorsStr,
                        fromAuthors
                ));
            }
        }

        // 5. Similar cu cartea cea mai apreciată de tine
        Book anchor = profile.bestLikedBook();
        if (anchor != null) {
            List<Book> similar = similarTo(anchor, profile, limitPerSection);
            if (!similar.isEmpty()) {
                sections.add(buildSection(
                        "similarTo",
                        "Asemănătoare cu " + anchor.getTitle(),
                        "Pe baza unei cărți pe care ai apreciat-o",
                        similar
                ));
            }
        }

        return sections;
    }

    /* ===================================================================
       Profilul utilizatorului
       =================================================================== */
    private UserProfile buildProfile(User user)
    {
        UserProfile p = new UserProfile();

        // Wishlist
        wishlistRepository.findByUser(user).forEach(w -> {
            Book b = w.getBook();
            if (b == null) return;
            p.seen.add(b.getBookId());
            p.addBookSignal(b, W_WISHLIST);
        });

        // Downloads
        downloadRepository.findByUserOrderByDownloadDateDesc(user).forEach(d -> {
            Book b = d.getBook();
            if (b == null) return;
            p.seen.add(b.getBookId());
            p.addBookSignal(b, W_DOWNLOAD);
        });

        // Reservations
        reservationRepository.findByUser(user).forEach(r -> {
            Book b = r.getBook();
            if (b == null) return;
            p.seen.add(b.getBookId());
            p.addBookSignal(b, W_RESERVATION);
        });

        // Reviews — semnale puternice pozitive/negative
        reviewRepository.findByUser(user).forEach(rv -> {
            Book b = rv.getBook();
            if (b == null) return;
            p.seen.add(b.getBookId());
            int rating = rv.getRating();
            if (rating >= 4) {
                p.addBookSignal(b, W_REVIEW_LIKED);
                p.likedBooks.put(b, rating);
            } else if (rating <= 2) {
                p.addBookSignal(b, W_REVIEW_DISLIKE);
            }
        });

        return p;
    }

    /* ===================================================================
       Scoring & picking
       =================================================================== */
    private List<Book> scoreAndPick(UserProfile profile, int limit, Long excludeBookId)
    {
        // cold start → cele mai noi cărți
        if (profile.isCold()) {
            return bookRepository.findAll().stream()
                    .filter(b -> excludeBookId == null || !b.getBookId().equals(excludeBookId))
                    .sorted((a, b) -> Integer.compare(
                            safeYear(b.getPublicationYear()),
                            safeYear(a.getPublicationYear())))
                    .limit(limit)
                    .toList();
        }

        // ranking content-based pe baza profilului
        Map<Long, Long> popularityIdx = popularityIndex();

        return bookRepository.findAll().stream()
                .filter(b -> !profile.seen.contains(b.getBookId()))
                .filter(b -> excludeBookId == null || !b.getBookId().equals(excludeBookId))
                .map(b -> new ScoredBook(b, score(b, profile, popularityIdx)))
                .filter(sb -> sb.score > 0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(limit)
                .map(sb -> sb.book)
                .toList();
    }

    private double score(Book b, UserProfile profile, Map<Long, Long> popularityIdx)
    {
        double s = 0;

        // category match
        if (b.getCategory() != null) {
            s += profile.categoryScore.getOrDefault(b.getCategory().getCategoryId(), 0);
        }

        // author match (cu multiplier mai mare)
        if (b.getAuthors() != null) {
            for (Author a : b.getAuthors()) {
                s += AUTHOR_WEIGHT_MULT *
                        profile.authorScore.getOrDefault(a.getAuthorId(), 0);
            }
        }

        // rating boost
        BookResponse br = bookService.toResponse(b);
        if (br.getReviewCount() >= 3) {
            if (br.getAverageRating() >= 4.5) s += 5;
            else if (br.getAverageRating() >= 4.0) s += 3;
        }

        // popularity boost (top descărcări + rezervări)
        long popularity = popularityIdx.getOrDefault(b.getBookId(), 0L);
        if (popularity >= 5) s += 2;
        if (popularity >= 10) s += 3;

        // recency boost — ultimii 5 ani
        int year = safeYear(b.getPublicationYear());
        int currentYear = java.time.Year.now().getValue();
        if (year >= currentYear - 5) s += 1;

        return s;
    }

    private Map<Long, Long> popularityIndex()
    {
        Map<Long, Long> idx = new HashMap<>();
        downloadRepository.findAll().forEach(d -> {
            if (d.getBook() != null) {
                idx.merge(d.getBook().getBookId(), 1L, Long::sum);
            }
        });
        reservationRepository.findAll().forEach(r -> {
            if (r.getBook() != null) {
                idx.merge(r.getBook().getBookId(), 1L, Long::sum);
            }
        });
        return idx;
    }

    /* ===================================================================
       Secțiuni speciale
       =================================================================== */
    private List<Book> trendingBooks(UserProfile profile, int limit)
    {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RECENT_DAYS);
        Map<Long, Long> recentCount = new HashMap<>();
        Map<Long, Book> bookCache = new HashMap<>();

        downloadRepository.findAll().forEach(d -> {
            if (d.getBook() == null || d.getDownloadDate() == null) return;
            if (d.getDownloadDate().isAfter(cutoff)) {
                recentCount.merge(d.getBook().getBookId(), 1L, Long::sum);
                bookCache.putIfAbsent(d.getBook().getBookId(), d.getBook());
            }
        });

        reservationRepository.findAll().forEach(r -> {
            if (r.getBook() == null || r.getReservationDate() == null) return;
            if (r.getReservationDate().isAfter(cutoff)) {
                recentCount.merge(r.getBook().getBookId(), 1L, Long::sum);
                bookCache.putIfAbsent(r.getBook().getBookId(), r.getBook());
            }
        });

        return recentCount.entrySet().stream()
                .filter(e -> !profile.seen.contains(e.getKey()))
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> bookCache.get(e.getKey()))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Book> topRatedBooks(UserProfile profile, int limit)
    {
        return bookRepository.findAll().stream()
                .filter(b -> !profile.seen.contains(b.getBookId()))
                .map(b -> {
                    BookResponse br = bookService.toResponse(b);
                    return new ScoredBook(b, br.getReviewCount() >= 3 ? br.getAverageRating() : 0);
                })
                .filter(sb -> sb.score >= 4.0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(limit)
                .map(sb -> sb.book)
                .toList();
    }

    private List<Book> booksFromFavoriteAuthors(UserProfile profile, int limit)
    {
        Set<Long> favAuthorIds = profile.authorScore.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return bookRepository.findAll().stream()
                .filter(b -> !profile.seen.contains(b.getBookId()))
                .filter(b -> b.getAuthors() != null &&
                        b.getAuthors().stream().anyMatch(a -> favAuthorIds.contains(a.getAuthorId())))
                .limit(limit)
                .toList();
    }

    private List<Book> similarTo(Book anchor, UserProfile profile, int limit)
    {
        Long anchorCatId = anchor.getCategory() != null ? anchor.getCategory().getCategoryId() : null;
        Set<Long> anchorAuthorIds = anchor.getAuthors() == null ? Set.of()
                : anchor.getAuthors().stream().map(Author::getAuthorId).collect(Collectors.toSet());

        return bookRepository.findAll().stream()
                .filter(b -> !b.getBookId().equals(anchor.getBookId()))
                .filter(b -> !profile.seen.contains(b.getBookId()))
                .map(b -> {
                    double s = 0;
                    if (anchorCatId != null && b.getCategory() != null &&
                            anchorCatId.equals(b.getCategory().getCategoryId())) s += 5;
                    if (b.getAuthors() != null) {
                        long common = b.getAuthors().stream()
                                .filter(a -> anchorAuthorIds.contains(a.getAuthorId()))
                                .count();
                        s += common * 4;
                    }
                    return new ScoredBook(b, s);
                })
                .filter(sb -> sb.score > 0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(limit)
                .map(sb -> sb.book)
                .toList();
    }

    /* ===================================================================
       Helpers
       =================================================================== */
    private Map<String, Object> buildSection(String id, String title, String reason, List<Book> books)
    {
        Map<String, Object> s = new HashMap<>();
        s.put("id", id);
        s.put("title", title);
        s.put("reason", reason);
        s.put("books", books.stream().map(bookService::toResponse).toList());
        return s;
    }

    private int safeYear(Integer y) { return y == null ? 0 : y; }

    /* ===================================================================
       Profile + scored book holders
       =================================================================== */
    private static class UserProfile
    {
        Set<Long> seen = new HashSet<>();
        Map<Long, Integer> categoryScore = new HashMap<>();
        Map<Long, Integer> authorScore = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        Map<Long, String> authorNames = new HashMap<>();
        Map<Book, Integer> likedBooks = new LinkedHashMap<>(); // Book → rating

        void addBookSignal(Book b, int weight)
        {
            if (b.getCategory() != null) {
                categoryScore.merge(b.getCategory().getCategoryId(), weight, Integer::sum);
                categoryNames.putIfAbsent(b.getCategory().getCategoryId(), b.getCategory().getName());
            }
            if (b.getAuthors() != null) {
                for (Author a : b.getAuthors()) {
                    authorScore.merge(a.getAuthorId(), weight, Integer::sum);
                    authorNames.putIfAbsent(a.getAuthorId(), a.getName());
                }
            }
        }

        boolean isCold()
        {
            // niciun semnal pozitiv pe categorii/autori
            boolean noCat = categoryScore.values().stream().noneMatch(v -> v > 0);
            boolean noAuth = authorScore.values().stream().noneMatch(v -> v > 0);
            return noCat && noAuth;
        }

        List<String> topCategoryNames(int n)
        {
            return categoryScore.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(n)
                    .map(e -> categoryNames.getOrDefault(e.getKey(), ""))
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        List<String> topAuthorNames(int n)
        {
            return authorScore.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(n)
                    .map(e -> authorNames.getOrDefault(e.getKey(), ""))
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        Book bestLikedBook()
        {
            return likedBooks.entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
    }

    private static class ScoredBook
    {
        final Book book;
        final double score;
        ScoredBook(Book b, double s) { this.book = b; this.score = s; }
    }
}