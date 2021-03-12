package chip8;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Timer;
import java.util.TimerTask;

public class Chip8 {

    private CPU cpu;
    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;
    private Sound sound;
    private Disassembler disassembler;

    private DebugViewGUI registerViewGUI;
    boolean registerGUIFlag;

    private int CPU_FREQ = 500;

    boolean soundUnavailable;

    boolean loadStoreQuirk;
    boolean shiftQuirk;
    boolean overlappingMode;

    public Chip8(String filename) {
        new Chip8(filename, 500, false, true, true, false);
    }

    public Chip8(String filename, int cpuFreq, boolean loadStoreQuirk, boolean shiftQuirk, boolean overlappingMode, boolean registerGUIFlag) {
        //quirks
        this.loadStoreQuirk = loadStoreQuirk;
        this.shiftQuirk = shiftQuirk;
        this.overlappingMode = overlappingMode;

        this.registerGUIFlag = registerGUIFlag;

        this.CPU_FREQ = cpuFreq;

        soundUnavailable = false;

        memory = new Memory();
        keyboard = new Keyboard();
        registry = new Registry();
        display = new SwingGUI(12, memory, keyboard, overlappingMode);
        cpu = new CPU(memory, registry, display, keyboard, loadStoreQuirk, shiftQuirk);
        disassembler = new Disassembler(memory);

        try {
            sound = new Sound();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            soundUnavailable = true;
        }

        int size;
        try {
            size = memory.loadFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        display.createGUI();

        if (registerGUIFlag) {
            registerViewGUI = new DebugViewGUI(registry, disassembler);
            registerViewGUI.createGUI();
        }
    }

    public void loop() {

        final int TIMER_TICK = 1000 / 60;
        final int CPU_TICK = 1000 / CPU_FREQ;

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

        if (registerGUIFlag) {
            registerViewGUI.updateRegisters();
            registerViewGUI.updateInstructions();
        }

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
