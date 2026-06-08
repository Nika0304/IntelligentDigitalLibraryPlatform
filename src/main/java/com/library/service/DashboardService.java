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

@Service
public class DashboardService {
    private static final Locale RO = new Locale("ro", "RO");

    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final BookCopyRepository copyRepo;
    private final ReservationRepository reservationRepo;
    private final DownloadHistoryRepository downloadRepo;
    private final ReviewRepository reviewRepo;
    private final FineRepository fineRepo;
    private final WishlistRepository wishlistRepo;
    private final ChallengeRepository challengeRepo;
    private final ChallengeParticipationRepository participationRepo;
    private final BookGroupRepository groupRepo;

    public DashboardService(UserRepository userRepo, BookRepository bookRepo,
                            BookCopyRepository copyRepo, ReservationRepository reservationRepo,
                            DownloadHistoryRepository downloadRepo, ReviewRepository reviewRepo,
                            FineRepository fineRepo, WishlistRepository wishlistRepo,
                            ChallengeRepository challengeRepo,
                            ChallengeParticipationRepository participationRepo,
                            BookGroupRepository groupRepo) {
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
        this.copyRepo = copyRepo;
        this.reservationRepo = reservationRepo;
        this.downloadRepo = downloadRepo;
        this.reviewRepo = reviewRepo;
        this.fineRepo = fineRepo;
        this.wishlistRepo = wishlistRepo;
        this.challengeRepo = challengeRepo;
        this.participationRepo = participationRepo;
        this.groupRepo = groupRepo;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard() {
        Map<String, Object> dash = new LinkedHashMap<>();
        dash.put("kpis", buildKpis());
        dash.put("monthlyTrends", buildMonthlyTrends());
        dash.put("topBooks", buildTopBooks(8));
        dash.put("topCategories", buildTopCategories());
        dash.put("topUsers", buildTopUsers(5));
        dash.put("activityFeed", buildActivityFeed(20));
        dash.put("alerts", buildAlerts());
        dash.put("reservationStatusBreakdown", buildReservationStatusBreakdown());
        dash.put("challengesStats", buildChallengesStats());
        return dash;
    }

    /* ============ KPI CARDS ============ */
    private Map<String, Object> buildKpis() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);

