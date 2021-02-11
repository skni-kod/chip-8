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
    }

    public void loop() {

        while (true) {

            cpu.fetch();

            cpu.incrementPC();

            cpu.decodeAndExecute();
        }
    }
}
