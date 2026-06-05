import React, { useEffect, useRef, useState } from "react";
import { MessageCircle, X, Send, BookOpen, RotateCcw } from "lucide-react";import { Link } from "react-router-dom";
import { chatMatch, chatSubmit, chatMyQuestions, chatFaq } from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function ChatWidget() {
    const { user } = useAuth();
    const [open, setOpen] = useState(false);
    const [messages, setMessages] = useState([
        { role: "bot", text: "Bună! Sunt asistentul Bibliotheca. Pot să te ajut cu rezervări, descărcări sau recomandări de cărți." },
    ]);
    const [input, setInput] = useState("");
    const [busy, setBusy] = useState(false);
    const endRef = useRef(null);
    const [suggestions, setSuggestions] = useState([]);

    useEffect(() => { endRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);

    useEffect(() => {
        if (open && suggestions.length === 0) {
            chatFaq()
                .then((list) => setSuggestions(list.slice(0, 5)))
                .catch(() => {});
        }
    }, [open, suggestions.length]);

    useEffect(() => {
        if (open && user) {
            chatMyQuestions().then((qs) => {
                const answered = qs
                    .filter((q) => q.status === "ANSWERED")
                    .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

                answered.forEach((q) => {
                    setMessages((m) => {
                        if (m.some((x) => x.questionId === q.questionId)) return m;
                        return [
                            ...m,
                            { role: "user", text: q.message, questionId: `${q.questionId}-q` },
                            { role: "bot", text: `Răspuns admin: ${q.answer}`, questionId: q.questionId },
                        ];
                    });
                });
            }).catch(() => {});
        }
    }, [open, user]);

    useEffect(() => {
        setMessages([
            { role: "bot", text: "Bună! Sunt asistentul Bibliotheca. Pot să te ajut cu rezervări, descărcări sau recomandări de cărți." },
        ]);
        setSuggestions([]);
        setInput("");
        setOpen(false);
    }, [user?.userId]);

    const send = async (textArg) => {
        const text = (textArg ?? input).trim();
        if (!text) return;
        setInput("");
        setSuggestions([]);

        setMessages((m) => [...m, { role: "user", text }]);
        setBusy(true);
        try {
            const res = await chatMatch(text);
            if (res.type === "FAQ") {
                setMessages((m) => [...m, { role: "bot", text: res.answer }]);
            } else if (res.type === "BOOKS") {
                setMessages((m) => [...m, { role: "bot", text: "Ți-am găsit aceste cărți:", books: res.books }]);
            } else {
                setMessages((m) => [...m, { role: "bot", text: "Nu am găsit un răspuns potrivit. Vrei să trimit întrebarea către admin?", canEscalate: true, original: text }]);
            }
        } catch {
            setMessages((m) => [...m, { role: "bot", text: "A apărut o eroare. Încearcă din nou." }]);
        } finally { setBusy(false); }
    };

    const escalate = async (original) => {
        if (!user) {
            setMessages((m) => [...m, { role: "bot", text: "Trebuie să fii autentificat ca să trimiți întrebarea adminului." }]);
            return;
        }
        try {
            await chatSubmit(original);
            setMessages((m) => [...m, { role: "bot", text: "Întrebarea ta a fost trimisă. Vei primi răspuns prin notificare + email." }]);
        } catch {
            setMessages((m) => [...m, { role: "bot", text: "Nu am putut trimite întrebarea." }]);
        }
    };

    const resetChat = () => {
        setMessages([
            { role: "bot", text: "Bună! Sunt asistentul Bibliotheca. Pot să te ajut cu rezervări, descărcări sau recomandări de cărți." },
        ]);
        setSuggestions([]);
        setInput("");
        // forțează re-fetch la următoarea deschidere
        chatFaq()
            .then((list) => setSuggestions(list.slice(0, 5)))
            .catch(() => {});
    };


    return (
        <>
            {!open && (
                <button
                    onClick={() => setOpen(true)}
                    data-testid="chat-toggle"
                    className="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full bg-[var(--ink)] text-[var(--paper)] grid place-items-center shadow-lg hover:scale-105 transition"
                    aria-label="Deschide chat"
                >
                    <MessageCircle size={22} />
                </button>
            )}

            {open && (
                <div
                    className="fixed bottom-6 right-6 z-50 w-[360px] max-h-[560px] rounded-2xl shadow-2xl flex flex-col overflow-hidden"
                    style={{ background: "var(--paper)", border: "1px solid var(--line)" }}
                    data-testid="chat-widget"
                >
                    <div className="flex items-center justify-between px-4 py-3" style={{ background: "var(--ink)", color: "var(--paper)" }}>
                        <span className="font-serif italic-soft text-lg">Asistentul Bibliotheca</span>
                        <div className="flex items-center gap-2">
                            <button
                                onClick={resetChat}
                                aria-label="Conversație nouă"
                                title="Începe o conversație nouă"
                                data-testid="chat-reset"
                                className="hover:opacity-70 transition"
                            >
                                <RotateCcw size={16} />
                            </button>
                            <button
                                onClick={() => setOpen(false)}
                                aria-label="Închide"
                                title="Închide chatul"
                                data-testid="chat-close"
                                className="hover:opacity-70 transition"
                            >
                                <X size={18} />
                            </button>
                        </div>
                    </div>

                    <div className="flex-1 overflow-y-auto px-3 py-3 space-y-3 text-sm">
                        {messages.map((m, i) => (
                            <div key={i} className={`flex ${m.role === "user" ? "justify-end" : "justify-start"}`}>
                                <div className={`max-w-[80%] px-3 py-2 rounded-2xl ${m.role === "user" ? "bg-[var(--ink)] text-[var(--paper)]" : "bg-[var(--cream-2)]"}`}>
                                    <div>{m.text}</div>
                                    {m.books && (
                                        <div className="mt-2 space-y-2">
                                            {m.books.map((b) => (
                                                <Link
                                                    key={b.bookId}
                                                    to={`/carte/${b.bookId}`}
                                                    onClick={() => setOpen(false)}
                                                    className="flex gap-2 items-center bg-[var(--paper)] rounded-lg p-2 hover:opacity-80"
                                                >
                                                    <div className="w-8 h-10 rounded overflow-hidden bg-[var(--cream-2)] shrink-0">
                                                        {b.coverImageURL && <img src={b.coverImageURL} alt="" className="w-full h-full object-cover" />}
                                                    </div>
                                                    <div className="text-xs flex-1">
                                                        <div className="font-serif text-sm leading-tight">{b.title}</div>
                                                        <div className="opacity-60">{b.categoryName}</div>
                                                    </div>
                                                    <BookOpen size={14} className="opacity-50" />
                                                </Link>
                                            ))}
                                        </div>
                                    )}
                                    {m.canEscalate && (
                                        <button
                                            onClick={() => escalate(m.original)}
                                            data-testid="chat-escalate"
                                            className="mt-2 underline text-xs opacity-80 hover:opacity-100"
                                        >
                                            → Trimite întrebarea către admin
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                        {suggestions.length > 0 && messages.length === 1 && (
                            <div className="flex flex-wrap gap-2 px-1" data-testid="chat-suggestions">
                                {suggestions.map((s) => (
                                    <button
                                        key={s.faqId}
                                        onClick={() => send(s.question)}
                                        className="text-xs px-3 py-1.5 rounded-full transition hover:opacity-80"
                                        style={{
                                            background: "var(--cream-2)",
                                            border: "1px solid var(--line)",
                                        }}
                                        data-testid={`chat-suggestion-${s.faqId}`}
                                    >
                                        {s.question}
                                    </button>
                                ))}
                            </div>
                        )}
                        <div ref={endRef} />
                    </div>

                    <div className="border-t flex items-center gap-2 p-2" style={{ borderColor: "var(--line)" }}>
                        <input
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && send()}
                            placeholder="Scrie o întrebare..."
                            data-testid="chat-input"
                            className="flex-1 px-3 py-2 text-sm bg-transparent outline-none"
                            disabled={busy}
                        />
                        <button onClick={() => send()} disabled={busy} data-testid="chat-send" className="p-2 hover:opacity-70">
                            <Send size={16} />
                        </button>
                    </div>
                </div>
            )}
        </>
    );
}