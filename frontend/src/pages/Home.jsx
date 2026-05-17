import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Search, BookMarked, Download, Sparkles, Bell, BarChart3, Quote } from "lucide-react";
import BookCard from "../components/BookCard";
import { fetchBooks, fetchStats } from "../lib/api";

const AUTHORS = [
  "Liviu Rebreanu", "Marin Preda", "Aristotel", "Stephen Hawking", "Mircea Eliade",
  "Yuval Noah Harari", "Immanuel Kant", "Donald Knuth", "Nicolae Iorga",
  "Emil Cioran", "Mihai Eminescu", "Ion Creangă", "Lucian Blaga",
];

export default function Home() {
  const [books, setBooks] = useState([]);
  const [stats, setStats] = useState(null);

  useEffect(() => {
    fetchBooks().then(setBooks).catch(() => {});
    fetchStats().then(setStats).catch(() => {});
  }, []);

  const featured = books.slice(0, 8);
  const heroImages = books.filter((b) => b.coverImageURL).slice(0, 6);

  return (
    <main className="grain" data-testid="home-page">
      {/* HERO */}
      <section className="max-w-[1400px] mx-auto px-6 lg:px-10 pt-6 pb-24 relative">
        <div className="grid lg:grid-cols-2 gap-12 lg:gap-6 items-start">
          <div className="reveal">
            <span className="eyebrow" data-testid="hero-eyebrow">Bibliotecă digitală inteligentă · v2.0</span>
            {/*<h1 className="display text-[3.2rem] sm:text-[4.5rem] lg:text-[5.6rem] mt-8" data-testid="hero-title">*/}
            {/*  Toate <span className="italic-soft">cărțile</span>,*/}
            {/*  <br />*/}
            {/*  într-un singur <span className="pill-rose">raft</span>*/}
            {/*  <span className="font-serif">.</span>*/}
            {/*</h1>*/}
              <h1
                  className="font-serif-display text-[56px] sm:text-7xl lg:text-[112px] leading-[0.88] tracking-tight text-ink word-reveal mt-8"
                  data-testid="hero-title"
              >
              <span style={{ animationDelay: "0.1s" }}>
                Toate
              </span>
                              <br />

                              <span
                                  style={{ animationDelay: "0.25s" }}
                                  className="relative inline-block"
                              >
                <em className="italic font-normal">cărțile</em>
                <svg
                    className="absolute -bottom-2 left-0 w-full h-3 text-terracotta draw-line"
                    viewBox="0 0 200 12"
                    preserveAspectRatio="none"
                    fill="none"
                >
                  <path
                      d="M2 8 Q 50 2, 100 6 T 198 4"
                      stroke="currentColor"
                      strokeWidth="2.5"
                      strokeLinecap="round"
                  />
                </svg>
              </span>

                              <span style={{ animationDelay: "0.4s" }}>,</span>
                              <br />

                              <span style={{ animationDelay: "0.55s" }}>
                într-un
              </span>{" "}

                              <span style={{ animationDelay: "0.7s" }}>
                singur
              </span>{" "}

                              <span
                                  style={{ animationDelay: "0.85s" }}
                                  className="bg-rose px-3 -mx-1 inline-block"
                              >
                raft
              </span>

                              <span
                                  style={{ animationDelay: "1s" }}
                                  className="text-ink-muted"
                              >
                .
              </span>
              </h1>
            <p className="mt-8 max-w-xl text-[1.05rem] opacity-80 leading-relaxed">
              O platformă hibridă care unește exemplarul tipărit cu PDF-ul descărcabil.
              Caută, rezervă, citește — apoi lasă recomandările să-ți spună ce urmează.
            </p>
            <div className="mt-10 flex flex-wrap items-center gap-3">
              <Link to="/catalog" className="btn btn-primary" data-testid="hero-cta-catalog">
                Explorează catalogul <ArrowRight size={16} />
              </Link>
              <Link to="/inregistrare" className="btn btn-secondary" data-testid="hero-cta-register">
                Creează un cont
              </Link>
            </div>

            <div className="mt-16 grid grid-cols-3 max-w-md gap-4">
              <Stat label="Volume" value={(stats?.totalBooks || 0).toLocaleString("ro-RO")} testid="stat-books" />
              <Stat label="Cititori" value={(stats?.totalUsers || 0).toLocaleString("ro-RO")} testid="stat-users" />
              <Stat label="Disponibilitate" value={stats ? `${Math.round(100 * (stats.availableCopies / Math.max(stats.totalCopies, 1)))}%` : "—"} testid="stat-avail" />
            </div>
          </div>

          <HeroCollage images={heroImages} />
        </div>
      </section>

      {/* Marquee authors */}
      <section className="overflow-hidden border-y" style={{ borderColor: "var(--line)", background: "var(--paper)" }} data-testid="marquee">
        <div className="py-5">
          <div className="marquee">
            {[...AUTHORS, ...AUTHORS].map((a, i) => (
              <span key={i} className="font-serif italic-soft text-2xl opacity-80">{a} · </span>
            ))}
          </div>
        </div>
      </section>

      {/*/!* Visitor card + features *!/*/}
      {/*<section className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-24" data-testid="features">*/}
      {/*  <div className="grid lg:grid-cols-12 gap-8">*/}
      {/*    <div className="lg:col-span-4 paper p-8">*/}
      {/*      <div className="text-xs uppercase tracking-widest opacity-60">Pentru tine · Vizitator</div>*/}
      {/*      <h2 className="font-serif text-3xl mt-3">Răsfoiește catalogul fără cont.</h2>*/}
      {/*      <p className="mt-4 text-sm opacity-80">*/}
      {/*        Descoperă peste {(stats?.totalBooks || 12000).toLocaleString("ro-RO")} de titluri, vezi recenzii și ratinguri.*/}
      {/*        Pentru rezervări, descărcări și liste de dorințe, creează un cont gratuit.*/}
      {/*      </p>*/}
      {/*      <ul className="mt-5 space-y-2 text-sm opacity-90">*/}
      {/*        <li>— Catalog complet vizibil</li>*/}
      {/*        <li>— Detalii și recenzii cărți</li>*/}
      {/*        <li>— Recomandări săptămânale publice</li>*/}
      {/*      </ul>*/}
      {/*      <Link to="/inregistrare" className="btn btn-primary mt-7" data-testid="features-cta">Creează cont <ArrowRight size={14} /></Link>*/}
      {/*    </div>*/}
      {/*    <div className="lg:col-span-8 grid sm:grid-cols-2 gap-5">*/}
      {/*      <Feature icon={<Search size={18} />} title="Catalog hibrid" desc="Cărți fizice și digitale într-un singur catalog inteligent." />*/}
      {/*      <Feature icon={<BookMarked size={18} />} title="Rezervări fizice" desc="Verificare automată a stocului și listă FIFO de așteptare." />*/}
      {/*      <Feature icon={<Download size={18} />} title="Descărcări PDF" desc="Acces instant la versiunile digitale, cu istoric complet." />*/}
      {/*      <Feature icon={<Sparkles size={18} />} title="Recomandări personalizate" desc="Algoritm bazat pe istoricul tău de lectură." />*/}
      {/*      <Feature icon={<Bell size={18} />} title="Notificări inteligente" desc="Reamintiri pentru returnări și alerte de disponibilitate." />*/}
      {/*      <Feature icon={<BarChart3 size={18} />} title="Statistici pentru bibliotecari" desc="Panou administrativ cu rapoarte și analize de utilizare." />*/}
      {/*    </div>*/}
      {/*  </div>*/}
      {/*</section>*/}
        {/* Visitor card + features */}
        <section
            className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-24"
            data-testid="features"
        >
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
                {/* Left visitor card */}
                <div className="lg:col-span-5 relative overflow-hidden rounded-[28px] border border-[var(--line)] bg-[var(--paper)] p-10 lg:p-12 min-h-[560px]">
                    <Search
                        className="absolute top-12 right-10 w-32 h-32 text-[var(--ink)] opacity-10"
                        strokeWidth={1}
                    />

                    <div className="text-xs uppercase tracking-[0.25em] opacity-50">
                        Pentru tine · Vizitator
                    </div>

                    <h2 className="font-serif text-4xl lg:text-5xl leading-[1.05] mt-7 max-w-md">
                        Răsfoiește catalogul fără cont
                    </h2>

                    <p className="mt-8 text-base lg:text-lg opacity-75 leading-relaxed max-w-md">
                        Descoperă peste {(stats?.totalBooks || 12000).toLocaleString("ro-RO")} de titluri,
                        vezi recenzii și ratinguri. Pentru rezervări, descărcări și liste de dorințe,
                        creează un cont gratuit.
                    </p>

                    <ul className="mt-9 space-y-4 text-sm lg:text-base">
                        <li className="flex items-center gap-4">
                            <span className="w-1.5 h-1.5 bg-[var(--ink)] inline-block" />
                            Catalog complet vizibil
                        </li>
                        <li className="flex items-center gap-4">
                            <span className="w-1.5 h-1.5 bg-[var(--ink)] inline-block" />
                            Detalii și recenzii cărți
                        </li>
                        <li className="flex items-center gap-4">
                            <span className="w-1.5 h-1.5 bg-[var(--ink)] inline-block" />
                            Recomandări săptămânale publice
                        </li>
                    </ul>

                    <Link
                        to="/inregistrare"
                        className="inline-flex items-center gap-3 mt-11 bg-[var(--ink)] text-[var(--paper)] px-7 py-4 rounded-none text-sm font-medium hover:opacity-90 transition"
                        data-testid="features-cta"
                    >
                        Creează cont <ArrowRight size={16} />
                    </Link>
                </div>

                {/* Right feature grid */}
                <div className="lg:col-span-7 rounded-[28px] overflow-hidden border border-[var(--line)] bg-[var(--paper)]">
                    <div className="grid sm:grid-cols-2">
                        <Feature
                            icon={<Search size={20} />}
                            title="Catalog hibrid"
                            desc="Cărți fizice și digitale într-un singur catalog inteligent."
                            accent="bg-[#F2D982]"
                        />

                        <Feature
                            icon={<BookMarked size={20} />}
                            title="Rezervări fizice"
                            desc="Verificare automată a stocului și listă FIFO de așteptare."
                            accent="bg-[#CDD9C3]"
                        />

                        <Feature
                            icon={<Download size={20} />}
                            title="Descărcări PDF"
                            desc="Acces instant la versiunile digitale, cu istoric complet."
                            accent="bg-[#DFA18E]"
                        />

                        <Feature
                            icon={<Sparkles size={20} />}
                            title="Recomandări personalizate"
                            desc="Algoritm bazat pe istoricul tău de lectură."
                            accent="bg-[#D4937F]"
                        />

                        <Feature
                            icon={<Bell size={20} />}
                            title="Notificări inteligente"
                            desc="Reamintiri pentru returnări și alerte de disponibilitate."
                            accent="bg-[#CDD9C3]"
                        />

                        <Feature
                            icon={<BarChart3 size={20} />}
                            title="Statistici pentru bibliotecari"
                            desc="Panou administrativ cu rapoarte și analize de utilizare."
                            accent="bg-[#F2D982]"
                        />
                    </div>
                </div>
            </div>
        </section>

      {/*/!* Manifest *!/*/}
      {/*<section className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-24" data-testid="manifest">*/}
      {/*  <div className="paper p-10 lg:p-14 relative overflow-hidden">*/}
      {/*    <div className="grid lg:grid-cols-12 gap-10 items-start">*/}
      {/*      <div className="lg:col-span-4">*/}
      {/*        <div className="text-xs uppercase tracking-widest opacity-60">Manifest · 2025</div>*/}
      {/*        <ul className="mt-6 space-y-2 text-sm">*/}
      {/*          <li>— Acces fără bariere</li>*/}
      {/*          <li>— Transparență totală</li>*/}
      {/*          <li>— Memorie colectivă</li>*/}
      {/*          <li>— Lectură pentru toți</li>*/}
      {/*        </ul>*/}
      {/*      </div>*/}
      {/*      <div className="lg:col-span-8">*/}
      {/*        <p className="font-serif text-3xl lg:text-4xl italic-soft leading-snug">*/}
      {/*          „Biblioteca este templul fără ziduri al memoriei colective.”*/}
      {/*        </p>*/}
      {/*        <p className="mt-6 opacity-80 max-w-2xl">*/}
      {/*          Construim o platformă unde fiecare carte — tipărită sau digitală — își găsește cititorul.*/}
      {/*          Unde un PDF la 2 dimineața este la fel de accesibil ca un raft la lumina blândă a unei lămpi.*/}
      {/*        </p>*/}
      {/*      </div>*/}
      {/*    </div>*/}
      {/*  </div>*/}
      {/*</section>*/}

        {/* Manifest */}
        <section
            className="relative bg-[var(--ink)] text-[var(--paper)] overflow-hidden mt-28"
            data-testid="manifest"
        >
            <div className="absolute inset-0 paper-grain opacity-40" />

            <div className="relative max-w-[1400px] mx-auto px-6 lg:px-10 py-28">
                <div className="grid grid-cols-1 lg:grid-cols-12 gap-12">
                    <div className="lg:col-span-3">
                        <p className="text-[10px] uppercase tracking-[0.3em] text-[var(--paper)]/50">
                            Manifest · 2025
                        </p>

                        <div className="mt-6 flex flex-col gap-2">
                            {[
                                "Acces fără bariere",
                                "Transparență totală",
                                "Memorie colectivă",
                                "Lectură pentru toți",
                            ].map((item) => (
                                <span
                                    key={item}
                                    className="text-xs uppercase tracking-[0.18em] text-[var(--paper)]/70"
                                >
              — {item}
            </span>
                            ))}
                        </div>
                    </div>

                    <div className="lg:col-span-9">
                        <Quote
                            className="w-12 h-12 text-[var(--rose)] mb-8"
                            strokeWidth={1}
                        />

                        <p className="font-serif text-4xl sm:text-5xl lg:text-7xl leading-[1.05] tracking-tight italic">
                            Biblioteca este templul fără ziduri al{" "}
                            <span className="not-italic underline decoration-[var(--rose)] decoration-4 underline-offset-8">
            memoriei
          </span>{" "}
                            colective.
                        </p>

                        <p className="mt-10 text-[var(--paper)]/60 max-w-2xl text-lg leading-relaxed">
                            Construim o platformă unde fiecare carte — tipărită sau digitală — își găsește cititorul.
                            Unde un PDF la 2 dimineața este la fel de accesibil ca un raft la lumina blândă a unei lămpi.
                        </p>
                    </div>
                </div>
            </div>
        </section>

      {/* Featured books */}
      <section className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-24" data-testid="featured-section">
        <div className="flex items-end justify-between gap-6 flex-wrap">
          <div>
            <div className="text-xs uppercase tracking-widest opacity-60">Selecție curatorială</div>
            <h2 className="font-serif text-4xl lg:text-5xl mt-3">Cărți care merită <span className="italic-soft">rezervate</span>.</h2>
            <p className="mt-3 opacity-70 max-w-xl">Titluri din colecția noastră de top, alese de echipa de bibliotecari.</p>
          </div>
          <Link to="/catalog" className="btn btn-secondary" data-testid="see-all-books">Vezi tot catalogul <ArrowRight size={14} /></Link>
        </div>

        <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 mt-10">
          {featured.map((b) => <BookCard key={b.bookId} book={b} />)}
        </div>
      </section>

      {/*/!* How it works *!/*/}
      {/*<section className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-28" data-testid="how-it-works">*/}
      {/*  <div className="text-xs uppercase tracking-widest opacity-60">Cum funcționează</div>*/}
      {/*  <h2 className="font-serif text-4xl lg:text-5xl mt-3 max-w-3xl">Patru pași până la <span className="italic-soft squiggle">prima pagină</span>.</h2>*/}
      {/*  <p className="mt-3 opacity-70 max-w-xl">Fie că ești student, profesor sau cititor pasionat, platforma te ghidează firesc de la căutare la lectură.</p>*/}
      {/*  <div className="grid md:grid-cols-4 gap-5 mt-10">*/}
      {/*    {[*/}
      {/*      ["01", "Caută", "În catalogul unificat, după titlu, autor sau categorie."],*/}
      {/*      ["02", "Rezervă", "Carte fizică pentru 14 zile sau descarcă PDF instant."],*/}
      {/*      ["03", "Primește", "Notificări inteligente despre status și disponibilitate."],*/}
      {/*      ["04", "Descoperă", "Recomandări care îți continuă firul gândurilor."],*/}
      {/*    ].map(([n, t, d]) => (*/}
      {/*      <div key={n} className="paper p-6">*/}
      {/*        <div className="font-serif text-3xl opacity-30">{n}</div>*/}
      {/*        <h4 className="mt-2 font-serif text-xl">{t}</h4>*/}
      {/*        <p className="mt-2 text-sm opacity-70">{d}</p>*/}
      {/*      </div>*/}
      {/*    ))}*/}
      {/*  </div>*/}
      {/*</section>*/}

        {/* How it works */}
        <section
            className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-28"
            data-testid="how-it-works"
        >
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start mb-16">
                <div className="lg:col-span-7">
                    <div className="text-xs uppercase tracking-[0.28em] opacity-50">
                        Cum funcționează
                    </div>

                    <h2 className="font-serif text-5xl sm:text-6xl lg:text-7xl leading-[0.95] tracking-tight mt-8 max-w-4xl">
                        Patru pași până la{" "}
                        <em className="italic font-normal">prima</em>
                        <br />
                        <em className="italic font-normal">pagină.</em>
                    </h2>
                </div>

                <p className="lg:col-span-5 text-lg lg:text-xl opacity-70 leading-relaxed lg:pt-16 max-w-xl">
                    Fie că ești student, profesor sau cititor pasionat, platforma te
                    ghidează firesc de la căutare la lectură.
                </p>
            </div>

            <div className="rounded-[28px] overflow-hidden border border-[var(--line)] bg-[var(--paper)]">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4">
                    <StepCard
                        number="01"
                        title="Caută"
                        desc="În catalogul unificat, după titlu, autor sau categorie."
                        icon={<Search size={21} />}
                        accent="bg-[#F2D982]"
                    />

                    <StepCard
                        number="02"
                        title="Rezervă"
                        desc="Carte fizică pentru 14 zile sau descarcă PDF instant."
                        icon={<BookMarked size={21} />}
                        accent="bg-[#E7B8AA]"
                    />

                    <StepCard
                        number="03"
                        title="Primește"
                        desc="Notificări inteligente despre status și disponibilitate."
                        icon={<Bell size={21} />}
                        accent="bg-[#CDD9C3]"
                    />

                    <StepCard
                        number="04"
                        title="Descoperă"
                        desc="Recomandări care îți continuă firul gândurilor."
                        icon={<Sparkles size={21} />}
                        accent="bg-[#D4937F]"
                    />
                </div>
            </div>
        </section>
        {/* Testimonials */}
        <section
            className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-28"
            data-testid="testimonials"
        >
            <div className="max-w-5xl mx-auto mb-14 text-center">
                <div className="text-xs uppercase tracking-[0.28em] opacity-50">
                    Voci ale comunității
                </div>

                <h2 className="font-serif text-5xl sm:text-6xl lg:text-7xl leading-[0.95] tracking-tight mt-8">
                    Ce spun cei care au{" "}
                    <em className="italic font-normal">deschis prima</em>
                    <br />
                    <em className="italic font-normal">pagină.</em>
                </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <TestimonialCard
                    quote="Am redescoperit plăcerea de a citi. Recomandările săptămânale sunt mereu pe gustul meu."
                    name="Ana Marinescu"
                    role="Studentă, UPT"
                    initial="A"
                />

                <TestimonialCard
                    quote="Pentru biblioteca noastră, importul automat din Google Books a tăiat 80% din timpul de catalogare."
                    name="Mihai Iordache"
                    role="Bibliotecar șef"
                    initial="M"
                />

                <TestimonialCard
                    quote="Lista de așteptare cu notificări m-a salvat de multe drumuri inutile la bibliotecă."
                    name="Tudor Popescu"
                    role="Cercetător"
                    initial="T"
                />
            </div>
        </section>


        {/* CTA */}
        {/* CTA */}
        <section
            className="max-w-[1400px] mx-auto px-6 lg:px-10 mt-28 mb-20"
            data-testid="cta-section"
        >
            <div
                className="relative overflow-hidden rounded-[28px] px-10 sm:px-16 py-14 sm:py-16 grid grid-cols-1 lg:grid-cols-12 gap-10 items-center"
                style={{
                    background: "#171717",
                    color: "#f7f1e7",
                }}
            >
                <div
                    className="absolute -right-20 -top-20 w-80 h-80 rounded-full blur-3xl pointer-events-none"
                    style={{
                        background: "rgba(212, 147, 127, 0.18)",
                    }}
                />

                <div className="lg:col-span-8 relative">
                    <BookMarked
                        className="w-10 h-10 mb-8"
                        strokeWidth={1.3}
                        style={{
                            color: "#efc7bd",
                        }}
                    />

                    <h2
                        className="font-serif text-4xl sm:text-5xl lg:text-6xl leading-[1.02] tracking-tight max-w-3xl"
                        style={{
                            color: "#f7f1e7",
                        }}
                    >
                        Pregătit să-ți deschizi propriul
                        <br />
                        raft digital?
                    </h2>

                    <p
                        className="mt-7 max-w-2xl text-base lg:text-lg leading-relaxed"
                        style={{
                            color: "rgba(247, 241, 231, 0.7)",
                        }}
                    >
                        Creează un cont gratuit și începe să rezervi, să descarci și să primești
                        recomandări personalizate.
                    </p>
                </div>

                <div className="lg:col-span-4 flex flex-wrap gap-4 relative lg:justify-end">
                    <Link
                        to="/inregistrare"
                        className="inline-flex items-center gap-3 px-8 py-4 text-sm lg:text-base font-medium rounded-full transition hover:opacity-90"
                        style={{
                            background: "#f7f1e7",
                            color: "#171717",
                        }}
                        data-testid="bottom-cta-register"
                    >
                        Creare cont <ArrowRight size={16} />
                    </Link>

                    <Link
                        to="/catalog"
                        className="inline-flex items-center gap-3 px-8 py-4 text-sm lg:text-base font-medium rounded-full border transition hover:opacity-80"
                        style={{
                            borderColor: "rgba(247, 241, 231, 0.75)",
                            color: "#f7f1e7",
                        }}
                        data-testid="bottom-cta-catalog"
                    >
                        Catalog
                    </Link>
                </div>
            </div>
        </section>
    </main>
  );
}

