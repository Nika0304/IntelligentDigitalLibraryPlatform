import React, { useEffect, useRef, useState } from "react";
import { useParams, Link, Navigate } from "react-router-dom";
import {
    ArrowLeft, Users2, Send, Trash2, BookOpen, Loader2,
    LogOut, LogIn, Bell, BellOff, Vote, Crown,
} from "lucide-react";
import {
    fetchGroupDetails,
    fetchGroupMessages,
    postGroupMessage,
    deleteGroupMessage,
    joinGroup,
    leaveGroup,
    fetchBooks,
    setFeaturedBook,
    castGroupVote,
    fetchGroupVote,
    applyVoteWinner,
    toggleGroupMute,
} from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function GroupDetail() {
    const { id } = useParams();
    const { user, isAdmin } = useAuth();
    const [g, setG] = useState(null);
    const [msgs, setMsgs] = useState([]);
    const [input, setInput] = useState("");
    const [busy, setBusy] = useState(false);
    const [showBookPicker, setShowBookPicker] = useState(false);
    const [pickerMode, setPickerMode] = useState("featured");
    const [books, setBooks] = useState([]);
    const [tally, setTally] = useState({ ranking: [], totalVotes: 0, myVote: "", period: "" });
    const endRef = useRef(null);
    const prevCount = useRef(0);

    const loadDetails = () => fetchGroupDetails(id).then(setG).catch(() => setG(false));
    const loadMessages = () => {
        if (!user || (g && !g.isMember && !isAdmin)) {
            setMsgs([]);
            return;
        }
        fetchGroupMessages(id).then(setMsgs).catch(() => {});
    };
    const loadTally = () => fetchGroupVote(id).then(setTally).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
    useEffect(() => {
        loadDetails();
        loadMessages();
        loadTally();
        const i = setInterval(loadMessages, 5000);
        return () => clearInterval(i);
    }, [id, user?.userId, g?.isMember]);

    useEffect(() => {
        if (msgs.length > prevCount.current && prevCount.current > 0) {
            endRef.current?.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "nearest" });
        }
        prevCount.current = msgs.length;
    }, [msgs]);

    if (g === null) return <Loader />;
    if (g === false) return <Navigate to="/cercuri" replace />;

    const send = async (e) => {
        e?.preventDefault();
        if (!input.trim()) return;
        setBusy(true);
        try {
            await postGroupMessage(id, input);
            setInput("");
            loadMessages();
        } catch (e) {
            alert(e.response?.data || "Mesajul nu a putut fi trimis.");
        } finally { setBusy(false); }
    };

    const handleJoin = async () => {
        try { await joinGroup(id); loadDetails(); }
        catch (e) { alert(e.response?.data || "Eroare la alăturare."); }
    };

    const handleLeave = async () => {
        if (!window.confirm("Sigur părăsești cercul?")) return;
        try { await leaveGroup(id); loadDetails(); }
        catch (e) { alert(e.response?.data || "Eroare."); }
    };

    const handleDelete = async (mid) => {
        if (!window.confirm("Ștergi mesajul?")) return;
        try { await deleteGroupMessage(mid); loadMessages(); }
        catch (e) { alert(e.response?.data || "Nu am putut șterge."); }
    };

    const toggleMute = async () => {
        try { await toggleGroupMute(id); loadDetails(); }
        catch (e) { alert(e.response?.data || "Eroare."); }
    };

    const openBookPicker = (mode = "vote") => {
        setPickerMode(mode);
        setShowBookPicker(true);
        if (books.length === 0) fetchBooks().then(setBooks).catch(() => {});
    };

    const pickBook = async (bookId) => {
        try {
            if (pickerMode === "featured") {
                await setFeaturedBook(id, bookId);
                loadDetails();
            } else {
                await castGroupVote(id, bookId);
                loadTally();
            }
            setShowBookPicker(false);
        } catch (e) {
            alert(e.response?.data || "Eroare.");
        }
    };

    const applyWinner = async () => {
        if (!window.confirm("Aplici cartea cu cele mai multe voturi ca „Cartea cercului”?")) return;
        try { await applyVoteWinner(id); loadDetails(); loadTally(); }
        catch (e) { alert(e.response?.data || "Eroare."); }
    };

    return (
        <main className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24" data-testid="group-detail">
            <Link to="/cercuri" className="text-xs opacity-60 inline-flex items-center gap-1 hover:opacity-100">
                <ArrowLeft size={12} /> Înapoi la cercuri
            </Link>

            <div className="grid lg:grid-cols-3 gap-8 mt-4">
                <div className="lg:col-span-2">
                    <div className="flex items-start justify-between gap-4 flex-wrap">
                        <div>
                            <span className="chip chip-physical">{g.theme}</span>
                            <h1 className="font-serif text-4xl lg:text-5xl mt-3">{g.name}</h1>
                            <p className="mt-2 text-sm opacity-70">Moderat de {g.creator?.fullName}</p>
                        </div>
                        <div className="flex items-center gap-2 flex-wrap">
                            {user && g.isMember && (
                                <button
                                    onClick={toggleMute}
                                    className="btn btn-ghost !text-sm"
                                    title={g.muted ? "Activează notificările" : "Oprește notificările"}
                                    data-testid="toggle-mute"
                                >
                                    {g.muted ? <BellOff size={14} /> : <Bell size={14} />}
                                    {g.muted ? "Mute activ" : "Notificări"}
                                </button>
                            )}
                            {user && (
                                g.isMember ? (
                                    <button onClick={handleLeave} className="btn btn-ghost !text-sm" data-testid="leave-group">
                                        <LogOut size={12} /> Părăsește
                                    </button>
                                ) : (
                                    <button onClick={handleJoin} className="btn btn-primary" data-testid="join-group">
                                        <LogIn size={14} /> Alătură-te
                                    </button>
                                )
                            )}
                        </div>
                    </div>

                    <p className="mt-5 font-serif italic-soft text-lg opacity-80">{g.description}</p>

                    {g.featuredBook && (
                        <div className="paper p-5 mt-6 flex items-center gap-4" style={{ background: "var(--butter)" }}>
                            <div className="w-14 h-20 rounded overflow-hidden shrink-0" style={{ background: "var(--paper)" }}>
                                {g.featuredBook.coverImageURL && (
                                    <img src={g.featuredBook.coverImageURL} alt="" className="w-full h-full object-cover" />
                                )}
                            </div>
                            <div className="flex-1">
                                <div className="text-xs uppercase tracking-widest opacity-60">Cartea cercului</div>
                                <Link to={`/carte/${g.featuredBook.bookId}`} className="font-serif text-xl hover:underline">
                                    {g.featuredBook.title}
                                </Link>
                            </div>
                            {g.isModerator && (
                                <button
                                    onClick={() => openBookPicker("featured")}
                                    className="btn btn-ghost !text-xs"
                                    data-testid="change-featured-book"
                                >
                                    Schimbă
                                </button>
                            )}
                        </div>
                    )}

                    {!g.featuredBook && g.isModerator && (
                        <button
                            onClick={() => openBookPicker("featured")}
                            className="btn btn-secondary mt-6"
                            data-testid="set-featured-book"
                        >
                            <BookOpen size={14} /> Setează cartea cercului
                        </button>
                    )}

                    {g.isMember && (
                        <div className="paper p-5 mt-6" data-testid="vote-section">
                            <div className="flex items-center justify-between gap-3 flex-wrap">
                                <div className="flex items-center gap-2">
                                    <Vote size={16} />
                                    <h3 className="font-serif text-xl">Vot pentru cartea cercului</h3>
                                </div>
                                {g.isModerator && tally.totalVotes > 0 && (
                                    <button
                                        onClick={applyWinner}
                                        className="btn btn-primary !text-xs"
                                        data-testid="apply-winner"
                                    >
                                        <Crown size={12} /> Aplică câștigătorul
                                    </button>
                                )}
                            </div>
                            <p className="text-xs opacity-60 mt-1">
                                Perioada {tally.period} · {tally.totalVotes} voturi
                            </p>

                            {tally.ranking.length === 0 ? (
                                <p className="text-sm opacity-60 mt-4">Niciun vot încă. Alege o carte mai jos.</p>
                            ) : (
                                <ul className="mt-4 space-y-2">
                                    {tally.ranking.map((r) => (
                                        <li key={r.bookId} className="flex items-center gap-3">
                                            <div className="w-8 h-12 rounded overflow-hidden" style={{ background: "var(--cream-2)" }}>
                                                {r.coverImageURL && (
                                                    <img src={r.coverImageURL} alt="" className="w-full h-full object-cover" />
                                                )}
                                            </div>
                                            <div className="flex-1">
                                                <div className="text-sm font-serif">{r.title}</div>
                                                <div className="h-1 mt-1 rounded-full overflow-hidden" style={{ background: "var(--cream-2)" }}>
                                                    <div
                                                        style={{
                                                            width: `${(r.votes / tally.totalVotes) * 100}%`,
                                                            height: "100%",
                                                            background: "var(--ink)",
                                                        }}
                                                    />
                                                </div>
                                            </div>
                                            <span className="text-sm opacity-70 w-10 text-right">{r.votes}</span>
                                        </li>
                                    ))}
                                </ul>
                            )}

                            <button
                                onClick={() => openBookPicker("vote")}
                                className="btn btn-secondary !text-xs mt-4"
                                data-testid="open-vote-picker"
                            >
                                <BookOpen size={12} /> {tally.myVote ? "Schimbă votul" : "Votează o carte"}
                            </button>
                        </div>
                    )}

                    <div className="paper mt-8 flex flex-col" style={{ minHeight: 400 }} data-testid="group-chat">
                        <div className="px-5 py-3 border-b font-serif text-lg" style={{ borderColor: "var(--line)" }}>
                            Discuții
                        </div>

                        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-3" style={{ maxHeight: 500 }}>
                            {(!user || (!g.isMember && !isAdmin)) ? (
                                <div className="text-center py-10">
                                    <p className="font-serif italic-soft text-xl opacity-80">Conversație privată</p>
                                    <p className="text-sm opacity-60 mt-2">
                                        {user
                                            ? "Alătură-te cercului ca să vezi discuțiile membrilor."
                                            : "Loghează-te și alătură-te cercului pentru a vedea conversația."}
                                    </p>
                                </div>
                            ) : msgs.length === 0 ? (
                                <p className="text-center opacity-60 py-10 font-serif italic-soft">
                                    Începe conversația.
                                </p>
                            ) : (
                                <>
                                    {msgs.map((m) => {
                                        const isMine = user && m.user?.userId === user.userId;
                                        const canDelete = user && (isMine || g.isModerator || isAdmin);
                                        return (
                                            <div
                                                key={m.messageId}
                                                className={`flex ${isMine ? "justify-end" : "justify-start"}`}
                                            >
                                                <div
                                                    className={`max-w-[75%] px-4 py-2 rounded-2xl ${
                                                        isMine ? "bg-[var(--ink)] text-[var(--paper)]" : "bg-[var(--cream-2)]"
                                                    }`}
                                                >
                                                    <div className="text-xs opacity-70 mb-1">
                                                        {m.user?.fullName} ·{" "}
                                                        {new Date(m.createdAt).toLocaleTimeString("ro-RO", {
                                                            hour: "2-digit",
                                                            minute: "2-digit",
                                                        })}
                                                    </div>
                                                    <div className={`text-sm ${m.deleted ? "italic opacity-50" : ""}`}>
                                                        {m.content}
                                                    </div>
                                                    {canDelete && !m.deleted && (
                                                        <button
                                                            onClick={() => handleDelete(m.messageId)}
                                                            className="text-xs opacity-50 hover:opacity-100 mt-1"
                                                            data-testid={`delete-msg-${m.messageId}`}
                                                        >
                                                            <Trash2 size={10} className="inline" /> șterge
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                        );
                                    })}
                                    <div ref={endRef} />
                                </>
                            )}
                        </div>

                        {user && g.isMember && (
                            <form
                                onSubmit={send}
                                className="border-t flex gap-2 p-3"
                                style={{ borderColor: "var(--line)" }}
                            >
                                <input
                                    value={input}
                                    onChange={(e) => setInput(e.target.value)}
                                    placeholder="Scrie un mesaj..."
                                    disabled={busy}
                                    className="flex-1 px-3 py-2 text-sm bg-transparent outline-none"
                                    data-testid="group-msg-input"
                                />
                                <button
                                    type="submit"
                                    disabled={busy}
                                    className="btn btn-primary !text-sm"
                                    data-testid="group-msg-send"
                                >
                                    <Send size={12} /> Trimite
                                </button>
                            </form>
                        )}

                        {user && !g.isMember && (
                            <div
                                className="border-t p-4 text-center text-sm opacity-70"
                                style={{ borderColor: "var(--line)" }}
                            >
                                Alătură-te cercului pentru a posta mesaje.
                            </div>
                        )}

                        {!user && (
                            <div
                                className="border-t p-4 text-center text-sm opacity-70"
                                style={{ borderColor: "var(--line)" }}
                            >
                                <Link to="/autentificare" className="underline">
                                    Loghează-te
                                </Link>{" "}
                                pentru a participa la discuție.
                            </div>
                        )}
                    </div>
                </div>

                <aside className="paper p-5 h-fit">
                    <h3 className="font-serif text-xl flex items-center gap-2">
                        <Users2 size={16} /> Membri ({g.members?.length || 0})
                    </h3>
                    <ul className="mt-4 space-y-2">
                        {g.members?.map((m) => (
                            <li key={m.userId} className="flex items-center gap-2 text-sm">
                                <div
                                    className="w-8 h-8 rounded-full grid place-items-center text-xs"
                                    style={{
                                        background: m.role === "MODERATOR" ? "var(--ink)" : "var(--cream-2)",
                                        color: m.role === "MODERATOR" ? "var(--paper)" : "var(--ink)",
                                    }}
                                >
                                    {m.fullName?.[0]}
                                </div>
                                <span className="flex-1">{m.fullName}</span>
                                {m.role === "MODERATOR" && (
                                    <span className="text-xs opacity-60">moderator</span>
                                )}
                            </li>
                        ))}
                    </ul>
                </aside>
            </div>

            {showBookPicker && (
                <div
                    className="fixed inset-0 z-50 grid place-items-center p-4"
                    style={{ background: "rgba(26,26,26,0.5)" }}
                >
                    <div
                        className="paper p-6 w-full max-w-2xl max-h-[80vh] overflow-y-auto"
                        data-testid="book-picker"
                    >
                        <h3 className="font-serif text-2xl mb-1">
                            {pickerMode === "featured" ? "Alege cartea cercului" : "Votează o carte"}
                        </h3>
                        <p className="text-sm opacity-60 mb-4">
                            {pickerMode === "featured"
                                ? "Cartea va fi afișată ca focus curent al cercului."
                                : "Votul tău contează pentru perioada curentă. Poți schimba opțiunea oricând."}
                        </p>
                        <div className="grid sm:grid-cols-2 gap-3">
                            {books.map((b) => (
                                <button
                                    key={b.bookId}
                                    onClick={() => pickBook(b.bookId)}
                                    className="paper paper-hover p-3 flex gap-3 items-center text-left"
                                    data-testid={`picker-book-${b.bookId}`}
                                >
                                    <div
                                        className="w-12 h-16 rounded overflow-hidden shrink-0"
                                        style={{ background: "var(--cream-2)" }}
                                    >
                                        {b.coverImageURL && (
                                            <img
                                                src={b.coverImageURL}
                                                className="w-full h-full object-cover"
                                                alt=""
                                            />
                                        )}
                                    </div>
                                    <div className="text-sm">
                                        <div className="font-serif">{b.title}</div>
                                        <div className="text-xs opacity-60">{b.authors?.join(", ")}</div>
                                    </div>
                                </button>
                            ))}
                        </div>
                        <button
                            onClick={() => setShowBookPicker(false)}
                            className="btn btn-secondary mt-4 w-full justify-center"
                        >
                            Anulează
                        </button>
                    </div>
                </div>
            )}
        </main>
    );
}

function Loader() {
    return (
        <div className="text-center opacity-60 py-20">
            <Loader2 className="animate-spin inline" />
        </div>
    );
}