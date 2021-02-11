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

    @Test
    public void callSubroutineTest() {
        registry.PC = 0x20A;
        registry.SP = -0x01; //empty stack

        cpu.callSubroutine((short) 0x300);

        assertEquals(0x0, registry.SP);
        assertEquals(0x20A, memory.getStack(registry.SP));
        assertEquals(0x300, registry.PC);
    }

    @Test
    public void skipEqualTest() {
        registry.PC = 0x200;
        registry.VReg[2] = (byte) 0x25;

        cpu.skipEqual((byte) 2,  (byte) 0x25);

        assertEquals(0x202, registry.PC); //compared values equal, PC incremented by 2
    }

    @Test
    public void skipEqualTestDiffVals() {
        registry.PC = 0x200;
        registry.VReg[2] = (byte) 0x25;

        cpu.skipEqual((byte) 2,  (byte) 0x28);

        assertEquals(0x200, registry.PC); //compared values not equal, PC stays the same
    }

    @Test
    public void skipNotEqualTest() {
        registry.PC = 0x200;
        registry.VReg[2] = (byte) 0x25;

        cpu.skipNotEqual((byte) 2,  (byte) 0x28);

        assertEquals(0x202, registry.PC); //compared values not equal, PC incremented by 2
    }

    @Test
    public void skipNotEqualTestSameVals() {
        registry.PC = 0x200;
        registry.VReg[2] = (byte) 0x25;

        cpu.skipNotEqual((byte) 2,  (byte) 0x25);

        assertEquals(0x200, registry.PC); //compared values equal, PC stays the same
    }

    @Test
    public void skipEqualRegsTest() {
        registry.PC = 0x200;
        registry.VReg[1] = 0x05;
        registry.VReg[3] = 0x05;

        cpu.skipEqualRegs((byte) 1, (byte) 3);

        assertEquals(0x202, registry.PC); //compared values are equal, PC incremented by 2
    }

    @Test
    public void setRegValTest() {
        cpu.setRegVal((byte) 2, (byte) 0xA0);

        assertEquals((byte) 0xA0, registry.VReg[2]);
    }
}