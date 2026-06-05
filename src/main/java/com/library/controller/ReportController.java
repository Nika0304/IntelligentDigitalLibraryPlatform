package com.library.controller;

import com.library.service.ReportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController
{
    private final ReportService reportService;

    public ReportController(ReportService reportService)
    {
        this.reportService = reportService;
    }

    @GetMapping("/users.csv")
    public ResponseEntity<byte[]> usersCsv() {
        return csv(reportService.exportUsersCsv(), "utilizatori");
    }

    @GetMapping("/reservations.csv")
    public ResponseEntity<byte[]> reservationsCsv() {
        return csv(reportService.exportReservationsCsv(), "rezervari");
    }

    @GetMapping("/fines.csv")
    public ResponseEntity<byte[]> finesCsv() {
        return csv(reportService.exportFinesCsv(), "penalitati");
    }

    @GetMapping("/downloads.csv")
    public ResponseEntity<byte[]> downloadsCsv() {
        return csv(reportService.exportDownloadsCsv(), "descarcari");
    }

    @GetMapping("/summary.pdf")
    public ResponseEntity<byte[]> summaryPdf() {
        byte[] pdf = reportService.generateMonthlySummaryPdf();
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bibliotheca-sumar.pdf\"");
        return new ResponseEntity<>(pdf, h, HttpStatus.OK);
    }

    private ResponseEntity<byte[]> csv(String content, String filename) {
        // BOM ca Excel să citească UTF-8 corect
        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        h.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"bibliotheca-" + filename + ".csv\"");
        return new ResponseEntity<>(out, h, HttpStatus.OK);
    }
}