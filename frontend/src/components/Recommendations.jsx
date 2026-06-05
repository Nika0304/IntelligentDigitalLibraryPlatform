import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Sparkles, Loader2 } from "lucide-react";
import { fetchRecommendations } from "../lib/api";

export default function Recommendations({ limit = 6 }) {
    const [data, setData] = useState(null);

    useEffect(() => {
        fetchRecommendations(limit).then(setData).catch(() => setData({ books: [] }));
    }, [limit]);

    if (!data) {
        return (
            <div className="text-center opacity-60 py-10">
                <Loader2 className="animate-spin inline" />
            </div>
        );
    }

    if (!data.books || data.books.length === 0) return null;

    return (
        <section className="mt-12" data-testid="recommendations-section">
            <div className="flex items-end justify-between gap-3 flex-wrap mb-6">
                <div>
                    <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                        <Sparkles size={12} /> Pentru tine
                    </div>
                    <h2 className="font-serif text-3xl lg:text-4xl mt-2">
                        Recomandări <span className="italic-soft">personalizate</span>.
                    </h2>
                    <p className="text-sm opacity-70 mt-1">{data.reason}</p>
                </div>
            </div>

            <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4" data-testid="recommendations-list">
                {data.books.map((b) => (
                    <Link
                        key={b.bookId}
                        to={`/carte/${b.bookId}`}
                        className="paper paper-hover p-3"
                        data-testid={`rec-book-${b.bookId}`}
                    >
                        <div className="aspect-[2/3] rounded overflow-hidden" style={{ background: "var(--cream-2)" }}>
                            {b.coverImageURL && (
                                <img src={b.coverImageURL} alt={b.title} className="w-full h-full object-cover" />
                            )}
                        </div>
                        <div className="font-serif text-sm mt-3 leading-tight line-clamp-2">{b.title}</div>
                        <div className="text-xs opacity-60 italic-soft font-serif line-clamp-1">
                            {b.authors?.join(", ")}
                        </div>
                    </Link>
                ))}
            </div>
        </section>
    );
}