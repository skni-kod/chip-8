package chip8;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class representing the chip-8 interpreter.
 */
public class Chip8 {

    /**
     * Chip-8's CPU.
     */
    private CPU cpu;

    /**
     * Chip-8's memory.
     */
    private Memory memory;

    /**
     * Chip-8's registry.
     */
    private Registry registry;

    /**
     * Chip-8's display.
     */
    private Display display;

    /**
     * Chip-8's keyboard.
     */
    private Keyboard keyboard;

    /**
     * Chip-8's sound system.
     */
    private Sound sound;

    /**
     * Disassembler used for the debugViewGUI.
     */
    private Disassembler disassembler;

    /**
     * GUI that allows to see chip-8's real time internals, such as registers and executed instructions.
     */
    private DebugViewGUI registerViewGUI;

    /**
     * Whether to use registerViewGUI.
     */
    boolean registerGUIFlag;

    /**
     * Frequency of the chip-8's CPU.
     */
    private int CPU_FREQ = 500;

    /**
     * Whether the sound is unavailable on current machine.
     */
    boolean soundUnavailable;


    /**
     * Whether the load-store quirk concerning Fx55 and Fx65 instructions should be used.
     */
    boolean loadStoreQuirk;

    /**
     * Whether the shift quirk concerning 8xyE and 8xy7 instructions should be used.
     */
    boolean shiftQuirk;

    /**
     * Whether sprites should overlap on the screen, when they reach the border.
     * Some chip-8 programs are written to overlap sprites only, if the whole sprite is beyond the screen.
     * References aren't certain, whether this should be a standard. Most programs use overlapping mode by default.
     */
    boolean overlappingMode;

    /**
     * Chip-8's constructor initializing all the parameters at the default values.
     * @param filename Filename/path of the chip-8 ROM.
     */
    public Chip8(String filename) throws IOException {
        new Chip8(filename, 500, false, true, true, false);
    }

    /**
     * Chip-8's main constructor. Sets all the fields and loads the ROM.
     * @param filename Filename/path of the chip-8 ROM.
     * @param cpuFreq Frequency of the CPU, 500 by default.
     * @param loadStoreQuirk Whether to use load-store quirk, false by default.
     * @param shiftQuirk Whether to use shift quirk, true by default.
     * @param overlappingMode Whether to use overlapping mode, true by default.
     * @param registerGUIFlag Whether to use debug view window, false by default.
     */
    public Chip8(String filename, int cpuFreq, boolean loadStoreQuirk, boolean shiftQuirk, boolean overlappingMode, boolean registerGUIFlag) throws IOException {
        //quirks
        this.loadStoreQuirk = loadStoreQuirk;
        this.shiftQuirk = shiftQuirk;
        this.overlappingMode = overlappingMode;

        this.CPU_FREQ = cpuFreq;
        this.registerGUIFlag = registerGUIFlag;

        soundUnavailable = false;

        //initializing fields
        memory = new Memory();
        keyboard = new Keyboard();
        registry = new Registry();
        display = new SwingGUI(12, memory, keyboard, overlappingMode);
        cpu = new CPU(memory, registry, display, keyboard, loadStoreQuirk, shiftQuirk);
        disassembler = new Disassembler(memory);

        //trying to initialize the sound system
        try {
            sound = new Sound();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            soundUnavailable = true;
        }

        //loading the rom
        int size;
        size = memory.loadFile(filename);


        display.createGUI();

        if (registerGUIFlag) {
            registerViewGUI = new DebugViewGUI(registry, disassembler);
            registerViewGUI.createGUI();
        }
    }

    /**
     * Chip-8's main fetch-decode-execute loop.
     */
    public void loop() {

        final int TIMER_TICK = 1000 / 60;
        final int CPU_TICK = 1000 / CPU_FREQ;

        //timer to call at 60Hz frequency - decrementing timers and rendering the screen
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

    /**
     * Method called at the 60Hz frequency, rendering the screen, updating the registers on the debug GUI
     * and decrementing the timers (delay timer, sound timer).
     * Chip-8's timers should be decremented at rate of 60Hz by default.
     */
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

    /**
     * Helper class to create a timer thread.
     */
    class DelayTask extends TimerTask {
        @Override
        public void run() {
            renderAndDecrementTimers();
        }
    }
}
