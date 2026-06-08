import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
    Sparkles, BookOpen, TrendingUp, Calendar, Flame,
    Star, Download, BookMarked, MessageSquare, Heart, Share2, Loader2, ArrowLeft
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { fetchMyWrapped } from "../lib/api";

const COLORS = ["#D4937F", "#CDD9C3", "#F2D982", "#E7B8AA", "#8F9B93"];

export default function Wrapped() {
    const { user } = useAuth();
    const [params, setParams] = useSearchParams();
    const initialYear = Number(params.get("year")) || new Date().getFullYear();
    const [year, setYear] = useState(initialYear);
    const [data, setData] = useState(null);
    const [err, setErr] = useState(null);
    const [shared, setShared] = useState(false);

    useEffect(() => {
        if (!user) return;
        setData(null);
        fetchMyWrapped(year)
            .then(setData)
            .catch(() => setErr("Nu am putut încărca raportul."));
        setParams({ year: String(year) }, { replace: true });
    }, [year, user, setParams]);

    const onShare = async () => {
        const url = window.location.href;
        try {
            if (navigator.share) {
                await navigator.share({
                    title: `Anul meu în cărți · ${year}`,
                    text: `Am citit ${data?.totalBooks || 0} cărți pe Bibliotheca anul ăsta!`,
                    url,
                });
            } else {
                await navigator.clipboard.writeText(url);
                setShared(true);
                setTimeout(() => setShared(false), 2500);
            }
        } catch {}
    };

    if (!user) {
        return (
            <main className="max-w-2xl mx-auto px-6 py-24 text-center" data-testid="wrapped-need-login">
                <h1 className="font-serif text-4xl">Trebuie să te autentifici</h1>
                <p className="mt-4 opacity-70">Pentru a-ți vedea raportul anual, intră în cont.</p>
                <Link to="/autentificare" className="btn btn-primary mt-8 inline-flex">Autentificare</Link>
            </main>
        );
    }

    if (err) {
        return (
            <main className="max-w-2xl mx-auto px-6 py-24 text-center">
                <p className="font-serif text-2xl opacity-70">{err}</p>
            </main>
        );
    }

    if (!data) {
        return (
            <main className="max-w-2xl mx-auto px-6 py-24 text-center opacity-60" data-testid="wrapped-loading">
                <Loader2 className="animate-spin inline" />
                <p className="mt-4 text-sm">Construim povestea ta de lectură…</p>
            </main>
        );
    }

    return (
        <main className="max-w-[1200px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="wrapped-page">
            {/* HEADER */}
            <div className="flex items-center justify-between flex-wrap gap-4 mb-12">
                <Link to="/profil" className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2 hover:opacity-100">
                    <ArrowLeft size={12} /> Înapoi la profil
                </Link>
                <YearPicker year={year} onChange={setYear} />
            </div>

            {/* HERO */}
            <section className="text-center reveal" data-testid="wrapped-hero">
                <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs uppercase tracking-widest"
                     style={{ background: "var(--rose)", color: "var(--ink)" }}>
                    <Sparkles size={12} /> Anul tău în cărți · {data.year}
                </div>
                <h1 className="font-serif text-6xl lg:text-8xl mt-8 leading-[0.95]">
                    Salut, <span className="italic-soft hl-rose">{firstName(data.fullName)}</span>.
                </h1>
                <p className="font-serif text-2xl italic-soft mt-6 opacity-80">
                    Iată povestea ta de lectură din {data.year}.
                </p>
                <div className="mt-10 inline-block paper px-6 py-3" style={{ background: "var(--butter)" }}>
                    <span className="text-xs uppercase tracking-widest opacity-60">Personalitate</span>
                    <div className="font-serif text-2xl italic mt-1" data-testid="wrapped-personality">
                        {data.personality}
                    </div>
                </div>
            </section>

            {/* BIG NUMBER */}
            <section className="mt-28 text-center reveal" style={{ animationDelay: "0.15s" }}>
                <div className="text-xs uppercase tracking-widest opacity-60">Cărți consumate</div>
                <div className="font-serif-display text-[160px] lg:text-[240px] leading-[0.9] text-ink"
                     data-testid="wrapped-total-books">
                    {data.totalBooks}
                </div>
                <p className="font-serif text-xl italic-soft opacity-70 mt-4">
                    {data.totalBooks === 0 ? "Niciuna încă — povestea ta abia începe." :
                        data.totalBooks === 1 ? "O carte explorată." :
                            "cărți explorate, descărcate sau rezervate."}
                </p>

                {data.totalBooks > 0 && (
                    <div className="mt-8 inline-flex items-center gap-3 text-sm">
                        <span className="opacity-60">vs media platformei:</span>
                        <span className="font-serif text-2xl">{data.platformAverageBooks}</span>
                        <span className={`text-xs px-2 py-1 rounded-full ${
                            data.totalBooks > data.platformAverageBooks ? "bg-[var(--leaf)]" : "bg-[var(--cream-2)]"
                        }`}>
                            {data.totalBooks > data.platformAverageBooks
                                ? `+${Math.round((data.totalBooks - data.platformAverageBooks) * 10) / 10} peste medie`
                                : `${Math.round((data.platformAverageBooks - data.totalBooks) * 10) / 10} sub medie`}
                        </span>
                    </div>
                )}
            </section>

            {/* GRID — top categorii + autor */}
            <section className="mt-28 grid lg:grid-cols-2 gap-8 reveal" style={{ animationDelay: "0.3s" }}>
                <CategoriesCard categories={data.topCategories} />
                <AuthorCard author={data.topAuthor} />
            </section>

            {/* MONTHLY ACTIVITY */}
            {data.totalBooks > 0 && (
                <section className="mt-12 paper p-8 lg:p-10 reveal" data-testid="wrapped-monthly">
                    <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                        <Calendar size={12} /> Ritmul tău
                    </div>
                    <h3 className="font-serif text-3xl lg:text-4xl mt-3">
                        Lună cu lună, <span className="italic-soft">povestea</span>.
                    </h3>
                    {data.mostActiveMonth && (
                        <p className="mt-3 opacity-70">
                            Luna ta cea mai activă: <strong>{data.mostActiveMonth.monthName}</strong>
                            {" "}cu <strong>{data.mostActiveMonth.count}</strong> {data.mostActiveMonth.count === 1 ? "interacțiune" : "interacțiuni"}.
                        </p>
                    )}
                    <BarChartMonthly months={data.monthlyActivity} />
                </section>
            )}

            {/* STATS ROW */}
            <section className="mt-12 grid grid-cols-2 lg:grid-cols-4 gap-4 reveal">
                <StatBox icon={<Flame size={20} />} value={data.readingStreak}
                         label="Zile consecutive active" accent="bg-[#E7B8AA]" testid="stat-streak" />
                <StatBox icon={<Download size={20} />} value={data.totalDownloads}
                         label="Descărcări" accent="bg-[#CDD9C3]" testid="stat-downloads" />
                <StatBox icon={<BookMarked size={20} />} value={data.totalReservations}
                         label="Rezervări" accent="bg-[#F2D982]" testid="stat-reservations" />
                <StatBox icon={<MessageSquare size={20} />} value={data.totalReviews}
                         label="Recenzii scrise" accent="bg-[#D4937F]" testid="stat-reviews" />
            </section>

            {/* FAVORITE BOOK */}
            {data.favoriteBook && (
                <section className="mt-12 reveal" style={{ animationDelay: "0.5s" }} data-testid="wrapped-favorite">
                    <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                        <Heart size={12} /> Cartea ta favorită
                    </div>
                    <h3 className="font-serif text-3xl lg:text-4xl mt-3 mb-8">
                        Cea pe care ai <span className="italic-soft">iubit-o</span>.
                    </h3>
                    <FavoriteBookCard book={data.favoriteBook} />
                </section>
            )}

            {/* REVIEWS SUMMARY */}
            {data.totalReviews > 0 && (
                <section className="mt-12 paper p-8 lg:p-10 reveal">
                    <div className="grid lg:grid-cols-2 gap-8 items-center">
                        <div>
                            <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                                <Star size={12} /> Recenzii
                            </div>
                            <h3 className="font-serif text-3xl mt-3">
                                Ai scris <span className="italic-soft">{data.totalReviews}</span> {data.totalReviews === 1 ? "recenzie" : "recenzii"} anul ăsta.
                            </h3>
                            <p className="opacity-70 mt-3">
                                Rating mediu acordat: <strong>{data.avgRatingGiven}</strong> din 5.
                            </p>
                        </div>
                        <div className="text-center">
                            <div className="font-serif-display text-[120px] leading-none">
                                {data.avgRatingGiven}
                            </div>
                            <div className="flex justify-center gap-1 mt-3">
                                {[1, 2, 3, 4, 5].map((s) => (
                                    <Star key={s} size={20}
                                          fill={s <= Math.round(data.avgRatingGiven) ? "var(--terracotta, #D4937F)" : "none"}
                                          stroke="var(--terracotta, #D4937F)" />
                                ))}
                            </div>
                        </div>
                    </div>
                </section>
            )}

            {/* SHARE / CTA */}
            <section className="mt-20 rounded-[28px] p-10 lg:p-14 text-center"
                     style={{ background: "var(--ink)", color: "var(--paper)" }}>
                <Sparkles className="inline mb-6 opacity-80" />
                <h3 className="font-serif text-3xl lg:text-5xl leading-tight">
                    Continuă <span className="italic">povestea</span>.
                </h3>
                <p className="opacity-70 mt-4 max-w-xl mx-auto">
                    Anul abia a început. Adaugă cărți în wishlist, descarcă PDF-uri, scrie recenzii — fiecare acțiune îți modelează raportul.
                </p>
                <div className="flex flex-wrap justify-center gap-3 mt-8">
                    <button
                        onClick={onShare}
                        className="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-medium transition hover:opacity-90"
                        style={{ background: "var(--paper)", color: "var(--ink)" }}
                        data-testid="wrapped-share"
                    >
                        <Share2 size={14} /> {shared ? "Link copiat ✓" : "Distribuie raportul"}
                    </button>
                    <Link
                        to="/catalog"
                        className="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm border transition hover:opacity-80"
                        style={{ borderColor: "rgba(247,241,231,0.5)", color: "var(--paper)" }}
                    >
                        <BookOpen size={14} /> Catalog
                    </Link>
                </div>
            </section>
        </main>
    );
}

