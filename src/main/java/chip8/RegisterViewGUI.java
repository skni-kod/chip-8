package chip8;

import javax.swing.*;
import java.awt.*;

public class RegisterViewGUI {

    private Registry registry;

    private JFrame frame;
    private JPanel textPanel;
    private JTextField[] regTextFields;

    public RegisterViewGUI(Registry registry) {
        this.registry = registry;
    }

    public void createGUI() {
        frame = new JFrame("Registers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(true);

        textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(21, 1));

        frame.add(textPanel);

        regTextFields = new JTextField[21];

        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField();
        }

        for (int i = 0; i < 16; i++) {
            regTextFields[i].setText(String.format("V%01X:%d", i, registry.VReg[i]));
        }
        regTextFields[16].setText(String.format("IReg:%d", registry.IReg));
        regTextFields[17].setText(String.format("DT:%d", registry.DT));
        regTextFields[18].setText(String.format("ST:%d", registry.ST));
        regTextFields[19].setText(String.format("PC:%d", registry.PC));
        regTextFields[20].setText(String.format("SP:%d", registry.SP));

        for (int i = 0; i < regTextFields.length; i++) {
            textPanel.add(regTextFields[i]);
        }

        frame.setSize(new Dimension(200, 500));
        frame.setVisible(true);
    }

    public void updateRegisters() {
        for (int i = 0; i < 16; i++) {
            regTextFields[i].setText(String.format("V%01X:%d", i, registry.VReg[i]));
        }
        regTextFields[16].setText(String.format("IReg:%d", registry.IReg));
        regTextFields[17].setText(String.format("DT:%d", registry.DT));
        regTextFields[18].setText(String.format("ST:%d", registry.ST));
        regTextFields[19].setText(String.format("PC:%d", registry.PC));
        regTextFields[20].setText(String.format("SP:%d", registry.SP));
    }
}
