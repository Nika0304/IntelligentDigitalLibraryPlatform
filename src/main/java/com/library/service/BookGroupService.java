package com.library.service;

import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookGroupService
{
    private final BookGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;
    private final GroupBookVoteRepository voteRepository;

    public BookGroupService(BookGroupRepository groupRepository,
                            GroupMembershipRepository membershipRepository,
                            GroupMessageRepository messageRepository,
                            UserRepository userRepository,
                            BookRepository bookRepository,
                            NotificationService notificationService,
                            GroupBookVoteRepository voteRepository)
    {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.notificationService = notificationService;
        this.voteRepository = voteRepository;
    }

    // ---- Group lifecycle ----
    @Transactional
    public BookGroup proposeGroup(Long userId, String name, String theme, String description)
    {
        User creator = findUser(userId);

        if (name == null || name.isBlank()) throw new RuntimeException("Numele grupului e obligatoriu.");
        if (theme == null || theme.isBlank()) throw new RuntimeException("Tema grupului e obligatorie.");
        if (description == null || description.isBlank()) throw new RuntimeException("Descrierea e obligatorie.");

        BookGroup group = new BookGroup(name.trim(), theme.trim(), description.trim(), creator);
        BookGroup saved = groupRepository.save(group);

        notificationService.notifyAdmins(
                "Grup nou propus de " + creator.getFullName() + ": „" + saved.getName() + "”. Necesită aprobare.",
                NotificationType.SYSTEM
        );

        return saved;
    }

    public List<Map<String, Object>> listApproved()
    {
        return groupRepository.findByStatusOrderByCreatedAtDesc("APPROVED")
                .stream().map(this::toCard).toList();
    }

    public List<Map<String, Object>> listPending()
    {
        return groupRepository.findByStatusOrderByCreatedAtDesc("PENDING")
                .stream().map(this::toCard).toList();
    }

    public Map<String, Object> getDetails(Long groupId, Long viewerId)
    {
        BookGroup g = findGroup(groupId);
        Map<String, Object> data = toCard(g);

        List<Map<String, Object>> members = membershipRepository.findByGroup(g)
                .stream().map(m -> Map.<String, Object>of(
                        "userId", m.getUser().getUserId(),
                        "fullName", m.getUser().getFullName(),
                        "role", m.getRole(),
                        "joinedAt", m.getJoinedAt()
                )).toList();

        data.put("members", members);
        data.put("isMember", viewerId != null && membershipRepository.findByGroupAndUser(g, findUser(viewerId)).isPresent());
        data.put("isModerator", viewerId != null && isModeratorOrCreator(g, findUser(viewerId)));
        data.put("muted", viewerId != null && membershipRepository.findByGroupAndUser(g, findUser(viewerId))
                .map(GroupMembership::isNotificationsMuted).orElse(false));

        return data;
    }

    @Transactional
    public BookGroup approveGroup(Long groupId)
    {
        BookGroup g = findGroup(groupId);
        if (!"PENDING".equals(g.getStatus())) throw new RuntimeException("Grupul nu e în așteptare.");
        g.setStatus("APPROVED");
        g.setDecidedAt(LocalDateTime.now());
        BookGroup saved = groupRepository.save(g);

        // creatorul devine automat moderator
        membershipRepository.save(new GroupMembership(saved, saved.getCreator(), "MODERATOR"));

        notify(saved.getCreator(),
                "Grupul tău „" + saved.getName() + "” a fost aprobat. Ești acum moderator.",
                NotificationType.SYSTEM);

        return saved;
    }

    @Transactional
    public BookGroup rejectGroup(Long groupId)
    {
        BookGroup g = findGroup(groupId);
        if (!"PENDING".equals(g.getStatus())) throw new RuntimeException("Grupul nu e în așteptare.");
        g.setStatus("REJECTED");
        g.setDecidedAt(LocalDateTime.now());
        BookGroup saved = groupRepository.save(g);

        notify(saved.getCreator(),
                "Propunerea ta de grup „" + saved.getName() + "” a fost respinsă.",
                NotificationType.SYSTEM);

        return saved;
    }

    @Transactional
    public void archiveGroup(Long groupId)
    {
        BookGroup g = findGroup(groupId);
        g.setStatus("ARCHIVED");
        groupRepository.save(g);
    }

    // ---- Membership ----

    @Transactional
    public void joinGroup(Long groupId, Long userId)
    {
        BookGroup g = findGroup(groupId);
        User u = findUser(userId);

        if (!"APPROVED".equals(g.getStatus())) throw new RuntimeException("Acest grup nu este disponibil.");
        if (membershipRepository.findByGroupAndUser(g, u).isPresent())
            throw new RuntimeException("Faci deja parte din acest grup.");

        membershipRepository.save(new GroupMembership(g, u, "MEMBER"));
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId)
    {
        BookGroup g = findGroup(groupId);
        User u = findUser(userId);

        GroupMembership m = membershipRepository.findByGroupAndUser(g, u)
                .orElseThrow(() -> new RuntimeException("Nu faci parte din acest grup."));

        if ("MODERATOR".equals(m.getRole()) && g.getCreator().getUserId().equals(u.getUserId()))
            throw new RuntimeException("Creatorul grupului nu poate părăsi grupul. Cere arhivare la admin.");

        membershipRepository.delete(m);
    }

    public List<Map<String, Object>> myGroups(Long userId)
    {
        User u = findUser(userId);
        return membershipRepository.findByUserOrderByJoinedAtDesc(u)
                .stream()
                .filter(m -> "APPROVED".equals(m.getGroup().getStatus()))
                .map(m -> {
                    Map<String, Object> card = toCard(m.getGroup());
                    card.put("myRole", m.getRole());
                    return card;
                }).toList();
    }

    // ---- Messages ----

    @Transactional
    public GroupMessage postMessage(Long groupId, Long userId, String content)
    {
        BookGroup g = findGroup(groupId);
        User u = findUser(userId);

        if (!"APPROVED".equals(g.getStatus())) throw new RuntimeException("Grupul nu este activ.");
        if (membershipRepository.findByGroupAndUser(g, u).isEmpty())
            throw new RuntimeException("Trebuie să te alături grupului pentru a posta.");
        if (content == null || content.isBlank())
            throw new RuntimeException("Mesajul nu poate fi gol.");

        GroupMessage saved = messageRepository.save(new GroupMessage(g, u, content.trim()));

        // notifică ceilalți membri non-muted
        String preview = content.trim().length() > 80
                ? content.trim().substring(0, 80) + "..."
                : content.trim();

        for (GroupMembership m : membershipRepository.findByGroup(g)) {
            if (m.getUser().getUserId().equals(u.getUserId())) continue; // nu te notifică pe tine
            if (m.isNotificationsMuted()) continue;
            notify(m.getUser(),
                    "Mesaj nou în „" + g.getName() + "” de la " + u.getFullName() + ": " + preview,
                    NotificationType.GENERAL);
        }

        return saved;
    }

    @Transactional
    public boolean toggleMute(Long groupId, Long userId)
    {
        BookGroup g = findGroup(groupId);
        User u = findUser(userId);
        GroupMembership m = membershipRepository.findByGroupAndUser(g, u)
                .orElseThrow(() -> new RuntimeException("Nu faci parte din acest grup."));
        m.setNotificationsMuted(!m.isNotificationsMuted());
        membershipRepository.save(m);
        return m.isNotificationsMuted();
    }

    public List<GroupMessage> listMessages(Long groupId, Long viewerId, boolean isAdmin)
    {
        BookGroup g = findGroup(groupId);

        if (viewerId == null)
            throw new RuntimeException("Trebuie să fii autentificat pentru a vedea conversația.");

        User viewer = findUser(viewerId);
        boolean isMember = membershipRepository.findByGroupAndUser(g, viewer).isPresent();

        if (!isMember && !isAdmin)
            throw new RuntimeException("Conversația acestui cerc este vizibilă doar membrilor.");

        return messageRepository.findByGroupOrderByCreatedAtAsc(g);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long actorUserId, boolean isAdmin)
    {
        GroupMessage m = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mesaj negăsit."));

        User actor = findUser(actorUserId);
        boolean isAuthor = m.getUser().getUserId().equals(actorUserId);
        boolean isMod = isModeratorOrCreator(m.getGroup(), actor);

        if (!isAuthor && !isMod && !isAdmin)
            throw new RuntimeException("Nu ai dreptul să ștergi acest mesaj.");

        m.setDeleted(true);
        m.setContent("[mesaj șters]");
        messageRepository.save(m);
    }

    // ---- Featured book ----

    @Transactional
    public BookGroup setFeaturedBook(Long groupId, Long actorUserId, Long bookId)
    {
        BookGroup g = findGroup(groupId);
        User actor = findUser(actorUserId);

        if (!isModeratorOrCreator(g, actor))
            throw new RuntimeException("Doar moderatorul poate seta cartea cercului.");

        if (bookId == null) {
            g.setFeaturedBook(null);
        } else {
            Book b = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Carte negăsită."));
            g.setFeaturedBook(b);
        }

        return groupRepository.save(g);
    }

    // ---- Helpers ----

    private Map<String, Object> toCard(BookGroup g)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", g.getGroupId());
        map.put("name", g.getName());
        map.put("theme", g.getTheme());
        map.put("description", g.getDescription());
        map.put("status", g.getStatus());
        map.put("createdAt", g.getCreatedAt());
        map.put("memberCount", membershipRepository.countByGroup(g));
        map.put("messageCount", messageRepository.countByGroupAndDeletedFalse(g));
        map.put("creator", Map.of(
                "userId", g.getCreator().getUserId(),
                "fullName", g.getCreator().getFullName()
        ));
        if (g.getFeaturedBook() != null) {
            map.put("featuredBook", Map.of(
                    "bookId", g.getFeaturedBook().getBookId(),
                    "title", g.getFeaturedBook().getTitle(),
                    "coverImageURL", g.getFeaturedBook().getCoverImageURL() == null ? "" : g.getFeaturedBook().getCoverImageURL()
            ));
        }
        return map;
    }

    private boolean isModeratorOrCreator(BookGroup g, User u)
    {
        if (g.getCreator().getUserId().equals(u.getUserId())) return true;
        return membershipRepository.findByGroupAndUser(g, u)
                .map(m -> "MODERATOR".equals(m.getRole()))
                .orElse(false);
    }

    private BookGroup findGroup(Long id)
    {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grup negăsit."));
    }

    private User findUser(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit."));
    }

    private void notify(User u, String msg, NotificationType type)
    {
        try { notificationService.createAutomaticNotification(u, msg, type); } catch (Exception ignored) {}
    }


    public List<Map<String, Object>> topActive(int limit)
    {
        return groupRepository.findByStatusOrderByCreatedAtDesc("APPROVED")
                .stream()
                .map(this::toCard)
                .sorted((a, b) -> Long.compare(
                        (long) b.get("messageCount") + (long) b.get("memberCount"),
                        (long) a.get("messageCount") + (long) a.get("memberCount")
                ))
                .limit(limit)
                .toList();
    }
    @Transactional
    public Map<String, Object> castVote(Long groupId, Long userId, Long bookId)
    {
        BookGroup g = findGroup(groupId);
        User u = findUser(userId);

        if (membershipRepository.findByGroupAndUser(g, u).isEmpty())
            throw new RuntimeException("Doar membrii pot vota.");

        Book b = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Carte negăsită."));

        String period = java.time.YearMonth.now().toString();

        GroupBookVote existing = voteRepository.findByGroupAndUserAndPeriod(g, u, period).orElse(null);
        if (existing == null) {
            voteRepository.save(new GroupBookVote(g, u, b));
        } else {
            existing.setBook(b);
            voteRepository.save(existing);
        }

        return getVoteTally(groupId, userId);
    }

    public Map<String, Object> getVoteTally(Long groupId, Long viewerId)
    {
        BookGroup g = findGroup(groupId);
        String period = java.time.YearMonth.now().toString();

        List<GroupBookVote> votes = voteRepository.findByGroupAndPeriod(g, period);

        Map<Long, Long> tally = new HashMap<>();
        Map<Long, Book> bookCache = new HashMap<>();
        for (GroupBookVote v : votes) {
            tally.merge(v.getBook().getBookId(), 1L, Long::sum);
            bookCache.putIfAbsent(v.getBook().getBookId(), v.getBook());
        }

        List<Map<String, Object>> ranking = tally.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Book b = bookCache.get(e.getKey());
                    return Map.<String, Object>of(
                            "bookId", b.getBookId(),
                            "title", b.getTitle(),
                            "coverImageURL", b.getCoverImageURL() == null ? "" : b.getCoverImageURL(),
                            "votes", e.getValue()
                    );
                }).toList();

        Long myVote = null;
        if (viewerId != null) {
            myVote = voteRepository.findByGroupAndUserAndPeriod(g, findUser(viewerId), period)
                    .map(v -> v.getBook().getBookId()).orElse(null);
        }

        return Map.of(
                "period", period,
                "ranking", ranking,
                "totalVotes", votes.size(),
                "myVote", myVote == null ? "" : myVote
        );
    }

    @Transactional
    public BookGroup applyVoteWinner(Long groupId, Long actorUserId)
    {
        BookGroup g = findGroup(groupId);
        User actor = findUser(actorUserId);
        if (!isModeratorOrCreator(g, actor))
            throw new RuntimeException("Doar moderatorul poate aplica rezultatul votului.");

        Map<String, Object> tally = getVoteTally(groupId, null);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) tally.get("ranking");
        if (ranking.isEmpty()) throw new RuntimeException("Nu există voturi încă.");

        Long winnerId = (Long) ranking.get(0).get("bookId");
        return setFeaturedBook(groupId, actorUserId, winnerId);
    }
}