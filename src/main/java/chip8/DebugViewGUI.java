package chip8;

import javax.swing.*;
import java.awt.*;

public class DebugViewGUI {

    private Registry registry;
    private Disassembler disassembler;

    private JFrame frame;
    private JPanel textPanel;
    private JTextField[] regTextFields;
    private JTextField[] instrTextFields;

    public DebugViewGUI(Registry registry, Disassembler disassembler) {
        this.registry = registry;
        this.disassembler = disassembler;
    }

    public void createGUI() {
        frame = new JFrame("Debug");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(true);

        textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(21, 2));

        frame.add(textPanel);

        regTextFields = new JTextField[21];
        instrTextFields = new JTextField[21];

        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField();
            instrTextFields[i] = new JTextField();
        }

        updateRegisters();
        updateInstructions();

        for (int i = 0; i < regTextFields.length; i++) {
            textPanel.add(regTextFields[i]);
            textPanel.add(instrTextFields[i]);
        }

        instrTextFields[10].setBackground(Color.LIGHT_GRAY);

        frame.setSize(new Dimension(400, 500));
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

    public void updateInstructions() {
        int index = registry.PC;
        for (int i = 0; i < instrTextFields.length; i++) {
            if (memIndexAllowed(index)) {
                instrTextFields[i].setText(disassembler.disassemble((short) index));
            } else {
                instrTextFields[i].setText("");
            }
            index++;
        }
    }

    private boolean memIndexAllowed(int memIndex) {
        return memIndex > 0 && memIndex < 0xFFF;
    }
}
