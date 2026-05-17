import React, { useEffect, useState } from "react";
import { Navigate, useSearchParams } from "react-router-dom";
import {
  fetchBooks, fetchUsers, fetchAllReservations, fetchAuthors, fetchCategories,
  fetchStats, createBook, deleteBook, createAuthor, deleteAuthor,
  createCategory, deleteCategory, createBookCopy, fetchCopiesByBook,
  updateUserStatus, markReadyForPickup, borrowBook, returnBook, cancelReservation,
} from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { Plus, Trash2, BookOpen, Users, BookMarked, Tag, BarChart3, Loader2 } from "lucide-react";

const TABS = [
  { id: "stats", label: "Sumar", icon: BarChart3 },
  { id: "books", label: "Cărți", icon: BookOpen },
  { id: "users", label: "Utilizatori", icon: Users },
  { id: "reservations", label: "Rezervări", icon: BookMarked },
  { id: "taxonomy", label: "Autori & Categorii", icon: Tag },
];

export default function Admin() {
  const { user, isAdmin } = useAuth();
  const [params, setParams] = useSearchParams();
  const active = params.get("tab") || "stats";
  if (!user) return <Navigate to="/autentificare" replace />;
  if (!isAdmin) return <Navigate to="/profil" replace />;

  return (
    <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="admin-page">
      <div className="text-xs uppercase tracking-widest opacity-60">Panou Administrativ</div>
      <h1 className="font-serif text-5xl lg:text-6xl mt-3">Gestiune <span className="italic-soft">Bibliotheca</span>.</h1>
      <p className="mt-3 opacity-70 max-w-2xl">Inventar, utilizatori, rezervări și taxonomie. Toate într-un loc.</p>

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
        {active === "users" && <UsersAdmin />}
        {active === "reservations" && <ReservationsAdmin />}
        {active === "taxonomy" && <Taxonomy />}
      </div>
    </main>
  );
}

function Stats() {
  const [s, setS] = useState(null);
  useEffect(() => { fetchStats().then(setS).catch(() => {}); }, []);
  if (!s) return <Loader />;
  const cards = [
    ["Cărți", s.totalBooks], ["Utilizatori", s.totalUsers],
    ["Exemplare fizice", s.totalCopies], ["Disponibile acum", s.availableCopies],
    ["Rezervări active", s.activeReservations], ["Total rezervări", s.totalReservations],
    ["Cărți digitale", s.digitalBooks],
  ];
  return (
    <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5" data-testid="admin-stats">
      {cards.map(([l, v]) => (
        <div key={l} className="paper p-6">
          <div className="text-xs uppercase tracking-widest opacity-60">{l}</div>
          <div className="font-serif text-5xl mt-3">{v}</div>
        </div>
      ))}
    </div>
  );
}