function Stat({ label, value, testid }) {
  return (
    <div className="pl-3" style={{ borderLeft: "1px solid var(--line)" }} data-testid={testid}>
      <div className="font-serif text-3xl">{value}</div>
      <div className="text-xs uppercase tracking-widest opacity-60 mt-1">{label}</div>
    </div>
  );
}

// function Feature({ icon, title, desc }) {
//   return (
//     <div className="paper paper-hover p-6">
//       <div className="w-10 h-10 rounded-full bg-[var(--cream-2)] grid place-items-center">{icon}</div>
//       <h4 className="mt-4 font-serif text-xl">{title}</h4>
//       <p className="mt-2 text-sm opacity-70">{desc}</p>
//     </div>
//   );
// }

function Feature({ icon, title, desc, accent }) {
    return (
        <div className="min-h-[185px] border-b sm:border-r border-[var(--line)] p-8 lg:p-10 hover:bg-[var(--cream-2)] transition-colors">
            <div
                className={`w-12 h-12 ${accent} border border-[var(--line)] grid place-items-center mb-8`}
            >
                {icon}
            </div>

            <h4 className="font-serif text-2xl leading-tight">
                {title}
            </h4>

            <p className="mt-4 text-sm lg:text-base opacity-70 leading-relaxed max-w-sm">
                {desc}
            </p>
        </div>
    );
}