/* ============== SUBCOMPONENTE ============== */

function YearPicker({ year, onChange }) {
    const currentYear = new Date().getFullYear();
    const years = [currentYear, currentYear - 1, currentYear - 2];
    return (
        <div className="flex gap-2" data-testid="wrapped-year-picker">
            {years.map((y) => (
                <button
                    key={y}
                    onClick={() => onChange(y)}
                    className="px-4 py-2 rounded-full text-sm transition"
                    style={{
                        background: y === year ? "var(--ink)" : "var(--cream-2)",
                        color: y === year ? "var(--paper)" : "var(--ink)",
                        border: "1px solid var(--line)",
                    }}
                    data-testid={`wrapped-year-${y}`}
                >
                    {y}
                </button>
            ))}
        </div>
    );
}

function CategoriesCard({ categories }) {
    if (!categories || categories.length === 0) {
        return (
            <div className="paper p-8 lg:p-10 opacity-60">
                <div className="text-xs uppercase tracking-widest opacity-80 inline-flex items-center gap-2">
                    <TrendingUp size={12} /> Categorii
                </div>
                <p className="font-serif text-xl italic-soft mt-6">
                    Niciun gen explorat încă.
                </p>
            </div>
        );
    }
    const total = categories.reduce((sum, c) => sum + c.count, 0);
    let acc = 0;
    const segments = categories.map((c, i) => {
        const portion = c.count / total;
        const start = acc;
        acc += portion;
        return { ...c, start, end: acc, color: COLORS[i % COLORS.length] };
    });

    return (
        <div className="paper p-8 lg:p-10" data-testid="wrapped-categories">
            <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                <TrendingUp size={12} /> Categorii preferate
            </div>
            <h3 className="font-serif text-3xl lg:text-4xl mt-3">
                Genuri pe care le-ai <span className="italic-soft">savurat</span>.
            </h3>
            <div className="mt-8 flex items-center gap-8 flex-wrap">
                <DonutChart segments={segments} size={180} />
                <ul className="space-y-2 flex-1 min-w-[180px]">
                    {segments.map((s, i) => (
                        <li key={s.name} className="flex items-center gap-3">
                            <span className="w-3 h-3 rounded-full" style={{ background: s.color }} />
                            <span className="font-serif text-lg flex-1">{s.name}</span>
                            <span className="text-sm opacity-70">{s.count}</span>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

function DonutChart({ segments, size = 180 }) {
    const r = (size - 30) / 2;
    const cx = size / 2;
    const cy = size / 2;
    return (
        <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
            {segments.map((s, i) => {
                const a1 = s.start * 2 * Math.PI - Math.PI / 2;
                const a2 = s.end * 2 * Math.PI - Math.PI / 2;
                const x1 = cx + r * Math.cos(a1);
                const y1 = cy + r * Math.sin(a1);
                const x2 = cx + r * Math.cos(a2);
                const y2 = cy + r * Math.sin(a2);
                const largeArc = s.end - s.start > 0.5 ? 1 : 0;
                const d = `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`;
                return <path key={i} d={d} fill={s.color} opacity={0.9} />;
            })}
            <circle cx={cx} cy={cy} r={r * 0.55} fill="var(--paper)" />
        </svg>
    );
}

function AuthorCard({ author }) {
    return (
        <div className="paper p-8 lg:p-10 relative overflow-hidden" data-testid="wrapped-author"
             style={{ background: "linear-gradient(160deg, var(--cream-2), var(--paper))" }}>
            <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                <BookOpen size={12} /> Autorul tău
            </div>
            {author ? (
                <>
                    <h3 className="font-serif text-3xl lg:text-4xl mt-3">
                        Vocea care te-a <span className="italic-soft">însoțit</span>.
                    </h3>
                    <div className="mt-10 text-center">
                        <div className="font-serif-display text-5xl lg:text-6xl leading-tight">{author.name}</div>
                        <div className="mt-4 text-sm opacity-70">
                            {author.count} {author.count === 1 ? "carte explorată" : "cărți explorate"} de acest autor.
                        </div>
                    </div>
                </>
            ) : (
                <p className="font-serif text-xl italic-soft mt-6 opacity-60">
                    Niciun autor favorit definit încă.
                </p>
            )}
        </div>
    );
}

function BarChartMonthly({ months }) {
    const max = Math.max(1, ...months.map((m) => m.count));
    return (
        <div className="mt-10 flex items-end justify-between gap-2 h-48" data-testid="wrapped-bar-chart">
            {months.map((m) => {
                const h = (m.count / max) * 100;
                const isPeak = m.count === max && m.count > 0;
                return (
                    <div key={m.month} className="flex-1 flex flex-col items-center gap-2">
                        <div className="text-xs opacity-50">{m.count || ""}</div>
                        <div className="w-full flex-1 flex items-end">
                            <div className="w-full rounded-t transition-all duration-700"
                                 style={{
                                     height: `${h}%`,
                                     minHeight: m.count > 0 ? "4px" : "1px",
                                     background: isPeak ? "var(--terracotta, #D4937F)" : "var(--ink)",
                                     opacity: m.count > 0 ? 0.85 : 0.15,
                                 }} />
                        </div>
                        <div className="text-[10px] uppercase opacity-60">{m.monthShort}</div>
                    </div>
                );
            })}
        </div>
    );
}

function StatBox({ icon, value, label, accent, testid }) {
    return (
        <div className="paper p-6" data-testid={testid}>
            <div className={`w-10 h-10 ${accent} grid place-items-center mb-4`}
                 style={{ border: "1px solid var(--line)" }}>
                {icon}
            </div>
            <div className="font-serif text-4xl leading-none">{value}</div>
            <div className="text-xs uppercase tracking-widest opacity-60 mt-3">{label}</div>
        </div>
    );
}

function FavoriteBookCard({ book }) {
    return (
        <Link
            to={`/carte/${book.bookId}`}
            className="paper paper-hover p-6 lg:p-8 flex flex-col md:flex-row gap-8 items-start"
            data-testid="wrapped-favorite-card"
        >
            <div className="w-48 aspect-[2/3] flex-shrink-0 overflow-hidden rounded"
                 style={{ background: "var(--cream-2)" }}>
                {book.coverImageURL ? (
                    <img src={book.coverImageURL} alt={book.title} className="w-full h-full object-cover" />
                ) : (
                    <div className="w-full h-full flex items-center justify-center font-serif text-6xl opacity-30">
                        {book.title?.[0]}
                    </div>
                )}
            </div>
            <div className="flex-1">
                <div className="flex gap-1 mb-4">
                    {[1, 2, 3, 4, 5].map((s) => (
                        <Star key={s} size={16}
                              fill={s <= book.userRating ? "var(--terracotta, #D4937F)" : "none"}
                              stroke="var(--terracotta, #D4937F)" />
                    ))}
                </div>
                <h4 className="font-serif text-3xl lg:text-4xl leading-tight">{book.title}</h4>
                <p className="font-serif italic-soft text-lg opacity-70 mt-2">
                    {book.authors?.join(", ")}
                </p>
                <p className="mt-6 text-sm opacity-70">
                    Ai dat acestei cărți {book.userRating} din 5 stele — cea mai mare notă a ta din acest an.
                </p>
            </div>
        </Link>
    );
}

function firstName(s) {
    if (!s) return "";
    return s.split(" ")[0];
}