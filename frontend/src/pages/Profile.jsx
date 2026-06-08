import React, { useEffect, useState, useCallback } from "react";
import { Link, Navigate, useSearchParams } from "react-router-dom";
import { Bell, BookMarked, Download, Heart, Check, X, Loader2, Sparkles, ArrowRight } from "lucide-react";
import {
    fetchUserReservations, cancelReservation, fetchUserNotifications,
    markNotificationRead, markAllRead, fetchWishlist, fetchDownloads, removeFromWishlist
} from "../lib/api";
import { useAuth } from "../context/AuthContext";
import Recommendations from "../components/Recommendations";

const TABS = [
    { id: "rezervari", label: "Rezervări", icon: BookMarked },
    { id: "descarcari", label: "Descărcări", icon: Download },
    { id: "wishlist", label: "Wishlist", icon: Heart },
    { id: "notificari", label: "Notificări", icon: Bell },
];

export default function Profile() {
    const { user } = useAuth();
    const [params, setParams] = useSearchParams();
    const active = params.get("tab") || "rezervari";

    if (!user) return <Navigate to="/autentificare" replace />;

    const setTab = (id) => setParams({ tab: id }, { replace: true });

    return (
        <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="profile-page">
            <div className="text-xs uppercase tracking-widest opacity-60">Profil</div>
            <h1 className="font-serif text-5xl lg:text-6xl mt-3">Bună, <span className="italic-soft">{user.fullName.split(" ")[0]}</span>.</h1>
            <p className="mt-3 opacity-70 max-w-2xl">Aici găsești tot ce ai rezervat, descărcat sau marcat pentru mai târziu. Lista ta vie de lectură.</p>

            {/* Reading Wrapped CTA */}
            <Link
                to="/wrapped"
                className="block paper paper-hover p-6 lg:p-8 mt-10 relative overflow-hidden"
                style={{ background: "linear-gradient(135deg, var(--rose) 0%, var(--butter) 100%)" }}
                data-testid="profile-wrapped-cta"
            >
                <div className="flex items-center justify-between gap-4 flex-wrap">
                    <div>
                        <div className="text-xs uppercase tracking-widest opacity-70 inline-flex items-center gap-2">
                            <Sparkles size={12} /> Nou
                        </div>
                        <h3 className="font-serif text-2xl lg:text-3xl mt-2">
                            Vezi <span className="italic-soft">anul tău</span> în cărți.
                        </h3>
                        <p className="text-sm opacity-70 mt-2 max-w-xl">
                            Statistici personalizate, top categorii, cartea ta favorită — toate într-o poveste vizuală.
                        </p>
                    </div>
                    <ArrowRight size={20} className="flex-shrink-0" />
                </div>
            </Link>

            <div className="mt-10 flex flex-wrap gap-2" data-testid="profile-tabs">
                {TABS.map((t) => {
                    const Icon = t.icon;
                    return (
                        <button
                            key={t.id}
                            onClick={() => setTab(t.id)}
                            className={`tab-link ${active === t.id ? "active" : ""} !inline-flex items-center gap-2`}
                            data-testid={`profile-tab-${t.id}`}
                        >
                            <Icon size={14} /> {t.label}
                        </button>
                    );
                })}
            </div>

            <div className="mt-8">
                {active === "rezervari" && <Reservations userId={user.userId} />}
                {active === "descarcari" && <Downloads userId={user.userId} />}
                {active === "wishlist" && <Wishlist userId={user.userId} />}
                {active === "notificari" && <Notifications userId={user.userId} />}
            </div>
            <Recommendations limit={6} />
        </main>
    );
}

