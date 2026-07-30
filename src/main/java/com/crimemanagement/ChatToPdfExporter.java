package com.crimemanagement;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatToPdfExporter {

    public static void main(String[] args) {
        String dest = "Project_Refinement_Log.pdf";
        try {
            exportChat(dest);
            System.out.println("Chat log exported successfully to: " + dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportChat(String dest) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(50, 100, 255));
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.ITALIC, BaseColor.GRAY);
        Font userFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(0, 150, 136));
        Font assistantFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(63, 81, 181));
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

        // Header
        Paragraph title = new Paragraph("Kerala Police ERP - Official Implementation Record", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Phases 4-10: Advanced Operations & AI Integration\nGenerated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20f);
        document.add(subtitle);

        // EMBED IMAGES
        try {
            Image emblem = Image.getInstance("kerala_police_emblem_logo_1774720766701.png");
            emblem.scaleToFit(100, 100);
            emblem.setAlignment(Element.ALIGN_CENTER);
            document.add(emblem);

            document.add(new Paragraph("\n"));

            Image hero = Image.getInstance("police_hero_banner_night_1774720782806.png");
            hero.scaleToFit(400, 200);
            hero.setAlignment(Element.ALIGN_CENTER);
            document.add(hero);
            
            Paragraph imgCaption = new Paragraph("Figure 1: New Citizen Intelligence Portal Branding", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
            imgCaption.setAlignment(Element.ALIGN_CENTER);
            document.add(imgCaption);
        } catch (Exception e) {
            document.add(new Paragraph("[Media Assets Not Loaded]", subtitleFont));
        }

        document.add(new Paragraph("\n"));

        // Content
        addSectionHeader(document, "PHASE 4: ELITE ROLE EXPANSION", subtitleFont);
        addMessage(document, "OBJECTIVE", "Integrate specialized administrative and front-line roles into the security module.", userFont, bodyFont);
        addMessage(document, "IMPLEMENTATION", "Added Role.FORENSIC_EXAMINER, PROSECUTOR, JAILER, PRO, and EMERGENCY_DISPATCHER to the authentication system. Updated Role Based Access Control (RBAC) in LoginDialog.", assistantFont, bodyFont);

        addSectionHeader(document, "PHASE 8: AI ROUTING ENGINE & CITIZEN PORTAL", subtitleFont);
        addMessage(document, "OBJECTIVE", "Automate complaint routing and launch the statewide public dashboard.", userFont, bodyFont);
        addMessage(document, "IMPLEMENTATION", "Developed 'ComplaintAssignmentService' implementing a keyword-driven AI parsing engine. High-priority keywords trigger an 'EMERGENCY_DISPATCH_112' status. Launched 'index.html' with real-time stats (Total Cases, Resolved, Stations Online).", assistantFont, bodyFont);

        addSectionHeader(document, "PHASE 10: VERIFICATION & BRANDING", subtitleFont);
        addMessage(document, "OBJECTIVE", "Finalize UI aesthetics and perform end-to-end dry runs.", userFont, bodyFont);
        addMessage(document, "IMPLEMENTATION", "Re-branded the Citizen Portal with a premium Dark Mode aesthetic, pulsing emblems, and dynamic response banners. Verified API endpoints and resolved database status enum constraints. Performed zero-warning build verification.", assistantFont, bodyFont);

        addSectionHeader(document, "TECHNICAL STACK SUMMARY", subtitleFont);
        document.add(new Paragraph("Backend: Spring Boot 3.2.0, Hibernate ORM\nUI: Java Swing (FlatLaf), Vanilla JS/CSS (Citizen Portal)\nSecurity: BCrypt hashing, Audit Logging, AI Routing Override", bodyFont));

        document.close();
    }

    private static void addSectionHeader(Document doc, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph("\n" + text, font);
        p.setSpacingBefore(15f);
        p.setSpacingAfter(5f);
        doc.add(p);
    }

    private static void addMessage(Document doc, String sender, String content, Font senderFont, Font bodyFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(10f);
        p.add(new Chunk(sender + ": ", senderFont));
        p.add(new Chunk(content, bodyFont));
        doc.add(p);
    }
}
