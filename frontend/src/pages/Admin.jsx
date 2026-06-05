import React, { useEffect, useState } from "react";
import { Navigate, useSearchParams } from "react-router-dom";
import {
    fetchBooks,
    fetchUsers,
    fetchAllReservations,
    fetchAuthors,
    fetchCategories,
    fetchStats,
    createBook,
    deleteBook,
    createAuthor,
    deleteAuthor,
    createCategory,
    deleteCategory,
    createBookCopy,
    updateUserStatus,
    markReadyForPickup,
    borrowBook,
    returnBook,
    cancelReservation,
    searchGoogleBooks,
    fetchAllFines,
    payFine,
    cancelFine,
    chatPending,
    chatAnswer,
    chatFaq,
    chatCreateFaq,
    chatDeleteFaq,
    fetchPendingGroups,
    approveGroup,
    rejectGroup,
    archiveGroup,
    fetchGroups as fetchAllGroups,
    downloadReport,
} from "../lib/api";
import { useAuth } from "../context/AuthContext";
import {
    Plus,
    Trash2,
    BookOpen,
    Users,
    BookMarked,
    Tag,
    BarChart3,
    Loader2,
    Search,
    Download,
    AlertTriangle,
    Sparkles,
    Check,
    MessageCircle,
    Users2,
    FileText
} from "lucide-react";

const TABS = [
    { id: "stats", label: "Sumar", icon: BarChart3 },
    { id: "books", label: "Cărți", icon: BookOpen },
    { id: "import", label: "Import Google Books", icon: Sparkles },
    { id: "users", label: "Utilizatori", icon: Users },
    { id: "reservations", label: "Rezervări", icon: BookMarked },
    { id: "fines", label: "Penalități", icon: AlertTriangle },
    { id: "taxonomy", label: "Autori & Categorii", icon: Tag },
    { id: "chat", label: "Chat & FAQ", icon: MessageCircle },
    { id: "groups", label: "Cercuri", icon: Users2 },
    { id: "reports", label: "Rapoarte", icon: FileText },
];

export default function Admin() {
    const { user, isAdmin } = useAuth();
    const [params, setParams] = useSearchParams();
    const active = params.get("tab") || "stats";

    if (!user) return <Navigate to="/autentificare" replace />;
    if (!isAdmin) return <Navigate to="/profil" replace />;

    return (
        <main
            className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24"
            data-testid="admin-page"
        >
            <div className="text-xs uppercase tracking-widest opacity-60">
                Panou Administrativ
            </div>

            <h1 className="font-serif text-5xl lg:text-6xl mt-3">
                Gestiune <span className="italic-soft">Bibliotheca</span>.
            </h1>

            <p className="mt-3 opacity-70 max-w-2xl">
                Inventar, utilizatori, rezervări, penalități și taxonomie. Toate într-un loc.
            </p>

            <div className="mt-10 flex flex-wrap gap-2" data-testid="admin-tabs">
                {TABS.map((t) => {
                    const Icon = t.icon;

                    return (
                        <button
                            key={t.id}
                            onClick={() => setParams({ tab: t.id }, { replace: true })}
                            className={`tab-link ${active === t.id ? "active" : ""} !inline-flex items-center gap-2`}
                            data-testid={`admin-tab-${t.id}`}
                        >
                            <Icon size={14} /> {t.label}
                        </button>
                    );
                })}
            </div>

            <div className="mt-8">
                {active === "stats" && <Stats />}
                {active === "books" && <BooksAdmin />}
                {active === "import" && <GoogleBooksImport />}
                {active === "users" && <UsersAdmin />}
                {active === "reservations" && <ReservationsAdmin />}
                {active === "fines" && <FinesAdmin />}
                {active === "taxonomy" && <Taxonomy />}
                {active === "chat" && <ChatAdmin />}
                {active === "groups" && <GroupsAdmin />}
                {active === "reports" && <ReportsAdmin />}
            </div>

        </main>
    );
}

