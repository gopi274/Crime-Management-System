package com.crimemanagement.ui;
import com.crimemanagement.util.DesignSystem;

import com.crimemanagement.entity.Case;
import com.crimemanagement.entity.Criminal;
import com.crimemanagement.entity.Suspect;
import com.crimemanagement.dao.SuspectDAO;
import com.crimemanagement.service.CriminalService;
import com.crimemanagement.service.InvestigationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CrimeGraphPanel extends JPanel {

    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<String, Node> nodeMap = new HashMap<>(); // Key: "TYPE_ID"

    private final CriminalService criminalService = new CriminalService();
    private final InvestigationService investigationService = new InvestigationService();
    private final SuspectDAO suspectDAO = new SuspectDAO();

    private final GraphCanvas canvas;
    private final Timer simulationTimer;
    private Node draggedNode = null;
    private Node hoveredNode = null;

    // Physics Constants
    private static final double REPULSION = 8000;
    private static final double ATTRACTION = 0.05;
    private static final double DAMPING = 0.85;
    private static final double MAX_VELOCITY = 5.0;

    public CrimeGraphPanel() {
        setLayout(new BorderLayout());
        setBackground(DesignSystem.BACKGROUND);

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = DesignSystem.createHeader("Intelligence Graph");
        top.add(title, BorderLayout.WEST);

        JButton refresh = DesignSystem.createPrimaryButton("Refresh / Reset");
        refresh.addActionListener(e -> reloadGraph());
        top.add(refresh, BorderLayout.EAST);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem("Terminated Case", DesignSystem.PRIMARY));
        legend.add(createLegendItem("Criminal", DesignSystem.DANGER));
        top.add(legend, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        canvas = new GraphCanvas();
        add(canvas, BorderLayout.CENTER);

        // Physics Loop (60 FPS)
        simulationTimer = new Timer(16, e -> {
            updatePhysics();
            canvas.repaint();
        });
        simulationTimer.start();

        reloadGraph();
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setBackground(color);
        dot.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        JLabel l = new JLabel(label);
        l.setFont(DesignSystem.CAPTION_FONT);
        l.setForeground(DesignSystem.TEXT_SECONDARY);

        p.add(dot);
        p.add(l);
        return p;
    }

    private void reloadGraph() {
        nodes.clear();
        edges.clear();
        nodeMap.clear();

        try {
            List<Suspect> suspects = suspectDAO.findAll();
            // Show recent connections
            List<Suspect> recentSuspects = suspects.stream().skip(Math.max(0, suspects.size() - 25)).toList();

            List<Case> allCases = investigationService.getAllCases();
            List<Criminal> allCrims = criminalService.getAllCriminals();

            java.util.Set<Integer> caseIds = recentSuspects.stream().map(Suspect::getCaseId).collect(java.util.stream.Collectors.toSet());
            java.util.Set<Integer> crimIds = recentSuspects.stream().map(Suspect::getCriminalId).collect(java.util.stream.Collectors.toSet());

            List<Case> cases = allCases.stream().filter(c -> caseIds.contains(c.getCaseId())).toList();
            List<Criminal> criminals = allCrims.stream().filter(c -> crimIds.contains(c.getCriminalId())).toList();

            Random rand = new Random();
            int width = Math.max(800, getWidth());
            int height = Math.max(500, getHeight());

            // 1. Add Cases (Blue Squares)
            for (Case c : cases) {
                String key = "CASE_" + c.getCaseId();
                Node n = new Node("Case #" + c.getCaseNumber(), Type.CASE,
                        rand.nextInt(width - 100) + 50, rand.nextInt(height - 100) + 50);
                nodes.add(n);
                nodeMap.put(key, n);
            }

            // 2. Add Criminals (Red Circles)
            for (Criminal c : criminals) {
                String key = "CRIM_" + c.getCriminalId();
                Node n = new Node(c.getFullName(), Type.CRIMINAL,
                        rand.nextInt(width - 100) + 50, rand.nextInt(height - 100) + 50);
                nodes.add(n);
                nodeMap.put(key, n);
            }

            // 3. Create Edges based on Suspect data - only for nodes we already added
            for (Suspect s : recentSuspects) {
                Node caseNode = nodeMap.get("CASE_" + s.getCaseId());
                Node crimNode = nodeMap.get("CRIM_" + s.getCriminalId());

                if (caseNode != null && crimNode != null) {
                    edges.add(new Edge(caseNode, crimNode));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        canvas.repaint();
    }

    private void updatePhysics() {
        if (nodes.isEmpty())
            return;

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width == 0)
            width = 800;
        if (height == 0)
            height = 600;

        // 1. Repulsion (Nodes push each other away)
        for (int i = 0; i < nodes.size(); i++) {
            Node n1 = nodes.get(i);
            if (n1 == draggedNode)
                continue;

            for (int j = i + 1; j < nodes.size(); j++) {
                Node n2 = nodes.get(j);
                double dx = n1.x - n2.x;
                double dy = n1.y - n2.y;
                double distSq = dx * dx + dy * dy;

                if (distSq < 100)
                    distSq = 100; // Avoid division by zero/singularity

                double force = REPULSION / distSq;
                double fx = (dx / Math.sqrt(distSq)) * force;
                double fy = (dy / Math.sqrt(distSq)) * force;

                n1.vx += fx;
                n1.vy += fy;
                n2.vx -= fx;
                n2.vy -= fy;
            }
        }

        // 2. Attraction (Connected nodes pull together)
        for (Edge e : edges) {
            Node n1 = e.source;
            Node n2 = e.target;

            double dx = n1.x - n2.x;
            double dy = n1.y - n2.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist == 0) {
                dx = Math.random() * 2 - 1;
                dy = Math.random() * 2 - 1;
                dist = Math.max(0.1, Math.sqrt(dx * dx + dy * dy));
            }

            // Hooke's Law: F = k * x
            double force = (dist - 150) * ATTRACTION; // 150 is ideal spring length

            double fx = (dx / dist) * force;
            double fy = (dy / dist) * force;

            if (n1 != draggedNode) {
                n1.vx -= fx;
                n1.vy -= fy;
            }
            if (n2 != draggedNode) {
                n2.vx += fx;
                n2.vy += fy;
            }
        }

        // 3. Gravity/Centering (Pull vaguely to center to prevent flying off)
        double centerX = width / 2;
        double centerY = height / 2;
        for (Node n : nodes) {
            if (n == draggedNode)
                continue;
            n.vx += (centerX - n.x) * 0.005;
            n.vy += (centerY - n.y) * 0.005;
        }

        // 4. Update Positions
        for (Node n : nodes) {
            if (n == draggedNode)
                continue;

            // Damping velocity
            n.vx *= DAMPING;
            n.vy *= DAMPING;

            // Limit velocity
            double v = Math.sqrt(n.vx * n.vx + n.vy * n.vy);
            if (v > MAX_VELOCITY) {
                n.vx = (n.vx / v) * MAX_VELOCITY;
                n.vy = (n.vy / v) * MAX_VELOCITY;
            }

            n.x += n.vx;
            n.y += n.vy;

            // Bounds check
            if (n.x < 30)
                n.x = 30;
            if (n.x > width - 30)
                n.x = width - 30;
            if (n.y < 30)
                n.y = 30;
            if (n.y > height - 30)
                n.y = height - 30;
        }
    }

    private enum Type {
        CRIMINAL, CASE
    }

    private static class Node {
        String label;
        Type type;
        double x, y;
        double vx, vy;

        Node(String label, Type type, double x, double y) {
            this.label = label;
            this.type = type;
            this.x = x;
            this.y = y;
        }

        boolean contains(Point p) {
            int r = 40; // hit box size
            return (p.x >= x - r / 2 && p.x <= x + r / 2 && p.y >= y - r / 2 && p.y <= y + r / 2);
        }
    }

    private static class Edge {
        Node source, target;

        Edge(Node s, Node t) {
            this.source = s;
            this.target = t;
        }
    }

    private class GraphCanvas extends JPanel {
        GraphCanvas() {
            setOpaque(false);

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    for (Node n : nodes) {
                        if (n.contains(e.getPoint())) {
                            draggedNode = n;
                            break;
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    draggedNode = null;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggedNode != null) {
                        draggedNode.x = e.getX();
                        draggedNode.y = e.getY();
                        draggedNode.vx = 0;
                        draggedNode.vy = 0;
                        repaint();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    Node oldHover = hoveredNode;
                    hoveredNode = null;
                    for (Node n : nodes) {
                        if (n.contains(e.getPoint())) {
                            hoveredNode = n;
                            break;
                        }
                    }
                    if (oldHover != hoveredNode) {
                        repaint();
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Edges
            g2.setStroke(new BasicStroke(2));
            for (Edge e : edges) {
                g2.setColor(new Color(60, 70, 100)); // Dark line
                if (hoveredNode != null && (e.source == hoveredNode || e.target == hoveredNode)) {
                    g2.setColor(DesignSystem.ACCENT); // Highlight connected edges
                    g2.setStroke(new BasicStroke(3));
                } else {
                    g2.setStroke(new BasicStroke(2));
                }
                g2.drawLine((int) e.source.x, (int) e.source.y, (int) e.target.x, (int) e.target.y);
            }

            // Draw Nodes
            for (Node n : nodes) {
                int r = 40; // Size
                int x = (int) (n.x - r / 2);
                int y = (int) (n.y - r / 2);

                Color baseColor = (n.type == Type.CRIMINAL) ? DesignSystem.DANGER : DesignSystem.PRIMARY;

                // Highlight effects (Glow)
                if (n == hoveredNode || n == draggedNode) {
                    RadialGradientPaint rgp = new RadialGradientPaint(
                            (float) n.x, (float) n.y, r + 10,
                            new float[] { 0.0f, 1.0f },
                            new Color[] { new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100),
                                    new Color(0, 0, 0, 0) });
                    g2.setPaint(rgp);
                    g2.fillOval(x - 10, y - 10, r + 20, r + 20);
                }

                // Node Shape
                g2.setColor(baseColor);
                if (n.type == Type.CRIMINAL) {
                    g2.fillOval(x, y, r, r);
                    // Add a small "C" for criminal
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                    g2.drawString("C", (int) n.x - 7, (int) n.y + 7);
                } else {
                    g2.fillRoundRect(x, y, r, r, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    g2.drawString("CS", (int) n.x - 11, (int) n.y + 6); // CS for Case
                }

                // Node Border
                g2.setColor(new Color(255, 255, 255, 150));
                g2.setStroke(new BasicStroke(2));
                if (n.type == Type.CRIMINAL) {
                    g2.drawOval(x, y, r, r);
                } else {
                    g2.drawRoundRect(x, y, r, r, 12, 12);
                }

                // Label
                g2.setFont(DesignSystem.CAPTION_FONT);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(n.label);

                g2.setColor(new Color(10, 15, 30, 220));
                g2.fillRoundRect((int) n.x - textW / 2 - 8, (int) n.y + r / 2 + 8, textW + 16, 22, 8, 8);

                g2.setColor(DesignSystem.TEXT_PRIMARY);
                g2.drawString(n.label, (int) n.x - textW / 2, (int) n.y + r / 2 + 23);
            }
        }
    }
}
