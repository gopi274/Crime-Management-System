package com.crimemanagement.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class UseCasePDFGenerator {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLACK);
    private static final Font TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

    public static void main(String[] args) {
        String dest = "USE_CASE_DIAGRAM.pdf";
        if (args.length > 0) {
            dest = args[0];
        }

        try {
            generateUseCasePDF(dest);
            System.out.println("PDF generated successfully at: " + dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateUseCasePDF(String dest) throws Exception {
        Document document = new Document(PageSize.A4.rotate()); // Landscape for better diagram layout
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        // Title
        Paragraph title = new Paragraph("Crime Management System - Use Case Diagram", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Introduction
        Paragraph intro = new Paragraph(
                "This document outlines the primary use cases and access levels for the different actors in the system.\n\n",
                new Font(Font.FontFamily.HELVETICA, 12));
        document.add(intro);

        // Use Case Table
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Table Headers
        addHeaderCell(table, "System Administrator (ADMIN)");
        addHeaderCell(table, "Police Officer (OFFICER)");
        addHeaderCell(table, "Court User (COURT)");

        // Common Use Cases (Row 1)
        addCell(table,
                "• Login & Authentication\n• Logout\n• View Dashboard Statistics\n• Search All Records\n• Manage User Accounts",
                true);
        addCell(table,
                "• Login & Authentication\n• Logout\n• View Dashboard Statistics\n• Search Records\n• Manage Officers (Profile)",
                true);
        addCell(table, "• Login & Authentication\n• Logout\n• View Dashboard Statistics\n• Search Cases", true);

        // Core Management (Row 2)
        addCell(table,
                "• Manage Officers (CRUD)\n• Manage Criminals (CRUD)\n• Manage FIRs (CRUD)\n• Manage Cases (CRUD)\n• Manage Suspects (CRUD)",
                false);
        addCell(table,
                "• Manage Criminals\n• Manage FIRs\n• Manage Suspects\n• View Own Assigned Cases\n• Collect Evidence",
                false);
        addCell(table, "• Manage Cases (CRUD)\n• View Criminal Records (Read-only)\n• View FIR Details (Read-only)",
                false);

        // Advanced/System Features (Row 3)
        addCell(table, "• System Configuration\n• Security Audit Logs\n• Database Maintenance", true);
        addCell(table, "• Update Case Status\n• Evidence Tracking\n• Generate Wanted Posters", true);
        addCell(table, "• Court Schedule Management\n• Legal Document Generation\n• Case Resolution Status", true);

        document.add(table);

        // Legend/Note
        document.add(new Paragraph("\nNote: (CRUD) refers to Create, Read, Update, and Delete permissions."));

        document.close();
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(new BaseColor(0, 51, 102)); // Dark Blue
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        if (alternate) {
            cell.setBackgroundColor(new BaseColor(240, 240, 240)); // Light Gray
        }
        cell.setPadding(10);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(cell);
    }
}