function normalizeText(value) {
    return String(value || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}

function getExternalCategoryText(book) {
    const categories = Array.isArray(book.categories) ? book.categories : [];

    return normalizeText([
        book.title,
        book.description,
        ...categories,
    ].join(" "));
}

function findCategoryByNames(existingCategories, possibleNames) {
    const normalizedNames = possibleNames.map(normalizeText);

    return existingCategories.find((category) =>
        normalizedNames.includes(normalizeText(category.name))
    );
}

function suggestCategoryForBook(book, existingCategories) {
    const text = getExternalCategoryText(book);

    const rules = [
        {
            categoryNames: ["Fantasy", "Fantezie"],
            keywords: [
                "fantasy",
                "magic",
                "wizard",
                "wizards",
                "witch",
                "dragon",
                "hogwarts",
                "juvenile fiction",
                "fairy tales",
            ],
        },
        {
            categoryNames: ["Literatură", "Literatura", "Roman"],
            keywords: [
                "fiction",
                "literature",
                "novel",
                "roman",
                "classic",
                "classics",
                "drama",
                "stories",
                "short stories",
            ],
        },
        {
            categoryNames: ["Filosofie", "Philosophy"],
            keywords: [
                "philosophy",
                "ethics",
                "metaphysics",
                "logic",
                "stoicism",
                "existentialism",
                "aristotle",
                "plato",
                "nietzsche",
                "cioran",
            ],
        },
        {
            categoryNames: ["Știință", "Stiinta", "Science"],
            keywords: [
                "science",
                "physics",
                "cosmology",
                "biology",
                "chemistry",
                "astronomy",
                "mathematics",
                "nature",
            ],
        },
        {
            categoryNames: ["Istorie", "Istoria", "History"],
            keywords: [
                "history",
                "historical",
                "war",
                "europe",
                "romania",
                "ancient",
                "civilization",
                "biography",
            ],
        },
        {
            categoryNames: ["Poezie", "Poetry"],
            keywords: [
                "poetry",
                "poems",
                "poet",
                "verse",
                "eminescu",
            ],
        },
        {
            categoryNames: ["Tehnologie", "Technology", "Programare", "Informatica"],
            keywords: [
                "technology",
                "computer",
                "programming",
                "software",
                "algorithms",
                "data structures",
                "artificial intelligence",
                "machine learning",
            ],
        },
        {
            categoryNames: ["Dezvoltare personală", "Self Development", "Self Help"],
            keywords: [
                "self-help",
                "self help",
                "personal development",
                "habits",
                "motivation",
                "productivity",
                "success",
                "psychology",
            ],
        },
    ];

    for (const rule of rules) {
        const matchedKeyword = rule.keywords.some((keyword) =>
            text.includes(normalizeText(keyword))
        );

        if (matchedKeyword) {
            const category = findCategoryByNames(existingCategories, rule.categoryNames);

            if (category) {
                return category;
            }
        }
    }

    return null;
}
// ===== Google Books / OpenLibrary Import =====

function GoogleBooksImport() {
    const [q, setQ] = useState("");
    const [results, setResults] = useState([]);
    const [busy, setBusy] = useState(false);
    const [err, setErr] = useState(null);

    const [cats, setCats] = useState([]);
    const [authors, setAuthors] = useState([]);

    const [imported, setImported] = useState({});
    const [showImport, setShowImport] = useState(null);

    const [importForm, setImportForm] = useState({
        categoryId: "",
        physical: true,
        digital: false,
        copies: 2,
    });

    const loadImportData = async () => {
        try {
            const [categoriesData, authorsData] = await Promise.all([
                fetchCategories(),
                fetchAuthors(),
            ]);

            const safeCategories = Array.isArray(categoriesData) ? categoriesData : [];
            const safeAuthors = Array.isArray(authorsData) ? authorsData : [];

            setCats(safeCategories);
            setAuthors(safeAuthors);

            return {
                categories: safeCategories,
                authors: safeAuthors,
            };
        } catch (e) {
            setCats([]);
            setAuthors([]);

            return {
                categories: [],
                authors: [],
            };
        }
    };

    useEffect(() => {
        loadImportData();
    }, []);

    const search = async (e) => {
        e?.preventDefault();

        if (!q.trim()) return;

        setBusy(true);
        setErr(null);

        try {
            const items = await searchGoogleBooks(q.trim());
            setResults(items);

            if (items.length === 0) {
                setErr("Niciun rezultat găsit.");
            }
        } catch (e) {
            setErr("Căutarea externă nu este disponibilă momentan.");
        } finally {
            setBusy(false);
        }
    };

    const startImport = async (book) => {
        setBusy(true);

        try {
            const { categories } = await loadImportData();
            const suggestedCategory = suggestCategoryForBook(book, categories);

            setShowImport({
                ...book,
                suggestedCategoryName: suggestedCategory?.name || null,
            });

            setImportForm({
                categoryId: suggestedCategory?.categoryId || "",
                physical: true,
                digital: false,
                copies: 2,
            });
        } finally {
            setBusy(false);
        }
    };

    const confirmImport = async () => {
        const book = showImport;

        if (!book) return;

        if (!importForm.categoryId) {
            alert("Alege o categorie.");
            return;
        }

        if (!importForm.physical && !importForm.digital) {
            alert("Cartea trebuie să fie fizică, digitală sau ambele.");
            return;
        }

        setBusy(true);

        try {
            const authorIds = [];
            const externalAuthors =
                Array.isArray(book.authors) && book.authors.length > 0
                    ? book.authors
                    : ["Necunoscut"];

            for (const name of externalAuthors) {
                let author = authors.find(
                    (a) => a.name?.toLowerCase() === name.toLowerCase()
                );

                if (!author) {
                    author = await createAuthor(name);
                    setAuthors((prev) => [...prev, author]);
                }

                authorIds.push(author.authorId);
            }

            const cleanDescription = (book.description || "")
                .replace(/<[^>]+>/g, "")
                .slice(0, 1900);

            const created = await createBook({
                title: book.title,
                description: cleanDescription,
                publicationYear: book.publicationYear || null,
                hasPhysicalCopy: importForm.physical,
                hasDigitalCopy: importForm.digital,
                digitalFilePath: importForm.digital
                    ? `/digital/external-${book.googleId || book.externalId}.pdf`
                    : null,
                coverImageURL: book.coverImageURL || "",
                categoryId: Number(importForm.categoryId),
                authorIds,
            });

            if (importForm.physical && Number(importForm.copies) > 0) {
                for (let i = 1; i <= Number(importForm.copies); i++) {
                    await createBookCopy(
                        `EXT-${created.bookId}-${i.toString().padStart(2, "0")}`,
                        created.bookId
                    );
                }
            }

            const importedKey = book.googleId || book.externalId;

            setImported((prev) => ({
                ...prev,
                [importedKey]: true,
            }));

            setShowImport(null);
            alert("Cartea a fost importată cu succes.");
        } catch (e) {
            alert(e.response?.data || e.response?.data?.detail || "Eroare la import.");
        } finally {
            setBusy(false);
        }
    };

    return (
        <div data-testid="google-books-import">
            <div className="paper p-6 mb-6">
                <h3 className="font-serif text-2xl">
                    Importă cărți direct din baze de date externe
                </h3>

                <p className="mt-2 text-sm opacity-70">
                    Caută orice carte. Backend-ul încearcă Google Books, iar dacă nu merge,
                    folosește automat OpenLibrary. Titlul, autorii, anul, descrierea și coperta
                    sunt completate automat.
                </p>

                <form onSubmit={search} className="mt-5 flex gap-3">
                    <div
                        className="flex items-center gap-2 paper px-4 flex-1"
                        style={{
                            background: "var(--cream-2)",
                            border: "1px solid var(--line)",
                            borderRadius: "999px",
                        }}
                    >
                        <Search size={16} className="opacity-60" />

                        <input
                            data-testid="gb-search-input"
                            value={q}
                            onChange={(e) => setQ(e.target.value)}
                            placeholder="ex: Dostoievski, Sapiens, Eminescu..."
                            className="bg-transparent outline-none py-3 text-sm w-full"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={busy}
                        className="btn btn-primary"
                        data-testid="gb-search-button"
                    >
                        {busy ? <Loader2 size={14} className="animate-spin" /> : <Search size={14} />}
                        Caută
                    </button>
                </form>

                {err && <div className="mt-3 text-sm opacity-70">{err}</div>}
            </div>

            {results.length > 0 && (
                <div
                    className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4"
                    data-testid="gb-results"
                >
                    {results.map((book) => {
                        const bookKey = book.googleId || book.externalId;

                        return (
                            <div key={bookKey} className="paper paper-hover p-4 flex gap-4">
                                <div
                                    className="w-20 h-28 rounded overflow-hidden flex-shrink-0"
                                    style={{ background: "var(--cream-2)" }}
                                >
                                    {book.coverImageURL ? (
                                        <img
                                            src={book.coverImageURL}
                                            alt={book.title}
                                            className="w-full h-full object-cover"
                                        />
                                    ) : (
                                        <div className="w-full h-full grid place-items-center opacity-30">
                                            <BookOpen size={20} />
                                        </div>
                                    )}
                                </div>

                                <div className="flex-1 min-w-0">
                                    <div className="font-serif text-base leading-snug">
                                        {book.title}
                                    </div>

                                    <div className="text-xs opacity-60 mt-1 italic-soft font-serif">
                                        {book.authors?.join(", ") || "Autor necunoscut"}
                                    </div>

                                    <div className="text-xs opacity-50 mt-1">
                                        {book.publicationYear || "—"} ·{" "}
                                        {book.pageCount ? `${book.pageCount}p` : ""} ·{" "}
                                        {book.language?.toUpperCase()}
                                        {book.source === "openlibrary" && (
                                            <span className="ml-2 opacity-60">via OpenLibrary</span>
                                        )}
                                    </div>

                                    {imported[bookKey] ? (
                                        <span className="chip chip-digital mt-3 inline-flex items-center gap-1">
                      <Check size={10} /> Importat
                    </span>
                                    ) : (
                                        <button
                                            onClick={() => startImport(book)}
                                            disabled={busy}
                                            className="btn btn-primary !text-xs !py-1.5 !px-3 mt-3"
                                            data-testid={`gb-import-${bookKey}`}
                                        >
                                            {busy ? <Loader2 size={12} className="animate-spin" /> : <Download size={12} />}
                                            Importă
                                        </button>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {showImport && (
                <div
                    className="fixed inset-0 z-50 grid place-items-center p-4"
                    style={{ background: "rgba(26,26,26,0.5)" }}
                >
                    <div className="paper p-6 w-full max-w-md" data-testid="gb-import-modal">
                        <h3 className="font-serif text-2xl">
                            Importă „{showImport.title}”
                        </h3>

                        <div className="mt-2 text-sm opacity-70">
                            {showImport.authors?.join(", ") || "Autor necunoscut"}
                        </div>

                        {showImport?.suggestedCategoryName && (
                            <div className="mt-3 text-sm">
                                <span className="chip chip-digital">
                                    Categorie sugerată: {showImport.suggestedCategoryName}
                                </span>
                            </div>
                        )}

                        <div className="mt-5 space-y-4">
                            <div>
                                <label className="text-xs uppercase tracking-widest opacity-60">
                                    Categorie
                                </label>

                                <select
                                    className="input-cream mt-2"
                                    value={importForm.categoryId}
                                    onChange={(e) =>
                                        setImportForm({ ...importForm, categoryId: e.target.value })
                                    }
                                    data-testid="gb-import-category"
                                >
                                    <option value="">
                                        {cats.length === 0 ? "Nu există categorii încă" : "Alege categorie..."}
                                    </option>
                                    {cats.map((c) => (
                                        <option key={c.categoryId} value={c.categoryId}>
                                            {c.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="flex items-center gap-4 text-sm">
                                <label className="flex items-center gap-2">
                                    <input
                                        type="checkbox"
                                        checked={importForm.physical}
                                        onChange={(e) =>
                                            setImportForm({
                                                ...importForm,
                                                physical: e.target.checked,
                                            })
                                        }
                                    />
                                    Fizic
                                </label>

                                <label className="flex items-center gap-2">
                                    <input
                                        type="checkbox"
                                        checked={importForm.digital}
                                        onChange={(e) =>
                                            setImportForm({
                                                ...importForm,
                                                digital: e.target.checked,
                                            })
                                        }
                                    />
                                    Digital
                                </label>
                            </div>

                            {importForm.physical && (
                                <div>
                                    <label className="text-xs uppercase tracking-widest opacity-60">
                                        Număr exemplare
                                    </label>

                                    <input
                                        type="number"
                                        min="1"
                                        max="20"
                                        className="input-cream mt-2"
                                        value={importForm.copies}
                                        onChange={(e) =>
                                            setImportForm({
                                                ...importForm,
                                                copies: parseInt(e.target.value) || 1,
                                            })
                                        }
                                        data-testid="gb-import-copies"
                                    />
                                </div>
                            )}
                        </div>

                        <div className="mt-6 flex gap-2">
                            <button
                                onClick={() => setShowImport(null)}
                                className="btn btn-secondary flex-1 justify-center"
                            >
                                Anulează
                            </button>

                            <button
                                onClick={confirmImport}
                                disabled={busy}
                                className="btn btn-primary flex-1 justify-center"
                                data-testid="gb-import-confirm"
                            >
                                {busy ? (
                                    <Loader2 size={14} className="animate-spin" />
                                ) : (
                                    <Download size={14} />
                                )}
                                Importă
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

// ===== Fines Admin =====

function FinesAdmin() {
    const [items, setItems] = useState(null);

    const load = () =>
        fetchAllFines()
            .then(setItems)
            .catch(() => setItems([]));

    useEffect(() => {
        load();
    }, []);

    const act = async (fn, fineId, confirmMsg) => {
        if (confirmMsg && !window.confirm(confirmMsg)) return;

        try {
            await fn(fineId);
            load();
        } catch (e) {
            alert(e.response?.data || e.response?.data?.detail || "Eroare");
        }
    };

    if (!items) return <Loader />;

    const pending = items.filter((f) => f.status === "PENDING");
    const totalPending = pending.reduce(
        (sum, f) => sum + Number(f.amount || 0),
        0
    );

    return (
        <div data-testid="admin-fines-list">
            <div className="grid sm:grid-cols-3 gap-4 mb-6">
                <div className="paper p-5">
                    <div className="text-xs uppercase tracking-widest opacity-60">
                        Total penalizări
                    </div>
                    <div className="font-serif text-4xl mt-2">{items.length}</div>
                </div>

                <div className="paper p-5">
                    <div className="text-xs uppercase tracking-widest opacity-60">
                        În așteptare
                    </div>
                    <div className="font-serif text-4xl mt-2">{pending.length}</div>
                </div>

                <div className="paper p-5" style={{ background: "var(--butter)" }}>
                    <div className="text-xs uppercase tracking-widest opacity-70">
                        Sumă neîncasată
                    </div>
                    <div className="font-serif text-4xl mt-2">
                        {totalPending.toFixed(2)}{" "}
                        <span className="text-base opacity-60">RON</span>
                    </div>
                </div>
            </div>

            <div className="grid gap-3">
                {items.map((fine) => (
                    <div
                        key={fine.fineId}
                        className="paper p-4 flex flex-col md:flex-row gap-3 items-stretch md:items-center"
                        data-testid={`admin-fine-${fine.fineId}`}
                    >
                        <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2">
                                <FineStatusChipAdmin status={fine.status} />
                                <span className="text-xs opacity-60">#{fine.fineId}</span>
                            </div>

                            <div className="font-serif mt-1">
                                {fine.user?.fullName || fine.user?.email || "Utilizator necunoscut"}
                            </div>

                            <div className="text-xs opacity-60 truncate">
                                {fine.reason || "Penalizare întârziere"}
                            </div>

                            <div className="text-xs opacity-50 mt-1">
                                {fine.overdueDays || 0} zile · emisă{" "}
                                {fine.createdAt
                                    ? new Date(fine.createdAt).toLocaleDateString("ro-RO")
                                    : "—"}
                            </div>
                        </div>

                        <div className="font-serif text-2xl">
                            {Number(fine.amount || 0).toFixed(2)}{" "}
                            <span className="text-xs opacity-60">RON</span>
                        </div>

                        {fine.status === "PENDING" && (
                            <>
                                <button
                                    onClick={() => act(payFine, fine.fineId)}
                                    className="btn btn-secondary !text-xs"
                                >
                                    Marchează plătită
                                </button>

                                <button
                                    onClick={() =>
                                        act(cancelFine, fine.fineId, "Anulezi această penalizare?")
                                    }
                                    className="btn btn-ghost !text-xs"
                                >
                                    Anulează
                                </button>
                            </>
                        )}
                    </div>
                ))}

                {items.length === 0 && (
                    <div className="paper p-8 text-center opacity-60">
                        Nicio penalizare emisă.
                    </div>
                )}
            </div>
        </div>
    );
}

function FineStatusChipAdmin({ status }) {
    const map = {
        PENDING: ["chip-rose", "În așteptare"],
        PAID: ["chip-digital", "Plătită"],
        CANCELLED: ["chip-physical", "Anulată"],
    };

    const [cls, label] = map[status] || ["chip-physical", status];

    return <span className={`chip ${cls}`}>{label}</span>;
}

// ===== Stats =====

function Stats() {
    const [stats, setStats] = useState(null);

    useEffect(() => {
        fetchStats()
            .then(setStats)
            .catch(() => setStats({}));
    }, []);

    if (!stats) return <Loader />;

    const cards = [
        ["Cărți", stats.totalBooks],
        ["Utilizatori", stats.totalUsers],
        ["Exemplare fizice", stats.totalCopies],
        ["Disponibile acum", stats.availableCopies],
        ["Rezervări active", stats.activeReservations],
        ["Total rezervări", stats.totalReservations],
        ["Cărți digitale", stats.digitalBooks],
    ];

    return (
        <div
            className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5"
            data-testid="admin-stats"
        >
            {cards.map(([label, value]) => (
                <div key={label} className="paper p-6">
                    <div className="text-xs uppercase tracking-widest opacity-60">
                        {label}
                    </div>
                    <div className="font-serif text-5xl mt-3">{value ?? 0}</div>
                </div>
            ))}
        </div>
    );
}

// ===== Books Admin =====

function BooksAdmin() {
    const [books, setBooks] = useState(null);
    const [cats, setCats] = useState([]);
    const [authors, setAuthors] = useState([]);

    const [form, setForm] = useState({
        title: "",
        description: "",
        publicationYear: "",
        categoryId: "",
        authorIds: [],
        hasPhysicalCopy: true,
        hasDigitalCopy: false,
        digitalFilePath: "",
        coverImageURL: "",
    });

    const [showCopy, setShowCopy] = useState(null);
    const [copyForm, setCopyForm] = useState({ inventoryCode: "" });

    const load = () =>
        fetchBooks()
            .then(setBooks)
            .catch(() => setBooks([]));

    useEffect(() => {
        load();
        fetchCategories().then(setCats).catch(() => setCats([]));
        fetchAuthors().then(setAuthors).catch(() => setAuthors([]));
    }, []);

    const submit = async (e) => {
        e.preventDefault();

        try {
            await createBook({
                ...form,
                publicationYear: Number(form.publicationYear) || null,
                categoryId: Number(form.categoryId) || null,
                authorIds: form.authorIds.map(Number),
                digitalFilePath: form.hasDigitalCopy ? form.digitalFilePath : null,
            });

            setForm({
                title: "",
                description: "",
                publicationYear: "",
                categoryId: "",
                authorIds: [],
                hasPhysicalCopy: true,
                hasDigitalCopy: false,
                digitalFilePath: "",
                coverImageURL: "",
            });

            load();
        } catch (e) {
            alert(e.response?.data || e.response?.data?.detail || "Eroare");
        }
    };

    const addCopy = async (bookId) => {
        try {
            await createBookCopy(copyForm.inventoryCode, bookId);

            setCopyForm({ inventoryCode: "" });
            setShowCopy(null);
            load();
        } catch (e) {
            alert(e.response?.data || e.response?.data?.detail || "Eroare");
        }
    };

    if (!books) return <Loader />;

    return (
        <div className="grid lg:grid-cols-3 gap-6">
            <form
                onSubmit={submit}
                className="paper p-6 space-y-3 lg:col-span-1 h-fit sticky top-6"
                data-testid="admin-book-form"
            >
                <h3 className="font-serif text-2xl mb-2">Carte nouă</h3>

                <input
                    required
                    className="input-cream"
                    placeholder="Titlu"
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                    data-testid="admin-book-title"
                />

                <textarea
                    rows={3}
                    className="input-cream"
                    placeholder="Descriere"
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                />

                <div className="grid grid-cols-2 gap-3">
                    <input
                        type="number"
                        className="input-cream"
                        placeholder="An"
                        value={form.publicationYear}
                        onChange={(e) =>
                            setForm({ ...form, publicationYear: e.target.value })
                        }
                    />

                    <select
                        className="input-cream"
                        value={form.categoryId}
                        onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                    >
                        <option value="">Categorie</option>
                        {cats.map((cat) => (
                            <option key={cat.categoryId} value={cat.categoryId}>
                                {cat.name}
                            </option>
                        ))}
                    </select>
                </div>

                <select
                    multiple
                    value={form.authorIds}
                    onChange={(e) =>
                        setForm({
                            ...form,
                            authorIds: Array.from(e.target.selectedOptions).map(
                                (option) => option.value
                            ),
                        })
                    }
                    className="input-cream h-28"
                >
                    {authors.map((author) => (
                        <option key={author.authorId} value={author.authorId}>
                            {author.name}
                        </option>
                    ))}
                </select>

                <input
                    className="input-cream"
                    placeholder="URL copertă"
                    value={form.coverImageURL}
                    onChange={(e) =>
                        setForm({ ...form, coverImageURL: e.target.value })
                    }
                />

                {form.hasDigitalCopy && (
                    <input
                        className="input-cream"
                        placeholder="Cale fișier digital"
                        value={form.digitalFilePath}
                        onChange={(e) =>
                            setForm({ ...form, digitalFilePath: e.target.value })
                        }
                    />
                )}

                <div className="flex items-center gap-4 text-sm">
                    <label className="flex items-center gap-2">
                        <input
                            type="checkbox"
                            checked={form.hasPhysicalCopy}
                            onChange={(e) =>
                                setForm({ ...form, hasPhysicalCopy: e.target.checked })
                            }
                        />
                        Fizic
                    </label>

                    <label className="flex items-center gap-2">
                        <input
                            type="checkbox"
                            checked={form.hasDigitalCopy}
                            onChange={(e) =>
                                setForm({ ...form, hasDigitalCopy: e.target.checked })
                            }
                        />
                        Digital
                    </label>
                </div>

                <button
                    className="btn btn-primary w-full justify-center"
                    type="submit"
                    data-testid="admin-create-book"
                >
                    <Plus size={14} /> Adaugă
                </button>
            </form>

            <div className="lg:col-span-2 grid gap-3" data-testid="admin-books-list">
                {books.map((book) => (
                    <div key={book.bookId} className="paper p-4 flex items-center gap-4 relative">
                        <div
                            className="w-14 h-20 rounded overflow-hidden shrink-0"
                            style={{ background: "var(--cream-2)" }}
                        >
                            {book.coverImageURL && (
                                <img
                                    src={book.coverImageURL}
                                    className="w-full h-full object-cover"
                                    alt={book.title}
                                />
                            )}
                        </div>

                        <div className="flex-1 min-w-0">
                            <div className="font-serif text-lg truncate">{book.title}</div>
                            <div className="text-xs opacity-60">
                                {book.authors?.join(", ")} · {book.publicationYear || "—"}
                            </div>
                            <div className="text-xs opacity-60 mt-1">
                                {book.totalCopies} exemplare · {book.availableCopies} libere
                            </div>
                        </div>

                        <button
                            onClick={() =>
                                setShowCopy(showCopy === book.bookId ? null : book.bookId)
                            }
                            className="btn btn-ghost !text-xs"
                            data-testid={`add-copy-${book.bookId}`}
                        >
                            <Plus size={12} /> Exemplar
                        </button>

                        <button
                            onClick={async () => {
                                if (!window.confirm(`Ștergi "${book.title}"?`)) return;

                                try {
                                    await deleteBook(book.bookId);
                                    load();
                                } catch (e) {
                                    alert(e.response?.data || e.response?.data?.detail || "Nu am putut șterge cartea.");
                                }
                            }}
                            className="btn btn-ghost !text-xs"
                            data-testid={`delete-book-${book.bookId}`}
                        >
                            <Trash2 size={12} />
                        </button>

                        {showCopy === book.bookId && (
                            <div className="absolute right-0 top-16 paper p-3 z-20 flex gap-2 items-center">
                                <input
                                    className="input-cream !py-2 !text-sm"
                                    placeholder="Cod inventar"
                                    value={copyForm.inventoryCode}
                                    onChange={(e) =>
                                        setCopyForm({ inventoryCode: e.target.value })
                                    }
                                />

                                <button
                                    onClick={() => addCopy(book.bookId)}
                                    className="btn btn-primary !py-2 !text-xs"
                                >
                                    OK
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

// ===== Users Admin =====

function UsersAdmin() {
    const [users, setUsers] = useState(null);

    const load = () =>
        fetchUsers()
            .then(setUsers)
            .catch(() => setUsers([]));

    useEffect(() => {
        load();
    }, []);

    if (!users) return <Loader />;

    return (
        <div className="grid gap-3" data-testid="admin-users-list">
            {users.map((user) => (
                <div key={user.userId} className="paper p-4 flex items-center gap-4">
                    <div
                        className="w-10 h-10 rounded-full grid place-items-center"
                        style={{
                            background:
                                user.roleName === "ADMIN" ? "var(--ink)" : "var(--rose)",
                            color: user.roleName === "ADMIN" ? "var(--paper)" : "var(--ink)",
                        }}
                    >
                        {user.fullName?.[0] || "U"}
                    </div>

                    <div className="flex-1">
                        <div className="font-medium">{user.fullName}</div>
                        <div className="text-xs opacity-60">{user.email}</div>
                    </div>

                    <span className="chip chip-physical">{user.roleName}</span>

                    <select
                        value={user.status}
                        onChange={async (e) => {
                            await updateUserStatus(user.userId, e.target.value);
                            load();
                        }}
                        className="input-cream !w-auto !py-2 !text-sm"
                        data-testid={`user-status-${user.userId}`}
                    >
                        <option value="ACTIVE">Activ</option>
                        <option value="SUSPENDED">Suspendat</option>
                        <option value="INACTIVE">Inactiv</option>
                    </select>
                </div>
            ))}
        </div>
    );
}

// ===== Reservations Admin =====

function ReservationsAdmin() {
    const [items, setItems] = useState(null);

    const load = () =>
        fetchAllReservations()
            .then(setItems)
            .catch(() => setItems([]));

    useEffect(() => {
        load();
    }, []);

    if (!items) return <Loader />;

    const act = async (fn, reservationId) => {
        try {
            await fn(reservationId);
            load();
        } catch (e) {
            alert(e.response?.data || e.response?.data?.detail || "Eroare");
        }
    };

    return (
        <div className="grid gap-3" data-testid="admin-reservations-list">
            {items.map((reservation) => (
                <div
                    key={reservation.reservationId}
                    className="paper p-4 flex items-center gap-4 flex-wrap"
                >
                    <div className="flex-1 min-w-[200px]">
                        <div className="font-serif">{reservation.book?.title}</div>

                        <div className="text-xs opacity-60">
                            {reservation.user?.fullName} · {reservation.user?.email}
                        </div>

                        <div className="text-xs opacity-60 mt-1">
                            {reservation.reservationDate
                                ? new Date(reservation.reservationDate).toLocaleString("ro-RO")
                                : "—"}
                        </div>
                    </div>

                    <span className="chip chip-physical">{reservation.status}</span>

                    {reservation.status === "CONFIRMED" && (
                        <button
                            onClick={() => act(markReadyForPickup, reservation.reservationId)}
                            className="btn btn-secondary !text-xs"
                        >
                            Gata de ridicat
                        </button>
                    )}

                    {reservation.status === "READY_FOR_PICKUP" && (
                        <button
                            onClick={() => act(borrowBook, reservation.reservationId)}
                            className="btn btn-secondary !text-xs"
                        >
                            Marchează împrumutat
                        </button>
                    )}

                    {reservation.status === "BORROWED" && (
                        <button
                            onClick={() => act(returnBook, reservation.reservationId)}
                            className="btn btn-secondary !text-xs"
                        >
                            Returnat
                        </button>
                    )}

                    {["CONFIRMED", "WAITING", "READY_FOR_PICKUP", "CREATED"].includes(
                        reservation.status
                    ) && (
                        <button
                            onClick={() => act(cancelReservation, reservation.reservationId)}
                            className="btn btn-ghost !text-xs"
                        >
                            Anulează
                        </button>
                    )}
                </div>
            ))}

            {items.length === 0 && (
                <div className="paper p-8 text-center opacity-60">
                    Nicio rezervare încă.
                </div>
            )}
        </div>
    );
}

// ===== Taxonomy =====

function Taxonomy() {
    const [cats, setCats] = useState([]);
    const [authors, setAuthors] = useState([]);

    const [categoryName, setCategoryName] = useState("");
    const [authorName, setAuthorName] = useState("");

    const load = () => {
        fetchCategories().then(setCats).catch(() => setCats([]));
        fetchAuthors().then(setAuthors).catch(() => setAuthors([]));
    };

    useEffect(() => {
        load();
    }, []);

    return (
        <div className="grid md:grid-cols-2 gap-6">
            <div className="paper p-6" data-testid="admin-categories">
                <h3 className="font-serif text-2xl mb-4">Categorii</h3>

                <form
                    className="flex gap-2 mb-4"
                    onSubmit={async (e) => {
                        e.preventDefault();

                        try {
                            await createCategory(categoryName);
                            setCategoryName("");
                            load();
                        } catch (err) {
                            alert(err.response?.data || err.response?.data?.detail || "Eroare");
                        }
                    }}
                >
                    <input
                        className="input-cream"
                        placeholder="Categorie nouă"
                        value={categoryName}
                        onChange={(e) => setCategoryName(e.target.value)}
                        required
                    />

                    <button className="btn btn-primary" type="submit">
                        <Plus size={14} />
                    </button>
                </form>

                <ul className="space-y-2">
                    {cats.map((category) => (
                        <li
                            key={category.categoryId}
                            className="flex items-center justify-between py-2 px-3 rounded-lg"
                            style={{ background: "var(--cream-2)" }}
                        >
                            <span>{category.name}</span>

                            <button
                                onClick={async () => {
                                    if (window.confirm(`Ștergi ${category.name}?`)) {
                                        try {
                                            await deleteCategory(category.categoryId);
                                            load();
                                        } catch (e) {
                                            alert(e.response?.data || "Eroare");
                                        }
                                    }
                                }}
                                className="btn btn-ghost !text-xs !p-1.5"
                            >
                                <Trash2 size={12} />
                            </button>
                        </li>
                    ))}
                </ul>
            </div>

            <div className="paper p-6" data-testid="admin-authors">
                <h3 className="font-serif text-2xl mb-4">Autori</h3>

                <form
                    className="flex gap-2 mb-4"
                    onSubmit={async (e) => {
                        e.preventDefault();

                        try {
                            await createAuthor(authorName);
                            setAuthorName("");
                            load();
                        } catch (err) {
                            alert(err.response?.data || err.response?.data?.detail || "Eroare");
                        }
                    }}
                >
                    <input
                        className="input-cream"
                        placeholder="Autor nou"
                        value={authorName}
                        onChange={(e) => setAuthorName(e.target.value)}
                        required
                    />

                    <button className="btn btn-primary" type="submit">
                        <Plus size={14} />
                    </button>
                </form>

                <ul className="space-y-2 max-h-96 overflow-auto">
                    {authors.map((author) => (
                        <li
                            key={author.authorId}
                            className="flex items-center justify-between py-2 px-3 rounded-lg"
                            style={{ background: "var(--cream-2)" }}
                        >
                            <span>{author.name}</span>

                            <button
                                onClick={async () => {
                                    if (window.confirm(`Ștergi ${author.name}?`)) {
                                        try {
                                            await deleteAuthor(author.authorId);
                                            load();
                                        } catch (e) {
                                            alert(e.response?.data || "Eroare");
                                        }
                                    }
                                }}
                                className="btn btn-ghost !text-xs !p-1.5"
                            >
                                <Trash2 size={12} />
                            </button>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

function GroupsAdmin() {
    const [pending, setPending] = useState(null);
    const [approved, setApproved] = useState(null);

    const load = () => {
        fetchPendingGroups().then(setPending).catch(() => setPending([]));
        fetchAllGroups().then(setApproved).catch(() => setApproved([]));
    };
    useEffect(load, []);

    const act = async (fn, id, confirmMsg) => {
        if (confirmMsg && !window.confirm(confirmMsg)) return;
        try { await fn(id); load(); }
        catch (e) { alert(e.response?.data || "Eroare"); }
    };

    if (!pending || !approved) return <Loader />;

    return (
        <div className="grid lg:grid-cols-2 gap-6" data-testid="admin-groups-tab">
            <section>
                <h3 className="font-serif text-2xl mb-4">
                    În așteptare <span className="text-base opacity-60">({pending.length})</span>
                </h3>
                {pending.length === 0 && (
                    <div className="paper p-8 text-center opacity-60">Nicio propunere nouă.</div>
                )}
                <div className="grid gap-3">
                    {pending.map((g) => (
                        <div key={g.groupId} className="paper p-4" data-testid={`pending-group-${g.groupId}`}>
                            <div className="flex items-start justify-between gap-3">
                                <div className="flex-1 min-w-0">
                                    <span className="chip chip-butter">{g.theme}</span>
                                    <div className="font-serif text-xl mt-2">{g.name}</div>
                                    <div className="text-xs opacity-60 mt-1">
                                        Propus de {g.creator?.fullName} ·{" "}
                                        {new Date(g.createdAt).toLocaleDateString("ro-RO")}
                                    </div>
                                    <p className="text-sm opacity-70 mt-2">{g.description}</p>
                                </div>
                            </div>
                            <div className="flex gap-2 mt-3">
                                <button
                                    onClick={() => act(approveGroup, g.groupId)}
                                    className="btn btn-primary !text-xs"
                                    data-testid={`approve-group-${g.groupId}`}
                                >
                                    Aprobă
                                </button>
                                <button
                                    onClick={() => act(rejectGroup, g.groupId, `Respingi „${g.name}”?`)}
                                    className="btn btn-ghost !text-xs"
                                    data-testid={`reject-group-${g.groupId}`}
                                >
                                    Respinge
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            <section>
                <h3 className="font-serif text-2xl mb-4">
                    Cercuri active <span className="text-base opacity-60">({approved.length})</span>
                </h3>
                {approved.length === 0 && (
                    <div className="paper p-8 text-center opacity-60">Niciun cerc activ.</div>
                )}
                <div className="grid gap-3">
                    {approved.map((g) => (
                        <div key={g.groupId} className="paper p-4" data-testid={`approved-group-${g.groupId}`}>
                            <div className="flex justify-between items-start gap-3">
                                <div>
                                    <span className="chip chip-digital">{g.theme}</span>
                                    <div className="font-serif text-lg mt-2">{g.name}</div>
                                    <div className="text-xs opacity-60 mt-1">
                                        {g.memberCount} membri · {g.messageCount} mesaje
                                    </div>
                                </div>
                                <button
                                    onClick={() => act(archiveGroup, g.groupId, `Arhivezi „${g.name}”? Nu va mai fi vizibil.`)}
                                    className="btn btn-ghost !text-xs"
                                    data-testid={`archive-group-${g.groupId}`}
                                >
                                    Arhivează
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
}

function ReportsAdmin() {
    const [busy, setBusy] = useState(null);

    const reports = [
        { id: "summary", title: "Raport sumar (PDF)", desc: "Statistici generale + ultimele rezervări. Format PDF tipăribil.", path: "summary.pdf", filename: "bibliotheca-sumar.pdf", color: "var(--butter)" },
        { id: "users", title: "Utilizatori (CSV)", desc: "Listă completă cu nume, email, rol și status.", path: "users.csv", filename: "bibliotheca-utilizatori.csv" },
        { id: "reservations", title: "Rezervări (CSV)", desc: "Toate rezervările cu utilizator, carte și status.", path: "reservations.csv", filename: "bibliotheca-rezervari.csv" },
        { id: "downloads", title: "Descărcări (CSV)", desc: "Istoric complet al descărcărilor de PDF-uri.", path: "downloads.csv", filename: "bibliotheca-descarcari.csv" },
        { id: "fines", title: "Penalități (CSV)", desc: "Penalități emise, plătite și sume neîncasate.", path: "fines.csv", filename: "bibliotheca-penalitati.csv" },
    ];

    const download = async (r) => {
        setBusy(r.id);
        try { await downloadReport(r.path, r.filename); }
        catch (e) { alert(e.response?.data || "Nu s-a putut descărca raportul."); }
        finally { setBusy(null); }
    };

    return (
        <div data-testid="admin-reports-tab">
            <div className="paper p-6 mb-6">
                <h3 className="font-serif text-2xl">Exportă date pentru analiză sau arhivare</h3>
                <p className="mt-2 text-sm opacity-70">
                    Rapoartele CSV se deschid în Excel sau Google Sheets cu diacritice corecte (UTF-8).
                    Raportul PDF e gata de printat.
                </p>
            </div>

            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {reports.map((r) => (
                    <div
                        key={r.id}
                        className="paper p-5 flex flex-col"
                        style={r.color ? { background: r.color } : {}}
                        data-testid={`report-card-${r.id}`}
                    >
                        <div className="flex items-center gap-2">
                            <FileText size={16} />
                            <h4 className="font-serif text-lg">{r.title}</h4>
                        </div>
                        <p className="text-sm opacity-70 mt-2 flex-1">{r.desc}</p>
                        <button
                            onClick={() => download(r)}
                            disabled={busy === r.id}
                            className="btn btn-primary !text-sm mt-4 justify-center"
                            data-testid={`download-report-${r.id}`}
                        >
                            {busy === r.id ? (
                                <Loader2 size={12} className="animate-spin" />
                            ) : (
                                <Download size={12} />
                            )}
                            Descarcă
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}
// ===== Loader =====

function Loader() {
    return (
        <div className="text-center opacity-60 py-10">
            <Loader2 className="animate-spin inline" />
        </div>
    );
}

// ===== Chat & FAQ Admin =====

function ChatAdmin() {
    const [pending, setPending] = useState(null);
    const [faqs, setFaqs] = useState(null);
    const [newFaq, setNewFaq] = useState({ question: "", answer: "", keywords: "" });
    const [replies, setReplies] = useState({});

    const load = () => {
        chatPending().then(setPending).catch(() => setPending([]));
        chatFaq().then(setFaqs).catch(() => setFaqs([]));
    };

    useEffect(() => {
        load();
    }, []);

    const sendAnswer = async (questionId) => {
        const text = replies[questionId]?.trim();

        if (!text) {
            alert("Scrie un răspuns înainte de a trimite.");
            return;
        }

        try {
            await chatAnswer(questionId, text);
            setReplies({ ...replies, [questionId]: "" });
            load();
        } catch (e) {
            alert(e.response?.data || e.response?.data?.detail || "Eroare la trimiterea răspunsului.");
        }
    };

    const addFaq = async (e) => {
        e.preventDefault();

        if (!newFaq.question.trim() || !newFaq.answer.trim() || !newFaq.keywords.trim()) {
            alert("Completează toate câmpurile FAQ-ului.");
            return;
        }

        try {
            await chatCreateFaq(newFaq);
            setNewFaq({ question: "", answer: "", keywords: "" });
            load();
        } catch (err) {
            alert(err.response?.data || err.response?.data?.detail || "Eroare la adăugarea FAQ-ului.");
        }
    };

    const removeFaq = async (faqId, question) => {
        if (!window.confirm(`Ștergi FAQ-ul "${question}"?`)) return;

        try {
            await chatDeleteFaq(faqId);
            load();
        } catch (e) {
            alert(e.response?.data || "Eroare la ștergere.");
        }
    };

    if (!pending || !faqs) return <Loader />;

    return (
        <div className="grid lg:grid-cols-2 gap-6" data-testid="admin-chat-tab">
            <section data-testid="admin-pending-questions">
                <h3 className="font-serif text-2xl mb-4">
                    Întrebări în așteptare{" "}
                    <span className="text-base opacity-60">({pending.length})</span>
                </h3>

                {pending.length === 0 && (
                    <div className="paper p-8 text-center opacity-60">
                        Nicio întrebare deschisă din partea utilizatorilor.
                    </div>
                )}

                <div className="grid gap-3">
                    {pending.map((q) => (
                        <div
                            key={q.questionId}
                            className="paper p-4"
                            data-testid={`pending-question-${q.questionId}`}
                        >
                            <div className="text-xs uppercase tracking-widest opacity-60">
                                {q.user?.fullName || q.user?.email || "Utilizator necunoscut"}
                                {" · "}
                                {q.createdAt
                                    ? new Date(q.createdAt).toLocaleString("ro-RO")
                                    : "—"}
                            </div>

                            <p className="font-serif text-lg mt-2 italic-soft">
                                „{q.message}”
                            </p>

                            <textarea
                                rows={3}
                                className="input-cream mt-3"
                                placeholder="Scrie răspunsul aici..."
                                value={replies[q.questionId] || ""}
                                onChange={(e) =>
                                    setReplies({ ...replies, [q.questionId]: e.target.value })
                                }
                                data-testid={`reply-input-${q.questionId}`}
                            />

                            <button
                                onClick={() => sendAnswer(q.questionId)}
                                className="btn btn-primary !text-sm mt-3"
                                data-testid={`reply-send-${q.questionId}`}
                            >
                                <Check size={12} /> Trimite răspunsul
                            </button>
                        </div>
                    ))}
                </div>
            </section>

            <section data-testid="admin-faq-section">
                <h3 className="font-serif text-2xl mb-4">
                    FAQ <span className="text-base opacity-60">({faqs.length})</span>
                </h3>

                <form
                    onSubmit={addFaq}
                    className="paper p-5 space-y-3 mb-5"
                    data-testid="admin-faq-form"
                >
                    <h4 className="font-serif text-lg">Adaugă întrebare nouă</h4>

                    <input
                        className="input-cream"
                        placeholder="Întrebarea (ex: Cum rezerv o carte?)"
                        value={newFaq.question}
                        onChange={(e) =>
                            setNewFaq({ ...newFaq, question: e.target.value })
                        }
                        data-testid="faq-question-input"
                    />

                    <textarea
                        rows={3}
                        className="input-cream"
                        placeholder="Răspunsul afișat utilizatorului"
                        value={newFaq.answer}
                        onChange={(e) =>
                            setNewFaq({ ...newFaq, answer: e.target.value })
                        }
                        data-testid="faq-answer-input"
                    />

                    <input
                        className="input-cream"
                        placeholder="Cuvinte cheie (separate prin virgulă, fără diacritice)"
                        value={newFaq.keywords}
                        onChange={(e) =>
                            setNewFaq({ ...newFaq, keywords: e.target.value })
                        }
                        data-testid="faq-keywords-input"
                    />

                    <button
                        type="submit"
                        className="btn btn-primary w-full justify-center"
                        data-testid="faq-add-button"
                    >
                        <Plus size={14} /> Adaugă FAQ
                    </button>
                </form>

                <div className="grid gap-2">
                    {faqs.map((f) => (
                        <div
                            key={f.faqId}
                            className="paper p-4 flex items-start justify-between gap-3"
                            data-testid={`faq-item-${f.faqId}`}
                        >
                            <div className="min-w-0 flex-1">
                                <div className="font-serif">{f.question}</div>
                                <div className="text-sm opacity-70 mt-1">{f.answer}</div>
                                <div className="text-xs opacity-50 mt-2">
                                    Cuvinte cheie: {f.keywords}
                                </div>
                            </div>

                            <button
                                onClick={() => removeFaq(f.faqId, f.question)}
                                className="btn btn-ghost !text-xs !p-2"
                                data-testid={`faq-delete-${f.faqId}`}
                            >
                                <Trash2 size={12} />
                            </button>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
}