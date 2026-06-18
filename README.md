# Bibliotheca – Intelligent Digital Library Platform

Bibliotheca este o aplicație web full-stack pentru gestionarea unei biblioteci digitale și fizice. Platforma permite utilizatorilor să caute cărți, să rezerve exemplare fizice, să descarce versiuni digitale, să adauge cărți în wishlist, să scrie recenzii, să participe la grupuri de lectură, să urmărească provocări de lectură și să vadă un rezumat personalizat de tip Reading Wrapped. Administratorul poate gestiona cărțile, exemplarele fizice, utilizatorii, rezervările, notificările, conținutul propus de utilizatori și poate urmări activitatea platformei printr-un dashboard administrativ.

## Funcționalități principale

### Vizitator

- poate accesa pagina principală;
- poate naviga prin catalogul de cărți;
- poate căuta și filtra cărți;
- poate vizualiza detaliile unei cărți.

### Utilizator autentificat

- își poate crea cont și se poate autentifica;
- poate rezerva exemplare fizice disponibile;
- poate descărca PDF-uri pentru cărțile care au versiune digitală;
- poate adăuga cărți în wishlist;
- poate scrie recenzii și acorda ratinguri;
- poate primi notificări;
- poate vizualiza istoricul activităților;
- poate primi recomandări personalizate de cărți;
- poate participa la provocări de lectură;
- poate vizualiza Reading Wrapped, cu statistici despre propria activitate;
- poate propune grupuri de lectură;
- poate trimite întrebări către bibliotecar/admin.

### Administrator

- poate adăuga, edita și șterge cărți;
- poate gestiona exemplarele fizice ale cărților;
- poate gestiona utilizatorii;
- poate răspunde la întrebările primite prin chat;
- poate aproba sau respinge grupurile de lectură propuse;
- primește notificări pentru acțiuni care necesită intervenție administrativă;
- poate vizualiza statistici despre activitatea platformei prin dashboard-ul de administrare.

## Funcționalități avansate

- sistem avansat de recomandări pentru cărți, bazat pe mai multe criterii;
- dashboard pentru administrator, cu statistici și date despre activitatea platformei;
- provocări de lectură pentru utilizatori;
- Reading Wrapped, adică un rezumat personalizat al activității de lectură;
- wishlist personalizat;
- sistem de recenzii și ratinguri;
- notificări pentru utilizatori și administratori;
- generare și descărcare PDF folosind OpenPDF;
- grupuri de lectură cu aprobare din partea administratorului;
- chat pentru întrebări adresate administratorului;
- statistici și istoric de activitate.

## Tehnologii folosite

### Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- OpenPDF

### Frontend

- React
- JavaScript
- HTML
- CSS
- Axios
- React Router

### Bază de date

- MySQL

## Structura proiectului

```text
Bibliotheca/
├── backend/
│   ├── src/main/java/com/library/
│   │   ├── controller/      # REST Controllers
│   │   ├── service/         # Logica aplicației
│   │   ├── repository/      # Interacțiunea cu baza de date
│   │   ├── model/           # Entitățile JPA
│   │   ├── dto/             # Obiecte folosite pentru request/response
│   │   └── config/          # Configurări, inclusiv securitate
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── pages/           # Paginile aplicației
│   │   ├── components/      # Componente reutilizabile
│   │   ├── services/        # Apeluri către API
│   │   └── assets/          # Resurse statice
│   └── package.json
│
└── README.md
```

## Instalare și rulare locală

### Cerințe

Pentru rulare locală sunt necesare:

- Java instalat;
- Maven instalat;
- Node.js și npm;
- MySQL Server;
- un IDE, de exemplu IntelliJ IDEA sau Visual Studio Code.

## Configurare bază de date

Creează o bază de date MySQL:

```sql
CREATE DATABASE bibliotheca;
```

