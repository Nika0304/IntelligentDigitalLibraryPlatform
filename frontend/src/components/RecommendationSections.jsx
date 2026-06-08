import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Sparkles, TrendingUp, Star, UserCheck, Layers, Loader2 } from "lucide-react";
import { fetchRecommendationSections } from "../lib/api";
import { useAuth } from "../context/AuthContext";

const SECTION_ICONS = {
    forYou:            <Sparkles size={12} />,
    trending:          <TrendingUp size={12} />,
    bestRated:         <Star size={12} />,
    byFavoriteAuthors: <UserCheck size={12} />,
    similarTo:         <Layers size={12} />,
};

export default function RecommendationSections({ limit = 6 }) {
    const { user } = useAuth();
    const [sections, setSections] = useState(null);
    const [error, setError] = useState(false);

    useEffect(() => {
        if (!user) return;
        fetchRecommendationSections(limit)
            .then((data) => setSections(Array.isArray(data) ? data : []))
            .catch(() => setError(true));
    }, [user, limit]);

    // doar useri logați
    if (!user) return null;
    if (error) return null;

    if (!sections) {
        return (
            <section className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-24" data-testid="rec-sections-loading">
                <div className="text-center opacity-60 py-10">
                    <Loader2 className="animate-spin inline" />
                </div>
            </section>
        );
    }

    if (sections.length === 0) return null;

    return (
        <section
            className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-24 space-y-16"
            data-testid="rec-sections"
        >
            <div>
                <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                    <Sparkles size={12} /> Recomandări inteligente
                </div>
                <h2 className="font-serif text-4xl lg:text-5xl mt-3">
                    Pentru <span className="italic-soft">tine</span>, {user.fullName?.split(" ")[0] || ""}.
                </h2>
                <p className="mt-3 opacity-70 max-w-2xl">
                    Algoritm hibrid bazat pe categoriile, autorii și recenziile tale.
                </p>
            </div>

            {sections.map((s) => (
                <SectionRow key={s.id} section={s} />
            ))}
        </section>
    );
}

function SectionRow({ section }) {
    if (!section.books || section.books.length === 0) return null;

    return (
        <div data-testid={`rec-section-${section.id}`}>
            <div className="flex items-end justify-between gap-4 flex-wrap mb-6">
                <div>
                    <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                        {SECTION_ICONS[section.id]} {section.title}
                    </div>
                    <h3 className="font-serif text-2xl lg:text-3xl mt-2 italic-soft">
                        {section.reason}
                    </h3>
                </div>
            </div>

            <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                {section.books.map((b) => (
                    <Link
                        key={b.bookId}
                        to={`/carte/${b.bookId}`}
                        className="paper paper-hover p-3 group"
                        data-testid={`rec-book-${section.id}-${b.bookId}`}
                    >
                        <div
                            className="aspect-[2/3] rounded overflow-hidden"
                            style={{ background: "var(--cream-2)" }}
                        >
                            {b.coverImageURL ? (
                                <img
                                    src={b.coverImageURL}
                                    alt={b.title}
                                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                                />
                            ) : (
                                <div className="w-full h-full flex items-center justify-center opacity-30 font-serif italic">
                                    {b.title?.[0]}
                                </div>
                            )}
                        </div>
                        <div className="font-serif text-sm mt-3 leading-tight line-clamp-2">
                            {b.title}
                        </div>
                        <div className="text-xs opacity-60 italic-soft font-serif line-clamp-1">
                            {b.authors?.join(", ")}
                        </div>
                        {b.averageRating > 0 && (
                            <div className="flex items-center gap-1 mt-2 text-xs opacity-70">
                                <Star size={10} fill="currentColor" />
                                <span>{b.averageRating.toFixed(1)}</span>
                                <span className="opacity-50">({b.reviewCount})</span>
                            </div>
                        )}
                    </Link>
                ))}
            </div>
        </div>
    );
}