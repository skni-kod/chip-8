package chip8;

public class Main {

    public static void main(String[] args) {

        //TODO fix the keyboard
        //TODO fix emulation speeds
        //TODO sounds

        String filename = "./c8games/HIDDEN";
        Memory memory = new Memory();
        int size = 0;
        try {
            size = memory.loadFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Disassembler disassembler = new Disassembler(memory);
        disassembler.disassembleToFile((short) 0x200, size, filename + ".txt");

        Chip8 chip8 = new Chip8(filename);
        chip8.loop();

    }
}
