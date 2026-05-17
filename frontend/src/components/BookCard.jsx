import React from "react";
import { Link } from "react-router-dom";
import { Star } from "lucide-react";

export default function BookCard({ book, onClick }) {
  const cover = book.coverImageURL || `https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=600&q=80`;
  const availability = book.hasPhysicalCopy
    ? book.availableCopies > 0
      ? `${book.availableCopies} din ${book.totalCopies} disponibile`
      : "Listă de așteptare"
    : null;

  return (
    <Link
      to={`/carte/${book.bookId}`}
      className="paper paper-hover p-4 flex flex-col gap-3 group"
      data-testid={`book-card-${book.bookId}`}
      onClick={onClick}
    >
      <div className="book-cover">
        <img src={cover} alt={book.title} loading="lazy" />
      </div>
      <div className="flex items-center gap-2 mt-1">
        {book.hasDigitalCopy && <span className="chip chip-digital" data-testid="chip-digital">Digital</span>}
        {book.hasPhysicalCopy && <span className="chip chip-physical" data-testid="chip-physical">Fizic</span>}
        {book.averageRating > 0 && (
          <span className="ml-auto inline-flex items-center gap-1 text-xs">
            <Star size={12} fill="currentColor" /> {book.averageRating}
          </span>
        )}
      </div>
      <div className="text-xs uppercase tracking-widest opacity-60">{book.categoryName || "—"}</div>
      <h3 className="font-serif text-xl leading-tight">{book.title}</h3>
      <p className="text-sm opacity-70 -mt-1">{book.authors?.join(", ") || "Autor necunoscut"}</p>
      <div className="flex items-center justify-between text-xs opacity-70 mt-auto pt-2" style={{ borderTop: "1px dashed var(--line)" }}>
        <span>{availability || "Doar digital"}</span>
        <span className="italic-soft font-serif">{book.publicationYear || "—"}</span>
      </div>
    </Link>
  );
}
