package chip8;

import javax.swing.*;
import java.awt.*;

/**
 * Class creating a Swing GUI window of a simple chip-8's real time internal viewer.
 * Allows to check chip-8's registers, timers, pointers' values
 * along with currently executed, disassembled, instructions.
 */
public class DebugViewGUI {

    /**
     * Reference to the registry.
     */
    private Registry registry;

    /**
     * Reference to the disassembler.
     */
    private Disassembler disassembler;

    /**
     * Swing JFrame.
     */
    private JFrame frame;

    /**
     * Swing JPanel that holds the JTextFields.
     */
    private JPanel textPanel;

    /**
     * An array of 21 JTextFields that holds all the registers, timers and pointers string values.
     */
    private JTextField[] regTextFields;

    /**
     * An array of 21 JTextFields that holds currently executed (disassembled) instruction as well as
     * 10 instructions before and after the one currently executed.
     */
    private JTextField[] instrTextFields;

    /**
     * Main constructor. Initializes the JTextField arrays.
     * @param registry Reference to the registry.
     * @param disassembler Reference to the disassembler.
     */
    public DebugViewGUI(Registry registry, Disassembler disassembler) {
        this.registry = registry;
        this.disassembler = disassembler;

        regTextFields = new JTextField[21];
        instrTextFields = new JTextField[21];
    }

    /**
     * Creates a Swing window, fills the JTextFields and connects them to the JPanel.
     */
    public void createGUI() {
        frame = new JFrame("Debug");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setResizable(true);

        textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(21, 2));

        frame.add(textPanel);

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

    /**
     * Updates the values of the register JTextFields using the values from the register.
     */
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

    /**
     * Updates the values of the instruction JTextFields using the currently used instruction
     * and the instructions in the range of 10.
     */
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

    /**
     * Checks whether the index of the memory isn't exceeding the memory's size.
     * @param memIndex Index of the memory.
     * @return True if index isn't exceeding the memory's size, otherwise false.
     */
    private boolean memIndexAllowed(int memIndex) {
        return memIndex > 0 && memIndex < 0xFFF;
    }
}