        long totalBooks       = bookRepo.count();
        long totalUsers       = userRepo.count();
        long totalCopies      = copyRepo.count();
        long activeRes        = reservationRepo.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED
                        || r.getStatus() == ReservationStatus.BORROWED
                        || r.getStatus() == ReservationStatus.WAITING
                        || r.getStatus() == ReservationStatus.READY_FOR_PICKUP).count();
        long totalDownloads   = downloadRepo.count();
        long totalReviews     = reviewRepo.count();
        long pendingFines     = fineRepo.findAll().stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING).count();

        long usersThisMonth = userRepo.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(monthStart)).count();
        long usersPrevMonth = userRepo.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                        && u.getCreatedAt().isAfter(prevMonthStart)
                        && u.getCreatedAt().isBefore(monthStart)).count();

        long downloadsThisMonth = downloadRepo.findAll().stream()
                .filter(d -> d.getDownloadDate() != null && d.getDownloadDate().isAfter(monthStart)).count();
        long downloadsPrevMonth = downloadRepo.findAll().stream()
                .filter(d -> d.getDownloadDate() != null
                        && d.getDownloadDate().isAfter(prevMonthStart)
                        && d.getDownloadDate().isBefore(monthStart)).count();

        long reservationsThisMonth = reservationRepo.findAll().stream()
                .filter(r -> r.getReservationDate() != null && r.getReservationDate().isAfter(monthStart)).count();
        long reservationsPrevMonth = reservationRepo.findAll().stream()
                .filter(r -> r.getReservationDate() != null
                        && r.getReservationDate().isAfter(prevMonthStart)
                        && r.getReservationDate().isBefore(monthStart)).count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalBooks",     totalBooks);
        kpis.put("totalCopies",    totalCopies);
        kpis.put("totalUsers",     totalUsers);
        kpis.put("activeReservations", activeRes);
        kpis.put("totalDownloads", totalDownloads);
        kpis.put("totalReviews",   totalReviews);
        kpis.put("pendingFines",   pendingFines);

        // Trend cards (cu delta față de luna trecută)
        List<Map<String, Object>> trends = new ArrayList<>();
        trends.add(trendCard("Utilizatori noi (lună)",   usersThisMonth,        usersPrevMonth));
        trends.add(trendCard("Descărcări (lună)",        downloadsThisMonth,    downloadsPrevMonth));
        trends.add(trendCard("Rezervări noi (lună)",     reservationsThisMonth, reservationsPrevMonth));
        kpis.put("trends", trends);

        return kpis;
    }

    private Map<String, Object> trendCard(String label, long current, long previous) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("label", label);
        t.put("value", current);
        t.put("previous", previous);
        long delta = current - previous;
        double pct = previous == 0 ? (current > 0 ? 100.0 : 0.0)
                : Math.round(((double) delta / previous) * 1000.0) / 10.0;
        t.put("delta", delta);
        t.put("deltaPct", pct);
        return t;
    }

    /* ============ MONTHLY TRENDS (ultimele 12 luni) ============ */
    private List<Map<String, Object>> buildMonthlyTrends() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).minusMonths(11);

        Map<String, int[]> byMonth = new LinkedHashMap<>(); // key="YYYY-MM" → [reservations, downloads, reviews]
        for (int i = 0; i < 12; i++) {
            LocalDateTime m = start.plusMonths(i);
            String key = m.getYear() + "-" + String.format("%02d", m.getMonthValue());
            byMonth.put(key, new int[]{0, 0, 0});
        }

        reservationRepo.findAll().forEach(r -> {
            if (r.getReservationDate() == null) return;
            LocalDateTime d = r.getReservationDate();
            if (d.isBefore(start)) return;
            String key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            if (byMonth.containsKey(key)) byMonth.get(key)[0]++;
        });
        downloadRepo.findAll().forEach(d -> {
            if (d.getDownloadDate() == null) return;
            if (d.getDownloadDate().isBefore(start)) return;
            String key = d.getDownloadDate().getYear() + "-" + String.format("%02d", d.getDownloadDate().getMonthValue());
            if (byMonth.containsKey(key)) byMonth.get(key)[1]++;
        });
        reviewRepo.findAll().forEach(rv -> {
            if (rv.getCreatedAt() == null) return;
            if (rv.getCreatedAt().isBefore(start)) return;
            String key = rv.getCreatedAt().getYear() + "-" + String.format("%02d", rv.getCreatedAt().getMonthValue());
            if (byMonth.containsKey(key)) byMonth.get(key)[2]++;
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, int[]> e : byMonth.entrySet()) {
            String[] parts = e.getKey().split("-");
            int monthNum = Integer.parseInt(parts[1]);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", parts[1] + "/" + parts[0].substring(2));
            entry.put("monthShort", capitalize(Month.of(monthNum).getDisplayName(TextStyle.SHORT, RO)));
            entry.put("reservations", e.getValue()[0]);
            entry.put("downloads",    e.getValue()[1]);
            entry.put("reviews",      e.getValue()[2]);
            result.add(entry);
        }
        return result;
    }

    /* ============ TOP BOOKS ============ */
    private List<Map<String, Object>> buildTopBooks(int limit) {
        Map<Long, Long> activityCount = new HashMap<>();
        Map<Long, Book> bookCache = new HashMap<>();

        downloadRepo.findAll().forEach(d -> {
            if (d.getBook() == null) return;
            activityCount.merge(d.getBook().getBookId(), 1L, Long::sum);
            bookCache.putIfAbsent(d.getBook().getBookId(), d.getBook());
        });
        reservationRepo.findAll().forEach(r -> {
            if (r.getBook() == null) return;
            activityCount.merge(r.getBook().getBookId(), 1L, Long::sum);
            bookCache.putIfAbsent(r.getBook().getBookId(), r.getBook());
        });

        return activityCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> {
                    Book b = bookCache.get(e.getKey());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bookId",  b.getBookId());
                    m.put("title",   b.getTitle());
                    m.put("cover",   b.getCoverImageURL());
                    m.put("category", b.getCategory() != null ? b.getCategory().getName() : null);
                    m.put("activityCount", e.getValue());
                    return m;
                })
                .toList();
    }

    /* ============ TOP CATEGORIES ============ */
    private List<Map<String, Object>> buildTopCategories() {
        Map<String, long[]> map = new LinkedHashMap<>(); // name → [bookCount, activityCount]

        bookRepo.findAll().forEach(b -> {
            if (b.getCategory() == null) return;
            map.computeIfAbsent(b.getCategory().getName(), k -> new long[]{0, 0})[0]++;
        });
        downloadRepo.findAll().forEach(d -> {
            if (d.getBook() == null || d.getBook().getCategory() == null) return;
            map.computeIfAbsent(d.getBook().getCategory().getName(), k -> new long[]{0, 0})[1]++;
        });
        reservationRepo.findAll().forEach(r -> {
            if (r.getBook() == null || r.getBook().getCategory() == null) return;
            map.computeIfAbsent(r.getBook().getCategory().getName(), k -> new long[]{0, 0})[1]++;
        });

        return map.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[1], a.getValue()[1]))
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("bookCount", e.getValue()[0]);
                    m.put("activityCount", e.getValue()[1]);
                    return m;
                })
                .toList();
    }

    /* ============ TOP USERS ============ */
    private List<Map<String, Object>> buildTopUsers(int limit) {
        Map<Long, Long> activity = new HashMap<>();
        Map<Long, User> userCache = new HashMap<>();

        downloadRepo.findAll().forEach(d -> {
            if (d.getUser() == null) return;
            activity.merge(d.getUser().getUserId(), 1L, Long::sum);
            userCache.putIfAbsent(d.getUser().getUserId(), d.getUser());
        });
        reservationRepo.findAll().forEach(r -> {
            if (r.getUser() == null) return;
            activity.merge(r.getUser().getUserId(), 1L, Long::sum);
            userCache.putIfAbsent(r.getUser().getUserId(), r.getUser());
        });
        reviewRepo.findAll().forEach(rv -> {
            if (rv.getUser() == null) return;
            activity.merge(rv.getUser().getUserId(), 1L, Long::sum);
            userCache.putIfAbsent(rv.getUser().getUserId(), rv.getUser());
        });

        return activity.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> {
                    User u = userCache.get(e.getKey());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId",   u.getUserId());
                    m.put("fullName", u.getFullName());
                    m.put("email",    u.getEmail());
                    m.put("activityCount", e.getValue());
                    return m;
                })
                .toList();
    }

    /* ============ ACTIVITY FEED ============ */
    private List<Map<String, Object>> buildActivityFeed(int limit) {
        List<Map<String, Object>> events = new ArrayList<>();

        reservationRepo.findAll().forEach(r -> {
            if (r.getReservationDate() == null) return;
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "RESERVATION");
            e.put("when", r.getReservationDate().toString());
            e.put("user", r.getUser() != null ? r.getUser().getFullName() : "?");
            e.put("book", r.getBook() != null ? r.getBook().getTitle() : "?");
            e.put("status", r.getStatus() != null ? r.getStatus().name() : null);
            events.add(e);
        });
        downloadRepo.findAll().forEach(d -> {
            if (d.getDownloadDate() == null) return;
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "DOWNLOAD");
            e.put("when", d.getDownloadDate().toString());
            e.put("user", d.getUser() != null ? d.getUser().getFullName() : "?");
            e.put("book", d.getBook() != null ? d.getBook().getTitle() : "?");
            events.add(e);
        });
        reviewRepo.findAll().forEach(rv -> {
            if (rv.getCreatedAt() == null) return;
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "REVIEW");
            e.put("when", rv.getCreatedAt().toString());
            e.put("user", rv.getUser() != null ? rv.getUser().getFullName() : "?");
            e.put("book", rv.getBook() != null ? rv.getBook().getTitle() : "?");
            e.put("rating", rv.getRating());
            events.add(e);
        });
        userRepo.findAll().forEach(u -> {
            if (u.getCreatedAt() == null) return;
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "REGISTER");
            e.put("when", u.getCreatedAt().toString());
            e.put("user", u.getFullName());
            events.add(e);
        });

        events.sort((a, b) -> String.valueOf(b.get("when")).compareTo(String.valueOf(a.get("when"))));
        return events.stream().limit(limit).toList();
    }

    /* ============ ALERTS ============ */
    private List<Map<String, Object>> buildAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        // Cărți cu rezervări multe în așteptare
        Map<Long, Long> waitingPerBook = new HashMap<>();
        Map<Long, Book> bookCache = new HashMap<>();
        reservationRepo.findAll().forEach(r -> {
            if (r.getStatus() != ReservationStatus.WAITING) return;
            if (r.getBook() == null) return;
            waitingPerBook.merge(r.getBook().getBookId(), 1L, Long::sum);
            bookCache.putIfAbsent(r.getBook().getBookId(), r.getBook());
        });
        waitingPerBook.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .forEach(e -> {
                    Book b = bookCache.get(e.getKey());
                    Map<String, Object> a = new LinkedHashMap<>();
                    a.put("type", "HIGH_DEMAND");
                    a.put("severity", "warning");
                    a.put("message", e.getValue() + " utilizatori așteaptă: " + b.getTitle());
                    a.put("bookId", b.getBookId());
                    alerts.add(a);
                });

        // Amenzi PENDING totale
        long pendingFines = fineRepo.findAll().stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING).count();

        if (pendingFines >= 5) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("type", "FINES_PENDING");
            a.put("severity", "danger");
            a.put("message", pendingFines + " amenzi neplătite");
            alerts.add(a);
        }

        // Cercuri PENDING
        long pendingGroups = groupRepo.findAll().stream()
                .filter(g -> "PENDING".equalsIgnoreCase(g.getStatus())).count();
        if (pendingGroups > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("type", "GROUPS_PENDING");
            a.put("severity", "info");
            a.put("message", pendingGroups + " cercuri așteaptă aprobare");
            alerts.add(a);
        }

        return alerts;
    }

    /* ============ RESERVATION STATUS BREAKDOWN ============ */
    private List<Map<String, Object>> buildReservationStatusBreakdown() {
        Map<String, Long> count = new LinkedHashMap<>();
        for (ReservationStatus s : ReservationStatus.values()) count.put(s.name(), 0L);
        reservationRepo.findAll().forEach(r -> {
            if (r.getStatus() != null) count.merge(r.getStatus().name(), 1L, Long::sum);
        });
        return count.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("status", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .toList();
    }

    /* ============ CHALLENGES STATS ============ */
    private Map<String, Object> buildChallengesStats() {
        long total = challengeRepo.count();
        long active = challengeRepo.findByActiveTrueOrderByCreatedAtDesc().size();
        long totalParticipations = participationRepo.count();
        long completed = participationRepo.findAll().stream()
                .filter(p -> p.getStatus() == ParticipationStatus.COMPLETED).count();

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", total);
        r.put("active", active);
        r.put("totalParticipations", totalParticipations);
        r.put("completed", completed);
        return r;
    }

    /* ============ HELPERS ============ */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(RO) + s.substring(1);
    }
}