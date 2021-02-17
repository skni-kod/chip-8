package chip8;


import java.util.Timer;
import java.util.TimerTask;

public class Chip8 {

    private CPU cpu;
    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;

    private Disassembler disassembler;

    public Chip8(String filename) {
        //quirks
        boolean loadStoreQuirk = true;
        boolean shiftQuirk = false;

        memory = new Memory();
        keyboard = new Keyboard();
        registry = new Registry();
        display = new Display(12, memory, keyboard);
        cpu = new CPU(memory, registry, display, keyboard, loadStoreQuirk, shiftQuirk);

        disassembler = new Disassembler(memory);


        int size;
        try {
            size = memory.loadFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        display.initDisplay();
    }

    public void loop() {

        final int TIMER_TICK = 1000 / 60;
        final int CPU_FREQ = 200;
        final int CPU_TICK = 1000 / CPU_FREQ;

        int loops = 0;

        Timer delayTimer = new Timer();

        delayTimer.schedule(new DelayTask(), TIMER_TICK, TIMER_TICK);

        while (true) {

            cpu.fetch();

            disassembler.disassemble(registry.PC);

            cpu.incrementPC();

            cpu.decodeAndExecute();

            try {
                Thread.sleep(CPU_TICK);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public void renderAndDecrementTimers() {
        display.render();

        if ((registry.DT & 0xFF) > 0) {
            registry.DT--;
        }

        if ((registry.ST & 0xFF) > 0) {
            registry.ST--;
        }
    }

    class DelayTask extends TimerTask {
        @Override
        public void run() {
            renderAndDecrementTimers();
        }
    }
}