function StepCard({ number, title, desc, icon, accent }) {
    return (
        <div className="relative min-h-[250px] border-b lg:border-b-0 lg:border-r border-[var(--line)] p-8 lg:p-10 hover:bg-[var(--cream-2)] transition-colors">
            <div className="flex items-start justify-between">
                <div className="font-serif text-5xl text-[#8F9B93] opacity-80">
                    {number}
                </div>

                <div
                    className={`w-14 h-14 ${accent} border border-[var(--line)] grid place-items-center`}
                >
                    {icon}
                </div>
            </div>

            <h4 className="font-serif text-3xl mt-14">
                {title}
            </h4>

            <p className="mt-5 text-sm lg:text-base opacity-70 leading-relaxed max-w-xs">
                {desc}
            </p>
        </div>
    );
}

function TestimonialCard({ quote, name, role, initial }) {
    return (
        <div className="rounded-[28px] border border-[var(--line)] bg-[var(--paper)] p-8 lg:p-10 min-h-[300px] flex flex-col justify-between hover:bg-[var(--cream-2)] transition-colors">
            <div>
                <Quote
                    size={28}
                    className="opacity-30 mb-8"
                    strokeWidth={1.5}
                />

                <p className="font-serif italic text-2xl leading-snug">
                    „{quote}”
                </p>
            </div>

            <div className="mt-10 flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-[var(--rose)] grid place-items-center font-serif text-lg">
                    {initial}
                </div>

                <div>
                    <div className="font-medium">
                        {name}
                    </div>
                    <div className="text-sm opacity-60 mt-1">
                        {role}
                    </div>
                </div>
            </div>
        </div>
    );
}

