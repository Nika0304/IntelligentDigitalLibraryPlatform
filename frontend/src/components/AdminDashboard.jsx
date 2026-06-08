import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
    BookOpen, Users, BookMarked, Download, Star, AlertCircle,
    TrendingUp, TrendingDown, Loader2, Activity, Trophy, UserPlus, MessageSquare, ArrowRight
} from "lucide-react";
import { fetchDashboard } from "../lib/api";

const RES_COLORS = {
    CREATED: "#F2D982", CONFIRMED: "#CDD9C3", WAITING: "#F2D982",
    READY_FOR_PICKUP: "#CDD9C3", BORROWED: "#D4937F",
    RETURNED: "#8F9B93", EXPIRED: "#E7B8AA", CANCELLED: "#E7B8AA",
};

const RES_LABELS = {
    CREATED: "Creată", CONFIRMED: "Confirmată", WAITING: "În așteptare",
    READY_FOR_PICKUP: "Gata pickup", BORROWED: "Împrumutată",
    RETURNED: "Returnată", EXPIRED: "Expirată", CANCELLED: "Anulată",
};

export default function AdminDashboard() {
    const [data, setData] = useState(null);
    const [err, setErr] = useState(null);

    useEffect(() => {
        fetchDashboard().then(setData).catch(() => setErr(true));
    }, []);

    if (err) return <p className="opacity-60 py-10 text-center">Nu am putut încărca dashboard-ul.</p>;
    if (!data) return <div className="text-center py-10 opacity-60"><Loader2 className="animate-spin inline" /></div>;

    const { kpis, monthlyTrends, topBooks, topCategories, topUsers,
        activityFeed, alerts, reservationStatusBreakdown, challengesStats } = data;

    return (
        <div className="space-y-10" data-testid="admin-dashboard">

            {/* ALERTS */}
            {alerts && alerts.length > 0 && (
                <div className="space-y-2" data-testid="dashboard-alerts">
                    {alerts.map((a, i) => (
                        <div key={i}
                             className={`paper p-4 flex items-center gap-3 ${
                                 a.severity === "danger" ? "border-l-4 border-red-500" :
                                     a.severity === "warning" ? "border-l-4 border-yellow-500" :
                                         "border-l-4 border-blue-500"
                             }`}>
                            <AlertCircle size={18} className="opacity-70" />
                            <span className="text-sm">{a.message}</span>
                        </div>
                    ))}
                </div>
            )}

            {/* MAIN KPI CARDS */}
            <div>
                <h3 className="font-serif text-2xl mb-4">Privire de ansamblu</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <KpiBox icon={<BookOpen size={18} />}    label="Cărți"        value={kpis.totalBooks}    accent="bg-[#CDD9C3]" />
                    <KpiBox icon={<Users size={18} />}       label="Utilizatori"  value={kpis.totalUsers}    accent="bg-[#F2D982]" />
                    <KpiBox icon={<BookMarked size={18} />}  label="Rezervări active" value={kpis.activeReservations} accent="bg-[#E7B8AA]" />
                    <KpiBox icon={<Download size={18} />}    label="Descărcări"   value={kpis.totalDownloads} accent="bg-[#D4937F]" />
                    <KpiBox icon={<Star size={18} />}        label="Recenzii"     value={kpis.totalReviews}  accent="bg-[#CDD9C3]" />
                    <KpiBox icon={<BookOpen size={18} />}    label="Exemplare"    value={kpis.totalCopies}   accent="bg-[#F2D982]" />
                    <KpiBox icon={<AlertCircle size={18} />} label="Amenzi PENDING" value={kpis.pendingFines} accent="bg-[#E7B8AA]" />
                    <KpiBox icon={<Trophy size={18} />}      label="Provocări active" value={challengesStats?.active ?? 0} accent="bg-[#D4937F]" />
                </div>
            </div>

            {/* TREND CARDS */}
            {kpis.trends && kpis.trends.length > 0 && (
                <div>
                    <h3 className="font-serif text-2xl mb-4">Evoluția lunii curente</h3>
                    <div className="grid md:grid-cols-3 gap-4">
                        {kpis.trends.map((t, i) => <TrendCard key={i} {...t} />)}
                    </div>
                </div>
            )}

            {/* MONTHLY CHART */}
            {monthlyTrends && monthlyTrends.length > 0 && (
                <div className="paper p-6">
                    <h3 className="font-serif text-2xl mb-2">Activitate lunară · ultimele 12 luni</h3>
                    <p className="text-sm opacity-60 mb-6">Rezervări (rose), descărcări (terracotta) și recenzii (leaf)</p>
                    <MonthlyChart data={monthlyTrends} />
                    <div className="flex gap-6 mt-4 text-xs">
                        <Legend color="#E7B8AA" label="Rezervări" />
                        <Legend color="#D4937F" label="Descărcări" />
                        <Legend color="#CDD9C3" label="Recenzii" />
                    </div>
                </div>
            )}

            <div className="grid lg:grid-cols-2 gap-6">
                {/* TOP BOOKS */}
                <div className="paper p-6">
                    <h3 className="font-serif text-2xl mb-4">Top cărți (după activitate)</h3>
                    {topBooks?.length > 0 ? (
                        <div className="space-y-2">
                            {topBooks.map((b, i) => (
                                <div key={b.bookId} className="flex items-center gap-3 py-2">
                                    <span className="opacity-40 font-serif text-xl w-6">#{i + 1}</span>
                                    {b.cover && (
                                        <Link to={`/carte/${b.bookId}`}>
                                            <img src={b.cover} alt="" className="w-10 h-14 object-cover rounded" />
                                        </Link>
                                    )}
                                    <div className="flex-1 min-w-0">
                                        <Link to={`/carte/${b.bookId}`} className="font-medium hover:underline line-clamp-1">
                                            {b.title}
                                        </Link>
                                        <div className="text-xs opacity-60">{b.category}</div>
                                    </div>
                                    <div className="text-sm font-medium">{b.activityCount}</div>
                                </div>
                            ))}
                        </div>
                    ) : <p className="opacity-60 text-sm">Fără date.</p>}
                </div>

                {/* RESERVATION STATUS DONUT */}
                <div className="paper p-6">
                    <h3 className="font-serif text-2xl mb-4">Stările rezervărilor</h3>
                    {reservationStatusBreakdown?.length > 0 ? (
                        <ReservationDonut segments={reservationStatusBreakdown} />
                    ) : <p className="opacity-60 text-sm">Fără rezervări.</p>}
                </div>

                {/* TOP CATEGORIES */}
                <div className="paper p-6">
                    <h3 className="font-serif text-2xl mb-4">Categorii — popularitate</h3>
                    {topCategories?.length > 0 ? (
                        <div className="space-y-3">
                            {topCategories.map((c) => {
                                const max = Math.max(...topCategories.map(x => x.activityCount), 1);
                                const pct = (c.activityCount / max) * 100;
                                return (
                                    <div key={c.name}>
                                        <div className="flex justify-between text-sm mb-1">
                                            <span>{c.name} <span className="opacity-50 text-xs">({c.bookCount} cărți)</span></span>
                                            <span className="font-medium">{c.activityCount}</span>
                                        </div>
                                        <div className="h-2 rounded-full overflow-hidden" style={{ background: "var(--cream-2)" }}>
                                            <div className="h-full transition-all" style={{ width: `${pct}%`, background: "var(--terracotta, #D4937F)" }} />
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    ) : <p className="opacity-60 text-sm">Fără date.</p>}
                </div>

                {/* TOP USERS */}
                <div className="paper p-6">
                    <h3 className="font-serif text-2xl mb-4">Top utilizatori activi</h3>
                    {topUsers?.length > 0 ? (
                        <div className="space-y-2">
                            {topUsers.map((u, i) => (
                                <div key={u.userId} className="flex items-center gap-3 py-2">
                                    <span className="opacity-40 font-serif text-xl w-6">#{i + 1}</span>
                                    <div className="w-10 h-10 rounded-full grid place-items-center font-serif text-sm"
                                         style={{ background: "var(--cream-2)" }}>
                                        {u.fullName?.[0]}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="font-medium line-clamp-1">{u.fullName}</div>
                                        <div className="text-xs opacity-60">{u.email}</div>
                                    </div>
                                    <div className="text-sm font-medium">{u.activityCount}</div>
                                </div>
                            ))}
                        </div>
                    ) : <p className="opacity-60 text-sm">Fără date.</p>}
                </div>
            </div>

            {/* ACTIVITY FEED */}
            {activityFeed?.length > 0 && (
                <div className="paper p-6">
                    <h3 className="font-serif text-2xl mb-4 flex items-center gap-2">
                        <Activity size={18} /> Activitate recentă
                    </h3>
                    <div className="space-y-1 max-h-[450px] overflow-y-auto pr-2">
                        {activityFeed.map((e, i) => <ActivityRow key={i} event={e} />)}
                    </div>
                </div>
            )}
        </div>
    );
}

/* ============ SUBCOMPONENTE ============ */

function KpiBox({ icon, label, value, accent }) {
    return (
        <div className="paper p-4">
            <div className={`w-9 h-9 ${accent} grid place-items-center mb-3`}
                 style={{ border: "1px solid var(--line)" }}>{icon}</div>
            <div className="font-serif text-3xl leading-none">{value}</div>
            <div className="text-xs uppercase tracking-widest opacity-60 mt-2">{label}</div>
        </div>
    );
}

function TrendCard({ label, value, previous, delta, deltaPct }) {
    const up = delta >= 0;
    return (
        <div className="paper p-5">
            <div className="text-xs uppercase tracking-widest opacity-60">{label}</div>
            <div className="font-serif text-4xl mt-2">{value}</div>
            <div className="flex items-center gap-2 mt-3 text-xs">
                <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full ${
                    up ? "bg-[var(--leaf)]" : "bg-[var(--rose)]"
                }`}>
                    {up ? <TrendingUp size={11} /> : <TrendingDown size={11} />}
                    {Math.abs(deltaPct)}%
                </span>
                <span className="opacity-60">vs luna trecută ({previous})</span>
            </div>
        </div>
    );
}

function MonthlyChart({ data }) {
    const max = Math.max(1, ...data.flatMap(d => [d.reservations, d.downloads, d.reviews]));
    return (
        <div className="flex items-end justify-between gap-2 h-56">
            {data.map((m) => (
                <div key={m.month} className="flex-1 flex flex-col items-center gap-1 group">
                    <div className="text-[10px] opacity-0 group-hover:opacity-100 transition">
                        {m.reservations + m.downloads + m.reviews}
                    </div>
                    <div className="w-full flex-1 flex items-end gap-[2px]">
                        <Bar value={m.reservations} max={max} color="#E7B8AA" />
                        <Bar value={m.downloads} max={max} color="#D4937F" />
                        <Bar value={m.reviews} max={max} color="#CDD9C3" />
                    </div>
                    <div className="text-[10px] opacity-60">{m.monthShort}</div>
                </div>
            ))}
        </div>
    );
}

function Bar({ value, max, color }) {
    const h = (value / max) * 100;
    return (
        <div className="flex-1 rounded-t transition-all duration-700"
             style={{
                 height: `${h}%`,
                 minHeight: value > 0 ? "2px" : "1px",
                 background: color,
                 opacity: value > 0 ? 0.9 : 0.15,
             }} />
    );
}

function Legend({ color, label }) {
    return (
        <span className="inline-flex items-center gap-2 opacity-70">
            <span className="w-3 h-3 rounded" style={{ background: color }} /> {label}
        </span>
    );
}

function ReservationDonut({ segments }) {
    const total = segments.reduce((s, x) => s + x.count, 0);
    let acc = 0;
    const enriched = segments.map((s) => {
        const portion = s.count / total;
        const start = acc;
        acc += portion;
        return { ...s, start, end: acc, color: RES_COLORS[s.status] || "#888" };
    });
    const size = 180, r = (size - 30) / 2, cx = size / 2, cy = size / 2;

    return (
        <div className="flex items-center gap-6 flex-wrap">
            <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
                {enriched.map((s, i) => {
                    const a1 = s.start * 2 * Math.PI - Math.PI / 2;
                    const a2 = s.end * 2 * Math.PI - Math.PI / 2;
                    const x1 = cx + r * Math.cos(a1);
                    const y1 = cy + r * Math.sin(a1);
                    const x2 = cx + r * Math.cos(a2);
                    const y2 = cy + r * Math.sin(a2);
                    const largeArc = (s.end - s.start) > 0.5 ? 1 : 0;
                    const d = `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`;
                    return <path key={i} d={d} fill={s.color} opacity={0.9} />;
                })}
                <circle cx={cx} cy={cy} r={r * 0.55} fill="var(--paper)" />
                <text x={cx} y={cy + 5} textAnchor="middle" className="font-serif text-2xl">{total}</text>
            </svg>
            <ul className="space-y-1 text-sm flex-1 min-w-[150px]">
                {enriched.map((s) => (
                    <li key={s.status} className="flex items-center gap-2">
                        <span className="w-3 h-3 rounded-full" style={{ background: s.color }} />
                        <span className="flex-1">{RES_LABELS[s.status] || s.status}</span>
                        <span className="opacity-60">{s.count}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
}

function ActivityRow({ event }) {
    const ICONS = {
        RESERVATION: <BookMarked size={14} />,
        DOWNLOAD:    <Download size={14} />,
        REVIEW:      <MessageSquare size={14} />,
        REGISTER:    <UserPlus size={14} />,
    };
    const COLORS = {
        RESERVATION: "bg-[#E7B8AA]",
        DOWNLOAD:    "bg-[#D4937F]",
        REVIEW:      "bg-[#CDD9C3]",
        REGISTER:    "bg-[#F2D982]",
    };
    const TYPE_LABELS = {
        RESERVATION: "rezervare", DOWNLOAD: "descărcare",
        REVIEW: "recenzie", REGISTER: "înregistrare",
    };
    const when = event.when ? new Date(event.when).toLocaleString("ro-RO") : "";
    return (
        <div className="flex items-center gap-3 py-2 border-b border-opacity-30" style={{ borderColor: "var(--line)" }}>
            <div className={`w-7 h-7 ${COLORS[event.type] || "bg-[var(--cream-2)]"} grid place-items-center rounded`}>
                {ICONS[event.type] || <Activity size={14} />}
            </div>
            <div className="flex-1 min-w-0 text-sm">
                <strong>{event.user}</strong>{" "}
                <span className="opacity-60">a făcut o {TYPE_LABELS[event.type]}</span>{" "}
                {event.book && <span className="italic-soft font-serif">„{event.book}"</span>}
                {event.rating && <span className="ml-1 opacity-60">({event.rating}★)</span>}
            </div>
            <div className="text-xs opacity-50 flex-shrink-0">{when}</div>
        </div>
    );
}