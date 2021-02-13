package chip8;

public class Chip8 {

    private CPU cpu;
    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;


    public Chip8() {
        memory = new Memory();
        keyboard = new Keyboard();
        registry = new Registry();
        display = new Display(12, memory, keyboard);
        cpu = new CPU(memory, registry, display, keyboard);

        int size;
        try {
            size = memory.loadFile("./c8games/roms/programs/IBM Logo.ch8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        display.initDisplay();
    }

    public void loop() {

        while (true) {

            cpu.fetch();

            cpu.incrementPC();

            cpu.decodeAndExecute();
        }
    }
}