function HeroCollage({ images }) {
  const positions = [
    { top: "0%", left: "5%", rotate: -6, w: 200 },
    { top: "8%", left: "32%", rotate: 3, w: 240 },
    { top: "2%", right: "0%", rotate: 8, w: 200 },
    { top: "42%", left: "0%", rotate: 4, w: 220 },
    { top: "44%", left: "30%", rotate: -2, w: 260 },
    { top: "48%", right: "2%", rotate: 5, w: 200 },
  ];
  return (
    <div className="relative h-[560px] hidden lg:block" data-testid="hero-collage">
      {images.map((b, i) => {
        const p = positions[i] || positions[0];
        return (
          <Link
            key={b.bookId}
            to={`/carte/${b.bookId}`}
            className="absolute paper paper-hover overflow-hidden p-2"
            style={{ top: p.top, left: p.left, right: p.right, width: p.w, transform: `rotate(${p.rotate}deg)`, background: "var(--paper)" }}
          >
            <div className="overflow-hidden rounded">
              <img src={b.coverImageURL} alt={b.title} className="w-full h-44 object-cover" />
            </div>
            <div className="mt-2 px-1 pb-1">
              <div className="text-[10px] uppercase tracking-widest opacity-60">{b.categoryName}</div>
              <div className="font-serif italic-soft text-sm leading-tight mt-1 truncate">{b.title}</div>
            </div>
          </Link>
        );
      })}
    </div>
  );
}
