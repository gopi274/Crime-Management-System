package com.crimemanagement.ui;

import com.crimemanagement.service.CriminalService;
import com.crimemanagement.service.FIRService;
import com.crimemanagement.service.OfficerService;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {
    private final OfficerService officerService = new OfficerService();
    private final CriminalService criminalService = new CriminalService();
    private final FIRService firService = new FIRService();

    private final JLabel officerCount = createStatLabel("0");
    private final JLabel criminalCount = createStatLabel("0");
    private final JLabel firCount = createStatLabel("0");
    private JPanel chartHolder = new JPanel(new BorderLayout());

    public StatsPanel() {
        super(new BorderLayout());
        JLabel title = new JLabel("Dashboard", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 16));
        grid.add(card("Officers", officerCount));
        grid.add(card("Criminals", criminalCount));
        grid.add(card("FIRs", firCount));
        add(grid, BorderLayout.NORTH);

        add(chartHolder, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadStats());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refresh);
        add(south, BorderLayout.SOUTH);

        loadStats();
    }

    private void loadStats() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            long o; long c; long f;
            @Override protected Void doInBackground() throws Exception {
                try { o = officerService.getOfficerCount(); } catch (Exception ignore) { o = 0; }
                try { c = criminalService.getCriminalCount(); } catch (Exception ignore) { c = 0; }
                try { f = firService.getFIRCount(); } catch (Exception ignore) { f = 0; }
                return null;
            }
            @Override protected void done() {
                officerCount.setText(String.valueOf(o));
                criminalCount.setText(String.valueOf(c));
                firCount.setText(String.valueOf(f));
            }
        };
        worker.execute();
        loadCaseTypeChart();
    }

    private void loadCaseTypeChart() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            java.util.Map<String, Long> typeToCount = new java.util.HashMap<>();
            @Override protected Void doInBackground() throws Exception {
                try {
                    // Derive from FIR crime types as a proxy for "types of cases"
                    java.util.List<com.crimemanagement.entity.FIR> all = new FIRService().getAllFIRs();
                    for (com.crimemanagement.entity.FIR f : all) {
                        String type = f.getCrimeType() != null ? f.getCrimeType() : "Unknown";
                        typeToCount.put(type, typeToCount.getOrDefault(type, 0L) + 1);
                    }
                } catch (Exception ignore) {}
                return null;
            }
            @Override protected void done() {
                chartHolder.removeAll();
                PieChart chart = new PieChartBuilder().width(600).height(400).title("Case Types").build();
                if (typeToCount.isEmpty()) {
                    chart.addSeries("No Data", 1);
                } else {
                    for (var e : typeToCount.entrySet()) chart.addSeries(e.getKey(), e.getValue());
                }
                chartHolder.add(new XChartPanel<>(chart), BorderLayout.CENTER);
                chartHolder.revalidate();
                chartHolder.repaint();
            }
        };
        w.execute();
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.PLAIN, 14f));
        p.add(t, BorderLayout.NORTH);
        value.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JLabel createStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 28f));
        return l;
    }
}