function StatusChip({ status }) {
    const map = {
        CREATED: ["chip-butter", "Creată"],
        CONFIRMED: ["chip-digital", "Confirmată"],
        WAITING: ["chip-butter", "Lista de așteptare"],
        READY_FOR_PICKUP: ["chip-digital", "Pregătită pentru ridicare"],
        BORROWED: ["chip-rose", "Împrumutată"],
        RETURNED: ["chip-physical", "Returnată"],
        EXPIRED: ["chip-physical", "Expirată"],
        CANCELLED: ["chip-physical", "Anulată"],
    };
    const [cls, label] = map[status] || ["chip-physical", status];
    return <span className={`chip ${cls}`}>{label}</span>;
}

function bookInfo(b) {
    if (!b) return { id: undefined, title: "", cover: "", category: "", authors: "" };
    const authorsList = Array.isArray(b.authors)
        ? b.authors.map((a) => (typeof a === "string" ? a : a?.name)).filter(Boolean)
        : [];
    return {
        id: b.bookId,
        title: b.title,
        cover: b.coverImageURL,
        category: b.categoryName || b.category?.name || "",
        authors: authorsList.join(", "),
    };
}

function Reservations({ userId }) {
    const [items, setItems] = useState(null);
    const load = useCallback(() => {
        fetchUserReservations(userId).then(setItems).catch(() => setItems([]));
    }, [userId]);
    useEffect(() => { load(); }, [load]);
    const cancel = async (rid) => {
        try { await cancelReservation(rid); load(); } catch (e) { alert(e.response?.data?.detail || "Eroare"); }
    };

    if (!items) return <Loader />;
    if (items.length === 0) return <Empty msg="Nicio rezervare încă. Începe explorarea catalogului." cta />;

    return (
        <div className="grid gap-4" data-testid="reservations-list">
            {items.map((r) => {
                const b = bookInfo(r.book);
                return (
                    <div key={r.reservationId} className="paper p-5 flex flex-col md:flex-row gap-5 items-stretch md:items-center">
                        <Link to={`/carte/${b.id}`} className="shrink-0">
                            <div className="w-20 h-28 rounded overflow-hidden" style={{ background: "var(--cream-2)" }}>
                                {b.cover && <img src={b.cover} alt="" className="w-full h-full object-cover" />}
                            </div>
                        </Link>
                        <div className="flex-1">
                            <div className="text-xs uppercase tracking-widest opacity-60">{b.category}</div>
                            <Link to={`/carte/${b.id}`} className="font-serif text-2xl hover:underline">{b.title}</Link>
                            <div className="text-sm opacity-70 italic-soft font-serif">{b.authors}</div>
                            <div className="mt-3 flex items-center gap-3 text-xs opacity-70">
                                <StatusChip status={r.status} />
                                {r.reservationDate && <span>Rezervat: {new Date(r.reservationDate).toLocaleDateString("ro-RO")}</span>}
                                {r.expirationDate && <span>Expiră: {new Date(r.expirationDate).toLocaleDateString("ro-RO")}</span>}
                            </div>
                        </div>
                        {["WAITING", "CONFIRMED", "READY_FOR_PICKUP", "CREATED"].includes(r.status) && (
                            <button onClick={() => cancel(r.reservationId)} className="btn btn-secondary !text-sm" data-testid={`cancel-res-${r.reservationId}`}>
                                Anulează
                            </button>
                        )}
                    </div>
                );
            })}
        </div>
    );
}

function Downloads({ userId }) {
    const [items, setItems] = useState(null);
    useEffect(() => { fetchDownloads(userId).then(setItems).catch(() => setItems([])); }, [userId]);
    if (!items) return <Loader />;
    if (items.length === 0) return <Empty msg="Niciun PDF descărcat încă." />;
    return (
        <div className="grid gap-3" data-testid="downloads-list">
            {items.map((d) => {
                const b = d.book || {};
                return (
                    <div key={d.downloadId} className="paper p-4 flex items-center gap-4">
                        <Link to={`/carte/${b.bookId}`} className="shrink-0">
                            <div className="w-12 h-16 rounded overflow-hidden" style={{ background: "var(--cream-2)" }}>
                                {b.coverImageURL && <img src={b.coverImageURL} className="w-full h-full object-cover" alt="" />}
                            </div>
                        </Link>
                        <div className="flex-1">
                            <Link to={`/carte/${b.bookId}`} className="font-serif text-lg hover:underline">{b.title}</Link>
                            <div className="text-xs opacity-60">Descărcat: {new Date(d.downloadDate).toLocaleString("ro-RO")}</div>
                        </div>
                    </div>
                );
            })}
        </div>
    );
}

