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
        frame.setSize(600, 500);
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

        // --- Passage Selection ---
        JPanel passagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passagePanel.setBorder(BorderFactory.createTitledBorder("Passage Selection"));

        String[] passages = {
            "Short: The quick brown fox jumps over the lazy dog",
            "Medium: Pack my box with five dozen liquor jugs...",
            "Long: How vexingly quick daft zebras jump...",
            "Custom"
        };
        passageDropdown = new JComboBox<>(passages);

        customPassageField = new JTextArea(2, 30);
        customPassageField.setLineWrap(true);
        customPassageField.setWrapStyleWord(true);
        customPassageField.setVisible(false);
        customPassageField.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Show custom text box only when Custom is selected
        passageDropdown.addActionListener(e -> {
            boolean isCustom = passageDropdown.getSelectedIndex() == 3;
            customPassageField.setVisible(isCustom);
            frame.revalidate();
        });

        passagePanel.add(new JLabel("Select passage: "));
        passagePanel.add(passageDropdown);
        passagePanel.add(customPassageField);
        settingsPanel.add(passagePanel);

        // --- Seat Count ---
        JPanel seatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        seatPanel.setBorder(BorderFactory.createTitledBorder("Number of Typists"));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(2, 2, 6, 1);
        seatCountSpinner = new JSpinner(spinnerModel);

        seatPanel.add(new JLabel("How many typists? (2-6): "));
        seatPanel.add(seatCountSpinner);
        settingsPanel.add(seatPanel);

        // --- Difficulty Modifiers ---
        JPanel modifierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modifierPanel.setBorder(BorderFactory.createTitledBorder("Difficulty Modifiers"));

        autocorrectCheckbox = new JCheckBox("Autocorrect (slideBack halved)");
        caffeineModeCheckbox = new JCheckBox("Caffeine Mode (speed boost first 10 turns, then burnout risk)");
        nightShiftCheckbox = new JCheckBox("Night Shift (everyone's accuracy reduced)");

        modifierPanel.add(autocorrectCheckbox);
        modifierPanel.add(caffeineModeCheckbox);
        modifierPanel.add(nightShiftCheckbox);
        settingsPanel.add(modifierPanel);

        frame.add(settingsPanel, BorderLayout.CENTER);

        // --- Start Button at bottom ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButton = new JButton("START RACE");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setBackground(Color.GREEN);
        startButton.setForeground(Color.WHITE);

        startButton.addActionListener(e -> startRace());

        bottomPanel.add(startButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void startRace()
    {
        // Get selected passage text
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
            // Get just the text part after the colon
            String selected = (String) passageDropdown.getSelectedItem();
            passage = selected.split(": ")[1];
        }

        // Get number of typists
        int seatCount = (int) seatCountSpinner.getValue();

        // Get modifiers
        boolean autocorrect = autocorrectCheckbox.isSelected();
        boolean caffeineMode = caffeineModeCheckbox.isSelected();
        boolean nightShift = nightShiftCheckbox.isSelected();

        // Close setup screen and open typist customisation
        frame.dispose();
        new TypistSetupScreen(passage, seatCount, autocorrect, caffeineMode, nightShift);
    }

    public static void main(String[] args)
    {
        new TypingRaceGUI();
    }
}