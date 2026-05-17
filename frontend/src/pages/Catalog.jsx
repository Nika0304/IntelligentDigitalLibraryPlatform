import React, { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Search, Filter } from "lucide-react";
import BookCard from "../components/BookCard";
import { fetchBooks, fetchCategories } from "../lib/api";

export default function Catalog() {
  const [params, setParams] = useSearchParams();
  const initialQ = params.get("q") || "";
  const initialType = params.get("type") || "all";
  const initialCat = params.get("cat") || "all";

  const [books, setBooks] = useState([]);
  const [cats, setCats] = useState([]);
  const [q, setQ] = useState(initialQ);
  const [type, setType] = useState(initialType);
  const [cat, setCat] = useState(initialCat);

  useEffect(() => {
    fetchBooks().then(setBooks).catch(() => {});
    fetchCategories().then(setCats).catch(() => {});
  }, []);

  useEffect(() => {
    const p = {};
    if (q) p.q = q;
    if (type !== "all") p.type = type;
    if (cat !== "all") p.cat = cat;
    setParams(p, { replace: true });
  }, [q, type, cat, setParams]);

  const filtered = useMemo(() => {
    return books.filter((b) => {
      if (q && !b.title.toLowerCase().includes(q.toLowerCase()) &&
          !(b.authors || []).some((a) => a.toLowerCase().includes(q.toLowerCase()))) {
        return false;
      }
      if (type === "digital" && !b.hasDigitalCopy) return false;
      if (type === "physical" && !b.hasPhysicalCopy) return false;
      if (cat !== "all" && String(b.categoryId) !== String(cat)) return false;
      return true;
    });
  }, [books, q, type, cat]);

  return (
    <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="catalog-page">
      <div className="text-xs uppercase tracking-widest opacity-60">Catalog</div>
      <h1 className="font-serif text-5xl lg:text-7xl mt-3 max-w-3xl">
        {filtered.length} <span className="italic-soft">titluri</span> te așteaptă.
      </h1>

      <div className="mt-10 paper p-4 flex flex-col md:flex-row gap-3 items-stretch">
        <div className="flex items-center gap-2 paper !rounded-full px-4 flex-1" style={{ background: "var(--cream-2)", border: "1px solid var(--line)" }}>
          <Search size={16} className="opacity-60" />
          <input
            data-testid="catalog-search-input"
            className="bg-transparent outline-none py-3 text-sm w-full"
            placeholder="Caută după titlu sau autor…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter size={14} className="opacity-60" />
          <select
            data-testid="catalog-type-select"
            value={type}
            onChange={(e) => setType(e.target.value)}
            className="input-cream !py-3 !rounded-full"
          >
            <option value="all">Toate tipurile</option>
            <option value="digital">Doar digitale</option>
            <option value="physical">Doar fizice</option>
          </select>
          <select
            data-testid="catalog-category-select"
            value={cat}
            onChange={(e) => setCat(e.target.value)}
            className="input-cream !py-3 !rounded-full"
          >
            <option value="all">Toate categoriile</option>
            {cats.map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
          </select>
        </div>
      </div>

      <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 mt-10">
        {filtered.map((b) => <BookCard key={b.bookId} book={b} />)}
        {filtered.length === 0 && (
          <div className="col-span-full paper p-10 text-center opacity-70" data-testid="catalog-empty">
            <p className="font-serif text-2xl italic-soft">Niciun rezultat.</p>
            <p className="mt-2 text-sm">Încearcă alte cuvinte cheie sau resetează filtrele.</p>
          </div>
        )}
      </div>
    </main>
  );
}
