package com.library.config;

import com.library.model.*;
import com.library.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder
{
    @Bean
    public CommandLineRunner seedData(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            CategoryRepository categoryRepository,
            BookCopyRepository bookCopyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            FaqEntryRepository faqRepository,
            BookGroupRepository groupRepository,
            GroupMembershipRepository membershipRepository,
            PasswordEncoder passwordEncoder
    )
    {
        return args -> {
            // 1. Utilizatori
            Role userRole = roleRepository.findByRoleName(RoleType.USER).orElseThrow();
            Role adminRole = roleRepository.findByRoleName(RoleType.ADMIN).orElseThrow();

            if (!userRepository.existsByEmail("admin@bibliotheca.ro"))
            {
                userRepository.save(new User(
                        "Administrator Bibliotecă",
                        "admin@bibliotheca.ro",
                        passwordEncoder.encode("admin123"),
                        adminRole
                ));
            }

            if (!userRepository.existsByEmail("ana@upt.ro"))
            {
                userRepository.save(new User(
                        "Ana Marinescu",
                        "ana@upt.ro",
                        passwordEncoder.encode("parola123"),
                        userRole
                ));
            }

            if (!userRepository.existsByEmail("mihai@upt.ro"))
            {
                userRepository.save(new User(
                        "Mihai Iordache",
                        "mihai@upt.ro",
                        passwordEncoder.encode("parola123"),
                        userRole
                ));
            }

            if (!userRepository.existsByEmail("tudor@upt.ro"))
            {
                userRepository.save(new User(
                        "Tudor Popescu",
                        "tudor@upt.ro",
                        passwordEncoder.encode("parola123"),
                        userRole
                ));
            }

            // 2. FAQ — independent de cărți
            if (faqRepository.count() == 0)
            {
                faqRepository.save(new FaqEntry(
                        "Cum rezerv o carte?",
                        "Deschide pagina cărții, apasă „Rezervă” și ai 2 zile la dispoziție pentru ridicare.",
                        "rezerv,rezervare,cum rezerv"));
                faqRepository.save(new FaqEntry(
                        "Cât timp pot împrumuta o carte?",
                        "Perioada standard de împrumut este de 14 zile, cu posibilitatea unei prelungiri.",
                        "imprumut,cat timp,perioada"));
                faqRepository.save(new FaqEntry(
                        "Cum descarc un PDF?",
                        "Pe pagina cărții, dacă există versiune digitală, vei vedea butonul „Descarcă PDF”.",
                        "pdf,descarc,download,digital"));
                faqRepository.save(new FaqEntry(
                        "Ce fac dacă nu mai sunt exemplare?",
                        "Te înscrii automat pe lista de așteptare și primești notificare + email când se eliberează o copie.",
                        "lista asteptare,nu mai sunt,epuizat"));
            }

            if (groupRepository.count() == 0)
            {
                User ana = userRepository.findByEmail("ana@upt.ro").orElseThrow();
                User mihai = userRepository.findByEmail("mihai@upt.ro").orElseThrow();

                BookGroup cerc1 = new BookGroup(
                        "Cercul Cioran",
                        "Filosofie",
                        "Întâlniri săptămânale despre nihilism, tristețe și frumusețe la Cioran. Citim împreună „Pe culmile disperării”.",
                        ana
                );
                cerc1.setStatus("APPROVED");
                cerc1.setDecidedAt(LocalDateTime.now());
                BookGroup saved1 = groupRepository.save(cerc1);
                membershipRepository.save(new GroupMembership(saved1, ana, "MODERATOR"));
                membershipRepository.save(new GroupMembership(saved1, mihai, "MEMBER"));

                BookGroup cerc2 = new BookGroup(
                        "Sci-fi & viitorul",
                        "Știință",
                        "Discutăm cum literatura SF anticipează prezentul. Începem cu Sapiens.",
                        mihai
                );
                groupRepository.save(cerc2); // rămâne PENDING — pentru ca adminul să-l aprobe în demo
            }

            // 3. Cărți — doar dacă DB-ul e gol
            if (bookRepository.count() > 0)
            {
                return;
            }

            // Categorii
            Category lit = categoryRepository.save(new Category("Literatură"));
            Category fil = categoryRepository.save(new Category("Filosofie"));
            Category sti = categoryRepository.save(new Category("Știință"));
            Category ist = categoryRepository.save(new Category("Istoria"));
            Category poe = categoryRepository.save(new Category("Poezie"));
            Category teh = categoryRepository.save(new Category("Tehnologie"));

            // Autori
            Author rebreanu = authorRepository.save(new Author("Liviu Rebreanu"));
            Author preda = authorRepository.save(new Author("Marin Preda"));
            Author aristotel = authorRepository.save(new Author("Aristotel"));
            Author hawking = authorRepository.save(new Author("Stephen Hawking"));
            Author eliade = authorRepository.save(new Author("Mircea Eliade"));
            Author harari = authorRepository.save(new Author("Yuval Noah Harari"));
            Author knuth = authorRepository.save(new Author("Donald E. Knuth"));
            Author iorga = authorRepository.save(new Author("Nicolae Iorga"));
            Author eminescu = authorRepository.save(new Author("Mihai Eminescu"));
            Author cioran = authorRepository.save(new Author("Emil Cioran"));

            // Cărți
            createBook(bookRepository, bookCopyRepository,
                    "Pădurea Spânzuraților", "Roman remarcabil despre dilema morală a unui ofițer.",
                    1922, true, true, "/digital/1.pdf",
                    "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=800&q=80",
                    lit, List.of(rebreanu), 3);

            createBook(bookRepository, bookCopyRepository,
                    "Moromeții", "Cronica vieții țărănești din Câmpia Dunării.",
                    1955, true, true, "/digital/2.pdf",
                    "https://images.unsplash.com/photo-1535905557558-afc4877a26fc?auto=format&fit=crop&w=800&q=80",
                    lit, List.of(preda), 4);

            createBook(bookRepository, bookCopyRepository,
                    "Etica Nicomahică", "Tratat fundamental al eticii aristotelice.",
                    300, true, true, "/digital/3.pdf",
                    "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=800&q=80",
                    fil, List.of(aristotel), 3);

            createBook(bookRepository, bookCopyRepository,
                    "O scurtă istorie a timpului", "Călătorie prin cosmologia modernă.",
                    1988, true, true, "/digital/4.pdf",
                    "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=800&q=80",
                    sti, List.of(hawking), 5);

            createBook(bookRepository, bookCopyRepository,
                    "Maitreyi", "Romanul iubirii imposibile din Calcutta.",
                    1933, true, true, "/digital/5.pdf",
                    "https://images.unsplash.com/photo-1495640388908-05fa85288e61?auto=format&fit=crop&w=800&q=80",
                    lit, List.of(eliade), 3);

            createBook(bookRepository, bookCopyRepository,
                    "Sapiens", "Scurtă istorie a omenirii.",
                    2011, true, true, "/digital/6.pdf",
                    "https://images.unsplash.com/photo-1491841550275-ad7854e35ca6?auto=format&fit=crop&w=800&q=80",
                    ist, List.of(harari), 4);

            createBook(bookRepository, bookCopyRepository,
                    "Algoritmi și structuri de date", "Bibliografie esențială pentru programatori.",
                    1997, true, true, "/digital/7.pdf",
                    "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=800&q=80",
                    teh, List.of(knuth), 4);

            createBook(bookRepository, bookCopyRepository,
                    "Istoria Românilor", "Sinteză a istoriei poporului român.",
                    1936, true, false, null, null,
                    ist, List.of(iorga), 2);

            createBook(bookRepository, bookCopyRepository,
                    "Luceafărul", "Poemul-univers al literaturii române.",
                    1883, true, true, "/digital/9.pdf",
                    "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=800&q=80",
                    poe, List.of(eminescu), 3);

            createBook(bookRepository, bookCopyRepository,
                    "Pe culmile disperării", "Prima carte a lui Cioran, manifest al neliniștii.",
                    1934, true, true, "/digital/10.pdf",
                    "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=800&q=80",
                    fil, List.of(cioran), 3);
        };
    }

    private void createBook(BookRepository br, BookCopyRepository bcr,
                            String title, String desc, int year,
                            boolean physical, boolean digital, String digitalPath, String cover,
                            Category cat, List<Author> authors, int copies)
    {
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

        if (physical)
        {
            for (int i = 1; i <= copies; i++)
            {
                BookCopy c = new BookCopy(
                        String.format("INV-%03d-%02d", saved.getBookId(), i),
                        saved
                );
                bcr.save(c);
            }
        }
    }
}