import React, { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { Star, BookMarked, Download, Heart, ArrowLeft, Loader2 } from "lucide-react";
import {
  fetchBook, fetchReviewsByBook, fetchCopiesByBook, createReservation,
    createReview, recordDownload, addToWishlist, downloadDigitalBook
} from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function BookDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth();
  const [book, setBook] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [copies, setCopies] = useState([]);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState(null);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: "" });

  const load = () => {
    fetchBook(id).then(setBook).catch(() => {});
    fetchReviewsByBook(id).then(setReviews).catch(() => {});
    fetchCopiesByBook(id).then(setCopies).catch(() => {});
  };
  useEffect(() => { load(); }, [id]);

  const flash = (type, text) => {
    setMsg({ type, text });
    setTimeout(() => setMsg(null), 4000);
  };

  const onReserve = async () => {
    if (!user) return nav("/autentificare");
    setBusy(true);
    try {
      const r = await createReservation(user.userId, Number(id));
      flash("ok", r.status === "CONFIRMED"
        ? "Rezervare confirmată. Ai 2 zile la dispoziție pentru ridicare."
        : "Adăugat în lista de așteptare. Vei fi notificat când o copie devine disponibilă.");
      load();
    } catch (e) {
      flash("err", e.response?.data?.detail || e.response?.data || "Eroare la rezervare");
    } finally { setBusy(false); }
  };

    const onDownload = async () => {
        if (!user) return nav("/autentificare");

        setBusy(true);

        try {
            await downloadDigitalBook(Number(id), book.title);


            flash("ok", "PDF descărcat cu succes. Cartea apare și în istoricul descărcărilor.");
        } catch (e) {
            flash("err", "Nu s-a putut descărca PDF-ul.");
        } finally {
            setBusy(false);
        }
    };

  const onWishlist = async () => {
    if (!user) return nav("/autentificare");
    try {
      await addToWishlist(user.userId, Number(id));
      flash("ok", "Adăugat în wishlist.");
    } catch (e) {
      flash("err", "Cartea este deja în wishlist.");
    }
  };

  const onReviewSubmit = async (e) => {
    e.preventDefault();
    if (!user) return nav("/autentificare");
    try {
      await createReview({
        userId: user.userId, bookId: Number(id),
        rating: Number(reviewForm.rating), comment: reviewForm.comment,
      });
      setReviewForm({ rating: 5, comment: "" });
      load();
    } catch (e) {
      flash("err", e.response?.data?.detail || "Eroare la trimiterea recenziei");
    }
  };

  if (!book) {
    return <div className="max-w-[1400px] mx-auto px-6 py-20 text-center opacity-60"><Loader2 className="animate-spin inline" /></div>;
  }

  const available = book.availableCopies || 0;

  return (
    <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid={`book-detail-${book.bookId}`}>
      <Link to="/catalog" className="inline-flex items-center gap-2 text-sm opacity-70 hover:opacity-100" data-testid="back-to-catalog">
        <ArrowLeft size={14} /> Înapoi la catalog
      </Link>

      <div className="grid lg:grid-cols-12 gap-10 mt-8">
        <div className="lg:col-span-4">
          <div className="paper p-3" style={{ background: "var(--paper)" }}>
            <div className="book-cover">
              <img src={book.coverImageURL || `https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=600&q=80`} alt={book.title} />
            </div>
          </div>

          <div className="mt-6 flex flex-wrap gap-2">
            {book.hasDigitalCopy && <span className="chip chip-digital">Digital</span>}
            {book.hasPhysicalCopy && <span className="chip chip-physical">Fizic</span>}
            {available > 0 ? <span className="chip chip-butter">{available} exemplare libere</span> : book.hasPhysicalCopy && <span className="chip chip-rose">Listă de așteptare</span>}
          </div>

          <div className="mt-6 space-y-3">
            {book.hasPhysicalCopy && (
              <button onClick={onReserve} disabled={busy} className="btn btn-primary w-full justify-center" data-testid="reserve-button">
                <BookMarked size={14} /> {available > 0 ? "Rezervă exemplar fizic" : "Înscrie-te pe listă"}
              </button>
            )}
            {book.hasDigitalCopy && (
              <button onClick={onDownload} disabled={busy} className="btn btn-secondary w-full justify-center" data-testid="download-button">
                <Download size={14} /> Descarcă PDF
              </button>
            )}
            <button onClick={onWishlist} className="btn btn-ghost w-full justify-center" data-testid="wishlist-button">
              <Heart size={14} /> Adaugă la wishlist
            </button>
          </div>

          {msg && (
            <div className={`mt-4 paper p-3 text-sm ${msg.type === "ok" ? "" : ""}`} style={{ background: msg.type === "ok" ? "var(--leaf)" : "var(--rose)" }} data-testid="flash-message">
              {msg.text}
            </div>
          )}
        </div>

        <div className="lg:col-span-8">
          <div className="text-xs uppercase tracking-widest opacity-60">{book.categoryName}</div>
          <h1 className="font-serif text-5xl lg:text-6xl mt-3 leading-tight">{book.title}</h1>
          <div className="mt-3 opacity-70 italic-soft font-serif text-lg">{book.authors?.join(", ")}</div>
          <div className="mt-2 text-sm opacity-70">{book.publicationYear ? `Publicată în ${book.publicationYear}` : ""}</div>

          {book.averageRating > 0 && (
            <div className="mt-4 inline-flex items-center gap-2">
              <Star size={16} fill="currentColor" />
              <span className="font-serif text-xl">{book.averageRating}</span>
              <span className="text-sm opacity-60">/ 5 · {book.reviewCount} recenzii</span>
            </div>
          )}

          <div className="mt-8 paper p-6">
            <h3 className="font-serif text-2xl">Despre carte</h3>
            <p className="mt-3 opacity-85 leading-relaxed">{book.description || "Fără descriere."}</p>
          </div>

          {book.hasPhysicalCopy && (
            <div className="mt-6 paper p-6">
              <h3 className="font-serif text-2xl">Exemplare fizice</h3>
              <div className="mt-4 grid sm:grid-cols-2 gap-3">
                {copies.map((c) => (
                  <div key={c.copyId} className="flex items-center justify-between text-sm py-2 px-3 rounded-lg" style={{ background: "var(--cream-2)" }} data-testid={`copy-${c.copyId}`}>
                    <span className="font-mono">{c.inventoryCode}</span>
                    <CopyStatus status={c.status} />
                  </div>
                ))}
                {copies.length === 0 && <div className="opacity-60 text-sm">Niciun exemplar înregistrat încă.</div>}
              </div>
            </div>
          )}

          <div className="mt-6 paper p-6" data-testid="reviews-section">
            <h3 className="font-serif text-2xl">Recenzii ({reviews.length})</h3>

            {user && (
              <form onSubmit={onReviewSubmit} className="mt-4 grid gap-3" data-testid="review-form">
                <div className="flex items-center gap-3">
                  <span className="text-sm opacity-70">Nota ta:</span>
                  <select className="input-cream !w-auto" value={reviewForm.rating} onChange={(e) => setReviewForm({ ...reviewForm, rating: e.target.value })} data-testid="review-rating">
                    {[5,4,3,2,1].map(r => <option key={r} value={r}>{r} ★</option>)}
                  </select>
                </div>
                <textarea
                  className="input-cream"
                  rows={3}
                  placeholder="Scrie o recenzie scurtă…"
                  value={reviewForm.comment}
                  onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
                  data-testid="review-comment"
                />
                <button className="btn btn-primary self-start" type="submit" data-testid="review-submit">Trimite recenzia</button>
              </form>
            )}

            <div className="mt-6 space-y-4">
              {reviews.map((r) => (
                <div key={r.reviewId} className="border-t pt-4" style={{ borderColor: "var(--line)" }} data-testid={`review-${r.reviewId}`}>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full grid place-items-center text-xs" style={{ background: "var(--rose)" }}>{(r.userName || "?")[0]}</div>
                    <div className="text-sm font-medium">{r.userName}</div>
                    <div className="ml-auto flex items-center gap-1 text-sm">
                      {Array.from({ length: r.rating }).map((_, i) => <Star key={i} size={12} fill="currentColor" />)}
                    </div>
                  </div>
                  <p className="mt-2 text-sm opacity-85">{r.comment}</p>
                </div>
              ))}
              {reviews.length === 0 && <p className="opacity-60 text-sm">Fii primul care lasă o recenzie.</p>}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

function CopyStatus({ status }) {
  const map = {
    AVAILABLE: ["chip-digital", "Disponibilă"],
    RESERVED: ["chip-butter", "Rezervată"],
    READY_FOR_PICKUP: ["chip-butter", "Pregătită pentru ridicare"],
    BORROWED: ["chip-rose", "Împrumutată"],
    RETURNED: ["chip-digital", "Returnată"],
    UNAVAILABLE: ["chip-physical", "Indisponibilă"],
  };
  const [cls, label] = map[status] || ["chip-physical", status];
  return <span className={`chip ${cls}`}>{label}</span>;
}
