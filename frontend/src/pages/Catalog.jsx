import React, { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Search, X, SlidersHorizontal, Star, Loader2 } from "lucide-react";
import BookCard from "../components/BookCard";
import { searchBooksAdvanced } from "../lib/api";

const SORTS = [
    { id: "relevance",      label: "Relevanță" },
    { id: "title",          label: "Titlu (A → Z)" },
    { id: "yearDesc",       label: "An (cel mai nou)" },
    { id: "yearAsc",        label: "An (cel mai vechi)" },
    { id: "ratingDesc",     label: "Rating (mare → mic)" },
    { id: "popularityDesc", label: "Popularitate" },
];

const TYPES = [
    { id: "all",      label: "Toate" },
    { id: "digital",  label: "Digitale" },
    { id: "physical", label: "Fizice" },
];

const PAGE_SIZE = 12;

export default function Catalog() {
    const [params, setParams] = useSearchParams();

    // === state ===
    const [q, setQ] = useState(params.get("q") || "");
    const [type, setType] = useState(params.get("type") || "all");
    const [sort, setSort] = useState(params.get("sort") || "relevance");
    const [selectedCats, setSelectedCats] = useState(parseIds(params.get("cats")));
    const [selectedAuthors, setSelectedAuthors] = useState(parseIds(params.get("authors")));
    const [minRating, setMinRating] = useState(numOrNull(params.get("rating")));
    const [yearFrom, setYearFrom] = useState(numOrNull(params.get("yf")));
    const [yearTo, setYearTo] = useState(numOrNull(params.get("yt")));
    const [onlyAvailable, setOnlyAvailable] = useState(params.get("avail") === "1");
    const [page, setPage] = useState(0);

    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showFilters, setShowFilters] = useState(false);
    const debounceRef = useRef(null);

    // sync URL cu state
    useEffect(() => {
        const p = {};
        if (q) p.q = q;
        if (type !== "all") p.type = type;
        if (sort !== "relevance") p.sort = sort;
        if (selectedCats.length) p.cats = selectedCats.join(",");
        if (selectedAuthors.length) p.authors = selectedAuthors.join(",");
        if (minRating) p.rating = minRating;
        if (yearFrom) p.yf = yearFrom;
        if (yearTo) p.yt = yearTo;
        if (onlyAvailable) p.avail = "1";
        setParams(p, { replace: true });
    }, [q, type, sort, selectedCats, selectedAuthors, minRating, yearFrom, yearTo, onlyAvailable, setParams]);

    // fetch (debounced pe text query)
    useEffect(() => {
        clearTimeout(debounceRef.current);
        setLoading(true);
        debounceRef.current = setTimeout(() => {
            searchBooksAdvanced({
                q, type, sort,
                categoryIds: selectedCats,
                authorIds: selectedAuthors,
                minRating,
                yearFrom,
                yearTo,
                onlyAvailable,
                page: 0,
                size: PAGE_SIZE,
            })
                .then((res) => { setData(res); setPage(0); })
                .catch(() => setData({ items: [], total: 0, totalPages: 0, facets: {} }))
                .finally(() => setLoading(false));
        }, 250);
        return () => clearTimeout(debounceRef.current);
    }, [q, type, sort, selectedCats, selectedAuthors, minRating, yearFrom, yearTo, onlyAvailable]);

    const loadMore = async () => {
        if (!data || page + 1 >= data.totalPages) return;
        setLoading(true);
        try {
            const next = await searchBooksAdvanced({
                q, type, sort,
                categoryIds: selectedCats,
                authorIds: selectedAuthors,
                minRating, yearFrom, yearTo, onlyAvailable,
                page: page + 1,
                size: PAGE_SIZE,
            });
            setData((prev) => ({ ...next, items: [...(prev?.items || []), ...next.items] }));
            setPage(page + 1);
        } finally { setLoading(false); }
    };

    const resetAll = () => {
        setQ(""); setType("all"); setSort("relevance");
        setSelectedCats([]); setSelectedAuthors([]);
        setMinRating(null); setYearFrom(null); setYearTo(null);
        setOnlyAvailable(false);
    };

    const toggleCat = (id) => setSelectedCats((s) => s.includes(id) ? s.filter(x => x !== id) : [...s, id]);
    const toggleAuthor = (id) => setSelectedAuthors((s) => s.includes(id) ? s.filter(x => x !== id) : [...s, id]);

    const activeFilters = useActiveFilters({
        q, type, selectedCats, selectedAuthors, minRating, yearFrom, yearTo, onlyAvailable,
        facets: data?.facets,
        onClear: (key) => {
            if (key === "q") setQ("");
            if (key === "type") setType("all");
            if (key === "rating") setMinRating(null);
            if (key === "yearFrom") setYearFrom(null);
            if (key === "yearTo") setYearTo(null);
            if (key === "available") setOnlyAvailable(false);
            if (key?.startsWith("cat:")) setSelectedCats((s) => s.filter(id => String(id) !== key.slice(4)));
            if (key?.startsWith("author:")) setSelectedAuthors((s) => s.filter(id => String(id) !== key.slice(7)));
        },
    });

    const facets = data?.facets || {};
    const categories = facets.categories || [];
    const authors = facets.authors || [];
    const yearRange = [facets.yearMin, facets.yearMax];

    return (
        <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="catalog-page">
            <div className="text-xs uppercase tracking-widest opacity-60">Catalog avansat</div>
            <h1 className="font-serif text-5xl lg:text-7xl mt-3 max-w-3xl">
                {data === null ? "—" : data.total} <span className="italic-soft">titluri</span>{" "}
                {q ? <>pentru „<em className="italic-soft">{q}</em>"</> : "te așteaptă"}.
            </h1>

            {/* TOP BAR — search + sort + mobile filter toggle */}
            <div className="mt-10 grid grid-cols-1 md:grid-cols-12 gap-3 items-stretch">
                <div className="md:col-span-7 flex items-center gap-2 paper !rounded-full px-4"
                     style={{ background: "var(--cream-2)", border: "1px solid var(--line)" }}>
                    <Search size={16} className="opacity-60" />
                    <input
                        data-testid="catalog-search-input"
                        className="bg-transparent outline-none py-3 text-sm w-full"
                        placeholder="Caută după titlu, autor sau descriere…"
                        value={q}
                        onChange={(e) => setQ(e.target.value)}
                    />
                    {q && (
                        <button onClick={() => setQ("")} className="opacity-60 hover:opacity-100" data-testid="clear-search">
                            <X size={14} />
                        </button>
                    )}
                </div>

                <select
                    data-testid="catalog-sort-select"
                    value={sort}
                    onChange={(e) => setSort(e.target.value)}
                    className="input-cream !py-3 !rounded-full md:col-span-3"
                >
                    {SORTS.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
                </select>

                <button
                    onClick={() => setShowFilters((v) => !v)}
                    className="btn btn-secondary md:col-span-2 justify-center lg:hidden"
                    data-testid="catalog-toggle-filters"
                >
                    <SlidersHorizontal size={14} /> Filtre
                </button>
            </div>

            {/* ACTIVE FILTERS */}
            {activeFilters.length > 0 && (
                <div className="flex flex-wrap gap-2 mt-6" data-testid="catalog-active-filters">
                    {activeFilters.map((f) => (
                        <button
                            key={f.key}
                            onClick={() => activeFilters.find(a => a.key === f.key)?.onClear()}
                            className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs"
                            style={{ background: "var(--ink)", color: "var(--paper)" }}
                            data-testid={`active-filter-${f.key}`}
                        >
                            {f.label} <X size={12} />
                        </button>
                    ))}
                    <button
                        onClick={resetAll}
                        className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs opacity-60 hover:opacity-100"
                        style={{ border: "1px solid var(--line)" }}
                        data-testid="catalog-reset-all"
                    >
                        Resetează tot
                    </button>
                </div>
            )}

            {/* LAYOUT — sidebar + grid */}
            <div className="grid lg:grid-cols-12 gap-8 mt-10">
                {/* SIDEBAR FILTERS */}
                <aside
                    className={`lg:col-span-3 space-y-6 ${showFilters ? "block" : "hidden lg:block"}`}
                    data-testid="catalog-filters"
                >
                    <FilterGroup title="Tip">
                        <div className="flex flex-wrap gap-2">
                            {TYPES.map((t) => (
                                <FilterChip
                                    key={t.id}
                                    active={type === t.id}
                                    onClick={() => setType(t.id)}
                                    testid={`type-${t.id}`}
                                >
                                    {t.label}
                                </FilterChip>
                            ))}
                        </div>
                    </FilterGroup>

                    <FilterGroup title="Disponibilitate">
                        <label className="flex items-center gap-2 text-sm cursor-pointer">
                            <input
                                type="checkbox"
                                checked={onlyAvailable}
                                onChange={(e) => setOnlyAvailable(e.target.checked)}
                                data-testid="filter-only-available"
                            />
                            Doar disponibile acum
                        </label>
                    </FilterGroup>

                    <FilterGroup title="Rating minim">
                        <div className="flex gap-1">
                            {[null, 3, 4, 4.5].map((r) => (
                                <FilterChip
                                    key={String(r)}
                                    active={minRating === r}
                                    onClick={() => setMinRating(r)}
                                    testid={`rating-${r ?? "any"}`}
                                >
                                    {r === null ? "Orice" : (
                                        <span className="inline-flex items-center gap-1">
                                            <Star size={10} fill="currentColor" /> {r}+
                                        </span>
                                    )}
                                </FilterChip>
                            ))}
                        </div>
                    </FilterGroup>

                    {categories.length > 0 && (
                        <FilterGroup title={`Categorii (${categories.length})`}>
                            <div className="space-y-1.5 max-h-64 overflow-y-auto pr-1">
                                {categories.map((c) => (
                                    <label key={c.id} className="flex items-center justify-between text-sm cursor-pointer hover:opacity-100 opacity-80">
                                        <span className="flex items-center gap-2">
                                            <input
                                                type="checkbox"
                                                checked={selectedCats.includes(c.id)}
                                                onChange={() => toggleCat(c.id)}
                                                data-testid={`filter-cat-${c.id}`}
                                            />
                                            {c.name}
                                        </span>
                                        <span className="text-xs opacity-50">{c.count}</span>
                                    </label>
                                ))}
                            </div>
                        </FilterGroup>
                    )}

                    {authors.length > 0 && (
                        <FilterGroup title={`Autori (${authors.length})`}>
                            <div className="space-y-1.5 max-h-64 overflow-y-auto pr-1">
                                {authors.map((a) => (
                                    <label key={a.id} className="flex items-center justify-between text-sm cursor-pointer hover:opacity-100 opacity-80">
                                        <span className="flex items-center gap-2">
                                            <input
                                                type="checkbox"
                                                checked={selectedAuthors.includes(a.id)}
                                                onChange={() => toggleAuthor(a.id)}
                                                data-testid={`filter-author-${a.id}`}
                                            />
                                            {a.name}
                                        </span>
                                        <span className="text-xs opacity-50">{a.count}</span>
                                    </label>
                                ))}
                            </div>
                        </FilterGroup>
                    )}

                    {yearRange[0] != null && yearRange[1] != null && (
                        <FilterGroup title="Anul publicării">
                            <div className="flex items-center gap-2">
                                <input
                                    type="number"
                                    placeholder={String(yearRange[0])}
                                    value={yearFrom ?? ""}
                                    onChange={(e) => setYearFrom(e.target.value ? Number(e.target.value) : null)}
                                    className="input-cream !py-2 !text-sm w-24"
                                    data-testid="filter-year-from"
                                />
                                <span className="opacity-50 text-sm">—</span>
                                <input
                                    type="number"
                                    placeholder={String(yearRange[1])}
                                    value={yearTo ?? ""}
                                    onChange={(e) => setYearTo(e.target.value ? Number(e.target.value) : null)}
                                    className="input-cream !py-2 !text-sm w-24"
                                    data-testid="filter-year-to"
                                />
                            </div>
                            <p className="text-xs opacity-50 mt-2">Interval disponibil: {yearRange[0]}–{yearRange[1]}</p>
                        </FilterGroup>
                    )}
                </aside>

                {/* RESULTS */}
                <section className="lg:col-span-9">
                    {loading && !data?.items?.length ? (
                        <div className="text-center py-20 opacity-60" data-testid="catalog-loading">
                            <Loader2 className="animate-spin inline" />
                        </div>
                    ) : data && data.items.length > 0 ? (
                        <>
                            <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                                {data.items.map((b) => <BookCard key={b.bookId} book={b} />)}
                            </div>

                            {page + 1 < data.totalPages && (
                                <div className="mt-10 text-center">
                                    <button
                                        onClick={loadMore}
                                        disabled={loading}
                                        className="btn btn-primary !py-4 !px-8"
                                        data-testid="catalog-load-more"
                                    >
                                        {loading ? (
                                            <><Loader2 size={14} className="animate-spin" /> Se încarcă…</>
                                        ) : (
                                            <>Mai multe ({data.total - data.items.length} rămase)</>
                                        )}
                                    </button>
                                </div>
                            )}
                        </>
                    ) : (
                        <div className="paper p-10 text-center opacity-70" data-testid="catalog-empty">
                            <p className="font-serif text-2xl italic-soft">Niciun rezultat.</p>
                            <p className="mt-2 text-sm">Încearcă alte cuvinte cheie sau resetează filtrele.</p>
                            <button onClick={resetAll} className="btn btn-secondary mt-6">
                                Resetează filtre
                            </button>
                        </div>
                    )}
                </section>
            </div>
        </main>
    );
}

/* ===== Sub-componente ===== */

function FilterGroup({ title, children }) {
    return (
        <div className="paper p-4">
            <div className="text-xs uppercase tracking-widest opacity-60 mb-3">{title}</div>
            {children}
        </div>
    );
}

function FilterChip({ active, onClick, children, testid }) {
    return (
        <button
            onClick={onClick}
            data-testid={testid}
            className="px-3 py-1.5 rounded-full text-xs transition"
            style={{
                background: active ? "var(--ink)" : "var(--cream-2)",
                color:      active ? "var(--paper)" : "var(--ink)",
                border:     active ? "1px solid var(--ink)" : "1px solid var(--line)",
            }}
        >
            {children}
        </button>
    );
}

/* ===== Helpers ===== */

function parseIds(str) {
    if (!str) return [];
    return str.split(",").map((s) => Number(s)).filter((n) => !Number.isNaN(n));
}

function numOrNull(str) {
    if (!str) return null;
    const n = Number(str);
    return Number.isNaN(n) ? null : n;
}

function useActiveFilters({ q, type, selectedCats, selectedAuthors, minRating, yearFrom, yearTo, onlyAvailable, facets, onClear }) {
    return useMemo(() => {
        const list = [];
        if (q) list.push({ key: "q", label: `„${q}"`, onClear: () => onClear("q") });
        if (type !== "all") {
            const t = TYPES.find(x => x.id === type)?.label || type;
            list.push({ key: "type", label: t, onClear: () => onClear("type") });
        }
        if (onlyAvailable) list.push({ key: "available", label: "Disponibile", onClear: () => onClear("available") });
        if (minRating)     list.push({ key: "rating", label: `★ ${minRating}+`, onClear: () => onClear("rating") });
        if (yearFrom)      list.push({ key: "yearFrom", label: `Din ${yearFrom}`, onClear: () => onClear("yearFrom") });
        if (yearTo)        list.push({ key: "yearTo", label: `Până în ${yearTo}`, onClear: () => onClear("yearTo") });

        const catsList = facets?.categories || [];
        selectedCats.forEach((id) => {
            const c = catsList.find(x => x.id === id);
            list.push({ key: `cat:${id}`, label: c?.name || `Cat #${id}`, onClear: () => onClear(`cat:${id}`) });
        });

        const authList = facets?.authors || [];
        selectedAuthors.forEach((id) => {
            const a = authList.find(x => x.id === id);
            list.push({ key: `author:${id}`, label: a?.name || `Autor #${id}`, onClear: () => onClear(`author:${id}`) });
        });

        return list;
    }, [q, type, selectedCats, selectedAuthors, minRating, yearFrom, yearTo, onlyAvailable, facets, onClear]);
}