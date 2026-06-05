package com.library.service;

import com.library.model.*;
import com.library.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService
{
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final DownloadHistoryRepository downloadRepository;
    private final BookRepository bookRepository;

    public ReportService(UserRepository userRepository,
                         ReservationRepository reservationRepository,
                         FineRepository fineRepository,
                         DownloadHistoryRepository downloadRepository,
                         BookRepository bookRepository)
    {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.fineRepository = fineRepository;
        this.downloadRepository = downloadRepository;
        this.bookRepository = bookRepository;
    }

    // ===== CSV =====

    public String exportUsersCsv()
    {
        StringBuilder sb = new StringBuilder("ID,Nume complet,Email,Rol,Status\n");
        for (User u : userRepository.findAll()) {
            sb.append(u.getUserId()).append(",")
                    .append(csv(u.getFullName())).append(",")
                    .append(csv(u.getEmail())).append(",")
                    .append(u.getRole() != null ? u.getRole().getRoleName() : "").append(",")
                    .append(u.getStatus()).append("\n");
        }
        return sb.toString();
    }

    public String exportReservationsCsv()
    {
        StringBuilder sb = new StringBuilder("ID,Utilizator,Email,Carte,Status,Rezervat,Expira\n");
        for (Reservation r : reservationRepository.findAll()) {
            sb.append(r.getReservationId()).append(",")
                    .append(csv(r.getUser() != null ? r.getUser().getFullName() : "")).append(",")
                    .append(csv(r.getUser() != null ? r.getUser().getEmail() : "")).append(",")
                    .append(csv(r.getBook() != null ? r.getBook().getTitle() : "")).append(",")
                    .append(r.getStatus()).append(",")
                    .append(fmt(r.getReservationDate())).append(",")
                    .append(fmt(r.getExpirationDate())).append("\n");
        }
        return sb.toString();
    }

    public String exportFinesCsv()
    {
        StringBuilder sb = new StringBuilder("ID,Utilizator,Email,Suma,Status,Motiv,Zile_intarziere,Emisa\n");
        for (Fine f : fineRepository.findAll()) {
            sb.append(f.getFineId()).append(",")
                    .append(csv(f.getUser() != null ? f.getUser().getFullName() : "")).append(",")
                    .append(csv(f.getUser() != null ? f.getUser().getEmail() : "")).append(",")
                    .append(f.getAmount()).append(",")
                    .append(f.getStatus()).append(",")
                    .append(csv(f.getReason())).append(",")
                    .append(f.getOverdueDays()).append(",")
                    .append(fmt(f.getCreatedAt())).append("\n");
        }
        return sb.toString();
    }

    public String exportDownloadsCsv()
    {
        StringBuilder sb = new StringBuilder("ID,Utilizator,Email,Carte,Data\n");
        for (DownloadHistory d : downloadRepository.findAll()) {
            sb.append(d.getDownloadId()).append(",")
                    .append(csv(d.getUser() != null ? d.getUser().getFullName() : "")).append(",")
                    .append(csv(d.getUser() != null ? d.getUser().getEmail() : "")).append(",")
                    .append(csv(d.getBook() != null ? d.getBook().getTitle() : "")).append(",")
                    .append(fmt(d.getDownloadDate())).append("\n");
        }
        return sb.toString();
    }

    // ===== PDF — Raport lunar =====

    public byte[] generateMonthlySummaryPdf()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = new Font(Font.HELVETICA, 22, Font.BOLD, Color.BLACK);
            Font subtitle = new Font(Font.HELVETICA, 11, Font.ITALIC, Color.GRAY);
            Font section = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
            Font body = new Font(Font.HELVETICA, 11);

            doc.add(new Paragraph("Bibliotheca — Raport sumar", title));
            doc.add(new Paragraph("Generat: " + fmt(LocalDateTime.now()), subtitle));
            doc.add(Chunk.NEWLINE);

            // Statistici generale
            doc.add(new Paragraph("Statistici generale", section));
            doc.add(Chunk.NEWLINE);

            PdfPTable stats = new PdfPTable(2);
            stats.setWidthPercentage(100);
            addRow(stats, "Total utilizatori", String.valueOf(userRepository.count()), body);
            addRow(stats, "Total cărți", String.valueOf(bookRepository.count()), body);
            addRow(stats, "Total rezervări", String.valueOf(reservationRepository.count()), body);
            addRow(stats, "Total descărcări", String.valueOf(downloadRepository.count()), body);

            long pendingFines = fineRepository.findAll().stream()
                    .filter(f -> "PENDING".equals(f.getStatus().toString()))
                    .count();
            addRow(stats, "Penalități neîncasate", String.valueOf(pendingFines), body);

            double totalAmount = fineRepository.findAll().stream()
                    .filter(f -> "PENDING".equals(f.getStatus().toString()))
                    .mapToDouble(f -> f.getAmount() == null ? 0.0 : f.getAmount().doubleValue())
                    .sum();
            addRow(stats, "Sumă neîncasată (RON)", String.format("%.2f", totalAmount), body);

            doc.add(stats);
            doc.add(Chunk.NEWLINE);

            // Ultimele 10 rezervări
            doc.add(new Paragraph("Ultimele rezervări", section));
            doc.add(Chunk.NEWLINE);

            PdfPTable resv = new PdfPTable(new float[]{2, 3, 1.5f, 1.5f});
            resv.setWidthPercentage(100);
            headerRow(resv, "Utilizator", "Carte", "Status", "Data");

            List<Reservation> recent = reservationRepository.findAll().stream()
                    .sorted((a, b) -> b.getReservationDate().compareTo(a.getReservationDate()))
                    .limit(10).toList();

            for (Reservation r : recent) {
                addRow(resv, body,
                        r.getUser() != null ? r.getUser().getFullName() : "—",
                        r.getBook() != null ? r.getBook().getTitle() : "—",
                        r.getStatus().toString(),
                        fmt(r.getReservationDate()));
            }
            doc.add(resv);

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Nu s-a putut genera raportul PDF.", e);
        }

        return out.toByteArray();
    }

    // ===== Helpers =====

    private String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return s.contains(",") || s.contains("\n") ? "\"" + s + "\"" : s;
    }

    private String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void addRow(PdfPTable t, String key, String value, Font font) {
        t.addCell(new PdfPCell(new Phrase(key, font)));
        t.addCell(new PdfPCell(new Phrase(value, font)));
    }

    private void addRow(PdfPTable t, Font font, String... values) {
        for (String v : values) t.addCell(new PdfPCell(new Phrase(v == null ? "" : v, font)));
    }

    private void headerRow(PdfPTable t, String... headers) {
        Font hf = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, hf));
            c.setBackgroundColor(Color.DARK_GRAY);
            c.setPadding(6);
            t.addCell(c);
        }
    }
}