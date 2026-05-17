import React from "react";
import { Link } from "react-router-dom";
import { BookOpen } from "lucide-react";

export default function Footer() {
  return (
    <footer className="mt-32 relative z-10" data-testid="site-footer">
      <div className="max-w-[1400px] mx-auto px-6 lg:px-10 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10">
          <div>
            <div className="flex items-center gap-3">
              <span className="w-9 h-9 rounded-full grid place-items-center bg-[var(--paper)] border" style={{ borderColor: "var(--line)" }}>
                <BookOpen size={16} strokeWidth={1.5} />
              </span>
              <span className="font-serif italic-soft text-2xl">Bibliotheca</span>
            </div>
            <p className="mt-4 text-sm opacity-70 max-w-xs">
              Bibliotecă digitală inteligentă — Universitatea Politehnica Timișoara, Facultatea de Automatică și Calculatoare, 2025.
            </p>
          </div>
          <div>
            <h4 className="text-sm uppercase tracking-widest opacity-60 mb-3">Catalog</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/catalog" className="hover:underline">Toate cărțile</Link></li>
              <li><Link to="/catalog?type=digital" className="hover:underline">Doar PDF</Link></li>
              <li><Link to="/catalog?type=physical" className="hover:underline">Doar fizic</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-sm uppercase tracking-widest opacity-60 mb-3">Cont</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/autentificare" className="hover:underline">Autentificare</Link></li>
              <li><Link to="/inregistrare" className="hover:underline">Înregistrare</Link></li>
              <li><Link to="/profil" className="hover:underline">Profil</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-sm uppercase tracking-widest opacity-60 mb-3">Manifest</h4>
            <ul className="space-y-2 text-sm opacity-80">
              <li>— Acces fără bariere</li>
              <li>— Transparență totală</li>
              <li>— Memorie colectivă</li>
              <li>— Lectură pentru toți</li>
            </ul>
          </div>
        </div>
        <div className="mt-12 pt-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-3 text-xs opacity-60" style={{ borderTop: "1px solid var(--line)" }}>
          <span>© {new Date().getFullYear()} Bibliotheca · UPT — proiect academic</span>
          <span className="italic-soft font-serif">Templul fără ziduri al memoriei colective.</span>
        </div>
      </div>
    </footer>
  );
}
