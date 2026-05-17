import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, ArrowLeft, BookOpen, Check, User as UserIcon, Mail, Lock, Eye, EyeOff } from "lucide-react";
import { useAuth } from "../context/AuthContext";

const STEPS = [
    { id: 1, label: "Nume", icon: UserIcon },
    { id: 2, label: "Email", icon: Mail },
    { id: 3, label: "Parolă", icon: Lock },
];

const BENEFITS = [
    ["📖", "Catalog hibrid", "Cărți fizice și digitale într-un singur loc."],
    ["⚡", "Rezervări rapide", "Verificare automată a stocului, listă FIFO."],
    ["📥", "Descărcări PDF", "Instant, fără limită, cu istoric complet."],
    ["💡", "Recomandări inteligente", "Algoritm pe baza istoricului tău."],
    ["🔔", "Notificări utile", "Reamintiri pentru returnări și disponibilități."],
];

export default function Register() {
    const { register } = useAuth();
    const nav = useNavigate();
    const [step, setStep] = useState(1);
    const [form, setForm] = useState({ fullName: "", email: "", password: "" });
    const [err, setErr] = useState(null);
    const [busy, setBusy] = useState(false);
    const [showPwd, setShowPwd] = useState(false);

    const next = () => {
        setErr(null);
        if (step === 1 && !form.fullName.trim()) return setErr("Spune-ne cum te numești");
        if (step === 2 && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return setErr("Email invalid");
        if (step < 3) setStep(step + 1);
    };

    const back = () => { setErr(null); if (step > 1) setStep(step - 1); };

    const submit = async (e) => {
        e.preventDefault();
        if (form.password.length < 6) return setErr("Parola trebuie să aibă cel puțin 6 caractere");
        setBusy(true); setErr(null);
        try {
            await register(form);
            nav("/profil");
        } catch (e) {
            setErr(e.response?.data?.detail || e.response?.data || "Eroare la înregistrare");
        } finally { setBusy(false); }
    };

    const onKeyDown = (e) => {
        if (e.key === "Enter" && step < 3) { e.preventDefault(); next(); }
    };

    return (
        <main className="min-h-[calc(100vh-100px)] grid lg:grid-cols-2 -mt-6" data-testid="register-page">
            {/* LEFT — form */}
            <section className="flex items-center justify-center p-6 sm:p-12 lg:p-16">
                <div className="w-full max-w-md reveal">
                    <div className="eyebrow" data-testid="register-eyebrow">Cont nou</div>
                    <h1 className="display text-5xl lg:text-6xl mt-6">
                        Deschide-ți <span className="italic-soft hl-rose">raftul</span>.
                    </h1>
                    <p className="mt-4 opacity-70 text-sm leading-relaxed">
                        Trei pași simpli, mai puțin de un minut. Apoi rezervi, descarci și ești la zi cu lecturile.
                    </p>

                    {/* Step indicator */}
                    <div className="mt-10 flex items-center gap-2" data-testid="register-stepper">
                        {STEPS.map((s, idx) => {
                            const Icon = s.icon;
                            const done = step > s.id;
                            const active = step === s.id;
                            return (
                                <React.Fragment key={s.id}>
                                    <div
                                        className={`flex items-center gap-2 px-3 py-2 rounded-full transition-all ${
                                            active ? "bg-[var(--ink)] text-[var(--paper)]" : done ? "bg-[var(--leaf)] text-[#2D4128]" : "bg-[var(--cream-2)] opacity-50"
                                        }`}
                                        data-testid={`step-${s.id}`}
                                    >
                                        {done ? <Check size={14} /> : <Icon size={14} />}
                                        <span className="text-xs uppercase tracking-wider">{s.label}</span>
                                    </div>
                                    {idx < STEPS.length - 1 && <div className="flex-1 h-px" style={{ background: "var(--line)" }} />}
                                </React.Fragment>
                            );
                        })}
                    </div>

                    <form onSubmit={submit} onKeyDown={onKeyDown} className="mt-10 space-y-5" data-testid="register-form">
                        {step === 1 && (
                            <div className="reveal" key="step1">
                                <label className="text-xs uppercase tracking-widest opacity-60 flex items-center gap-2 mb-2">
                                    <UserIcon size={12} /> Cum te numești?
                                </label>
                                <input
                                    autoFocus required
                                    value={form.fullName}
                                    onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                                    className="input-cream !text-2xl !font-serif italic-soft !py-4"
                                    placeholder="Maria Popescu"
                                    data-testid="register-name"
                                />
                                <p className="mt-3 text-xs opacity-60">Apare în recenzii și în istoria ta de lectură.</p>
                            </div>
                        )}

                        {step === 2 && (
                            <div className="reveal" key="step2">
                                <label className="text-xs uppercase tracking-widest opacity-60 flex items-center gap-2 mb-2">
                                    <Mail size={12} /> Ce email folosești?
                                </label>
                                <input
                                    autoFocus required type="email"
                                    value={form.email}
                                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                                    className="input-cream !text-xl !py-4"
                                    placeholder="maria@upt.ro"
                                    data-testid="register-email"
                                />
                                <p className="mt-3 text-xs opacity-60">Pe el primești notificările și confirmările.</p>
                            </div>
                        )}

                        {step === 3 && (
                            <div className="reveal" key="step3">
                                <label className="text-xs uppercase tracking-widest opacity-60 flex items-center gap-2 mb-2">
                                    <Lock size={12} /> Setează o parolă
                                </label>
                                <div className="relative">
                                    <input
                                        autoFocus required minLength={6}
                                        type={showPwd ? "text" : "password"}
                                        value={form.password}
                                        onChange={(e) => setForm({ ...form, password: e.target.value })}
                                        className="input-cream !text-xl !py-4 pr-12"
                                        placeholder="cel puțin 6 caractere"
                                        data-testid="register-password"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPwd((s) => !s)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 opacity-50 hover:opacity-100"
                                        tabIndex={-1}
                                    >
                                        {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                                    </button>
                                </div>
                                <PwdStrength pwd={form.password} />
                            </div>
                        )}

                        {err && (
                            <div className="paper p-3 text-sm" style={{ background: "var(--rose)" }} data-testid="register-error">
                                ⚠️ {String(err)}
                            </div>
                        )}

                        <div className="flex items-center gap-3 pt-2">
                            {step > 1 && (
                                <button type="button" onClick={back} className="btn btn-secondary" data-testid="register-back">
                                    <ArrowLeft size={14} /> Înapoi
                                </button>
                            )}
                            {step < 3 ? (
                                <button type="button" onClick={next} className="btn btn-primary flex-1 justify-center !py-4" data-testid="register-next">
                                    Continuă <ArrowRight size={16} />
                                </button>
                            ) : (
                                <button type="submit" disabled={busy} className="btn btn-primary flex-1 justify-center !py-4" data-testid="register-submit">
                                    {busy ? "Se creează…" : <>Creează contul <Check size={16} /></>}
                                </button>
                            )}
                        </div>
                    </form>

                    <p className="text-sm opacity-70 text-center mt-8">
                        Ai deja cont?{" "}
                        <Link to="/autentificare" className="text-[var(--ink)] underline decoration-[var(--rose-2)] decoration-2 underline-offset-4" data-testid="link-to-login">
                            Autentifică-te
                        </Link>
                    </p>
                </div>
            </section>

            {/* RIGHT — benefits */}
            <aside className="hidden lg:flex relative overflow-hidden items-center justify-center p-12" style={{ background: "linear-gradient(160deg, var(--cream-2) 0%, var(--paper) 100%)" }}>
                <div className="max-w-md w-full">
                    <Link to="/" className="flex items-center gap-3 group w-fit mb-8">
            <span className="w-9 h-9 rounded-full grid place-items-center bg-[var(--ink)] text-[var(--paper)]">
              <BookOpen size={16} strokeWidth={1.5} />
            </span>
                        <span className="font-serif italic-soft text-2xl">Bibliotheca</span>
                    </Link>

                    <div className="text-xs uppercase tracking-widest opacity-60 mb-4">Ce primești</div>
                    <h2 className="font-serif text-4xl leading-tight">
                        Cinci motive să-ți faci <span className="italic-soft">contul azi</span>.
                    </h2>

                    <ul className="mt-10 space-y-5" data-testid="register-benefits">
                        {BENEFITS.map(([emoji, title, desc], i) => (
                            <li key={i} className="flex items-start gap-4 reveal" style={{ animationDelay: `${0.08 * i}s` }}>
                                <div className="w-12 h-12 rounded-full grid place-items-center text-xl flex-shrink-0" style={{ background: "var(--paper)", border: "1px solid var(--line)" }}>
                                    {emoji}
                                </div>
                                <div>
                                    <h4 className="font-serif text-lg">{title}</h4>
                                    <p className="text-sm opacity-70 mt-1">{desc}</p>
                                </div>
                            </li>
                        ))}
                    </ul>

                    <div className="mt-12 paper p-5 text-xs italic-soft font-serif text-center" style={{ background: "var(--butter)" }}>
                        „Singura amenințare la adresa unui cititor este lipsa de cărți." — Bibliotheca
                    </div>
                </div>
            </aside>
        </main>
    );
}

function PwdStrength({ pwd }) {
    if (!pwd) return null;
    const score = (pwd.length >= 8 ? 1 : 0) + (/[A-Z]/.test(pwd) ? 1 : 0) + (/[0-9]/.test(pwd) ? 1 : 0) + (/[^A-Za-z0-9]/.test(pwd) ? 1 : 0);
    const labels = ["Slabă", "Acceptabilă", "Bună", "Excelentă"];
    const colors = ["var(--rose-2)", "var(--butter)", "var(--leaf)", "#7BB37A"];
    const pct = pwd.length >= 6 ? Math.max(25, score * 25) : 10;
    return (
        <div className="mt-3" data-testid="pwd-strength">
            <div className="h-1 rounded-full overflow-hidden" style={{ background: "var(--cream-2)" }}>
                <div className="h-full transition-all duration-300" style={{ width: `${pct}%`, background: colors[Math.max(0, score - 1)] || colors[0] }} />
            </div>
            <div className="mt-2 text-xs opacity-60">Puterea parolei: <span className="font-medium">{pwd.length < 6 ? "Prea scurtă" : labels[Math.max(0, score - 1)] || labels[0]}</span></div>
        </div>
    );
}
