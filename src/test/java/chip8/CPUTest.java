package chip8;

import chip8.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CPUTest {

    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;
    private CPU cpu;

    @BeforeEach
    public void setUp() {
        memory = new Memory();
        registry = new Registry();
        keyboard = new Keyboard();
        display = new SwingGUI(12, memory, keyboard, true); //most games use screen overlapping mode
        cpu = new CPU(memory, registry, display, keyboard, false, false);
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

        registry.PC = 0x200;
        registry.VReg[1] = 0x05;
        registry.VReg[3] = 0x09;

        cpu.skipEqualRegs((byte) 1, (byte) 3);

        assertEquals(0x200, registry.PC); //compared values are not equal, PC stays the same
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
        //0x3C - 0x0D = 0x2F, VF (not carry) = 1
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0D;

        cpu.subRegReg((byte) 1, (byte) 2);

        assertEquals((byte) 0x2F, registry.VReg[1]);
        assertEquals((byte) 0x1, registry.VReg[0xF]);

        //0x01 - 0x0D = 0xF4, VF (not carry) = 0 (sub with borrow 0x101 - 0x0D = 0xF4)
        registry.VReg[3] = (byte) 0x01;
        registry.VReg[4] = (byte) 0x0D;

        cpu.subRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0xF4, registry.VReg[3]);
        assertEquals((byte) 0x0, registry.VReg[0xF]);
    }

    @Test
    public void shiftRightTest() {
        //0x0A >> 1 = 0x5, VF = 0
        registry.VReg[2] = (byte) 0x0A;

        cpu.shiftRight((byte) 1, (byte) 2);

        assertEquals((byte) 0x5, registry.VReg[1]); //V2 (Vy) shifted right stored in V1 (Vx)
        assertEquals((byte) 0x0, registry.VReg[0xF]); //least significant bit stored in VF
        assertEquals((byte) 0x0A, registry.VReg[2]); //V2 (Vy) remains untouched

        //0xFF >> 1 = 0x7F, VF = 1
        registry.VReg[2] = (byte) 0xFF;

        cpu.shiftRight((byte) 1,(byte) 2);

        assertEquals((byte) 0x7F, registry.VReg[1]);
        assertEquals((byte) 0x1, registry.VReg[0xF]);
        assertEquals((byte) 0xFF, registry.VReg[2]);
    }

    @Test
    public void subNegativeRegRegTest() {
        //0x0D - 0x3C = 0xD1, VF (not carry) = 0
        registry.VReg[1] = (byte) 0x3C;
        registry.VReg[2] = (byte) 0x0D;

        cpu.subNegativeRegReg((byte) 1, (byte) 2);

        assertEquals((byte) 0xD1, registry.VReg[1]);
        assertEquals((byte) 0x0, registry.VReg[0xF]);

        //0x0D - 0x01 = 0x0C, VF (not carry) = 1
        registry.VReg[3] = (byte) 0x01;
        registry.VReg[4] = (byte) 0x0D;

        cpu.subNegativeRegReg((byte) 3, (byte) 4);

        assertEquals((byte) 0x0C, registry.VReg[3]);
        assertEquals((byte) 0x1, registry.VReg[0xF]);
    }

    @Test
    public void shiftLeftTest() {
        //0x0A << 1 = 0x14, VF = 0
        registry.VReg[2] = (byte) 0x0A;

        cpu.shiftLeft((byte) 1, (byte) 2);

        assertEquals((byte) 0x14, registry.VReg[1]); //result stored in Vx.
        assertEquals((byte) 0x0, registry.VReg[0xF]); //most significant bit stored in VF
        assertEquals((byte) 0x0A, registry.VReg[2]); //register Vy stays the same

        //0xFF << 1 = 0xFE, VF = 1 (0xFF << 1 = 0x1FE -> lower byte -> 0xFE)
        registry.VReg[2] = (byte) 0xFF;

        cpu.shiftLeft((byte) 1, (byte) 2);

        assertEquals((byte) 0xFE, registry.VReg[1]); //result stored in Vx
        assertEquals((byte) 0x1, registry.VReg[0xF]); //most significant bit stored in VF
        assertEquals((byte) 0x0FF, registry.VReg[2]); //register Vy stays the same
    }

    @Test
    public void skipNotEqualRegsTest() {
        registry.PC = 0x200;
        registry.VReg[1] = 0x05;
        registry.VReg[3] = 0x05;

        cpu.skipNotEqualRegs((byte) 1, (byte) 3);

        assertEquals(0x200, registry.PC); //compared values are equal, PC stays the same

        registry.PC = 0x200;
        registry.VReg[1] = 0x05;
        registry.VReg[3] = 0x09;

        cpu.skipNotEqualRegs((byte) 1, (byte) 3);

        assertEquals(0x202, registry.PC); //compared values are not equal, PC incremented by 2
    }

    @Test
    public void setIRegTest() {
        cpu.setIReg((short) 0x30A);

        assertEquals((short) 0x30A, registry.IReg);
    }

    @Test
    public void jumpAddV0Test() {
        //setting the value of V0
        registry.VReg[0] = (byte) 0xA1;

        cpu.jumpAddV0((short) 0x200);

        assertEquals(0x2A1, registry.PC);
    }

    @Test
    public void drawTest() {
        //normal drawing
        //set registers to draw Sprite "0" at position (20, 2)
        registry.IReg = Memory.SPRITE_0;
        registry.VReg[1] = 20;
        registry.VReg[2] = 2;

        //draw the sprite
        cpu.draw((byte) 1, (byte) 2, (byte) 5);

        //check whether pixels on screen are equal to the sprite's bytes in memory
        for (int i = 0; i < 5; i++) {
            assertEquals(memory.get((short) (Memory.SPRITE_0 + i)), (short) display.getByte(20, 2 + i));
        }
        //VF = 1 - no collision
        assertEquals((byte) 0x0, registry.VReg[0xF]);

        //horizontal overlapping
        //set registers to draw Sprite "4" at position (62, 8)
        registry.IReg = Memory.SPRITE_4;
        registry.VReg[1] = 62;
        registry.VReg[2] = 8;

        //draw the sprite
        cpu.draw((byte) 1, (byte) 2, (byte) 5);

        //check whether pixels on screen are equal to the sprite's bytes in memory
        for (int i = 0; i < 5; i++) {
            assertEquals(memory.get((short) (Memory.SPRITE_4 + i)), (short) display.getByte(62, 8 + i));
        }
        //VF = 1 - no collision
        assertEquals((byte) 0x0, registry.VReg[0xF]);

        //vertical overlapping
        //set registers to draw Sprite "A" at position (63, 30)
        registry.IReg = Memory.SPRITE_A;
        registry.VReg[1] = 62;
        registry.VReg[2] = 30;

        //draw the sprite
        cpu.draw((byte) 1, (byte) 2, (byte) 5);

        //check whether pixels on screen are equal to the sprite's bytes in memory
        for (int i = 0; i < 5; i++) {
            assertEquals(memory.get((short) (Memory.SPRITE_A + i)), (short) display.getByte(62, 30 + i));
        }
        //VF = 1 - no collision
        assertEquals((byte) 0x0, registry.VReg[0xF]);

        //sprite XORing
        //set registers to draw Sprite "0" at position (20, 2)
        registry.IReg = Memory.SPRITE_0;
        registry.VReg[1] = 21;
        registry.VReg[2] = 2;

        memory.set((short) 0x200, (byte) 0x88);
        memory.set((short) 0x201, (byte) 0xD8);
        memory.set((short) 0x202, (byte) 0xD8);
        memory.set((short) 0x203, (byte) 0xD8);
        memory.set((short) 0x204, (byte) 0x88);

        //draw the sprite
        cpu.draw((byte) 1, (byte) 2, (byte) 5);

        //check whether pixels on screen are equal to the sprite's bytes in memory
        for (int i = 0; i < 5; i++) {
            assertEquals(memory.get((short) (0x200 + i)), (short) display.getByte(20, 2 + i));
        }
        //VF = 1 - collision occurred
        assertEquals((byte) 0x1, registry.VReg[0xF]);
    }

    @Test
    public void skipKeyPressedTest() {
        //setting registers and the key as pressed.
        registry.PC = 0x200;
        registry.VReg[2] = 0xA;
        keyboard.setKey(0xA, true, false);

        cpu.skipKeyPressed((byte) 0x2);

        assertEquals(0x202, registry.PC); //key pressed, PC incremented by 2

        //setting registers and the key as pressed.
        registry.PC = 0x200;
        registry.VReg[2] = 0xA;
        keyboard.setKey(0xA, false, false);

        cpu.skipKeyPressed((byte) 0x2);

        assertEquals(0x200, registry.PC); //key released, PC stays the same
    }

    @Test
    public void skipKeyNotPressedTest() {
        //setting registers and the key as pressed.
        registry.PC = 0x200;
        registry.VReg[2] = 0xA;
        keyboard.setKey(0xA, false, false);

        cpu.skipKeyNotPressed((byte) 0x2);

        assertEquals(0x202, registry.PC); //key released, PC incremented by 2

        //setting registers and the key as pressed.
        registry.PC = 0x200;
        registry.VReg[2] = 0xA;
        keyboard.setKey(0xA, true, false);

        cpu.skipKeyNotPressed((byte) 0x2);

        assertEquals(0x200, registry.PC); //key pressed, PC stays the same
    }

    @Test
    public void setRegDTTest() {
        registry.DT = (byte) 0xFF;

        cpu.setRegDT((byte) 0x3);

        assertEquals((byte) 0xFF, registry.VReg[0x3]); //register 0x3 was set the value of the DT reg
    }

    @Test
    public void waitKeySetRegTest() {
        //pressing the key
        keyboard.setKey(0xF, true, false);

        cpu.waitKeySetReg((byte) 0x2);

        //releasing the key
        keyboard.setKey(0xF, false, false);

        //value of the key saved in register 0x2
        assertEquals((byte) 0xF, registry.VReg[0x2]);
    }

    @Test
    public void setDTRegTest() {
        registry.VReg[0x8] = 0x5A;

        cpu.setDTReg((byte) 0x8);

        assertEquals(0x5A, registry.VReg[0x8]);
    }

    @Test
    public void setSTRegTest() {
        registry.VReg[0xA] = (byte) 0x9F;

        cpu.setSTReg((byte) 0xA);

        assertEquals((byte) 0x9F, registry.ST);
    }

    @Test
    public void setISpriteAddrRegTest() {
        //for every sprite
        for (int i = 0x0; i < 0xF; i++) {
            //set the sprite's value in the register 0x3
            registry.VReg[0x3] = (byte) i;

            cpu.setISpriteAddrReg((byte) 0x3);

            //addresses in I register and expected one are the same
            assertEquals((byte) (i * 5), registry.IReg);
        }
    }

    @Test
    public void setBCDRegITest() {
        //value of 0xFF is 255
        registry.VReg[0xC] = (byte) 0xFF;

        cpu.setBCDRegI((byte) 0xC);

        assertEquals(2, memory.get(registry.IReg));
        assertEquals(5, memory.get((short) (registry.IReg + 1)));
        assertEquals(5, memory.get((short) (registry.IReg + 2)));

        //value of 0x55 is 85, BCD representation is 085
        registry.VReg[0xC] = (byte) 0x55;

        cpu.setBCDRegI((byte) 0xC);

        assertEquals(0, memory.get(registry.IReg));
        assertEquals(8, memory.get((short) (registry.IReg + 1)));
        assertEquals(5, memory.get((short) (registry.IReg + 2)));
    }

    @Test
    public void storeRegsAtITest() {
        //setting the registry
        for (int i = 0; i <= 0xF; i++) {
            registry.VReg[i] = (byte) (i + 20);
        }

        //address to store the regs at
        registry.IReg = 0x400;

        cpu.storeRegsAtI((byte) 0xF);

        //checking the values stored in memory
        for (int i = 0; i <= 0xF; i++) {
            assertEquals((byte) (i + 20), memory.get((short) (0x400 + i)));
        }
        assertEquals(0x410, registry.IReg); //I = I + 0xF + 1
    }

    @Test
    public void loadRegsAtITest() {
        //setting the memory
        for (short i = 0; i <= 0xF; i++) {
            memory.set((short) (0x500 + i), (byte) i);
        }

        //setting the address in the memory
        registry.IReg = 0x500;

        cpu.loadRegsAtI((byte) 0xF);

        for (int i = 0; i <= 0xF; i++) {
            assertEquals((byte) i, registry.VReg[i]);
        }
        assertEquals(0x510, registry.IReg); //I = I + 0xF + 1
    }
}