package Part2;

import javax.swing.*;

public class TypistSetupScreen
{
    public TypistSetupScreen(String passage, int seatCount, 
                              boolean autocorrect, boolean caffeineMode, 
                              boolean nightShift)
    {
        JFrame frame = new JFrame("Typist Setup");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("implementing typist setup", SwingConstants.CENTER);
        frame.add(label);

        frame.setVisible(true);
    }
}