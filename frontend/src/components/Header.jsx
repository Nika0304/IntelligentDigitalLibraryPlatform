import React, { useEffect, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { Search, BookOpen, Bell, ChevronDown, User as UserIcon, LogOut, Heart, Shield } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { fetchUserNotifications } from "../lib/api";

export default function Header() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [unread, setUnread] = useState(0);
  const [q, setQ] = useState("");

  useEffect(() => {
    if (!user) { setUnread(0); return; }
    fetchUserNotifications(user.userId).then((ns) => {
      setUnread(ns.filter((n) => !n.isRead).length);
    }).catch(() => {});
  }, [user]);

  const submitSearch = (e) => {
    e.preventDefault();
    if (q.trim()) navigate(`/catalog?q=${encodeURIComponent(q.trim())}`);
  };

  return (
    <header className="relative z-20" data-testid="site-header">
      <div className="max-w-[1400px] mx-auto px-6 lg:px-10 py-5 flex items-center justify-between gap-4">
        <Link to="/" className="flex items-center gap-3 group" data-testid="logo-link">
          <span className="w-9 h-9 rounded-full grid place-items-center bg-[var(--paper)] border" style={{ borderColor: "var(--line)" }}>
            <BookOpen size={16} strokeWidth={1.5} />
          </span>
          <span className="font-serif italic-soft text-2xl tracking-tight">Bibliotheca</span>
        </Link>

        <nav className="hidden md:flex items-center gap-1 text-sm">
          <NavLink to="/" end className={({isActive}) => `tab-link ${isActive ? "active" : ""}`} data-testid="nav-home">Acasă</NavLink>
          <NavLink to="/catalog" className={({isActive}) => `tab-link ${isActive ? "active" : ""}`} data-testid="nav-catalog">Catalog</NavLink>
            <NavLink
                to="/cercuri"
                className={({isActive}) => `tab-link ${isActive ? "active" : ""}`}
                data-testid="nav-groups">
                Cercuri
            </NavLink>
            <NavLink
                to="/provocari"
                className={({isActive}) => `tab-link ${isActive ? "active" : ""}`}
                data-testid="nav-challenges">
                Provocări
            </NavLink>
            {user && <NavLink to="/profil" className={({isActive}) => `tab-link ${isActive ? "active" : ""}`} data-testid="nav-profile">Profil</NavLink>}
          {isAdmin && <NavLink to="/admin" className={({isActive}) => `tab-link ${isActive ? "active" : ""}`} data-testid="nav-admin">Admin</NavLink>}
        </nav>

        <div className="flex items-center gap-2">
          <form onSubmit={submitSearch} className="hidden sm:flex items-center gap-2 paper px-3 py-2 rounded-full" style={{ background: "var(--paper)" }}>
            <Search size={15} className="opacity-60" />
            <input
              data-testid="header-search-input"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Caută o carte…"
              className="bg-transparent outline-none text-sm w-40 placeholder:opacity-60"
            />
          </form>

          {user ? (
            <div className="relative">
              <button
                onClick={() => setMenuOpen((o) => !o)}
                className="btn btn-secondary !py-2 !px-3 !text-sm"
                data-testid="user-menu-button"
              >
                {user.fullName.split(" ")[0]}
                <ChevronDown size={14} />
              </button>
              {menuOpen && (
                <div className="absolute right-0 mt-2 w-60 paper p-2 z-30" data-testid="user-menu">
                  <Link to="/profil" onClick={() => setMenuOpen(false)} className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-[var(--cream-2)] text-sm" data-testid="menu-profile">
                    <UserIcon size={14} /> Profilul meu
                  </Link>
                  <Link to="/profil?tab=notificari" onClick={() => setMenuOpen(false)} className="flex items-center justify-between px-3 py-2 rounded-lg hover:bg-[var(--cream-2)] text-sm" data-testid="menu-notifications">
                    <span className="flex items-center gap-2"><Bell size={14} /> Notificări</span>
                    {unread > 0 && <span className="chip chip-rose">{unread}</span>}
                  </Link>
                  <Link to="/profil?tab=wishlist" onClick={() => setMenuOpen(false)} className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-[var(--cream-2)] text-sm" data-testid="menu-wishlist">
                    <Heart size={14} /> Wishlist
                  </Link>
                  {isAdmin && (
                    <Link to="/admin" onClick={() => setMenuOpen(false)} className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-[var(--cream-2)] text-sm" data-testid="menu-admin">
                      <Shield size={14} /> Panou Admin
                    </Link>
                  )}
                  <div className="h-px my-1" style={{ background: "var(--line)" }} />
                  <button
                    onClick={() => { logout(); setMenuOpen(false); navigate("/"); }}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-[var(--cream-2)] text-sm w-full text-left"
                    data-testid="menu-logout"
                  >
                    <LogOut size={14} /> Deconectare
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link to="/autentificare" className="btn btn-ghost !py-2 !px-3 !text-sm" data-testid="login-link">
                Autentificare
              </Link>
              <Link to="/inregistrare" className="btn btn-primary !py-2 !px-4 !text-sm" data-testid="register-link">
                Creează cont
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
