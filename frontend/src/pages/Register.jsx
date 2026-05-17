import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const { register } = useAuth();
  const nav = useNavigate();
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [err, setErr] = useState(null);
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true); setErr(null);
    try {
      await register(form);
      nav("/profil");
    } catch (e) {
      setErr(e.response?.data?.detail || e.response?.data || "Eroare la înregistrare");
    } finally { setBusy(false); }
  };

  return (
    <main className="max-w-md mx-auto px-6 py-20" data-testid="register-page">
      <div className="text-xs uppercase tracking-widest opacity-60">Cont nou</div>
      <h1 className="font-serif text-5xl mt-3">Deschide-ți <span className="italic-soft">raftul</span>.</h1>
      <p className="mt-3 opacity-70 text-sm">Creează un cont gratuit pentru rezervări, descărcări PDF și recomandări personalizate.</p>

      <form onSubmit={onSubmit} className="paper p-7 mt-8 space-y-4" data-testid="register-form">
        <div>
          <label className="text-xs uppercase tracking-widest opacity-60">Nume complet</label>
          <input
            required
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            className="input-cream mt-2"
            placeholder="Maria Popescu"
            data-testid="register-name"
          />
        </div>
        <div>
          <label className="text-xs uppercase tracking-widest opacity-60">Email</label>
          <input
            type="email" required
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="input-cream mt-2"
            placeholder="maria@upt.ro"
            data-testid="register-email"
          />
        </div>
        <div>
          <label className="text-xs uppercase tracking-widest opacity-60">Parolă</label>
          <input
            type="password" required minLength={6}
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            className="input-cream mt-2"
            placeholder="cel puțin 6 caractere"
            data-testid="register-password"
          />
        </div>
        {err && <div className="paper p-3 text-sm" style={{ background: "var(--rose)" }} data-testid="register-error">{String(err)}</div>}
        <button type="submit" disabled={busy} className="btn btn-primary w-full justify-center" data-testid="register-submit">
          {busy ? "Se creează…" : "Creează cont"}
        </button>
        <p className="text-xs opacity-60 text-center pt-2">
          Ai deja cont? <Link to="/autentificare" className="underline">Autentifică-te</Link>
        </p>
      </form>
    </main>
  );
}