function Wishlist({ userId }) {
    const [items, setItems] = useState(null);
    const load = useCallback(() => {
        fetchWishlist(userId).then(setItems).catch(() => setItems([]));
    }, [userId]);
    useEffect(() => { load(); }, [load]);
    if (!items) return <Loader />;
    if (items.length === 0) return <Empty msg="Wishlist-ul tău e gol. Marchează cărți pentru mai târziu." />;

    return (
        <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5" data-testid="wishlist-list">
            {items.map((b) => (
                <div key={b.bookId} className="paper paper-hover p-4">
                    <Link to={`/carte/${b.bookId}`}>
                        <div className="book-cover"><img src={b.coverImageURL} alt={b.title} /></div>
                    </Link>
                    <Link to={`/carte/${b.bookId}`} className="font-serif text-lg mt-3 block">{b.title}</Link>
                    <div className="text-xs opacity-60 italic-soft font-serif">{b.authors?.join(", ")}</div>
                    <button
                        onClick={async () => { await removeFromWishlist(userId, b.bookId); load(); }}
                        className="btn btn-ghost !text-xs !py-1.5 !px-3 mt-3"
                        data-testid={`remove-wishlist-${b.bookId}`}
                    >
                        <X size={12} /> Elimină
                    </button>
                </div>
            ))}
        </div>
    );
}

function Notifications({ userId }) {
    const [items, setItems] = useState(null);
    const load = useCallback(() => {
        fetchUserNotifications(userId).then(setItems).catch(() => setItems([]));
    }, [userId]);
    useEffect(() => { load(); }, [load]);
    if (!items) return <Loader />;
    if (items.length === 0) return <Empty msg="Nicio notificare." />;

    return (
        <div>
            <div className="flex justify-end mb-3">
                <button onClick={async () => { await markAllRead(userId); load(); }} className="btn btn-secondary !text-xs" data-testid="mark-all-read">
                    <Check size={12} /> Marchează toate ca citite
                </button>
            </div>
            <div className="grid gap-2" data-testid="notifications-list">
                {items.map((n) => (
                    <div
                        key={n.notificationId}
                        className={`paper p-4 flex items-start gap-4 ${n.isRead ? "opacity-60" : ""}`}
                        data-testid={`notification-${n.notificationId}`}
                    >
                        <div className="w-2 h-2 mt-2 rounded-full" style={{ background: n.isRead ? "var(--line)" : "var(--rose-2)" }} />
                        <div className="flex-1">
                            <div className="text-xs uppercase tracking-widest opacity-60">{n.type.replace(/_/g, " ").toLowerCase()}</div>
                            <p className="mt-1 text-sm">{n.message}</p>
                            <div className="text-xs opacity-50 mt-1">{new Date(n.createdAt).toLocaleString("ro-RO")}</div>
                        </div>
                        {!n.isRead && (
                            <button onClick={async () => { await markNotificationRead(n.notificationId); load(); }} className="btn btn-ghost !text-xs !p-2">
                                <Check size={12} />
                            </button>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

function Empty({ msg, cta }) {
    return (
        <div className="paper p-10 text-center" data-testid="empty-state">
            <p className="font-serif text-2xl italic-soft">{msg}</p>
            {cta && <Link to="/catalog" className="btn btn-primary mt-5 inline-flex">Catalogul ne așteaptă</Link>}
        </div>
    );
}
function Loader() { return <div className="text-center opacity-60 py-10"><Loader2 className="animate-spin inline" /></div>; }