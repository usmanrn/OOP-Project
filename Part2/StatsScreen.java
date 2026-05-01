import java.awt.*;
import java.util.List;
import javax.swing.*;

public class StatsScreen extends JFrame {

    private final List<RaceRecord> raceResults;
    private final StatsManager     stats = StatsManager.getInstance();

    public StatsScreen(List<RaceRecord> raceResults) {
        this.raceResults = raceResults;

        setTitle("Race Results & Statistics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Race Results",   buildResultsPanel());
        tabs.addTab("Personal Bests", buildPersonalBestsPanel());
        tabs.addTab("History",        buildHistoryPanel());
        tabs.addTab("Compare",        buildComparePanel());

        add(tabs, BorderLayout.CENTER);

        JButton newRaceBtn = new JButton("New Race");
        newRaceBtn.addActionListener(e -> { dispose(); new TypingRaceGUI(); });
        JPanel bottom = new JPanel();
        bottom.add(newRaceBtn);
        add(bottom, BorderLayout.SOUTH);

        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Tab 1: results from this race
    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = { "Pos", "Name", "WPM", "Accuracy %", "Burnouts", "Acc Before", "Acc After" };
        Object[][] data = new Object[raceResults.size()][cols.length];

        for (int i = 0; i < raceResults.size(); i++) {
            RaceRecord r = raceResults.get(i);
            data[i][0] = r.position;
            data[i][1] = r.name;
            data[i][2] = String.format("%.1f", r.wpm);
            data[i][3] = String.format("%.1f%%", r.accuracyPercent);
            data[i][4] = r.burnouts;
            data[i][5] = String.format("%.3f", r.accuracyBefore);
            data[i][6] = String.format("%.3f", r.accuracyAfter);
        }

        JTable table = new JTable(data, cols);
        table.setEnabled(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Personal best notice
        JPanel notices = new JPanel(new GridLayout(raceResults.size(), 1));
        for (RaceRecord r : raceResults) {
            double pb = stats.getPersonalBest(r.name);
            if (Math.abs(r.wpm - pb) < 0.01) {
                notices.add(new JLabel("  ** " + r.name + " set a new personal best: "
                    + String.format("%.1f", pb) + " WPM!"));
            }
        }
        panel.add(notices, BorderLayout.SOUTH);

        return panel;
    }

    // Tab 2: best WPM per typist across all races
    private JPanel buildPersonalBestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        List<String> names = stats.getAllNames();
        String[] cols = { "Name", "Best WPM" };
        Object[][] data = new Object[names.size()][2];

        for (int i = 0; i < names.size(); i++) {
            data[i][0] = names.get(i);
            data[i][1] = String.format("%.1f", stats.getPersonalBest(names.get(i)));
        }

        JTable table = new JTable(data, cols);
        table.setEnabled(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Tab 3: full race history for each typist
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        List<String> names = stats.getAllNames();
        if (names.isEmpty()) {
            panel.add(new JLabel("No history yet."), BorderLayout.CENTER);
            return panel;
        }

        // Dropdown to pick a typist
        JComboBox<String> picker = new JComboBox<>(names.toArray(new String[0]));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Show history for:"));
        top.add(picker);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = { "Race #", "Pos", "WPM", "Accuracy %", "Burnouts" };
        JTable table = new JTable(new Object[0][0], cols);
        table.setEnabled(false);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        // Update table when typist is selected
        picker.addActionListener(e -> {
            String selected = (String) picker.getSelectedItem();
            List<RaceRecord> records = stats.getHistory(selected);
            Object[][] data = new Object[records.size()][cols.length];
            for (int i = 0; i < records.size(); i++) {
                RaceRecord r = records.get(i);
                data[i][0] = i + 1;
                data[i][1] = r.position;
                data[i][2] = String.format("%.1f", r.wpm);
                data[i][3] = String.format("%.1f%%", r.accuracyPercent);
                data[i][4] = r.burnouts;
            }
            panel.remove(scroll);
            JTable newTable = new JTable(data, cols);
            newTable.setEnabled(false);
            panel.add(new JScrollPane(newTable), BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });

        // Trigger initial load
        picker.getActionListeners()[0].actionPerformed(null);

        return panel;
    }

    // Tab 4: compare two typists side by side
    private JPanel buildComparePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        List<String> names = stats.getAllNames();
        if (names.size() < 2) {
            panel.add(new JLabel("Need at least 2 typists to compare."), BorderLayout.CENTER);
            return panel;
        }

        JComboBox<String> picker1 = new JComboBox<>(names.toArray(new String[0]));
        JComboBox<String> picker2 = new JComboBox<>(names.toArray(new String[0]));
        picker2.setSelectedIndex(1);

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Compare:"));
        top.add(picker1);
        top.add(new JLabel("vs"));
        top.add(picker2);
        panel.add(top, BorderLayout.NORTH);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(output), BorderLayout.CENTER);

        Runnable refresh = () -> {
            String n1 = (String) picker1.getSelectedItem();
            String n2 = (String) picker2.getSelectedItem();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-30s %-15s %-15s%n", "Metric", n1, n2));
            sb.append("-".repeat(60)).append("\n");

            double pb1 = stats.getPersonalBest(n1);
            double pb2 = stats.getPersonalBest(n2);
            sb.append(String.format("%-30s %-15s %-15s%n",
                "Best WPM",
                String.format("%.1f", pb1),
                String.format("%.1f", pb2)));

            List<RaceRecord> h1 = stats.getHistory(n1);
            List<RaceRecord> h2 = stats.getHistory(n2);

            double avgWpm1 = h1.stream().mapToDouble(r -> r.wpm).average().orElse(0);
            double avgWpm2 = h2.stream().mapToDouble(r -> r.wpm).average().orElse(0);
            sb.append(String.format("%-30s %-15s %-15s%n",
                "Avg WPM",
                String.format("%.1f", avgWpm1),
                String.format("%.1f", avgWpm2)));

            double avgAcc1 = h1.stream().mapToDouble(r -> r.accuracyPercent).average().orElse(0);
            double avgAcc2 = h2.stream().mapToDouble(r -> r.accuracyPercent).average().orElse(0);
            sb.append(String.format("%-30s %-15s %-15s%n",
                "Avg Accuracy %",
                String.format("%.1f%%", avgAcc1),
                String.format("%.1f%%", avgAcc2)));

            int totalBurnouts1 = h1.stream().mapToInt(r -> r.burnouts).sum();
            int totalBurnouts2 = h2.stream().mapToInt(r -> r.burnouts).sum();
            sb.append(String.format("%-30s %-15s %-15s%n",
                "Total Burnouts",
                totalBurnouts1,
                totalBurnouts2));

            sb.append(String.format("%-30s %-15s %-15s%n",
                "Races Completed",
                h1.size(),
                h2.size()));

            output.setText(sb.toString());
        };

        picker1.addActionListener(e -> refresh.run());
        picker2.addActionListener(e -> refresh.run());
        refresh.run();

        return panel;
    }
}