import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Trophy, Target, Calendar, Users2, Check, Loader2, Flame, BookOpen, Star, Sparkles } from "lucide-react";
import { fetchChallenges, fetchMyChallenges, joinChallenge, leaveChallenge } from "../lib/api";
import { useAuth } from "../context/AuthContext";

const TYPE_LABELS = {
    READ_FROM_CATEGORY: "Categorie",
    READ_FROM_AUTHOR:   "Autor",
    READ_DIGITAL:       "Digitale",
    READ_PHYSICAL:      "Fizice",
    WRITE_REVIEWS:      "Recenzii",
    ANY_READ:           "Orice",
};

const TYPE_ICONS = {
    READ_FROM_CATEGORY: <BookOpen size={14} />,
    READ_FROM_AUTHOR:   <Star size={14} />,
    READ_DIGITAL:       <BookOpen size={14} />,
    READ_PHYSICAL:      <BookOpen size={14} />,
    WRITE_REVIEWS:      <Sparkles size={14} />,
    ANY_READ:           <Target size={14} />,
};

export default function Challenges() {
    const { user } = useAuth();
    const [params, setParams] = useSearchParams();
    const tab = params.get("tab") || "all";

    const [all, setAll] = useState(null);
    const [mine, setMine] = useState(null);
    const [busy, setBusy] = useState(null);

    const load = () => {
        fetchChallenges().then(setAll).catch(() => setAll([]));
        if (user) fetchMyChallenges().then(setMine).catch(() => setMine([]));
    };
    useEffect(load, [user]);

    const onJoin = async (id) => {
        setBusy(id);
        try { await joinChallenge(id); load(); }
        catch (e) { alert(e.response?.data || "Eroare"); }
        finally { setBusy(null); }
    };
    const onLeave = async (id) => {
        setBusy(id);
        try { await leaveChallenge(id); load(); }
        finally { setBusy(null); }
    };

    const list = tab === "mine" ? (mine || []) : (all || []);
    const loading = tab === "mine" ? mine === null : all === null;

    return (
        <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="challenges-page">
            <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-2">
                <Trophy size={12} /> Provocări
            </div>
            <h1 className="font-serif text-5xl lg:text-7xl mt-3 max-w-3xl">
                Citește cu <span className="italic-soft">scop</span>.
            </h1>
            <p className="mt-3 opacity-70 max-w-2xl">
                Provocări lunare, sezoniere sau speciale. Înscrie-te și urmărește-ți progresul automat.
            </p>

            {/* TAB SWITCHER */}
            <div className="mt-10 flex gap-2" data-testid="challenges-tabs">
                <TabBtn active={tab === "all"} onClick={() => setParams({ tab: "all" }, { replace: true })}>
                    Toate ({all?.length || 0})
                </TabBtn>
                {user && (
                    <TabBtn active={tab === "mine"} onClick={() => setParams({ tab: "mine" }, { replace: true })}>
                        Ale mele ({mine?.length || 0})
                    </TabBtn>
                )}
            </div>

            {/* CONTENT */}
            {loading ? (
                <div className="text-center py-20 opacity-60" data-testid="challenges-loading">
                    <Loader2 className="animate-spin inline" />
                </div>
            ) : list.length === 0 ? (
                <div className="paper p-10 text-center mt-10" data-testid="challenges-empty">
                    <p className="font-serif text-2xl italic-soft opacity-70">
                        {tab === "mine" ? "Nu ești înscris la nicio provocare încă." : "Nicio provocare activă în acest moment."}
                    </p>
                    {tab === "mine" && (
                        <button
                            onClick={() => setParams({ tab: "all" }, { replace: true })}
                            className="btn btn-primary mt-6 inline-flex"
                        >
                            Vezi toate provocările
                        </button>
                    )}
                </div>
            ) : (
                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-5 mt-10" data-testid="challenges-grid">
                    {list.map((c) => (
                        <ChallengeCard
                            key={c.challengeId}
                            challenge={c}
                            user={user}
                            busy={busy === c.challengeId}
                            onJoin={() => onJoin(c.challengeId)}
                            onLeave={() => onLeave(c.challengeId)}
                        />
                    ))}
                </div>
            )}
        </main>
    );
}

function TabBtn({ active, onClick, children }) {
    return (
        <button
            onClick={onClick}
            className="px-4 py-2 rounded-full text-sm transition"
            style={{
                background: active ? "var(--ink)" : "var(--cream-2)",
                color: active ? "var(--paper)" : "var(--ink)",
                border: "1px solid var(--line)",
            }}
        >
            {children}
        </button>
    );
}

