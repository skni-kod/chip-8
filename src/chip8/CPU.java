package chip8;

public class CPU {

    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;

    short currentInstr;

    public CPU(Memory memory, Registry registry, Display display, Keyboard keyboard) {
        this.memory = memory;
        this.registry = registry;
        this.display = display;
        this.keyboard = keyboard;

        this.registry.PC = 0x0200;
    }

    void fetch() {
        // Two bytes of the instruction
        byte first = memory.get(registry.PC);
        byte second = memory.get((short) (registry.PC + 1));

        // A full 2 byte instruction held in short
        currentInstr = (short) (((first & 0xFF) << 8) | (second & 0xFF));
    }

    void incrementPC() {
        registry.PC = (short) (registry.PC + 0x02);
    }

    void decodeAndExecute() {

        switch (currentInstr) {
            //00E0 - Clears the screen
            case (0x00E0): {
                clearScreen();
                break;
            }

            //00EE - Returns from a subroutine
            case (0x0EE): {
                returnSubroutine();
                break;
            }
        }
    }

    /**
     * 00E0 - CLS
     * Clear the display.
     */
    private void clearScreen() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                display.setPixel(x, y, false);
            }
        }
    }

    private void returnSubroutine() {

    }

}
