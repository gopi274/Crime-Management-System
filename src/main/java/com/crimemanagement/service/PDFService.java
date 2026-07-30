package com.crimemanagement.service;

import com.crimemanagement.entity.Criminal;
import com.crimemanagement.entity.FIR;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;

public class PDFService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 36, Font.BOLD, BaseColor.RED);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
    private static final Font BODY_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);

    public void generateWantedPoster(Criminal c, String dest) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        Paragraph title = new Paragraph("WANTED", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        // Placeholder for photo (real app would load image bytes)
        PdfPTable photoTable = new PdfPTable(1);
        PdfPCell cell = new PdfPCell(new Phrase("PHOTO NOT AVAILABLE"));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setFixedHeight(200);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        photoTable.addCell(cell);
        document.add(photoTable);

        document.add(new Paragraph("\n"));

        addDetail(document, "NAME", c.getFullName());
        addDetail(document, "ALIAS", c.getAlias());
        addDetail(document, "GENDER", c.getGender());
        addDetail(document, "DOB", c.getDateOfBirth() != null ? c.getDateOfBirth().toString() : "Unknown");

        document.add(new Paragraph("\nWARNING: CONSIDERED DANGEROUS",
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.RED)));
        document.add(new Paragraph("If you have any information, contact local authorities immediately."));

        document.close();
    }

    public void generateFIRReport(FIR fir, String dest) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
        Paragraph header = new Paragraph("FIRST INFORMATION REPORT", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        document.add(new Paragraph(
                "----------------------------------------------------------------------------------------------------------------"));

        document.add(new Paragraph("FIR ID: " + fir.getFirId(), SUBTITLE_FONT));
        document.add(new Paragraph("Date: " + fir.getDateOfIncident(), BODY_FONT));
        document.add(new Paragraph("Status: " + fir.getStatus(), BODY_FONT));

        document.add(new Paragraph("\nDetails:", SUBTITLE_FONT));
        document.add(new Paragraph("Crime Type: " + fir.getCrimeType()));
        document.add(new Paragraph("Description: " + fir.getCrimeDescription()));

        document.add(new Paragraph("\nComplainant:", SUBTITLE_FONT));
        document.add(new Paragraph("Citizen ID: " + (fir.getCitizenId() != null ? fir.getCitizenId() : "N/A")));

        document.add(new Paragraph("\n\n\n_________________________\nOfficer Signature"));

        document.close();
    }

    private void addDetail(Document doc, String label, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        p.add(new Chunk(value != null ? value : "N/A", BODY_FONT));
        doc.add(p);
    }
}
