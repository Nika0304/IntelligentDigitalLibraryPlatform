import React, { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { Users2, MessageCircle, Plus, Sparkles, Loader2, X } from "lucide-react";
import { fetchGroups, fetchMyGroups, proposeGroup } from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function Groups() {
    const { user } = useAuth();
    const [groups, setGroups] = useState(null);
    const [mine, setMine] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [form, setForm] = useState({ name: "", theme: "", description: "" });
    const [busy, setBusy] = useState(false);
    const [err, setErr] = useState(null);
    const [ok, setOk] = useState(null);

    const load = () => {
        fetchGroups().then(setGroups).catch(() => setGroups([]));
        if (user) fetchMyGroups().then(setMine).catch(() => setMine([]));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    useEffect(load, [user?.userId]);

    const submit = async (e) => {
        e.preventDefault();
        setBusy(true); setErr(null); setOk(null);
        try {
            await proposeGroup(form);
            setOk("Propunerea ta a fost trimisă. Adminul îți va răspunde curând.");
            setForm({ name: "", theme: "", description: "" });
            setShowForm(false);
            load();
        } catch (e) {
            setErr(e.response?.data || "Nu am putut trimite propunerea.");
        } finally {
            setBusy(false);
        }
    };

    if (!groups) return <Loader />;

    const myIds = new Set(mine.map((m) => m.groupId));

    return (
        <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="groups-page">
            <div className="text-xs uppercase tracking-widest opacity-60">Cercuri de lectură</div>
            <div className="flex flex-wrap items-end justify-between gap-4 mt-3">
                <h1 className="font-serif text-5xl lg:text-6xl">
                    Citește <span className="italic-soft">împreună</span>.
                </h1>
                {user && (
                    <button
                        onClick={() => setShowForm(true)}
                        className="btn btn-primary"
                        data-testid="propose-group-btn"
                    >
                        <Plus size={14} /> Propune un cerc nou
                    </button>
                )}
            </div>
            <p className="mt-3 opacity-70 max-w-2xl">
                Adună-te cu alți cititori în jurul unei teme. Fiecare cerc are o conversație proprie și, opțional, o „carte a cercului” pe care o citiți împreună.
            </p>

            {ok && <div className="paper p-4 mt-6" style={{ background: "var(--butter)" }} data-testid="groups-flash-ok">{ok}</div>}

            {mine.length > 0 && (
                <section className="mt-12">
                    <h2 className="font-serif text-3xl mb-5">Cercurile tale</h2>
                    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5" data-testid="my-groups-list">
                        {mine.map((g) => <GroupCard key={g.groupId} g={g} isMember />)}
                    </div>
                </section>
            )}

            <section className="mt-12">
                <h2 className="font-serif text-3xl mb-5">Toate cercurile</h2>
                {groups.length === 0 ? (
                    <div className="paper p-10 text-center">
                        <Sparkles size={20} className="inline opacity-60" />
                        <p className="font-serif text-2xl italic-soft mt-3">Niciun cerc activ încă.</p>
                        <p className="opacity-60 mt-2 text-sm">Fii primul care propune unul.</p>
                    </div>
                ) : (
                    <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5" data-testid="groups-list">
                        {groups.map((g) => <GroupCard key={g.groupId} g={g} isMember={myIds.has(g.groupId)} />)}
                    </div>
                )}
            </section>

            {showForm && (
                <div className="fixed inset-0 z-50 grid place-items-center p-4" style={{ background: "rgba(26,26,26,0.5)" }}>
                    <div className="paper p-7 w-full max-w-lg" data-testid="propose-group-modal">
                        <div className="flex justify-between items-start">
                            <div>
                                <div className="text-xs uppercase tracking-widest opacity-60">Propunere cerc</div>
                                <h3 className="font-serif text-3xl mt-2">Adună cititori în jurul unei idei.</h3>
                            </div>
                            <button onClick={() => setShowForm(false)} aria-label="Închide"><X size={18} /></button>
                        </div>

                        <form onSubmit={submit} className="mt-6 space-y-3">
                            <input
                                required
                                placeholder="Nume cerc (ex: Cercul Eminescu)"
                                value={form.name}
                                onChange={(e) => setForm({ ...form, name: e.target.value })}
                                className="input-cream"
                                data-testid="propose-name"
                            />
                            <input
                                required
                                placeholder="Temă (ex: Poezie românească)"
                                value={form.theme}
                                onChange={(e) => setForm({ ...form, theme: e.target.value })}
                                className="input-cream"
                                data-testid="propose-theme"
                            />
                            <textarea
                                required
                                rows={4}
                                placeholder="Descriere — ce vor discuta membrii? Cum se vor întâlni?"
                                value={form.description}
                                onChange={(e) => setForm({ ...form, description: e.target.value })}
                                className="input-cream"
                                data-testid="propose-description"
                            />
                            {err && <div className="text-sm opacity-80" style={{ color: "var(--rose-2)" }}>{err}</div>}
                            <button
                                type="submit"
                                disabled={busy}
                                className="btn btn-primary w-full justify-center"
                                data-testid="propose-submit"
                            >
                                {busy ? <Loader2 size={14} className="animate-spin" /> : <Sparkles size={14} />}
                                Trimite spre aprobare
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {!user && (
                <div className="paper p-5 mt-10 text-sm opacity-80">
                    Vrei să propui un cerc sau să te alături? <Link to="/autentificare" className="underline">Loghează-te</Link>.
                </div>
            )}
        </main>
    );
}

function GroupCard({ g, isMember }) {
    return (
        <Link
            to={`/cercuri/${g.groupId}`}
            className="paper paper-hover p-5 flex flex-col h-full"
            data-testid={`group-card-${g.groupId}`}
        >
            <div className="flex items-center justify-between">
                <span className="chip chip-physical">{g.theme}</span>
                {isMember && <span className="chip chip-digital">Membru</span>}
            </div>
            <h3 className="font-serif text-2xl mt-3 leading-tight">{g.name}</h3>
            <p className="mt-2 text-sm opacity-70 line-clamp-3">{g.description}</p>

            {g.featuredBook && (
                <div className="mt-4 flex items-center gap-3 p-2 rounded-lg" style={{ background: "var(--cream-2)" }}>
                    <div className="w-10 h-14 rounded overflow-hidden shrink-0" style={{ background: "var(--paper)" }}>
                        {g.featuredBook.coverImageURL && <img src={g.featuredBook.coverImageURL} alt="" className="w-full h-full object-cover" />}
                    </div>
                    <div className="text-xs">
                        <div className="uppercase tracking-widest opacity-60">Cartea cercului</div>
                        <div className="font-serif text-sm leading-tight">{g.featuredBook.title}</div>
                    </div>
                </div>
            )}

            <div className="flex items-center gap-4 text-xs opacity-60 mt-auto pt-4">
                <span className="inline-flex items-center gap-1"><Users2 size={12} /> {g.memberCount} membri</span>
                <span className="inline-flex items-center gap-1"><MessageCircle size={12} /> {g.messageCount} mesaje</span>
            </div>
        </Link>
    );
}

function Loader() {
    return <div className="text-center opacity-60 py-20"><Loader2 className="animate-spin inline" /></div>;
}