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

    @Test
    public void addRegValTest() {
        registry.VReg[4] = (byte) 0x03;

        cpu.addRegVal((byte) 4, (byte) 4);

        assertEquals((byte) 7, registry.VReg[4]); //value added to the register
    }

    @Test
    public void setRegRegTest() {
        registry.VReg[4] = (byte) 0x03;
        registry.VReg[5] = (byte) 0x05;

        cpu.setRegReg((byte) 4, (byte) 5);

        assertEquals((byte) 0x05, registry.VReg[5]); //set registers are equal
        assertEquals(registry.VReg[4], registry.VReg[5]);
    }

    @Test
    public void orRegRegTest() {
        //0x3C or 0x0C = 0x3C
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0C;

        //0xA0 or 0x0A = 0xAA
        registry.VReg[3] = (byte) 0xA0;
        registry.VReg[4] = (byte) 0x0A;

        cpu.orRegReg((byte) 1, (byte) 2);
        cpu.orRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0x3C, registry.VReg[1]);
        assertEquals((byte) 0xAA, registry.VReg[3]);
    }

    @Test
    public void andRegRegTest() {
        //0x3C AND 0x0C = 0x0C
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0C;

        //0xA0 AND 0x0A = 0x00
        registry.VReg[3] = (byte) 0xA0;
        registry.VReg[4] = (byte) 0x0A;

        cpu.andRegReg((byte) 1, (byte) 2);
        cpu.andRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0x0C, registry.VReg[1]);
        assertEquals((byte) 0x00, registry.VReg[3]);
    }

    @Test
    public void xorRegRegTest() {
        //0x3C XOR 0x0D = 0x31
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0D;

        //0xA0 XOR 0x0A = 0xAA
        registry.VReg[3] = (byte) 0xA0;
        registry.VReg[4] = (byte) 0x0A;

        cpu.xorRegReg((byte) 1, (byte) 2);
        cpu.xorRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0x31, registry.VReg[1]);
        assertEquals((byte) 0xAA, registry.VReg[3]);
    }

    @Test
    public void addRegRegTest() {
        //0x3C + 0x0D = 0x49, carry = 0
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0D;

        cpu.addRegReg((byte) 1, (byte) 2);

        assertEquals((byte) 0x49, registry.VReg[1]);
        assertEquals((byte) 0x0, registry.VReg[0xF]);

        //0xFF + 0xFF = 0x1FE -> lower byte -> 0xFE, carry = 1
        registry.VReg[3] = (byte) 0x0FF;
        registry.VReg[4] = (byte) 0x0FF;

        cpu.addRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0xFE, registry.VReg[3]);
        assertEquals((byte) 0x1, registry.VReg[0xF]);
    }

    @Test
    public void subRegRegTest() {
        //0x3C - 0x0D = 0x2F, carry = 1
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0D;

        cpu.subRegReg((byte) 1, (byte) 2);

        assertEquals((byte) 0x2F, registry.VReg[1]);
        assertEquals((byte) 0x1, registry.VReg[0xF]);

        //0x01 - 0x0D = 0xF4, carry = 0 (sub with borrow 0x101 - 0x0D = 0xF4)
        registry.VReg[3] = (byte) 0x01;
        registry.VReg[4] = (byte) 0x0D;

        cpu.subRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0xF4, registry.VReg[3]);
        assertEquals((byte) 0x0, registry.VReg[0xF]);
    }
}