import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [err, setErr] = useState(null);
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true); setErr(null);
    try {
      const u = await login(form.email, form.password);
      nav(u.roleName === "ADMIN" ? "/admin" : "/profil");
    } catch (e) {
      setErr(e.response?.data?.detail || "Email sau parolă invalidă");
    } finally { setBusy(false); }
  };

  return (
    <main className="max-w-md mx-auto px-6 py-20" data-testid="login-page">
      <div className="text-xs uppercase tracking-widest opacity-60">Autentificare</div>
      <h1 className="font-serif text-5xl mt-3">Bine ai <span className="italic-soft">revenit</span>.</h1>
      <p className="mt-3 opacity-70 text-sm">Intră în cont pentru a rezerva, descărca și gestiona lista ta de lectură.</p>

      <form onSubmit={onSubmit} className="paper p-7 mt-8 space-y-4" data-testid="login-form">
        <div>
          <label className="text-xs uppercase tracking-widest opacity-60">Email</label>
          <input
            type="email" required
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="input-cream mt-2"
            placeholder="ana@upt.ro"
            data-testid="login-email"
          />
        </div>
        <div>
          <label className="text-xs uppercase tracking-widest opacity-60">Parolă</label>
          <input
            type="password" required
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            className="input-cream mt-2"
            placeholder="••••••••"
            data-testid="login-password"
          />
        </div>
        {err && <div className="paper p-3 text-sm" style={{ background: "var(--rose)" }} data-testid="login-error">{err}</div>}
        <button type="submit" disabled={busy} className="btn btn-primary w-full justify-center" data-testid="login-submit">
          {busy ? "Se conectează…" : "Autentificare"}
        </button>
        <p className="text-xs opacity-60 text-center pt-2">
          Nu ai cont? <Link to="/inregistrare" className="underline">Creează unul acum</Link>
        </p>
      </form>

      <div className="paper p-5 mt-6 text-xs opacity-80" data-testid="demo-credentials">
        <div className="uppercase tracking-widest opacity-60 mb-2">Conturi demo</div>
        <div>Admin: <span className="font-mono">admin@bibliotheca.ro</span> / <span className="font-mono">admin123</span></div>
        <div>User: <span className="font-mono">ana@upt.ro</span> / <span className="font-mono">parola123</span></div>
      </div>
    </main>
  );
}
