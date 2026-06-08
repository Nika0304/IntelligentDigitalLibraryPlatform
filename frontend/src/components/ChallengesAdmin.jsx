import React, { useEffect, useState } from "react";
import { Loader2, Plus, X, Check, Trash2 } from "lucide-react";
import {
    fetchChallenges, createChallenge, updateChallenge, archiveChallenge,
    fetchCategories, fetchAuthors,
} from "../lib/api";

const TYPES = [
    { id: "ANY_READ",           label: "Orice citire" },
    { id: "READ_FROM_CATEGORY", label: "Dintr-o categorie" },
    { id: "READ_FROM_AUTHOR",   label: "De la un autor" },
    { id: "READ_DIGITAL",       label: "Cărți digitale" },
    { id: "READ_PHYSICAL",      label: "Cărți fizice" },
    { id: "WRITE_REVIEWS",      label: "Recenzii scrise" },
];

const EMPTY = {
    title: "",
    description: "",
    type: "ANY_READ",
    targetCount: 5,
    categoryId: "",
    authorId: "",
    startDate: "",
    endDate: "",
    iconEmoji: "🎯",
};

export default function ChallengesAdmin() {
    const [items, setItems] = useState(null);
    const [cats, setCats] = useState([]);
    const [authors, setAuthors] = useState([]);
    const [form, setForm] = useState(EMPTY);
    const [showForm, setShowForm] = useState(false);
    const [busy, setBusy] = useState(false);
    const [err, setErr] = useState(null);

    const load = () => fetchChallenges().then(setItems).catch(() => setItems([]));
    useEffect(() => {
        load();
        fetchCategories().then(setCats).catch(() => {});
        fetchAuthors().then(setAuthors).catch(() => {});
    }, []);

    const submit = async (e) => {
        e.preventDefault();
        setErr(null);
        setBusy(true);
        try {
            const payload = {
                ...form,
                targetCount: Number(form.targetCount),
                categoryId: form.categoryId ? Number(form.categoryId) : null,
                authorId: form.authorId ? Number(form.authorId) : null,
                startDate: form.startDate + "T00:00:00",
                endDate: form.endDate + "T23:59:59",
            };
            await createChallenge(payload);
            setForm(EMPTY);
            setShowForm(false);
            load();
        } catch (e) {
            setErr(e.response?.data || "Eroare la creare");
        } finally {
            setBusy(false);
        }
    };

    const archive = async (id) => {
        if (!window.confirm("Sigur arhivezi provocarea?")) return;
        await archiveChallenge(id);
        load();
    };

    if (items === null) return <div className="text-center py-10 opacity-60"><Loader2 className="animate-spin inline" /></div>;

    return (
        <div data-testid="admin-challenges">
            <div className="flex items-center justify-between mb-6">
                <h3 className="font-serif text-2xl">Provocări de lectură</h3>
                <button
                    onClick={() => setShowForm((v) => !v)}
                    className="btn btn-primary !text-sm"
                    data-testid="admin-new-challenge"
                >
                    {showForm ? <><X size={14} /> Anulează</> : <><Plus size={14} /> Nouă</>}
                </button>
            </div>

            {showForm && (
                <form onSubmit={submit} className="paper p-6 mb-6 space-y-4" data-testid="admin-challenge-form">
                    <div className="grid md:grid-cols-2 gap-4">
                        <Field label="Titlu">
                            <input
                                required
                                value={form.title}
                                onChange={(e) => setForm({ ...form, title: e.target.value })}
                                className="input-cream"
                                placeholder="Ex: Lectură de toamnă"
                                data-testid="challenge-form-title"
                            />
                        </Field>
                        <Field label="Emoji">
                            <input
                                value={form.iconEmoji}
                                onChange={(e) => setForm({ ...form, iconEmoji: e.target.value })}
                                className="input-cream !w-24 text-center text-2xl"
                                maxLength={4}
                            />
                        </Field>
                    </div>

                    <Field label="Descriere">
                        <textarea
                            value={form.description}
                            onChange={(e) => setForm({ ...form, description: e.target.value })}
                            className="input-cream"
                            rows={3}
                            placeholder="Descrie pe scurt provocarea…"
                        />
                    </Field>

                    <div className="grid md:grid-cols-3 gap-4">
                        <Field label="Tip">
                            <select
                                value={form.type}
                                onChange={(e) => setForm({ ...form, type: e.target.value, categoryId: "", authorId: "" })}
                                className="input-cream"
                                data-testid="challenge-form-type"
                            >
                                {TYPES.map((t) => <option key={t.id} value={t.id}>{t.label}</option>)}
                            </select>
                        </Field>
                        <Field label="Țintă (nr. cărți / recenzii)">
                            <input
                                type="number" min="1" required
                                value={form.targetCount}
                                onChange={(e) => setForm({ ...form, targetCount: e.target.value })}
                                className="input-cream"
                            />
                        </Field>
                        {form.type === "READ_FROM_CATEGORY" && (
                            <Field label="Categorie">
                                <select
                                    value={form.categoryId}
                                    onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                                    className="input-cream" required
                                >
                                    <option value="">— alege —</option>
                                    {cats.map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
                                </select>
                            </Field>
                        )}
                        {form.type === "READ_FROM_AUTHOR" && (
                            <Field label="Autor">
                                <select
                                    value={form.authorId}
                                    onChange={(e) => setForm({ ...form, authorId: e.target.value })}
                                    className="input-cream" required
                                >
                                    <option value="">— alege —</option>
                                    {authors.map((a) => <option key={a.authorId} value={a.authorId}>{a.name}</option>)}
                                </select>
                            </Field>
                        )}
                    </div>

                    <div className="grid md:grid-cols-2 gap-4">
                        <Field label="Start">
                            <input
                                type="date" required
                                value={form.startDate}
                                onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                                className="input-cream"
                            />
                        </Field>
                        <Field label="Sfârșit">
                            <input
                                type="date" required
                                value={form.endDate}
                                onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                                className="input-cream"
                            />
                        </Field>
                    </div>

                    {err && (
                        <div className="text-sm p-3 rounded" style={{ background: "var(--rose)" }}>
                            ⚠️ {String(err)}
                        </div>
                    )}

                    <button type="submit" disabled={busy} className="btn btn-primary" data-testid="challenge-form-submit">
                        {busy ? "Se creează…" : <><Check size={14} /> Creează provocarea</>}
                    </button>
                </form>
            )}

            {items.length === 0 ? (
                <p className="opacity-60 text-center py-10">Nicio provocare creată încă.</p>
            ) : (
                <div className="grid gap-3" data-testid="admin-challenges-list">
                    {items.map((c) => (
                        <div key={c.challengeId} className="paper p-4 flex items-center gap-4">
                            <div className="w-12 h-12 grid place-items-center text-2xl rounded"
                                 style={{ background: "var(--butter)" }}>
                                {c.iconEmoji || "🎯"}
                            </div>
                            <div className="flex-1">
                                <div className="font-medium">{c.title}</div>
                                <div className="text-xs opacity-60 mt-1">
                                    {c.type} · țintă {c.targetCount} · {c.participantsCount} înscriși
                                    {c.categoryName && ` · ${c.categoryName}`}
                                    {c.authorName && ` · ${c.authorName}`}
                                </div>
                            </div>
                            <button
                                onClick={() => archive(c.challengeId)}
                                className="btn btn-ghost !text-xs !text-red-600"
                                data-testid={`admin-archive-${c.challengeId}`}
                            >
                                <Trash2 size={12} /> Arhivează
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

function Field({ label, children }) {
    return (
        <label className="block">
            <span className="text-xs uppercase tracking-widest opacity-60 mb-1.5 block">{label}</span>
            {children}
        </label>
    );
}