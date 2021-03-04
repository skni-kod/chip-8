package chip8;

import javax.sound.sampled.*;
import java.util.Timer;
import java.util.TimerTask;

public class Chip8 {

    private CPU cpu;
    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;
    private Sound sound;

    boolean soundUnavailable;


    public Chip8(String filename) {
        //quirks
        boolean loadStoreQuirk = false;
        boolean shiftQuirk = true;
        boolean overlappingMode = true;

        soundUnavailable = false;

        memory = new Memory();
        keyboard = new Keyboard();
        registry = new Registry();
        display = new Display(12, memory, keyboard, overlappingMode);
        cpu = new CPU(memory, registry, display, keyboard, loadStoreQuirk, shiftQuirk);

        try {
            sound = new Sound();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            soundUnavailable = true;
        }

        int size;
        try {
            size = memory.loadFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        display.createSwingGUI();
    }

    public void loop() {

        final int TIMER_TICK = 1000 / 60;
        final int CPU_FREQ = 500;
        final int CPU_TICK = 1000 / CPU_FREQ;

        int loops = 0;

        Timer delayTimer = new Timer();

        delayTimer.schedule(new DelayTask(), TIMER_TICK, TIMER_TICK);

        while (true) {

            cpu.fetch();

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

        //according to mattmikolay's reference, minimum value that the timer will respond to is 0x02
        if ((registry.ST & 0xFF) > 0x1) {
            if (soundUnavailable) {
                registry.ST--;
            } else {
                sound.startSound();
                registry.ST--;
                if (registry.ST <= 0x1) {
                    sound.stopSound();
                }
            }
        }
    }

    class DelayTask extends TimerTask {
        @Override
        public void run() {
            renderAndDecrementTimers();
        }
    }
}
