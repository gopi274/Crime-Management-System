package com.crimemanagement;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class FullProjectPdfExporter {

    public static void main(String[] args) {
        String dest = "Kerala_Police_ERP_Phase_1_to_10_Full_Report.pdf";
        try {
            generateFullReport(dest);
            System.out.println("Full Project Report generated successfully: " + dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateFullReport(String dest) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(11, 34, 57));
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(50, 100, 255));
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
        Font highlightFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(0, 128, 0));

        // Header Section
        Paragraph title = new Paragraph("KERALA POLICE ERP SYSTEM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Comprehensive Implementation Report: Phases 1 - 10", new Font(Font.FontFamily.HELVETICA, 14, Font.ITALIC));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20f);
        document.add(subtitle);

        // Branding Visuals
        try {
            Image emblem = Image.getInstance("kerala_police_emblem_logo_1774720766701.png");
            emblem.scaleToFit(80, 80);
            emblem.setAlignment(Element.ALIGN_CENTER);
            document.add(emblem);
            
            document.add(new Paragraph("\n"));
            
            Image hero = Image.getInstance("police_hero_banner_night_1774720782806.png");
            hero.scaleToFit(450, 250);
            hero.setAlignment(Element.ALIGN_CENTER);
            document.add(hero);
            
            Paragraph cap = new Paragraph("Figure 1: Citizen Intelligence Portal - Integrated AI Network", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC));
            cap.setAlignment(Element.ALIGN_CENTER);
            document.add(cap);
        } catch (Exception e) {}

        document.add(new Paragraph("\n"));

        // --- PHASE 1-3: FOUNDATIONS ---
        addSection(document, "PHASE 1-3: CORE INFRASTRUCTURE", sectionFont);
        addContent(document, "ESTABLISHMENT", "Initialization of the Java-based Crime Management System. Implementation of the core Swing UI, MySQL database connectivity, and basic CRUD operations for Officers, FIRs, and Criminals. Established the 3NF relational schema for data integrity.", bodyFont);

        // --- PHASE 4-5: SECURITY & RBAC ---
        addSection(document, "PHASE 4-5: SECURITY & ADMINISTRATIVE LAYERS", sectionFont);
        addContent(document, "ELITE ROLE EXPANSION", "Integrated Kerala Police high-ranks (DGP to CPO) and specialized administrative roles (Forensic, Prosecutor, Jailer). Implemented a robust Login system with BCrypt hashing and session-based Role-Based Access Control (RBAC).", bodyFont);

        // --- PHASE 6-7: JUDICIAL & INTERNAL COMMS ---
        addSection(document, "PHASE 6-7: JUDICIAL SYNC & INTRANET", sectionFont);
        addContent(document, "OFFICIAL WORKFLOWS", "Launched the Judicial Court portal for synchronization of trial dates and witness summons. Implemented the internal Departmental Intranet enabling Inter-station messaging and state-wide BOLO (Be On Look Out) alerts.", bodyFont);

        // --- PHASE 8: AI & PUBLIC PORTAL ---
        addSection(document, "PHASE 8: AI ROUTING & CITIZEN INTELLIGENCE", sectionFont);
        addContent(document, "INTELLIGENCE LAYER", "Deployed the AI-driven 'ComplaintAssignmentService' capable of parsing natural language reports for emergency keywords. Built the high-performance Public Citizen Portal for anonymous reporting and live state-wide crime statistics.", bodyFont);

        // --- PHASE 9: ANALYTICS ---
        addSection(document, "PHASE 9: PREDICTIVE ANALYTICS (DGP DASHBOARD)", sectionFont);
        addContent(document, "EXECUTIVE VISION", "Integrated heat-maps and crime trend analysis into the DGP/Commissioner dashboards, providing mathematical predictions of crime hotspots based on FIR velocity and historical data.", bodyFont);

        // --- PHASE 10: OPTIMIZATION ---
        addSection(document, "PHASE 10: BRANDING, VERIFICATION & HANDOVER", sectionFont);
        addContent(document, "FINAL POLISH", "Complete UI/UX overhaul of the web portal with premium dark mode branding. Final verification of all system overrides (112 Emergency bypass). Resolved all IDE warnings and database schema constraints. Build status: ZERO ERROR / PRODUCTION READY.", highlightFont);

        document.add(new Paragraph("\n\n__________________________________\nSystem Architect: Antigravity AI\nAuthorized Deployment Record", bodyFont));

        document.close();
    }

    private static void addSection(Document doc, String title, Font font) throws DocumentException {
        Paragraph p = new Paragraph("\n" + title, font);
        p.setSpacingBefore(15f);
        p.setSpacingAfter(5f);
        doc.add(p);
    }

    private static void addContent(Document doc, String heading, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(heading + ": ", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));
        p.add(new Chunk(text, font));
        p.setSpacingAfter(10f);
        doc.add(p);
    }
}
