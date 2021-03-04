package chip8;

public class Main {

    public static void main(String[] args) {

        //TODO fix the keyboard
        //TODO fix emulation speeds
        //TODO sounds
        //TODO rebuild display
        //TODO rebuild keyboard to use it in debugging

        String filename = "./chip_roms/c8games/TANK";
        Memory memory = new Memory();
        int size = 0;
        try {
            size = memory.loadFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Chip8 chip8 = new Chip8(filename);
        chip8.loop();

    }
}
