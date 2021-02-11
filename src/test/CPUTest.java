package test;

import chip8.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class CPUTest {

    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;
    private CPU cpu;

    @Before
    public void setUp() throws Exception {
        memory = new Memory();
        registry = new Registry();
        keyboard = new Keyboard();
        display = new Display(12, memory, keyboard);
        cpu = new CPU(memory, registry, display, keyboard);
    }

    @Test
    public void clearScreenTest() {
        //fill the screen
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                display.setPixel(x, y, true, false);
            }
        }

        cpu.clearScreen();

        //empty screen to compare to
        boolean[][] emptyScreen = new boolean[64][32];

        assertTrue(Arrays.deepEquals(display.getScreen(), emptyScreen));
    }

    @Test
    public void returnSubroutineTest() {
        //save random adress on the stack and increment the stack pointer
        memory.setStack((short) 0x0, (short) 0x0203);
        registry.SP = (byte) (registry.SP + 1);

        cpu.returnSubroutine();

        assertEquals((short) -0x01, registry.SP); //SP is decremented
        assertEquals((short) 0x0203, registry.PC); //PC = top of the stack

    }

    @Test
    public void jumpTest() {
        registry.PC = (short) 0x200;

        cpu.jump((short) 0x300);

        assertEquals(0x300, registry.PC);
    }

    @Test
    public void jumpTestInstruction() {
        registry.PC = (short) 0x200;
        short instr = (short) 0x1300;

        cpu.jump((short) (instr & 0x0FFF));

        assertEquals(0x300, registry.PC);
    }

}