package com.library.service;

import com.library.dto.BookResponse;
import com.library.dto.BookSearchResponse;
import com.library.model.Author;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.repository.DownloadHistoryRepository;
import com.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookSearchService
{
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final DownloadHistoryRepository downloadRepo;
    private final ReservationRepository reservationRepo;

    public BookSearchService(BookRepository bookRepository,
                             BookService bookService,
                             DownloadHistoryRepository downloadRepo,
                             ReservationRepository reservationRepo)
    {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.downloadRepo = downloadRepo;
        this.reservationRepo = reservationRepo;
    }

    @Transactional(readOnly = true)
    public BookSearchResponse search(String q,
                                     List<Long> categoryIds,
                                     List<Long> authorIds,
                                     String type,           // "all" | "digital" | "physical"
                                     Integer yearFrom,
                                     Integer yearTo,
                                     Double minRating,
                                     Boolean onlyAvailable,
                                     String sort,           // relevance | title | yearDesc | yearAsc | ratingDesc | popularityDesc
                                     int page,
                                     int size)
    {
        List<Book> all = bookRepository.findAll();

        // 1. Construire BookResponse o singură dată per carte (rating + copies) ca să nu reapelăm
        Map<Long, BookResponse> respCache = new HashMap<>();
        for (Book b : all) respCache.put(b.getBookId(), bookService.toResponse(b));

        Map<Long, Long> popularity = popularityIndex();

        String qLower = q == null ? null : q.trim().toLowerCase();
        boolean hasQ = qLower != null && !qLower.isEmpty();

        // 2. Filtrare
        List<Book> filtered = all.stream().filter(b -> {
            BookResponse br = respCache.get(b.getBookId());

            if (hasQ) {
                String hay = (safe(b.getTitle()) + " " + safe(b.getDescription()) + " "
                        + br.getAuthors().stream().collect(Collectors.joining(" "))).toLowerCase();
                if (!hay.contains(qLower)) return false;
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                if (b.getCategory() == null) return false;
                if (!categoryIds.contains(b.getCategory().getCategoryId())) return false;
            }

            if (authorIds != null && !authorIds.isEmpty()) {
                if (b.getAuthors() == null || b.getAuthors().isEmpty()) return false;
                boolean any = b.getAuthors().stream()
                        .anyMatch(a -> authorIds.contains(a.getAuthorId()));
                if (!any) return false;
            }

            if ("digital".equalsIgnoreCase(type) && !b.isHasDigitalCopy()) return false;
            if ("physical".equalsIgnoreCase(type) && !b.isHasPhysicalCopy()) return false;

            if (yearFrom != null && (b.getPublicationYear() == null || b.getPublicationYear() < yearFrom)) return false;
            if (yearTo   != null && (b.getPublicationYear() == null || b.getPublicationYear() > yearTo)) return false;

            if (minRating != null && br.getAverageRating() < minRating) return false;

            if (Boolean.TRUE.equals(onlyAvailable)) {
                // disponibilă = are exemplare fizice libere SAU e digitală
                boolean ok = br.getAvailableCopies() > 0 || b.isHasDigitalCopy();
                if (!ok) return false;
            }

            return true;
        }).collect(Collectors.toCollection(ArrayList::new));

        // 3. Sortare
        Comparator<Book> cmp = comparator(sort, respCache, popularity, qLower);
        filtered.sort(cmp);

        // 4. Facete (pe baza setului filtrat — minus filtrul de categorie/autor curent, ca să arate alte opțiuni cu count)
        Map<String, Object> facets = buildFacets(all, respCache, q, type, yearFrom, yearTo, minRating, onlyAvailable);

        long total = filtered.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;

        // 5. Paginare
        int from = Math.max(0, page * size);
        int to = Math.min(filtered.size(), from + size);
        List<BookResponse> pageItems = (from >= filtered.size())
                ? List.of()
                : filtered.subList(from, to).stream()
                .map(b -> respCache.get(b.getBookId()))
                .toList();

        return new BookSearchResponse(pageItems, total, page, size, totalPages, facets);
    }

    /* ===== sortare ===== */
    private Comparator<Book> comparator(String sort,
                                        Map<Long, BookResponse> resp,
                                        Map<Long, Long> popularity,
                                        String qLower)
    {
        if (sort == null) sort = "relevance";

        switch (sort) {
            case "title":
                return Comparator.comparing(b -> safe(b.getTitle()).toLowerCase());
            case "yearDesc":
                return Comparator.comparingInt((Book b) -> safeYear(b.getPublicationYear())).reversed();
            case "yearAsc":
                return Comparator.comparingInt((Book b) -> safeYear(b.getPublicationYear()));
            case "ratingDesc":
                return Comparator.comparingDouble((Book b) ->
                        resp.get(b.getBookId()).getAverageRating()).reversed();
            case "popularityDesc":
                return Comparator.comparingLong((Book b) ->
                        popularity.getOrDefault(b.getBookId(), 0L)).reversed();
            case "relevance":
            default:
                if (qLower == null || qLower.isEmpty()) {
                    // fără query → ordonare pe popularitate
                    return Comparator.comparingLong((Book b) ->
                            popularity.getOrDefault(b.getBookId(), 0L)).reversed();
                }
                // cu query → titlul care începe cu termenul primește boost
                return Comparator.comparingInt((Book b) -> relevanceScore(b, qLower)).reversed();
        }
    }

    private int relevanceScore(Book b, String qLower)
    {
        String t = safe(b.getTitle()).toLowerCase();
        if (t.equals(qLower))      return 100;
        if (t.startsWith(qLower))  return 50;
        if (t.contains(qLower))    return 20;
        return 1; // match pe alt câmp
    }

    /* ===== popularitate ===== */
    private Map<Long, Long> popularityIndex()
    {
        Map<Long, Long> idx = new HashMap<>();
        downloadRepo.findAll().forEach(d -> {
            if (d.getBook() != null) idx.merge(d.getBook().getBookId(), 1L, Long::sum);
        });
        reservationRepo.findAll().forEach(r -> {
            if (r.getBook() != null) idx.merge(r.getBook().getBookId(), 1L, Long::sum);
        });
        return idx;
    }

    /* ===== facete ===== */
    private Map<String, Object> buildFacets(List<Book> all,
                                            Map<Long, BookResponse> resp,
                                            String q,
                                            String type,
                                            Integer yearFrom,
                                            Integer yearTo,
                                            Double minRating,
                                            Boolean onlyAvailable)
    {
        // pentru facets — luăm cărțile care trec FILTRELE GLOBALE (fără cele de categorie/autor),
        // ca să arătăm câte cărți există în fiecare categorie pentru contextul curent
        String qLower = q == null ? null : q.trim().toLowerCase();
        boolean hasQ = qLower != null && !qLower.isEmpty();

        List<Book> base = all.stream().filter(b -> {
            BookResponse br = resp.get(b.getBookId());

            if (hasQ) {
                String hay = (safe(b.getTitle()) + " " + safe(b.getDescription()) + " "
                        + br.getAuthors().stream().collect(Collectors.joining(" "))).toLowerCase();
                if (!hay.contains(qLower)) return false;
            }
            if ("digital".equalsIgnoreCase(type) && !b.isHasDigitalCopy()) return false;
            if ("physical".equalsIgnoreCase(type) && !b.isHasPhysicalCopy()) return false;
            if (yearFrom != null && (b.getPublicationYear() == null || b.getPublicationYear() < yearFrom)) return false;
            if (yearTo   != null && (b.getPublicationYear() == null || b.getPublicationYear() > yearTo))   return false;
            if (minRating != null && br.getAverageRating() < minRating) return false;
            if (Boolean.TRUE.equals(onlyAvailable)) {
                boolean ok = br.getAvailableCopies() > 0 || b.isHasDigitalCopy();
                if (!ok) return false;
            }
            return true;
        }).toList();

        // Categorii
        Map<Long, long[]> catCount = new LinkedHashMap<>(); // id -> [count]
        Map<Long, String> catName = new HashMap<>();
        for (Book b : base) {
            if (b.getCategory() != null) {
                catCount.computeIfAbsent(b.getCategory().getCategoryId(), k -> new long[1])[0]++;
                catName.putIfAbsent(b.getCategory().getCategoryId(), b.getCategory().getName());
            }
        }
        List<Map<String, Object>> categories = catCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", e.getKey());
                    m.put("name", catName.get(e.getKey()));
                    m.put("count", e.getValue()[0]);
                    return m;
                })
                .sorted((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")))
                .toList();

        // Autori
        Map<Long, long[]> authCount = new LinkedHashMap<>();
        Map<Long, String> authName = new HashMap<>();
        for (Book b : base) {
            if (b.getAuthors() != null) {
                for (Author a : b.getAuthors()) {
                    authCount.computeIfAbsent(a.getAuthorId(), k -> new long[1])[0]++;
                    authName.putIfAbsent(a.getAuthorId(), a.getName());
                }
            }
        }
        List<Map<String, Object>> authors = authCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", e.getKey());
                    m.put("name", authName.get(e.getKey()));
                    m.put("count", e.getValue()[0]);
                    return m;
                })
                .sorted((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")))
                .limit(30) // top 30 autori
                .toList();

        // Ani
        int minYear = base.stream()
                .filter(b -> b.getPublicationYear() != null)
                .mapToInt(Book::getPublicationYear)
                .min().orElse(1900);
        int maxYear = base.stream()
                .filter(b -> b.getPublicationYear() != null)
                .mapToInt(Book::getPublicationYear)
                .max().orElse(java.time.Year.now().getValue());

        Map<String, Object> facets = new LinkedHashMap<>();
        facets.put("categories", categories);
        facets.put("authors", authors);
        facets.put("yearMin", minYear);
        facets.put("yearMax", maxYear);
        facets.put("totalAfterCommonFilters", base.size());
        return facets;
    }

    /* ===== util ===== */
    private String safe(String s) { return s == null ? "" : s; }
    private int safeYear(Integer y) { return y == null ? 0 : y; }
}