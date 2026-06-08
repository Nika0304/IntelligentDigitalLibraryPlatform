package com.library.config;

import com.library.model.*;
import com.library.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(
            BookRepository bookRepo,
            AuthorRepository authorRepo,
            CategoryRepository categoryRepo,
            BookCopyRepository copyRepo,
            UserRepository userRepo,
            RoleRepository roleRepo,
            FaqEntryRepository faqRepo,
            BookGroupRepository groupRepo,
            GroupMembershipRepository membershipRepo,
            ReviewRepository reviewRepo,
            WishlistRepository wishlistRepo,
            ReservationRepository reservationRepo,
            DownloadHistoryRepository downloadRepo,
            ChallengeRepository challengeRepo,
            ChallengeParticipationRepository participationRepo,
            PasswordEncoder pwd
    ) {
        return args -> {

            // ============ 1. USERI ============
            Role userRole  = roleRepo.findByRoleName(RoleType.USER).orElseThrow();
            Role adminRole = roleRepo.findByRoleName(RoleType.ADMIN).orElseThrow();

            ensureUser(userRepo, pwd, "Administrator Bibliotecă", "admin@bibliotheca.ro", "admin123", adminRole);
            ensureUser(userRepo, pwd, "Ana Marinescu",             "ana@upt.ro",            "parola123", userRole);
            ensureUser(userRepo, pwd, "Mihai Iordache",            "mihai@upt.ro",          "parola123", userRole);
            ensureUser(userRepo, pwd, "Tudor Popescu",             "tudor@upt.ro",          "parola123", userRole);
            ensureUser(userRepo, pwd, "Elena Vasilescu",           "elena@upt.ro",          "parola123", userRole);
            ensureUser(userRepo, pwd, "Radu Ionescu",              "radu@upt.ro",           "parola123", userRole);
            ensureUser(userRepo, pwd, "Bianca Stoica",             "bianca@upt.ro",         "parola123", userRole);

            // ============ 2. FAQ ============
            if (faqRepo.count() == 0) {
                faqRepo.save(new FaqEntry("Cum rezerv o carte?", "Deschide pagina cărții, apasă „Rezervă” și ai 2 zile la dispoziție pentru ridicare.", "rezerv,rezervare,cum rezerv"));
                faqRepo.save(new FaqEntry("Cât timp pot împrumuta o carte?", "Perioada standard de împrumut este de 14 zile, cu posibilitatea unei prelungiri.", "imprumut,cat timp,perioada"));
                faqRepo.save(new FaqEntry("Cum descarc un PDF?", "Pe pagina cărții, dacă există versiune digitală, vei vedea butonul „Descarcă PDF”.", "pdf,descarc,download,digital"));
                faqRepo.save(new FaqEntry("Ce fac dacă nu mai sunt exemplare?", "Te înscrii automat pe lista de așteptare și primești notificare + email când se eliberează o copie.", "lista asteptare,nu mai sunt,epuizat"));
                faqRepo.save(new FaqEntry("Pot anula o rezervare?", "Da, oricând înainte de ridicare. Mergi în Profil → Rezervări și apasă Anulează.", "anulare,renunta,cancel"));
                faqRepo.save(new FaqEntry("Ce sunt provocările?", "Sunt obiective lunare sau sezoniere create de administrator. Te înscrii și sistemul îți urmărește automat progresul.", "provocari,challenge,gamification"));
            }

            // ============ 3. CĂRȚI (DOAR dacă DB-ul e gol) ============
            if (bookRepo.count() > 0) {
                seedChallengesIfEmpty(challengeRepo, categoryRepo, authorRepo);
                return;
            }

            // Categorii (10)
            Category lit       = categoryRepo.save(new Category("Literatură"));
            Category fil       = categoryRepo.save(new Category("Filosofie"));
            Category sti       = categoryRepo.save(new Category("Știință"));
            Category ist       = categoryRepo.save(new Category("Istorie"));
            Category poe       = categoryRepo.save(new Category("Poezie"));
            Category teh       = categoryRepo.save(new Category("Tehnologie"));
            Category sf        = categoryRepo.save(new Category("Science Fiction"));
            Category mister    = categoryRepo.save(new Category("Mister"));
            Category psiho     = categoryRepo.save(new Category("Psihologie"));
            Category copii     = categoryRepo.save(new Category("Cărți pentru copii"));

            // Autori (15)
            Author rebreanu   = authorRepo.save(new Author("Liviu Rebreanu"));
            Author preda      = authorRepo.save(new Author("Marin Preda"));
            Author aristotel  = authorRepo.save(new Author("Aristotel"));
            Author hawking    = authorRepo.save(new Author("Stephen Hawking"));
            Author eliade     = authorRepo.save(new Author("Mircea Eliade"));
            Author harari     = authorRepo.save(new Author("Yuval Noah Harari"));
            Author knuth      = authorRepo.save(new Author("Donald E. Knuth"));
            Author iorga      = authorRepo.save(new Author("Nicolae Iorga"));
            Author eminescu   = authorRepo.save(new Author("Mihai Eminescu"));
            Author cioran     = authorRepo.save(new Author("Emil Cioran"));
            Author orwell     = authorRepo.save(new Author("George Orwell"));
            Author asimov     = authorRepo.save(new Author("Isaac Asimov"));
            Author tolkien    = authorRepo.save(new Author("J.R.R. Tolkien"));
            Author christie   = authorRepo.save(new Author("Agatha Christie"));
            Author kahneman   = authorRepo.save(new Author("Daniel Kahneman"));

            // ===== CĂRȚI (30+) =====
            List<Book> all = new ArrayList<>();
            String c1 = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=800&q=80";
            String c2 = "https://images.unsplash.com/photo-1535905557558-afc4877a26fc?auto=format&fit=crop&w=800&q=80";
            String c3 = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=800&q=80";
            String c4 = "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=800&q=80";
            String c5 = "https://images.unsplash.com/photo-1495640388908-05fa85288e61?auto=format&fit=crop&w=800&q=80";
            String c6 = "https://images.unsplash.com/photo-1491841550275-ad7854e35ca6?auto=format&fit=crop&w=800&q=80";
            String c7 = "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=800&q=80";
            String c8 = "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=800&q=80";
            String c9 = "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=800&q=80";
            String c10 = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80";

            // Literatură (5)
            all.add(createBook(bookRepo, copyRepo, "Pădurea Spânzuraților", "Roman remarcabil despre dilema morală a unui ofițer în timpul Primului Război Mondial.", 1922, true, true, "/digital/1.pdf", c1, lit, List.of(rebreanu), 3));
            all.add(createBook(bookRepo, copyRepo, "Moromeții", "Cronica vieții țărănești din Câmpia Dunării. O frescă socială completă.", 1955, true, true, "/digital/2.pdf", c2, lit, List.of(preda), 4));
            all.add(createBook(bookRepo, copyRepo, "Maitreyi", "Romanul iubirii imposibile din Calcutta. O poveste autobiografică tulburătoare.", 1933, true, true, "/digital/3.pdf", c5, lit, List.of(eliade), 3));
            all.add(createBook(bookRepo, copyRepo, "Cel mai iubit dintre pământeni", "Ultimul roman al lui Marin Preda, considerat capodopera sa.", 1980, true, true, "/digital/4.pdf", c2, lit, List.of(preda), 2));
            all.add(createBook(bookRepo, copyRepo, "Ion", "Marele roman al țăranului român, capodopera lui Rebreanu.", 1920, true, true, "/digital/5.pdf", c1, lit, List.of(rebreanu), 3));

            // Filosofie (3)
            all.add(createBook(bookRepo, copyRepo, "Etica Nicomahică", "Tratat fundamental al eticii aristotelice. Despre virtute și fericire.", -300, true, true, "/digital/6.pdf", c3, fil, List.of(aristotel), 2));
            all.add(createBook(bookRepo, copyRepo, "Pe culmile disperării", "Prima carte a lui Cioran, manifest al neliniștii existențiale.", 1934, true, true, "/digital/7.pdf", c9, fil, List.of(cioran), 3));
            all.add(createBook(bookRepo, copyRepo, "Schimbarea la față a României", "Eseu despre destinul cultural românesc.", 1936, true, true, "/digital/8.pdf", c9, fil, List.of(cioran), 2));

            // Știință (3)
            all.add(createBook(bookRepo, copyRepo, "O scurtă istorie a timpului", "Călătorie prin cosmologia modernă explicată pe înțelesul tuturor.", 1988, true, true, "/digital/9.pdf", c4, sti, List.of(hawking), 5));
            all.add(createBook(bookRepo, copyRepo, "Universul într-o coajă de nucă", "Continuarea lui Hawking despre teoria M și multivers.", 2001, true, true, "/digital/10.pdf", c4, sti, List.of(hawking), 3));
            all.add(createBook(bookRepo, copyRepo, "Marele plan", "Hawking explorează cele mai mari întrebări ale universului.", 2010, false, true, "/digital/11.pdf", c4, sti, List.of(hawking), 0));

            // Istorie (3)
            all.add(createBook(bookRepo, copyRepo, "Sapiens", "Scurtă istorie a omenirii. Cum am ajuns de la maimuțe la stăpânii planetei.", 2011, true, true, "/digital/12.pdf", c6, ist, List.of(harari), 4));
            all.add(createBook(bookRepo, copyRepo, "Homo Deus", "Continuarea lui Sapiens. Viitorul speciei umane.", 2016, true, true, "/digital/13.pdf", c6, ist, List.of(harari), 3));
            all.add(createBook(bookRepo, copyRepo, "Istoria Românilor", "Sinteză monumentală a istoriei poporului român.", 1936, true, false, null, c1, ist, List.of(iorga), 2));

            // Poezie (2)
            all.add(createBook(bookRepo, copyRepo, "Luceafărul", "Poemul-univers al literaturii române. Capodopera lui Eminescu.", 1883, true, true, "/digital/14.pdf", c8, poe, List.of(eminescu), 4));
            all.add(createBook(bookRepo, copyRepo, "Poezii", "Antologie completă a poemelor eminesciene.", 1884, true, true, "/digital/15.pdf", c8, poe, List.of(eminescu), 3));

            // Tehnologie (2)
            all.add(createBook(bookRepo, copyRepo, "Algoritmi și structuri de date", "Bibliografie esențială pentru programatori.", 1997, true, true, "/digital/16.pdf", c7, teh, List.of(knuth), 4));
            all.add(createBook(bookRepo, copyRepo, "The Art of Computer Programming Vol.1", "Lucrarea fundamentală a lui Knuth despre algoritmi.", 1968, true, true, "/digital/17.pdf", c7, teh, List.of(knuth), 2));

            // Science Fiction (4)
            all.add(createBook(bookRepo, copyRepo, "1984", "Distopia clasică a unui regim totalitar. Big Brother te urmărește.", 1949, true, true, "/digital/18.pdf", c10, sf, List.of(orwell), 5));
            all.add(createBook(bookRepo, copyRepo, "Ferma animalelor", "Alegorie politică despre revoluție și putere.", 1945, true, true, "/digital/19.pdf", c10, sf, List.of(orwell), 4));
            all.add(createBook(bookRepo, copyRepo, "Fundația", "Saga galactică despre prăbușirea unui imperiu și nașterea altuia.", 1951, true, true, "/digital/20.pdf", c4, sf, List.of(asimov), 3));
            all.add(createBook(bookRepo, copyRepo, "Eu, robotul", "Colecție de povestiri despre legile roboticii.", 1950, true, true, "/digital/21.pdf", c4, sf, List.of(asimov), 4));

            // Fantasy în Literatură (3)
            all.add(createBook(bookRepo, copyRepo, "Hobbitul", "Aventura lui Bilbo Baggins în Pământul de Mijloc.", 1937, true, true, "/digital/22.pdf", c5, lit, List.of(tolkien), 4));
            all.add(createBook(bookRepo, copyRepo, "Stăpânul Inelelor: Frăția Inelului", "Începutul epopeii moderne fantasy.", 1954, true, true, "/digital/23.pdf", c5, lit, List.of(tolkien), 3));
            all.add(createBook(bookRepo, copyRepo, "Stăpânul Inelelor: Cele Două Turnuri", "Continuarea epopeii. Războiul pentru Pământul de Mijloc.", 1954, true, true, "/digital/24.pdf", c5, lit, List.of(tolkien), 3));

            // Mister (3)
            all.add(createBook(bookRepo, copyRepo, "Crima din Orient Express", "Hercule Poirot anchetează o crimă într-un tren oprit în zăpadă.", 1934, true, true, "/digital/25.pdf", c3, mister, List.of(christie), 3));
            all.add(createBook(bookRepo, copyRepo, "Și nu a mai rămas niciunul", "Zece străini pe o insulă. Pe rând, mor toți.", 1939, true, true, "/digital/26.pdf", c3, mister, List.of(christie), 4));
            all.add(createBook(bookRepo, copyRepo, "Moartea pe Nil", "Poirot pe un croazieră ce devine scena unei crime.", 1937, true, true, "/digital/27.pdf", c3, mister, List.of(christie), 2));

            // Psihologie (2)
            all.add(createBook(bookRepo, copyRepo, "Thinking, Fast and Slow", "Cele două sisteme de gândire ale minții umane.", 2011, true, true, "/digital/28.pdf", c6, psiho, List.of(kahneman), 3));
            all.add(createBook(bookRepo, copyRepo, "Noise", "Despre erorile sistematice de judecată.", 2021, false, true, "/digital/29.pdf", c6, psiho, List.of(kahneman), 0));

            // Copii (1)
            all.add(createBook(bookRepo, copyRepo, "Aventurile lui Bilbo - ediție ilustrată", "Versiunea pentru copii a lui Hobbitul.", 2012, true, false, null, c5, copii, List.of(tolkien), 5));

            // ============ 4. CERCURI ============
            User ana   = userRepo.findByEmail("ana@upt.ro").orElseThrow();
            User mihai = userRepo.findByEmail("mihai@upt.ro").orElseThrow();
            User tudor = userRepo.findByEmail("tudor@upt.ro").orElseThrow();
            User elena = userRepo.findByEmail("elena@upt.ro").orElseThrow();
            User radu  = userRepo.findByEmail("radu@upt.ro").orElseThrow();
            User bianca = userRepo.findByEmail("bianca@upt.ro").orElseThrow();

            if (groupRepo.count() == 0) {
                BookGroup g1 = new BookGroup("Cercul Cioran", "Filosofie",
                        "Întâlniri săptămânale despre nihilism, tristețe și frumusețe la Cioran.", ana);
                g1.setStatus("APPROVED");
                g1.setDecidedAt(LocalDateTime.now());
                BookGroup s1 = groupRepo.save(g1);
                membershipRepo.save(new GroupMembership(s1, ana, "MODERATOR"));
                membershipRepo.save(new GroupMembership(s1, mihai, "MEMBER"));
                membershipRepo.save(new GroupMembership(s1, elena, "MEMBER"));

                BookGroup g2 = new BookGroup("Sci-fi & viitorul", "Știință",
                        "Discutăm cum literatura SF anticipează prezentul.", mihai);
                g2.setStatus("APPROVED");
                g2.setDecidedAt(LocalDateTime.now());
                BookGroup s2 = groupRepo.save(g2);
                membershipRepo.save(new GroupMembership(s2, mihai, "MODERATOR"));
                membershipRepo.save(new GroupMembership(s2, radu, "MEMBER"));
                membershipRepo.save(new GroupMembership(s2, tudor, "MEMBER"));

                BookGroup g3 = new BookGroup("Mister la cafea", "Mister",
                        "Cazurile lui Hercule Poirot și Miss Marple, citite împreună.", elena);
                groupRepo.save(g3); // PENDING — pentru ca adminul să-l aprobe în demo
            }

            // ============ 5. ACTIVITATE UTILIZATORI ============

            // ANA — cititoare voraceă de literatură + filosofie (rating-uri mari)
            addReview(reviewRepo, ana, all.get(0), 5, "Capodoperă a literaturii române.");
            addReview(reviewRepo, ana, all.get(1), 5, "Frescă socială impresionantă.");
            addReview(reviewRepo, ana, all.get(2), 4, "Frumos, dar uneori prea liric.");
            addReview(reviewRepo, ana, all.get(6), 5, "Cioran e medicament pentru sufletele neliniștite.");
            addReview(reviewRepo, ana, all.get(13), 5, "Luceafărul e univers în versuri.");
            addWishlist(wishlistRepo, ana, all.get(7));
            addWishlist(wishlistRepo, ana, all.get(4));
            addReservation(reservationRepo, ana, all.get(3), ReservationStatus.BORROWED, -10);
            addReservation(reservationRepo, ana, all.get(0), ReservationStatus.RETURNED, -45);
            addDownload(downloadRepo, ana, all.get(1), -30);
            addDownload(downloadRepo, ana, all.get(13), -25);
            addDownload(downloadRepo, ana, all.get(6), -20);
            addDownload(downloadRepo, ana, all.get(13), -5);

            // MIHAI — fan SF + știință
            addReview(reviewRepo, mihai, all.get(17), 5, "1984 e mai actual ca niciodată.");
            addReview(reviewRepo, mihai, all.get(8), 5, "Hawking explicat impecabil.");
            addReview(reviewRepo, mihai, all.get(11), 4, "Sapiens m-a făcut să gândesc diferit.");
            addReview(reviewRepo, mihai, all.get(19), 4, "Asimov rămâne maestrul SF-ului.");
            addReview(reviewRepo, mihai, all.get(18), 5, "Alegorie politică perfectă.");
            addWishlist(wishlistRepo, mihai, all.get(9));
            addWishlist(wishlistRepo, mihai, all.get(12));
            addReservation(reservationRepo, mihai, all.get(20), ReservationStatus.CONFIRMED, -3);
            addReservation(reservationRepo, mihai, all.get(8), ReservationStatus.RETURNED, -60);
            addDownload(downloadRepo, mihai, all.get(17), -40);
            addDownload(downloadRepo, mihai, all.get(11), -35);
            addDownload(downloadRepo, mihai, all.get(8), -20);
            addDownload(downloadRepo, mihai, all.get(19), -10);
            addDownload(downloadRepo, mihai, all.get(20), -5);

            // TUDOR — fan mister + classics
            addReview(reviewRepo, tudor, all.get(24), 5, "Crima clasică, finalul surprinde.");
            addReview(reviewRepo, tudor, all.get(25), 5, "Cea mai bună carte a lui Christie.");
            addReview(reviewRepo, tudor, all.get(26), 4, "Atmosferă orientală fantastică.");
            addReview(reviewRepo, tudor, all.get(21), 4, "Hobbitul, eternul favorit.");
            addWishlist(wishlistRepo, tudor, all.get(22));
            addWishlist(wishlistRepo, tudor, all.get(23));
            addReservation(reservationRepo, tudor, all.get(24), ReservationStatus.RETURNED, -20);
            addReservation(reservationRepo, tudor, all.get(22), ReservationStatus.WAITING, -1);
            addDownload(downloadRepo, tudor, all.get(24), -15);
            addDownload(downloadRepo, tudor, all.get(25), -12);
            addDownload(downloadRepo, tudor, all.get(21), -3);

            // ELENA — psihologie + filosofie
            addReview(reviewRepo, elena, all.get(27), 5, "Kahneman e revelație totală.");
            addReview(reviewRepo, elena, all.get(6), 4, "Cioran greu de digerat dar profund.");
            addWishlist(wishlistRepo, elena, all.get(28));
            addDownload(downloadRepo, elena, all.get(27), -25);
            addDownload(downloadRepo, elena, all.get(28), -10);

            // RADU — utilizator casual, doar câteva acțiuni
            addReview(reviewRepo, radu, all.get(11), 3, "OK dar așteptam mai mult.");
            addWishlist(wishlistRepo, radu, all.get(17));
            addDownload(downloadRepo, radu, all.get(11), -50);

            // BIANCA — cititor nou (foarte puține interacțiuni — pentru cold start în recomandări)
            addWishlist(wishlistRepo, bianca, all.get(13));

            // ============ 6. PROVOCĂRI ============
            seedChallengesIfEmpty(challengeRepo, categoryRepo, authorRepo);

            // Înscrie utilizatorii la unele provocări
            for (Challenge ch : challengeRepo.findAll()) {
                if (ch.getType() == ChallengeType.ANY_READ) {
                    joinChallenge(participationRepo, ana, ch, -30);
                    joinChallenge(participationRepo, mihai, ch, -25);
                    joinChallenge(participationRepo, tudor, ch, -10);
                } else if (ch.getType() == ChallengeType.READ_FROM_CATEGORY) {
                    joinChallenge(participationRepo, ana, ch, -20);
                } else if (ch.getType() == ChallengeType.WRITE_REVIEWS) {
                    joinChallenge(participationRepo, ana, ch, -15);
                    joinChallenge(participationRepo, tudor, ch, -10);
                } else if (ch.getType() == ChallengeType.READ_DIGITAL) {
                    joinChallenge(participationRepo, mihai, ch, -20);
                    joinChallenge(participationRepo, elena, ch, -5);
                }
            }

            System.out.println("✅ DataSeeder complet: " + bookRepo.count() + " cărți, " +
                    userRepo.count() + " utilizatori, " + reviewRepo.count() + " recenzii, " +
                    reservationRepo.count() + " rezervări, " + downloadRepo.count() + " descărcări, " +
                    challengeRepo.count() + " provocări.");
        };
    }

    /* ============ HELPERS ============ */

    private void ensureUser(UserRepository repo, PasswordEncoder pwd,
                            String fullName, String email, String pass, Role role) {
        if (!repo.existsByEmail(email)) {
            repo.save(new User(fullName, email, pwd.encode(pass), role));
        }
    }

    private Book createBook(BookRepository br, BookCopyRepository bcr,
                            String title, String desc, int year,
                            boolean physical, boolean digital, String digitalPath, String cover,
                            Category cat, List<Author> authors, int copies) {
        Book b = new Book();
        b.setTitle(title);
        b.setDescription(desc);
        b.setPublicationYear(year);
        b.setHasPhysicalCopy(physical);
        b.setHasDigitalCopy(digital);
        b.setDigitalFilePath(digitalPath);
        b.setCoverImageURL(cover);
        b.setCategory(cat);
        b.setAuthors(authors);

        Book saved = br.save(b);
        if (physical) {
            for (int i = 1; i <= copies; i++) {
                bcr.save(new BookCopy(String.format("INV-%03d-%02d", saved.getBookId(), i), saved));
            }
        }
        return saved;
    }

    private void addReview(ReviewRepository repo, User user, Book book, int rating, String comment) {
        Review r = new Review(rating, comment, user, book);
        repo.save(r);
    }

    private void addWishlist(WishlistRepository repo, User user, Book book) {
        repo.save(new Wishlist(user, book));
    }

    private void addReservation(ReservationRepository repo, User user, Book book,
                                ReservationStatus status, int daysAgo) {
        Reservation r = new Reservation(user, book);
        LocalDateTime when = LocalDateTime.now().plusDays(daysAgo);
        r.setReservationDate(when);
        r.setExpirationDate(when.plusDays(14));
        r.setStatus(status);
        repo.save(r);
    }

    private void addDownload(DownloadHistoryRepository repo, User user, Book book, int daysAgo) {
        DownloadHistory d = new DownloadHistory(user, book);
        d.setDownloadDate(LocalDateTime.now().plusDays(daysAgo));
        repo.save(d);
    }

    private void joinChallenge(ChallengeParticipationRepository repo, User user,
                               Challenge challenge, int daysAgo) {
        if (repo.existsByUserAndChallenge(user, challenge)) return;
        ChallengeParticipation p = new ChallengeParticipation();
        p.setUser(user);
        p.setChallenge(challenge);
        p.setStatus(ParticipationStatus.IN_PROGRESS);
        p.setJoinedAt(LocalDateTime.now().plusDays(daysAgo));
        repo.save(p);
    }

    private void seedChallengesIfEmpty(ChallengeRepository repo,
                                       CategoryRepository catRepo,
                                       AuthorRepository authRepo) {
        if (repo.count() > 0) return;

        LocalDateTime start = LocalDateTime.now().minusMonths(2);
        LocalDateTime end   = LocalDateTime.now().plusMonths(3);

        // 1. ANY_READ — accesibilă tuturor
        Challenge c1 = new Challenge();
        c1.setTitle("Lectură de iarnă");
        c1.setDescription("Citește 5 cărți de orice fel în această iarnă. Începem!");
        c1.setType(ChallengeType.ANY_READ);
        c1.setTargetCount(5);
        c1.setStartDate(start);
        c1.setEndDate(end);
        c1.setIconEmoji("❄️");
        c1.setActive(true);
        repo.save(c1);

        // 2. READ_DIGITAL
        Challenge c2 = new Challenge();
        c2.setTitle("Digital First");
        c2.setDescription("Descarcă și citește 3 cărți digitale.");
        c2.setType(ChallengeType.READ_DIGITAL);
        c2.setTargetCount(3);
        c2.setStartDate(start);
        c2.setEndDate(end);
        c2.setIconEmoji("📱");
        c2.setActive(true);
        repo.save(c2);

        // 3. WRITE_REVIEWS
        Challenge c3 = new Challenge();
        c3.setTitle("Criticul Săptămânii");
        c3.setDescription("Scrie 3 recenzii pentru cărțile pe care le-ai citit.");
        c3.setType(ChallengeType.WRITE_REVIEWS);
        c3.setTargetCount(3);
        c3.setStartDate(start);
        c3.setEndDate(end);
        c3.setIconEmoji("✍️");
        c3.setActive(true);
        repo.save(c3);

        // 4. READ_FROM_CATEGORY — Literatură
        Category lit = catRepo.findAll().stream()
                .filter(c -> "Literatură".equals(c.getName()))
                .findFirst().orElse(null);
        if (lit != null) {
            Challenge c4 = new Challenge();
            c4.setTitle("Maratonul Literaturii Române");
            c4.setDescription("Citește 2 cărți din categoria Literatură.");
            c4.setType(ChallengeType.READ_FROM_CATEGORY);
            c4.setTargetCount(2);
            c4.setCategoryId(lit.getCategoryId());
            c4.setStartDate(start);
            c4.setEndDate(end);
            c4.setIconEmoji("📚");
            c4.setActive(true);
            repo.save(c4);
        }

        // 5. READ_FROM_AUTHOR — Eminescu
        Author eminescu = authRepo.findAll().stream()
                .filter(a -> "Mihai Eminescu".equals(a.getName()))
                .findFirst().orElse(null);
        if (eminescu != null) {
            Challenge c5 = new Challenge();
            c5.setTitle("Tot Eminescu");
            c5.setDescription("Explorează 2 opere de Mihai Eminescu.");
            c5.setType(ChallengeType.READ_FROM_AUTHOR);
            c5.setTargetCount(2);
            c5.setAuthorId(eminescu.getAuthorId());
            c5.setStartDate(start);
            c5.setEndDate(end);
            c5.setIconEmoji("⭐");
            c5.setActive(true);
            repo.save(c5);
        }

        // 6. READ_PHYSICAL
        Challenge c6 = new Challenge();
        c6.setTitle("Mirosul de hârtie");
        c6.setDescription("Rezervă 2 cărți fizice de la bibliotecă.");
        c6.setType(ChallengeType.READ_PHYSICAL);
        c6.setTargetCount(2);
        c6.setStartDate(start);
        c6.setEndDate(end);
        c6.setIconEmoji("📖");
        c6.setActive(true);
        repo.save(c6);
    }
}