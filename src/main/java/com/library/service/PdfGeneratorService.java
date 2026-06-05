package com.library.service;

import com.library.model.Author;
import com.library.model.Book;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

@Service
public class PdfGeneratorService
{
    public byte[] generateBookPdf(Book book)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("Bibliotheca Digital Library", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Titlu: " + book.getTitle(), normalFont));

            if (book.getAuthors() != null && !book.getAuthors().isEmpty())
            {
                String authors = book.getAuthors()
                        .stream()
                        .map(Author::getName)
                        .collect(Collectors.joining(", "));

                document.add(new Paragraph("Autor/i: " + authors, normalFont));
            }

            if (book.getCategory() != null)
            {
                document.add(new Paragraph("Categorie: " + book.getCategory().getName(), normalFont));
            }

            if (book.getPublicationYear() != null)
            {
                document.add(new Paragraph("An publicare: " + book.getPublicationYear(), normalFont));
            }

            document.add(new Paragraph(" "));

            if (book.getDescription() != null && !book.getDescription().isBlank())
            {
                document.add(new Paragraph("Descriere:", normalFont));
                document.add(new Paragraph(book.getDescription(), normalFont));
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph(
                    "Acesta este un fisier digital demonstrativ generat automat pentru proiectul Bibliotheca.",
                    normalFont
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Intr-o versiune reala a aplicatiei, aici ar fi continutul complet al cartii sau al materialului digital.",
                    normalFont
            ));

            document.close();

            return outputStream.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Nu s-a putut genera PDF-ul.");
        }
    }
}