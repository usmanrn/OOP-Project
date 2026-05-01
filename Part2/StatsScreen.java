import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class StatsScreen extends JFrame {

    private final List<RaceRecord> raceResults;
    private final StatsManager     stats   = StatsManager.getInstance();
    private final RewardManager    rewards = RewardManager.getInstance();

    public StatsScreen(List<RaceRecord> raceResults) {
        this.raceResults = raceResults;

        setTitle("Results & Statistics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Race Results",    buildResultsPanel());
        tabs.addTab("Personal Bests",  buildPersonalBestsPanel());
        tabs.addTab("History",         buildHistoryPanel());
        tabs.addTab("Compare",         buildComparePanel());
        tabs.addTab("Leaderboard",     buildLeaderboardPanel());
        tabs.addTab("Financials",      buildFinancialsPanel());

        add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        JButton newRaceBtn = new JButton("New Race");
        newRaceBtn.addActionListener(e -> { dispose(); new TypingRaceGUI(); });
        bottom.add(newRaceBtn);
        add(bottom, BorderLayout.SOUTH);

        setSize(750, 520);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Tab 1: race results
    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = { "Pos", "Name", "WPM", "Accuracy %", "Burnouts", "Acc Before", "Acc After", "Points", "Coins" };
        Object[][] data = new Object[raceResults.size()][cols.length];

        RewardManager rewards = RewardManager.getInstance();
        for (int i = 0; i < raceResults.size(); i++) {
            RaceRecord r = raceResults.get(i);
            int pts   = rewards.calculatePoints(r.position, r.wpm, r.burnouts);
            int coins = rewards.calculateEarnings(r.position, r.wpm, r.burnouts, 0, r.accuracyPercent);
            data[i][0] = r.position;
            data[i][1] = r.name;
            data[i][2] = String.format("%.1f", r.wpm);
            data[i][3] = String.format("%.1f%%", r.accuracyPercent);
            data[i][4] = r.burnouts;
            data[i][5] = String.format("%.3f", r.accuracyBefore);
            data[i][6] = String.format("%.3f", r.accuracyAfter);
            data[i][7] = pts;
            data[i][8] = coins;
        }

        JTable table = new JTable(data, cols);
        table.setEnabled(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Personal best notices
        JPanel notices = new JPanel(new GridLayout(raceResults.size(), 1));
        for (RaceRecord r : raceResults) {
            double pb = stats.getPersonalBest(r.name);
            if (Math.abs(r.wpm - pb) < 0.01) {
                notices.add(new JLabel("  ** " + r.name + " set a new personal best: "
                    + String.format("%.1f", pb) + " WPM!"));
            }
            List<String> b = rewards.getBadges(r.name);
            if (!b.isEmpty()) {
                notices.add(new JLabel("  Badges for " + r.name + ": " + String.join(", ", b)));
            }
        }
        panel.add(notices, BorderLayout.SOUTH);
        return panel;
    }

    // Tab 2: personal bests
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

    // Tab 3: history
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        List<String> names = stats.getAllNames();
        if (names.isEmpty()) {
            panel.add(new JLabel("No history yet."), BorderLayout.CENTER);
            return panel;
        }

        JComboBox<String> picker = new JComboBox<>(names.toArray(new String[0]));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Show history for:"));
        top.add(picker);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = { "Race #", "Pos", "WPM", "Accuracy %", "Burnouts" };
        JScrollPane[] scrollRef = new JScrollPane[1];
        scrollRef[0] = new JScrollPane(new JTable(new Object[0][0], cols));
        panel.add(scrollRef[0], BorderLayout.CENTER);

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
            panel.remove(scrollRef[0]);
            JTable newTable = new JTable(data, cols);
            newTable.setEnabled(false);
            scrollRef[0] = new JScrollPane(newTable);
            panel.add(scrollRef[0], BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });

        picker.getActionListeners()[0].actionPerformed(null);
        return panel;
    }

    // Tab 4: compare
    private JPanel buildComparePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        List<String> names = stats.getAllNames();
        if (names.size() < 2) {
            panel.add(new JLabel("Need at least 2 typists with history to compare."), BorderLayout.CENTER);
            return panel;
        }

        JComboBox<String> picker1 = new JComboBox<>(names.toArray(new String[0]));
        JComboBox<String> picker2 = new JComboBox<>(names.toArray(new String[0]));
        if (names.size() > 1) picker2.setSelectedIndex(1);

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
            List<RaceRecord> h1 = stats.getHistory(n1);
            List<RaceRecord> h2 = stats.getHistory(n2);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-28s %-15s %-15s%n", "Metric", n1, n2));
            sb.append("-".repeat(58)).append("\n");
            sb.append(String.format("%-28s %-15s %-15s%n", "Best WPM",
                String.format("%.1f", stats.getPersonalBest(n1)),
                String.format("%.1f", stats.getPersonalBest(n2))));
            sb.append(String.format("%-28s %-15s %-15s%n", "Avg WPM",
                String.format("%.1f", h1.stream().mapToDouble(r -> r.wpm).average().orElse(0)),
                String.format("%.1f", h2.stream().mapToDouble(r -> r.wpm).average().orElse(0))));
            sb.append(String.format("%-28s %-15s %-15s%n", "Avg Accuracy %",
                String.format("%.1f%%", h1.stream().mapToDouble(r -> r.accuracyPercent).average().orElse(0)),
                String.format("%.1f%%", h2.stream().mapToDouble(r -> r.accuracyPercent).average().orElse(0))));
            sb.append(String.format("%-28s %-15s %-15s%n", "Total Burnouts",
                h1.stream().mapToInt(r -> r.burnouts).sum(),
                h2.stream().mapToInt(r -> r.burnouts).sum()));
            sb.append(String.format("%-28s %-15s %-15s%n", "Total Points",
                rewards.getPoints(n1), rewards.getPoints(n2)));
            sb.append(String.format("%-28s %-15s %-15s%n", "Total Coins",
                rewards.getEarnings(n1), rewards.getEarnings(n2)));
            sb.append(String.format("%-28s %-15s %-15s%n", "Badges",
                String.join(", ", rewards.getBadges(n1)),
                String.join(", ", rewards.getBadges(n2))));
            output.setText(sb.toString());
        };

        picker1.addActionListener(e -> refresh.run());
        picker2.addActionListener(e -> refresh.run());
        refresh.run();
        return panel;
    }

    // Tab 5: points leaderboard (Option A)
    private JPanel buildLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JLabel("  Points Leaderboard (Option A)", JLabel.LEFT), BorderLayout.NORTH);

        Map<String, Integer> allPoints = rewards.getAllPoints();
        List<String> names = new ArrayList<>(allPoints.keySet());
        names.sort((a, b) -> allPoints.get(b) - allPoints.get(a));

        String[] cols = { "Rank", "Name", "Total Points", "Badges" };
        Object[][] data = new Object[names.size()][4];
        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            data[i][0] = i + 1;
            data[i][1] = n;
            data[i][2] = allPoints.get(n);
            data[i][3] = String.join(", ", rewards.getBadges(n));
        }

        JTable table = new JTable(data, cols);
        table.setEnabled(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Tab 6: financial leaderboard + upgrades shop (Option B)
    private JPanel buildFinancialsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top: earnings leaderboard
        Map<String, Integer> allEarnings = rewards.getAllEarnings();
        List<String> names = new ArrayList<>(allEarnings.keySet());
        names.sort((a, b) -> allEarnings.get(b) - allEarnings.get(a));

        String[] cols = { "Rank", "Name", "Total Coins", "Upgrades Owned" };
        Object[][] data = new Object[names.size()][4];
        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            data[i][0] = i + 1;
            data[i][1] = n;
            data[i][2] = allEarnings.get(n);
            data[i][3] = String.join(", ", rewards.getUpgrades(n));
        }
        JTable table = new JTable(data, cols);
        table.setEnabled(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom: upgrade shop
        JPanel shop = new JPanel(new BorderLayout());
        shop.setBorder(BorderFactory.createTitledBorder("Upgrade Shop (Option B)"));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> typistPicker = new JComboBox<>(names.toArray(new String[0]));
        JComboBox<String> upgradePicker = new JComboBox<>(RewardManager.UPGRADE_NAMES);
        JButton buyBtn = new JButton("Buy");
        JLabel shopMsg = new JLabel("");

        buyBtn.addActionListener(e -> {
            String typist = (String) typistPicker.getSelectedItem();
            int upgradeIdx = upgradePicker.getSelectedIndex();
            if (typist == null) return;
            boolean success = rewards.buyUpgrade(typist, upgradeIdx);
            if (success) {
                shopMsg.setText("Bought for " + typist + "! Coins left: " + rewards.getEarnings(typist));
            } else {
                shopMsg.setText("Not enough coins!");
            }
        });

        controls.add(new JLabel("Typist:"));
        controls.add(typistPicker);
        controls.add(new JLabel("Upgrade:"));
        controls.add(upgradePicker);
        controls.add(buyBtn);
        controls.add(shopMsg);

        shop.add(controls, BorderLayout.CENTER);
        panel.add(shop, BorderLayout.SOUTH);

        return panel;
    }
}