function BooksAdmin() {
  const [books, setBooks] = useState(null);
  const [cats, setCats] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [form, setForm] = useState({ title: "", description: "", publicationYear: "", categoryId: "", authorIds: [], hasPhysicalCopy: true, hasDigitalCopy: false, coverImageURL: "" });
  const [showCopy, setShowCopy] = useState(null);
  const [copyForm, setCopyForm] = useState({ inventoryCode: "" });

  const load = () => fetchBooks().then(setBooks).catch(() => setBooks([]));
  useEffect(() => {
    load();
    fetchCategories().then(setCats);
    fetchAuthors().then(setAuthors);
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    try {
      await createBook({
        ...form,
        publicationYear: Number(form.publicationYear) || null,
        categoryId: Number(form.categoryId) || null,
        authorIds: form.authorIds.map(Number),
      });
      setForm({ title: "", description: "", publicationYear: "", categoryId: "", authorIds: [], hasPhysicalCopy: true, hasDigitalCopy: false, coverImageURL: "" });
      load();
    } catch (e) { alert(e.response?.data?.detail || "Eroare"); }
  };

  const addCopy = async (bookId) => {
    try {
      await createBookCopy(copyForm.inventoryCode, bookId);
      setCopyForm({ inventoryCode: "" });
      setShowCopy(null);
      load();
    } catch (e) { alert(e.response?.data?.detail || "Eroare"); }
  };

  if (!books) return <Loader />;

  return (
    <div className="grid lg:grid-cols-3 gap-6">
      <form onSubmit={submit} className="paper p-6 space-y-3 lg:col-span-1 h-fit sticky top-6" data-testid="admin-book-form">
        <h3 className="font-serif text-2xl mb-2">Carte nouă</h3>
        <input required className="input-cream" placeholder="Titlu" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} data-testid="admin-book-title" />
        <textarea rows={3} className="input-cream" placeholder="Descriere" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        <div className="grid grid-cols-2 gap-3">
          <input type="number" className="input-cream" placeholder="An" value={form.publicationYear} onChange={(e) => setForm({ ...form, publicationYear: e.target.value })} />
          <select className="input-cream" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            <option value="">Categorie</option>
            {cats.map(c => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
          </select>
        </div>
        <select multiple value={form.authorIds} onChange={(e) => setForm({ ...form, authorIds: Array.from(e.target.selectedOptions).map((o) => o.value) })} className="input-cream h-28">
          {authors.map(a => <option key={a.authorId} value={a.authorId}>{a.name}</option>)}
        </select>
        <input className="input-cream" placeholder="URL copertă" value={form.coverImageURL} onChange={(e) => setForm({ ...form, coverImageURL: e.target.value })} />
        <div className="flex items-center gap-4 text-sm">
          <label className="flex items-center gap-2"><input type="checkbox" checked={form.hasPhysicalCopy} onChange={(e) => setForm({ ...form, hasPhysicalCopy: e.target.checked })} /> Fizic</label>
          <label className="flex items-center gap-2"><input type="checkbox" checked={form.hasDigitalCopy} onChange={(e) => setForm({ ...form, hasDigitalCopy: e.target.checked })} /> Digital (PDF)</label>
        </div>
        <button className="btn btn-primary w-full justify-center" type="submit" data-testid="admin-create-book"><Plus size={14} /> Adaugă</button>
      </form>

      <div className="lg:col-span-2 grid gap-3" data-testid="admin-books-list">
        {books.map((b) => (
          <div key={b.bookId} className="paper p-4 flex items-center gap-4">
            <div className="w-14 h-20 rounded overflow-hidden shrink-0" style={{ background: "var(--cream-2)" }}>
              {b.coverImageURL && <img src={b.coverImageURL} className="w-full h-full object-cover" alt="" />}
            </div>
            <div className="flex-1 min-w-0">
              <div className="font-serif text-lg truncate">{b.title}</div>
              <div className="text-xs opacity-60">{b.authors?.join(", ")} · {b.publicationYear}</div>
              <div className="text-xs opacity-60 mt-1">{b.totalCopies} exemplare · {b.availableCopies} libere</div>
            </div>
            <button onClick={() => setShowCopy(showCopy === b.bookId ? null : b.bookId)} className="btn btn-ghost !text-xs" data-testid={`add-copy-${b.bookId}`}><Plus size={12} /> Exemplar</button>
            <button onClick={async () => { if (window.confirm(`Ștergi "${b.title}"?`)) { await deleteBook(b.bookId); load(); } }} className="btn btn-ghost !text-xs" data-testid={`delete-book-${b.bookId}`}><Trash2 size={12} /></button>
            {showCopy === b.bookId && (
              <div className="absolute right-0 mt-24 paper p-3 z-20 flex gap-2 items-center">
                <input className="input-cream !py-2 !text-sm" placeholder="Cod inventar" value={copyForm.inventoryCode} onChange={(e) => setCopyForm({ inventoryCode: e.target.value })} />
                <button onClick={() => addCopy(b.bookId)} className="btn btn-primary !py-2 !text-xs">OK</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

function UsersAdmin() {
  const [users, setUsers] = useState(null);
  const load = () => fetchUsers().then(setUsers).catch(() => setUsers([]));
  useEffect(() => { load(); }, []);
  if (!users) return <Loader />;
  return (
    <div className="grid gap-3" data-testid="admin-users-list">
      {users.map((u) => (
        <div key={u.userId} className="paper p-4 flex items-center gap-4">
          <div className="w-10 h-10 rounded-full grid place-items-center" style={{ background: u.roleName === "ADMIN" ? "var(--ink)" : "var(--rose)", color: u.roleName === "ADMIN" ? "var(--paper)" : "var(--ink)" }}>{u.fullName[0]}</div>
          <div className="flex-1">
            <div className="font-medium">{u.fullName}</div>
            <div className="text-xs opacity-60">{u.email}</div>
          </div>
          <span className="chip chip-physical">{u.roleName}</span>
          <select value={u.status} onChange={async (e) => { await updateUserStatus(u.userId, e.target.value); load(); }} className="input-cream !w-auto !py-2 !text-sm" data-testid={`user-status-${u.userId}`}>
            <option value="ACTIVE">Activ</option>
            <option value="SUSPENDED">Suspendat</option>
            <option value="INACTIVE">Inactiv</option>
          </select>
        </div>
      ))}
    </div>
  );
}

function ReservationsAdmin() {
  const [items, setItems] = useState(null);
  const load = () => fetchAllReservations().then(setItems).catch(() => setItems([]));
  useEffect(() => { load(); }, []);
  if (!items) return <Loader />;

  const act = async (fn, rid) => {
    try { await fn(rid); load(); } catch (e) { alert(e.response?.data?.detail || "Eroare"); }
  };

  return (
    <div className="grid gap-3" data-testid="admin-reservations-list">
      {items.map((r) => (
        <div key={r.reservationId} className="paper p-4 flex items-center gap-4 flex-wrap">
          <div className="flex-1 min-w-[200px]">
            <div className="font-serif">{r.book?.title}</div>
            <div className="text-xs opacity-60">{r.user?.fullName} · {r.user?.email}</div>
            <div className="text-xs opacity-60 mt-1">{new Date(r.reservationDate).toLocaleString("ro-RO")}</div>
          </div>
          <span className="chip chip-physical">{r.status}</span>
          {r.status === "CONFIRMED" && <button onClick={() => act(markReadyForPickup, r.reservationId)} className="btn btn-secondary !text-xs">Gata de ridicat</button>}
          {r.status === "READY_FOR_PICKUP" && <button onClick={() => act(borrowBook, r.reservationId)} className="btn btn-secondary !text-xs">Marchează împrumutat</button>}
          {r.status === "BORROWED" && <button onClick={() => act(returnBook, r.reservationId)} className="btn btn-secondary !text-xs">Returnat</button>}
          {["CONFIRMED","WAITING","READY_FOR_PICKUP","CREATED"].includes(r.status) && (
            <button onClick={() => act(cancelReservation, r.reservationId)} className="btn btn-ghost !text-xs">Anulează</button>
          )}
        </div>
      ))}
      {items.length === 0 && <div className="paper p-8 text-center opacity-60">Nicio rezervare încă.</div>}
    </div>
  );
}

function Taxonomy() {
  const [cats, setCats] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [cName, setCName] = useState("");
  const [aName, setAName] = useState("");
  const load = () => {
    fetchCategories().then(setCats);
    fetchAuthors().then(setAuthors);
  };
  useEffect(() => { load(); }, []);

  return (
    <div className="grid md:grid-cols-2 gap-6">
      <div className="paper p-6" data-testid="admin-categories">
        <h3 className="font-serif text-2xl mb-4">Categorii</h3>
        <form className="flex gap-2 mb-4" onSubmit={async (e) => { e.preventDefault(); try { await createCategory(cName); setCName(""); load(); } catch (err) { alert(err.response?.data?.detail || "Eroare"); } }}>
          <input className="input-cream" placeholder="Categorie nouă" value={cName} onChange={(e) => setCName(e.target.value)} required />
          <button className="btn btn-primary" type="submit"><Plus size={14} /></button>
        </form>
        <ul className="space-y-2">
          {cats.map((c) => (
            <li key={c.categoryId} className="flex items-center justify-between py-2 px-3 rounded-lg" style={{ background: "var(--cream-2)" }}>
              <span>{c.name}</span>
              <button onClick={async () => { if (window.confirm(`Ștergi ${c.name}?`)) { try { await deleteCategory(c.categoryId); load(); } catch (e) { alert("Eroare"); } } }} className="btn btn-ghost !text-xs !p-1.5"><Trash2 size={12} /></button>
            </li>
          ))}
        </ul>
      </div>
      <div className="paper p-6" data-testid="admin-authors">
        <h3 className="font-serif text-2xl mb-4">Autori</h3>
        <form className="flex gap-2 mb-4" onSubmit={async (e) => { e.preventDefault(); try { await createAuthor(aName); setAName(""); load(); } catch (err) { alert("Eroare"); } }}>
          <input className="input-cream" placeholder="Autor nou" value={aName} onChange={(e) => setAName(e.target.value)} required />
          <button className="btn btn-primary" type="submit"><Plus size={14} /></button>
        </form>
        <ul className="space-y-2 max-h-96 overflow-auto">
          {authors.map((a) => (
            <li key={a.authorId} className="flex items-center justify-between py-2 px-3 rounded-lg" style={{ background: "var(--cream-2)" }}>
              <span>{a.name}</span>
              <button onClick={async () => { if (window.confirm(`Ștergi ${a.name}?`)) { try { await deleteAuthor(a.authorId); load(); } catch (e) { alert("Eroare"); } } }} className="btn btn-ghost !text-xs !p-1.5"><Trash2 size={12} /></button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function Loader() { return <div className="text-center opacity-60 py-10"><Loader2 className="animate-spin inline" /></div>; }
