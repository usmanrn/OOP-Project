import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

public class RaceScreen extends JFrame {

    private final String             passage;
    private final List<TypistConfig> configs;
    private final boolean autocorrect;
    private final boolean caffeineMode;

    private static final double MISTYPE_BASE_CHANCE = 0.30;
    private static final int    BURNOUT_DURATION    = 3;

    private List<Typist> typists;
    private int[]        burnoutCounts;
    private int[]        totalKeystrokes;
    private int[]        mistypeCounts;
    private double[]     accuracyBefore;
    private long         raceStartTime;

    private JTextPane[] passagePanes;
    private JLabel[]    statusLabels;
    private JLabel      headerLabel;

    public RaceScreen(String passage, List<TypistConfig> configs,
                      boolean autocorrect, boolean caffeineMode, boolean nightShift)
    {
        this.passage      = passage;
        this.configs      = configs;
        this.autocorrect  = autocorrect;
        this.caffeineMode = caffeineMode;

        buildTypists();
        buildUI();
        startRaceThread();
    }

    private void buildTypists() {
        int n = configs.size();
        typists         = new ArrayList<>();
        burnoutCounts   = new int[n];
        totalKeystrokes = new int[n];
        mistypeCounts   = new int[n];
        accuracyBefore  = new double[n];

        for (int i = 0; i < n; i++) {
            TypistConfig cfg = configs.get(i);
            typists.add(new Typist(cfg.symbol, cfg.name, cfg.baseAccuracy));
            accuracyBefore[i] = cfg.baseAccuracy;
        }
    }

    private void buildUI() {
        setTitle("Typing Race");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        headerLabel = new JLabel("Race in progress...", JLabel.CENTER);
        add(headerLabel, BorderLayout.NORTH);

        int n = configs.size();
        passagePanes = new JTextPane[n];
        statusLabels = new JLabel[n];

        JPanel lanePanel = new JPanel();
        lanePanel.setLayout(new BoxLayout(lanePanel, BoxLayout.Y_AXIS));
        lanePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < n; i++) {
            JPanel row = new JPanel(new BorderLayout(5, 5));
            row.setBorder(BorderFactory.createTitledBorder(
                configs.get(i).name + " " + configs.get(i).symbol
                + "  (acc: " + String.format("%.2f", configs.get(i).baseAccuracy) + ")"));

            JTextPane pane = new JTextPane();
            pane.setText(passage);
            pane.setEditable(false);
            pane.setFont(new Font("Monospaced", Font.PLAIN, 13));
            passagePanes[i] = pane;
            row.add(new JScrollPane(pane), BorderLayout.CENTER);

            JLabel status = new JLabel("Ready");
            status.setPreferredSize(new Dimension(200, 20));
            statusLabels[i] = status;
            row.add(status, BorderLayout.SOUTH);

            lanePanel.add(row);
            lanePanel.add(Box.createVerticalStrut(6));
        }

        add(new JScrollPane(lanePanel), BorderLayout.CENTER);

        setSize(800, 160 + n * 130);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateHighlight(int i, int progress) {
        JTextPane pane = passagePanes[i];
        Highlighter highlighter = pane.getHighlighter();
        highlighter.removeAllHighlights();

        int safeProgress = Math.min(progress, passage.length());
        if (safeProgress <= 0) return;

        try {
            Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(configs.get(i).colour);
            highlighter.addHighlight(0, safeProgress, painter);
        } catch (BadLocationException e) {
            // ignore
        }
    }

    private void startRaceThread() {
        raceStartTime = System.currentTimeMillis();
        for (Typist t : typists) t.resetToStart();

        Thread raceThread = new Thread(() -> {
            int turn         = 0;
            boolean finished = false;
            int winnerIndex  = -1;

            while (!finished) {
                turn++;
                final int currentTurn = turn;

                for (int i = 0; i < typists.size(); i++) {
                    advanceTypist(i, currentTurn);
                }

                final int[]     snap_progress = new int[typists.size()];
                final boolean[] snap_burnt    = new boolean[typists.size()];
                final int[]     snap_turns    = new int[typists.size()];
                final double[]  snap_acc      = new double[typists.size()];

                for (int i = 0; i < typists.size(); i++) {
                    snap_progress[i] = typists.get(i).getProgress();
                    snap_burnt[i]    = typists.get(i).isBurntOut();
                    snap_turns[i]    = typists.get(i).getBurnoutTurnsRemaining();
                    snap_acc[i]      = typists.get(i).getAccuracy();
                }

                final long elapsed = System.currentTimeMillis() - raceStartTime;
                SwingUtilities.invokeLater(() ->
                    updateUI(snap_progress, snap_burnt, snap_turns, snap_acc, elapsed));

                for (int i = 0; i < typists.size(); i++) {
                    if (typists.get(i).getProgress() >= passage.length()) {
                        finished    = true;
                        winnerIndex = i;
                        break;
                    }
                }

                try { Thread.sleep(220); } catch (InterruptedException ignored) {}
            }

            final int winner = winnerIndex;
            SwingUtilities.invokeLater(() -> showResults(winner));
        });

        raceThread.setDaemon(true);
        raceThread.start();
    }

