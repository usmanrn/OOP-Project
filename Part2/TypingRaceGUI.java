import java.awt.*;
import javax.swing.*;

public class TypingRaceGUI
{
    // Main window
    private JFrame frame;

    // Passage selection
    private JComboBox<String> passageDropdown;
    private JTextArea customPassageField;

    // Seat count
    private JSpinner seatCountSpinner;

    // Difficulty modifiers
    private JCheckBox autocorrectCheckbox;
    private JCheckBox caffeineModeCheckbox;
    private JCheckBox nightShiftCheckbox;

    // Start button
    private JButton startButton;

    public TypingRaceGUI()
    {
        buildSetupScreen();
    }

    private void buildSetupScreen()
    {
        // Create main window
        frame = new JFrame("Typing Race Simulator");
        frame.setSize(700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Title at the top
        JLabel titleLabel = new JLabel("TYPING RACE SIMULATOR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        frame.add(titleLabel, BorderLayout.NORTH);

        // Main settings panel in the centre
        JPanel settingsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // passage selection 
        JPanel passagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passagePanel.setBorder(BorderFactory.createTitledBorder("Passage Selection"));

        String[] passages = {
            "Short: The quick brown fox jumps over the lazy dog",
            "Medium: How razorback jumping frogs can level six piqued gymnasts quickly",
            "Long: The five boxing wizards jump quickly while the quick brown fox jumps over the lazy dog near the river bank",
            "Custom"
        };
        passageDropdown = new JComboBox<>(passages);

        customPassageField = new JTextArea(2, 30);
        customPassageField.setLineWrap(true);
        customPassageField.setWrapStyleWord(true);
        customPassageField.setVisible(false);
        customPassageField.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // show custom text box only when Custom is selected
        passageDropdown.addActionListener(e -> {
            boolean isCustom = passageDropdown.getSelectedIndex() == 3;
            customPassageField.setVisible(isCustom);
            frame.revalidate();
            frame.repaint();
        });

        passagePanel.add(new JLabel("Select passage: "));
        passagePanel.add(passageDropdown);
        passagePanel.add(customPassageField);
        settingsPanel.add(passagePanel);

        // seat count
        JPanel seatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        seatPanel.setBorder(BorderFactory.createTitledBorder("Number of Typists"));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(2, 2, 6, 1);
        seatCountSpinner = new JSpinner(spinnerModel);

        seatPanel.add(new JLabel("How many typists? (2-6): "));
        seatPanel.add(seatCountSpinner);
        settingsPanel.add(seatPanel);

        // difficulty modifiers 
        JPanel modifierPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        modifierPanel.setBorder(BorderFactory.createTitledBorder("Difficulty Modifiers"));

        autocorrectCheckbox = new JCheckBox(
            "Autocorrect — slideBack amount is halved");
        caffeineModeCheckbox = new JCheckBox(
            "Caffeine Mode — speed boost for first 10 turns, then increased burnout risk");
        nightShiftCheckbox = new JCheckBox(
            "Night Shift — all typist accuracy reduced by 0.1 (everyone is tired)");

        modifierPanel.add(autocorrectCheckbox);
        modifierPanel.add(caffeineModeCheckbox);
        modifierPanel.add(nightShiftCheckbox);
        settingsPanel.add(modifierPanel);

        frame.add(settingsPanel, BorderLayout.CENTER);

        // positioning the start button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButton = new JButton("NEXT: SET UP TYPISTS");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setBackground(new Color(0, 153, 0));
        startButton.setForeground(Color.WHITE);
        startButton.setOpaque(true);
        startButton.setBorderPainted(false);

        startButton.addActionListener(e -> goToTypistSetup());

        bottomPanel.add(startButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void goToTypistSetup()
    {
        // get selected passage text
        String passage;
        if (passageDropdown.getSelectedIndex() == 3)
        {
            passage = customPassageField.getText().trim();
            if (passage.isEmpty())
            {
                JOptionPane.showMessageDialog(frame,
                    "Please enter a custom passage!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        else
        {
            // get just the text part after the colon
            String selected = (String) passageDropdown.getSelectedItem();
            passage = selected.split(": ")[1];
        }

        // get number of typists
        int seatCount = (int) seatCountSpinner.getValue();

        // get modifiers
        boolean autocorrect = autocorrectCheckbox.isSelected();
        boolean caffeineMode = caffeineModeCheckbox.isSelected();
        boolean nightShift = nightShiftCheckbox.isSelected();

        // close setup screen and open typist customisation
        frame.dispose();
        new TypistSetupScreen(passage, seatCount, autocorrect, caffeineMode, nightShift);
    }

    public static void main(String[] args)
    {
        new TypingRaceGUI();
    }

    public static void startRaceGUI()
    {
        main(new String[0]);
    }
}