În backend, configurează conexiunea în `application.properties` sau `application-local.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bibliotheca
spring.datasource.username=root
spring.datasource.password=parola_ta

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Datele de conectare trebuie adaptate în funcție de configurația locală MySQL.

## Rulare backend

Din folderul backend:

```bash
mvn clean install
mvn spring-boot:run
```

Backend-ul va porni, în mod normal, la adresa:

```text
http://localhost:8080
```

## Rulare frontend

Din folderul frontend:

```bash
npm install
npm start
```

sau, dacă proiectul folosește Vite:

```bash
npm install
npm run dev
```

Frontend-ul va porni, de obicei, la una dintre adresele:

```text
http://localhost:3000
```

sau

```text
http://localhost:5173
```

## Exemple de endpoint-uri REST

```text
/api/auth              # autentificare și cont utilizator
/api/books             # gestionare și afișare cărți
/api/book-copies       # gestionare exemplare fizice
/api/reservations      # rezervări
/api/wishlist          # wishlist
/api/reviews           # recenzii
/api/notifications     # notificări
/api/download          # descărcare PDF
/api/groups            # grupuri de lectură
/api/chat              # întrebări și răspunsuri
/api/stats             # statistici și dashboard admin
/api/recommendations   # recomandări de cărți
/api/challenges        # provocări de lectură
/api/wrapped           # Reading Wrapped
```

## Securitate și roluri

Aplicația folosește Spring Security pentru controlul accesului. Există mai multe tipuri de utilizatori:

- `VISITOR` – utilizator neautentificat;
- `USER` – utilizator autentificat;
- `ADMIN` – administrator.

Accesul este diferențiat în funcție de rol. De exemplu, un utilizator poate vedea cărțile și poate face rezervări, dar doar administratorul poate adăuga sau modifica date administrative.

## Exemple de fluxuri în aplicație

### Rezervarea unei cărți

1. Utilizatorul se autentifică.
2. Accesează pagina unei cărți.
3. Verifică exemplarele fizice disponibile.
4. Apasă pe butonul de rezervare.
5. Rezervarea este salvată în sistem.
6. Utilizatorul și administratorul pot primi notificări relevante.

### Propunerea unui grup de lectură

1. Utilizatorul completează formularul pentru un grup nou.
2. Grupul este salvat cu status de așteptare.
3. Administratorul primește notificare.
4. Administratorul aprobă sau respinge propunerea.
5. Utilizatorul primește notificare cu decizia.

### Descărcarea unui PDF

1. Utilizatorul accesează o carte cu versiune digitală disponibilă.
2. Apasă pe butonul de descărcare.
3. Backend-ul generează PDF-ul folosind OpenPDF.
4. Fișierul este returnat ca răspuns către browser.
5. Acțiunea poate fi salvată în istoricul utilizatorului.

### Recomandări de cărți

1. Utilizatorul accesează secțiunea de recomandări.
2. Sistemul analizează informații precum categoria, autorul, popularitatea, istoricul și ratingurile.
3. Aplicația afișează cărți potrivite pentru utilizator.

### Provocări de lectură și Reading Wrapped

1. Utilizatorul poate participa la provocări de lectură.
2. Activitatea sa este salvată în istoric.
3. Platforma poate genera un rezumat personalizat de tip Reading Wrapped.
4. Utilizatorul vede statistici precum numărul de cărți citite, categorii preferate și activitate generală.

### Dashboard administrator

1. Administratorul se autentifică în platformă.
2. Accesează dashboard-ul administrativ.
3. Poate vedea statistici despre utilizatori, cărți, rezervări, descărcări, recenzii și activitatea generală.
4. Dashboard-ul ajută la urmărirea modului în care este folosită biblioteca.

## Obiectivul proiectului

Scopul proiectului este realizarea unei platforme moderne pentru bibliotecă, care combină funcționalități clasice de gestionare a cărților cu funcționalități digitale și interactive. Aplicația urmărește să ofere o experiență clară pentru utilizatori și instrumente eficiente de administrare pentru bibliotecar.

## Autori

Proiect realizat de:

- Nicoleta Tîrsîna
- Alexandra Tănase

## Status proiect

Proiectul este într-o versiune funcțională, cu funcționalități principale și avansate implementate și testate local. Printre funcționalitățile avansate se numără sistemul de recomandări, dashboard-ul pentru administrator, provocările de lectură, Reading Wrapped, grupurile de lectură, notificările și chat-ul cu administratorul. Ulterior pot fi adăugate îmbunătățiri precum documentare Swagger, Flyway pentru migrarea bazei de date și deploy în cloud.
