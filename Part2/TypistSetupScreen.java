import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TypistSetupScreen extends JFrame {

    private final String  passage;
    private final int     seatCount;
    private final boolean autocorrect;
    private final boolean caffeineMode;
    private final boolean nightShift;

    private final List<JTextField>        nameFields      = new ArrayList<>();
    private final List<JTextField>        symbolFields    = new ArrayList<>();
    private final List<Color>             chosenColours   = new ArrayList<>();
    private final List<JComboBox<String>> styleBoxes      = new ArrayList<>();
    private final List<JComboBox<String>> keyboardBoxes   = new ArrayList<>();
    private final List<JCheckBox>         wristChecks     = new ArrayList<>();
    private final List<JCheckBox>         drinkChecks     = new ArrayList<>();
    private final List<JCheckBox>         headphoneChecks = new ArrayList<>();

    private static final String[] STYLES = {
        "Touch Typist", "Hunt & Peck", "Phone Thumbs", "Voice-to-Text"
    };
    private static final double[] STYLE_ACC_DELTA    = { +0.10, -0.20, -0.10, -0.05 };
    private static final double[] STYLE_BURNOUT_MULT = {  1.3,   0.7,   0.9,   0.6  };

    private static final String[] KEYBOARDS = {
        "Mechanical", "Membrane", "Touchscreen", "Stenography"
    };
    private static final double[] KB_ACC_DELTA    = { +0.05,  0.00, -0.10, +0.15 };
    private static final double[] KB_SPEED_MULT   = {  1.1,   1.0,   0.9,   1.4  };
    private static final double[] KB_MISTYPE_MULT = {  0.9,   1.0,   1.2,   0.7  };

    private static final char[]  DEFAULT_SYMBOLS = { '①','②','③','④','⑤','⑥' };
    private static final Color[] DEFAULT_COLOURS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN
    };

    public TypistSetupScreen(String passage, int seatCount,
                             boolean autocorrect, boolean caffeineMode, boolean nightShift)
    {
        this.passage      = passage;
        this.seatCount    = seatCount;
        this.autocorrect  = autocorrect;
        this.caffeineMode = caffeineMode;
        this.nightShift   = nightShift;

        setTitle("Typist Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildCards(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(600, 150 + seatCount * 180);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JScrollPane buildCards() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        for (int i = 0; i < seatCount; i++) {
            container.add(buildCard(i));
        }

        return new JScrollPane(container);
    }

    private JPanel buildCard(int i) {
        chosenColours.add(DEFAULT_COLOURS[i]);

        JPanel card = new JPanel(new GridLayout(0, 2, 5, 5));
        card.setBorder(BorderFactory.createTitledBorder("Typist " + (i + 1)));

        card.add(new JLabel("Name:"));
        JTextField nameField = new JTextField("TYPIST_" + (i + 1));
        nameFields.add(nameField);
        card.add(nameField);

        card.add(new JLabel("Symbol:"));
        JTextField symField = new JTextField(String.valueOf(DEFAULT_SYMBOLS[i]));
        symbolFields.add(symField);
        card.add(symField);

        card.add(new JLabel("Colour:"));
        JButton colBtn = new JButton("Pick Colour");
        final int idx = i;
        colBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Pick colour", chosenColours.get(idx));
            if (c != null) chosenColours.set(idx, c);
        });
        card.add(colBtn);

        card.add(new JLabel("Typing Style:"));
        JComboBox<String> styleBox = new JComboBox<>(STYLES);
        styleBoxes.add(styleBox);
        card.add(styleBox);

        card.add(new JLabel("  Touch Typist=+0.10acc  Hunt&Peck=-0.20acc"));
        card.add(new JLabel("  PhoneThumbs=-0.10acc  Voice=-0.05acc"));

        card.add(new JLabel("Keyboard:"));
        JComboBox<String> kbBox = new JComboBox<>(KEYBOARDS);
        keyboardBoxes.add(kbBox);
        card.add(kbBox);

        card.add(new JLabel("  Mechanical=+0.05acc  Membrane=none"));
        card.add(new JLabel("  Touchscreen=-0.10acc  Steno=+0.15acc"));

        card.add(new JLabel("Accessories:"));
        card.add(new JLabel(""));

        JCheckBox wrist = new JCheckBox("Wrist Support (burnout duration -1)");
        wristChecks.add(wrist);
        card.add(wrist);
        card.add(new JLabel(""));

        JCheckBox drink = new JCheckBox("Energy Drink (+0.10 acc first half, -0.10 second)");
        drinkChecks.add(drink);
        card.add(drink);
        card.add(new JLabel(""));

        JCheckBox phones = new JCheckBox("Headphones (mistype chance x0.75)");
        headphoneChecks.add(phones);
        card.add(phones);
        card.add(new JLabel(""));

        return card;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton startBtn = new JButton("Start Race");
        startBtn.addActionListener(e -> onStart());
        panel.add(startBtn);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> { dispose(); new TypingRaceGUI(); });
        panel.add(backBtn);

        return panel;
    }

    private void onStart() {
        List<TypistConfig> configs = new ArrayList<>();

        for (int i = 0; i < seatCount; i++) {
            String name   = nameFields.get(i).getText().trim();
            String symStr = symbolFields.get(i).getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Typist " + (i + 1) + " needs a name!");
                return;
            }
            if (symStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Typist " + (i + 1) + " needs a symbol!");
                return;
            }

            int si = styleBoxes.get(i).getSelectedIndex();
            int ki = keyboardBoxes.get(i).getSelectedIndex();

            double acc = 0.75 + STYLE_ACC_DELTA[si] + KB_ACC_DELTA[ki];
            if (nightShift) acc -= 0.08;
            acc = Math.max(0.05, Math.min(0.99, acc));

            configs.add(new TypistConfig(
                symStr.charAt(0), name, acc, chosenColours.get(i),
                STYLE_BURNOUT_MULT[si],
                KB_SPEED_MULT[ki],
                KB_MISTYPE_MULT[ki],
                wristChecks.get(i).isSelected(),
                drinkChecks.get(i).isSelected(),
                headphoneChecks.get(i).isSelected()
            ));
        }

        dispose();
        new RaceScreen(passage, configs, autocorrect, caffeineMode, nightShift);
    }
}