    private void advanceTypist(int i, int turn) {
        Typist       t   = typists.get(i);
        TypistConfig cfg = configs.get(i);

        if (t.isBurntOut()) { t.recoverFromBurnout(); return; }

        double accMod = 0.0;
        if (cfg.hasEnergyDrink) {
            accMod = (turn <= passage.length() / 4) ? +0.10 : -0.10;
        }
        double speedMod     = (caffeineMode && turn <= 10) ? 1.25 : 1.0;
        double effectiveAcc = Math.min(0.99, Math.max(0.01, t.getAccuracy() + accMod));

        totalKeystrokes[i]++;
        if (Math.random() < effectiveAcc * cfg.speedMultiplier * speedMod) {
            t.typeCharacter();
        }

        double mistypeChance = (1.0 - effectiveAcc)
                               * MISTYPE_BASE_CHANCE
                               * cfg.effectiveMistypeMultiplier();
        if (autocorrect) mistypeChance *= 0.5;
        if (Math.random() < mistypeChance) {
            t.slideBack(autocorrect ? 1 : 2);
            mistypeCounts[i]++;
            totalKeystrokes[i]++;
        }

        double burnoutChance = 0.05 * effectiveAcc * effectiveAcc * cfg.burnoutMultiplier;
        if (caffeineMode && turn > 10) burnoutChance *= 1.5;
        if (Math.random() < burnoutChance) {
            t.burnOut(cfg.effectiveBurnoutDuration(BURNOUT_DURATION));
            burnoutCounts[i]++;
        }
    }

    private void updateUI(int[] progress, boolean[] burnt, int[] burnoutTurns,
                          double[] acc, long elapsedMs) {
        for (int i = 0; i < typists.size(); i++) {
            updateHighlight(i, progress[i]);
            if (burnt[i]) {
                statusLabels[i].setText("BURNT OUT (" + burnoutTurns[i] + " turns remaining)");
            } else {
                double wpm = (progress[i] / 5.0) / Math.max(0.001, elapsedMs / 60000.0);
                statusLabels[i].setText(String.format("%.0f WPM | acc: %.2f", wpm, acc[i]));
            }
        }
    }

    private void showResults(int winnerIndex) {
        long elapsedMs = System.currentTimeMillis() - raceStartTime;

        // sort by who got furthest
        Integer[] order = new Integer[typists.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;

        // bubble sorting to find the furthest
        for (int i = 0; i < order.length - 1; i++) {
            for (int j = 0; j < order.length - 1 - i; j++) {
                int temp = order[j];
                if (typists.get(order[j]).getProgress() < typists.get(order[j+1]).getProgress()) {
                    order[j] = order[j+1];
                    order[j+1] = temp;
                }
            }
        }

        List<RaceRecord> results  = new ArrayList<>();
        StatsManager   stats     = StatsManager.getInstance();
        RewardManager  rewards   = RewardManager.getInstance();

        for (int rank = 0; rank < order.length; rank++) {
            int i    = order[rank];
            Typist t = typists.get(i);

            double wpm = (t.getProgress() / 5.0) / Math.max(0.001, elapsedMs / 60000.0);

            double accPct = totalKeystrokes[i] == 0 ? 100.0
                : ((totalKeystrokes[i] - mistypeCounts[i]) / (double) totalKeystrokes[i]) * 100.0;

            double accBefore = accuracyBefore[i];
            double accAfter  = t.getAccuracy();

            if (i == winnerIndex) {
                t.setAccuracy(accAfter + 0.02);
                accAfter = t.getAccuracy();
            }

            int position = rank + 1;

            // Option A: points + badges
            int pts = rewards.calculatePoints(position, wpm, burnoutCounts[i]);
            rewards.addPoints(t.getName(), pts);
            rewards.updateBadges(t.getName(), position, burnoutCounts[i]);

            // Option B: earnings
            int coins = rewards.calculateEarnings(
                position, wpm, burnoutCounts[i],
                configs.get(i).sponsorIndex, accPct);
            rewards.addEarnings(t.getName(), coins);

            RaceRecord record = new RaceRecord(
                t.getName(), wpm, accPct,
                burnoutCounts[i], accBefore, accAfter, position
            );
            results.add(record);
            stats.addRecord(record);
        }

        headerLabel.setText("Winner: " + typists.get(winnerIndex).getName() + "! ("
            + String.format("%.2f", elapsedMs / 1000.0) + "s)");

        dispose();
        new StatsScreen(results);
    }
}