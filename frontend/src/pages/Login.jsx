import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, BookOpen, Eye, EyeOff, Mail, Lock } from "lucide-react";
import { useAuth } from "../context/AuthContext";

const QUOTES = [
    ["O bibliotecă infinită nu poate exista — dar memoria sa, da.", "Jorge Luis Borges"],
    ["Citește, ca să trăiești de mai multe ori.", "Gustave Flaubert"],
    ["O cameră fără cărți este ca un trup fără suflet.", "Cicero"],
    ["Cititorii trăiesc o mie de vieți înainte să moară.", "George R.R. Martin"],
    ["Cărțile sunt prieteni tăcuți și constanți.", "Charles W. Eliot"],
];

const BOOK_IMAGES = [
    "https://images.unsplash.com/photo-1535905557558-afc4877a26fc?auto=format&fit=crop&w=600&q=80",
    "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=600&q=80",
    "https://images.unsplash.com/photo-1495640388908-05fa85288e61?auto=format&fit=crop&w=600&q=80",
    "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80",
    "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=600&q=80",
    "https://images.unsplash.com/photo-1491841550275-ad7854e35ca6?auto=format&fit=crop&w=600&q=80",
];

export default function Login() {
    const { login } = useAuth();
    const nav = useNavigate();
    const [form, setForm] = useState({ email: "", password: "" });
    const [err, setErr] = useState(null);
    const [busy, setBusy] = useState(false);
    const [showPwd, setShowPwd] = useState(false);
    const [quote, setQuote] = useState(QUOTES[0]);

    useEffect(() => {
        const i = Math.floor(Math.random() * QUOTES.length);
        setQuote(QUOTES[i]);
    }, []);

    const onSubmit = async (e) => {
        e.preventDefault();
        setBusy(true); setErr(null);
        try {
            const u = await login(form.email, form.password);
            nav(u.roleName === "ADMIN" ? "/admin" : "/profil");
        } catch (e) {
            setErr(e.response?.data?.detail || e.response?.data || "Email sau parolă invalidă");
        } finally { setBusy(false); }
    };

    const fill = (email, password) => {
        setForm({ email, password });
    };

    return (
        <main className="min-h-[calc(100vh-100px)] grid lg:grid-cols-2 gap-0 -mt-6 relative" data-testid="login-page">
            {/* LEFT — Decorative library scene */}
            <aside className="hidden lg:flex relative overflow-hidden items-stretch" style={{ background: "var(--ink)" }}>
                <div className="absolute inset-0 grid grid-cols-3 grid-rows-2 gap-3 p-8 opacity-90">
                    {BOOK_IMAGES.map((src, i) => (
                        <div
                            key={i}
                            className="paper p-2 reveal"
                            style={{
                                background: "var(--paper)",
                                animationDelay: `${i * 0.08}s`,
                                transform: `rotate(${(i % 2 === 0 ? -1 : 1) * (2 + i)}deg) translateY(${(i % 3) * 6}px)`,
                            }}
                        >
                            <div className="overflow-hidden rounded h-full">
                                <img src={src} alt="" className="w-full h-full object-cover" loading="lazy" />
                            </div>
                        </div>
                    ))}
                </div>
                <div className="absolute inset-0" style={{ background: "linear-gradient(180deg, rgba(26,26,26,0.55) 0%, rgba(26,26,26,0.95) 80%)" }} />
                <div className="relative z-10 p-12 flex flex-col justify-between text-[var(--paper)]">
                    <Link to="/" className="flex items-center gap-3 group w-fit">
            <span className="w-9 h-9 rounded-full grid place-items-center border border-[var(--paper)]/30">
              <BookOpen size={16} strokeWidth={1.5} />
            </span>
                        <span className="font-serif italic-soft text-2xl">Bibliotheca</span>
                    </Link>
                    <div className="max-w-md">
                        <div className="text-xs uppercase tracking-widest opacity-60 mb-4">— De pe rafturile noastre</div>
                        <p className="font-serif italic-soft text-3xl xl:text-4xl leading-snug" data-testid="login-quote">
                            „{quote[0]}"
                        </p>
                        <div className="mt-5 font-serif text-sm opacity-70">— {quote[1]}</div>
                    </div>
                    <div className="text-xs opacity-50">UPT · Bibliotecă Digitală Inteligentă · 2025</div>
                </div>
            </aside>

            {/* RIGHT — Form */}
            <section className="flex items-center justify-center p-6 sm:p-12 lg:p-16 reveal">
                <div className="w-full max-w-md">
                    <div className="eyebrow" data-testid="login-eyebrow">Bun venit înapoi</div>
                    <h1 className="display text-5xl lg:text-6xl mt-6">
                        Intră în <span className="italic-soft hl-rose">raftul</span> tău.
                    </h1>
                    <p className="mt-4 opacity-70 text-sm leading-relaxed">
                        Reia lectura, verifică rezervările sau descarcă următorul PDF preferat.
                    </p>

                    <form onSubmit={onSubmit} className="mt-10 space-y-5" data-testid="login-form">
                        <div className="space-y-2">
                            <label className="text-xs uppercase tracking-widest opacity-60 flex items-center gap-2">
                                <Mail size={12} /> Email
                            </label>
                            <input
                                type="email" required autoFocus
                                value={form.email}
                                onChange={(e) => setForm({ ...form, email: e.target.value })}
                                className="input-cream"
                                placeholder="ana@upt.ro"
                                data-testid="login-email"
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs uppercase tracking-widest opacity-60 flex items-center gap-2">
                                <Lock size={12} /> Parolă
                            </label>
                            <div className="relative">
                                <input
                                    type={showPwd ? "text" : "password"} required
                                    value={form.password}
                                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                                    className="input-cream pr-12"
                                    placeholder="••••••••"
                                    data-testid="login-password"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPwd((s) => !s)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 opacity-50 hover:opacity-100"
                                    tabIndex={-1}
                                    data-testid="login-toggle-pwd"
                                >
                                    {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                                </button>
                            </div>
                        </div>

                        {err && (
                            <div className="paper p-3 text-sm flex items-start gap-2" style={{ background: "var(--rose)" }} data-testid="login-error">
                                <span>⚠️</span><span>{String(err)}</span>
                            </div>
                        )}

                        <button type="submit" disabled={busy} className="btn btn-primary w-full justify-center !py-4 !text-base" data-testid="login-submit">
                            {busy ? "Se conectează…" : <>Intră în cont <ArrowRight size={16} /></>}
                        </button>

                        <p className="text-sm opacity-70 text-center pt-2">
                            Încă nu ai cont?{" "}
                            <Link to="/inregistrare" className="text-[var(--ink)] underline decoration-[var(--rose-2)] decoration-2 underline-offset-4">
                                Creează unul în 30 de secunde
                            </Link>
                        </p>
                    </form>
                </div>
            </section>
        </main>
    );
}