function ChallengeCard({ challenge: c, user, busy, onJoin, onLeave }) {
    const progress = c.userProgress ?? 0;
    const pct = Math.min(100, Math.round((progress / c.targetCount) * 100));
    const completed = c.userStatus === "COMPLETED";
    const daysLeft = daysUntil(c.endDate);
    const expired = daysLeft < 0;

    return (
        <div
            className="paper paper-hover p-6 flex flex-col relative overflow-hidden"
            data-testid={`challenge-${c.challengeId}`}
            style={completed ? { borderColor: "var(--leaf)", borderWidth: "2px" } : undefined}
        >
            {completed && (
                <div className="absolute top-3 right-3 inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full"
                     style={{ background: "var(--leaf)", color: "#2D4128" }}>
                    <Check size={12} /> Completat
                </div>
            )}

            <div className="flex items-start gap-4">
                <div className="w-14 h-14 grid place-items-center text-3xl rounded"
                     style={{ background: "var(--butter)", border: "1px solid var(--line)" }}>
                    {c.iconEmoji || "🎯"}
                </div>
                <div className="flex-1 min-w-0">
                    <div className="text-xs uppercase tracking-widest opacity-60 inline-flex items-center gap-1">
                        {TYPE_ICONS[c.type]} {TYPE_LABELS[c.type]}
                    </div>
                    <h3 className="font-serif text-xl mt-1 leading-tight">{c.title}</h3>
                </div>
            </div>

            <p className="text-sm opacity-70 mt-4 line-clamp-3 flex-1">{c.description}</p>

            {/* Detalii */}
            <div className="flex flex-wrap items-center gap-3 mt-4 text-xs opacity-70">
                <span className="inline-flex items-center gap-1">
                    <Target size={11} /> Țintă: <strong>{c.targetCount}</strong>
                </span>
                {c.categoryName && (
                    <span className="chip chip-physical !text-[10px]">{c.categoryName}</span>
                )}
                {c.authorName && (
                    <span className="chip chip-butter !text-[10px]">{c.authorName}</span>
                )}
                <span className="inline-flex items-center gap-1">
                    <Users2 size={11} /> {c.participantsCount}
                </span>
                <span className="inline-flex items-center gap-1">
                    <Calendar size={11} />
                    {expired ? "Expirată" : daysLeft === 0 ? "Ultima zi" : `${daysLeft} zile rămase`}
                </span>
            </div>

            {/* Progress bar (doar pentru cei înscriși) */}
            {c.joined && (
                <div className="mt-5">
                    <div className="flex items-center justify-between text-xs mb-2">
                        <span className="opacity-70">Progresul tău</span>
                        <span className="font-medium">
                            {progress} / {c.targetCount}
                        </span>
                    </div>
                    <div className="h-2 rounded-full overflow-hidden" style={{ background: "var(--cream-2)" }}>
                        <div
                            className="h-full transition-all duration-700"
                            style={{
                                width: `${pct}%`,
                                background: completed ? "var(--leaf)" : "var(--terracotta, #D4937F)",
                            }}
                        />
                    </div>
                </div>
            )}

            {/* Actions */}
            <div className="mt-5">
                {!user ? (
                    <Link to="/autentificare" className="btn btn-secondary w-full justify-center !text-sm">
                        Loghează-te ca să te înscrii
                    </Link>
                ) : c.joined ? (
                    completed ? (
                        <div className="inline-flex items-center gap-2 text-sm font-medium px-4 py-2 rounded-full w-full justify-center"
                             style={{ background: "var(--leaf)", color: "#2D4128" }}>
                            <Trophy size={14} /> Felicitări!
                        </div>
                    ) : (
                        <button
                            onClick={onLeave}
                            disabled={busy}
                            className="btn btn-ghost w-full justify-center !text-sm"
                            data-testid={`leave-challenge-${c.challengeId}`}
                        >
                            {busy ? <Loader2 size={12} className="animate-spin" /> : "Renunță"}
                        </button>
                    )
                ) : expired ? (
                    <button disabled className="btn btn-secondary w-full justify-center !text-sm opacity-50 cursor-not-allowed">
                        Expirată
                    </button>
                ) : (
                    <button
                        onClick={onJoin}
                        disabled={busy}
                        className="btn btn-primary w-full justify-center !text-sm"
                        data-testid={`join-challenge-${c.challengeId}`}
                    >
                        {busy ? <Loader2 size={12} className="animate-spin" /> : (
                            <><Flame size={14} /> Înscrie-mă</>
                        )}
                    </button>
                )}
            </div>
        </div>
    );
}

function daysUntil(dateStr) {
    if (!dateStr) return 0;
    const end = new Date(dateStr);
    const now = new Date();
    return Math.ceil((end - now) / (1000 * 60 * 60 * 24));
}