package chip8;

import java.util.Random;

public class CPU {

    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;

    private Random randomGen;

    short currentInstr;

    public CPU(Memory memory, Registry registry, Display display, Keyboard keyboard) {
        this.memory = memory;
        this.registry = registry;
        this.display = display;
        this.keyboard = keyboard;

        randomGen = new Random();

        this.registry.PC = (short) 0x200;
        this.registry.SP = -1;
    }

    void fetch() {
        // Two bytes of the instruction
        byte first = memory.get(registry.PC);
        byte second = memory.get((short) (registry.PC + 1));

        // A full 2 byte instruction held in short
        currentInstr = (short) (((first & 0xFF) << 8) | (second & 0xFF));
    }

    void incrementPC() {
        registry.PC = (short) (registry.PC + 0x02);
    }

    void decodeAndExecute() {
        byte firstNib = (byte) ((currentInstr >> 12) & 0xF);

        switch (firstNib) {
            case 0x00:
                if ((currentInstr & 0xFFFF) == 0x00E0) {
                    //00E0 - Clears the screen
                    clearScreen();
                } else if ((currentInstr & 0xFFFF) == 0x00EE) {
                    //00EE - Returns from a subroutine
                    returnSubroutine();
                }
                break;

            case 0x01:
                //1nnn - Jump to location nnn.
                jump((short) (currentInstr & 0x0FFF));
                break;

            case 0x02:
                //2nnn - Call subroutine at nnn.
                callSubroutine((short) (currentInstr & 0x0FFF));
                break;

            case 0x03:
                //3xkk - Skip next instruction if Vx = kk
                skipEqual((byte) ((currentInstr & 0x0F00) >> 8), (byte) (currentInstr & 0x0FF));
                break;

            case 0x04:
                //4xkk - Skip next instruction if Vx != kk
                skipNotEqual((byte) ((currentInstr & 0x0F00) >> 8), (byte) (currentInstr & 0x0FF));
                break;

            case 0x05:
                //5xy0 - Skip next instruction if Vx = Vy
                skipEqualRegs((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                break;

            case 0x06:
                //6xkk - Set Vx = kk
                setRegVal((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x0FF)));
                break;

            case 0x07:
                //7xkk - Set Vx = Vx + kk
                addRegVal((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x0FF)));
                break;

            case 0x08:
                if ((currentInstr & 0x0F) == 0x00) {
                    //8xy0 - Set Vx = Vy
                    setRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x01) {
                    //8xy1 - Set Vx = Vx or Vy
                    orRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x02) {
                    //8xy2 - Vx = Vx AND Vy
                    andRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x03) {
                    //8xy3 - Set Vx = Vx XOR Vy
                    xorRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x04) {
                    //8xy4 - Set Vx = Vx + Vy, set VF = carry
                    addRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x05) {
                    //8xy5 - Set Vx = Vx - Vy, set VF = NOT borrow
                    subRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x06) {
                    //8xy6 - Set Vx = Vx SHR 1, store least significant bit in VF
                    shiftRight((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0x0F) == 0x07) {
                    //8xy7 - Set Vx = Vy - Vx, set VF = NOT borrow
                    subNegativeRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x0E) {
                    //8xyE - Set Vx = Vx SHL 1, store most significant bit on VF
                    shiftLeft((byte) ((currentInstr & 0x0F00) >> 8));
                }

                break;

            case 0x09:
                //9xy0 - Skip next instruction if Vx != Vy
                skipNotEqualRegs((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                break;

            case 0x0A:
                //Annn - set I = nnn
                setIReg((short) (currentInstr & 0x0FFF));
                break;

            case 0x0B:
                //Bnnn - Jump to location nnn + V0
                jumpAddV0((short) (currentInstr & 0x0FFF));
                break;

            case 0x0C:
                //Cxkk - Set Vx = random byte AND kk
                rand((byte) ((currentInstr & 0x0F00) >> 8), (byte) (currentInstr & 0x0FF));
                break;

            case 0x0D:
                //Dxyn - Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision
                draw((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4), (byte) (currentInstr & 0x0F));
                break;

            case 0x0E:
                if ((currentInstr & 0xFF) == 0x9E) {
                    //Ex9E - Skip next instruction if key with value of Vx is pressed.
                    skipKeyPressed((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0xA1) {
                    //ExA1 - Skip next instruction if key with the value of Vx is not pressed.
                    skipKeyNotPressed((byte) ((currentInstr & 0x0F00) >> 8));
                }

                break;

//            case 0x0F:
//                if ((instr & 0xFF) == 0x07) {
//                    //Fx07 - Set Vx = delay timer value.
//                    System.out.println(String.format("LD V%01x, DT", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x0A) {
//                    //Fx0A - Wait for a key press, store the value of the key in Vx.
//                    System.out.println(String.format("LD V%01x, K", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x15) {
//                    //Fx15 - Set delay timer = Vx
//                    System.out.println(String.format("LD DT, V%01x", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x18) {
//                    //Fx18 - Set sound timer = Vx
//                    System.out.println(String.format("LD ST, V%01x", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x1E) {
//                    //Fx1E - Set I = I + Vx
//                    System.out.println(String.format("ADD I, V%01x", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x29) {
//                    //Fx29 - Set I = location of sprite for digit Vx
//                    System.out.println(String.format("LD F, V%01x", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x33) {
//                    //Fx33 - Store BCD representation of Vx in memory locations I, I+1, I+2
//                    System.out.println(String.format("LD B, V%01x", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x55) {
//                    //Fx55 - Store registers V0 through Vx in memory starting at location I.
//                    System.out.println(String.format("LD [I], V%01x", (instr & 0x0F00) >> 8));
//                } else if ((instr & 0xFF) == 0x65) {
//                    //Fx65 - Read registers V0 through Vx from memory starting at location I.
//                    System.out.println(String.format("LD V%01x, [I]", (instr & 0x0F00) >> 8));
//                } else {
//                    System.out.println("UNKN F");
//                }
//                break;

            default:
                System.out.println();
                break;
        }
    }

    /**
     * 00E0 - CLS.
     * Clear the display.
     */
    public void clearScreen() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                display.setPixel(x, y, false, false);
            }
        }
    }

    /**
     * 00EE - RET.
     * Return from a subroutine.
     * Sets the program counter to the address at the top of the stack, then decrements the SP.
     */
    public void returnSubroutine() {
        registry.PC = memory.getStack(registry.SP);
        registry.SP = (byte) (registry.SP - 1);
    }

    /**
     * 1nnn - JP addr.
     * Sets the program counter to nnn.
     * @param address Adress of the jump location.
     */
    public void jump(short address) {
        registry.PC = address;
    }

    /**
     * 2nnn - CALL addr.
     * Call subroutine at nnn.
     * Increments the SP, puts the current PC on the top of the stack.
     */
    public void callSubroutine(short address) {
        registry.SP = (byte) (registry.SP + 1); //increment the SP
        memory.setStack(registry.SP, registry.PC); //set stack[SP] = PC
        registry.PC = address;
    }

    /**
     * 3xkk - SE Vx, byte.
     * Skip next instruction if Vx == kk.
     * If equal, increment PC by 2.
     * @param reg Register to compare.
     * @param value Value to compare.
     */
    public void skipEqual(byte reg, byte value) {
        if (registry.VReg[reg] == value) {
            registry.PC = (short) (registry.PC + 2);
        }
    }

    /**
     * 4xkk - SN Vx, byte.
     * Skip next instruction if Vx != kk.
     * If not equal, increment PC by 2.
     * @param reg Register to compare.
     * @param value Value to compare.
     */
    public void skipNotEqual(byte reg, byte value) {
        if (registry.VReg[reg] != value) {
            registry.PC = (short) (registry.PC + 2);
        }
    }

    /**
     * 5xy0 - SE Vx, Vy.
     * Skip next instruction, if Vx == Vx.
     * If regs are equal, increment PC by 2.
     * @param first First register to compare.
     * @param second Second register to compare.
     */
    public void skipEqualRegs(byte first, byte second) {
        if (registry.VReg[first] == registry.VReg[second]) {
            registry.PC = (short) (registry.PC + 2);
        }
    }

    /**
     * 6xkk - LD Vx, byte.
     * Set Vx = kk.
     * @param reg Register to set.
     * @param val Value to set.
     */
    public void setRegVal(byte reg, byte val) {
        registry.VReg[reg] = val;
    }

    /**
     * 7xkk - ADD Vx, byte.
     * Set Vx = Vx + kk.
     * Adds the value kk to the value of register Vx, stores the result in Vx.
     * @param reg Register to add to.
     * @param val Value to add.
     */
    public void addRegVal(byte reg, byte val) {
        registry.VReg[reg] = (byte) (registry.VReg[reg] + val);
    }

    /**
     * 8xy0 - LD Vx, Vy.
     * Set Vx = Vy.
     * Stores the value of register Vy in register Vx.
     * @param first Register to store value in.
     * @param second Register with the value to store.
     */
    public void setRegReg(byte first, byte second) {
        registry.VReg[first] = registry.VReg[second];
    }

    /**
     * 8xy1 - OR Vx, Vy.
     * Set Vx = Vx OR Vy.
     * Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx.
     * @param first First register to OR.
     * @param second Second register to OR.
     */
    public void orRegReg(byte first, byte second) {
        registry.VReg[first] = (byte) (registry.VReg[first] | registry.VReg[second]);
    }

    /**
     * 8xy2 - AND Vx, Vy.
     * Set Vx = Vx AND Vy.
     * Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
     * @param first First register to AND.
     * @param second Second register to AND.
     */
    public void andRegReg(byte first, byte second) {
        registry.VReg[first] = (byte) (registry.VReg[first] & registry.VReg[second]);
    }

    /**
     * 8xy3 - XOR Vx, Vy.
     * Set Vx = Vx XOR Vy.
     * Performs a bitwise XOR on the values of Vx and Vy, then stores the result in Vx.
     * @param first First register to XOR.
     * @param second Second register to XOR.
     */
    public void xorRegReg(byte first, byte second) {
        registry.VReg[first] = (byte) (registry.VReg[first] ^ registry.VReg[second]);
    }

    /**
     * 8xy4 - ADD Vx, Vy.
     * Set Vx = Vx + Vy, set VF = carry.
     * Values of Vx and Vy are added together, If the result is greater than 8 bits (255), VF is set to 1, otherwise 0.
     * Lowest 8 bits of the result are stored in Vx.
     * @param first First register to add.
     * @param second Second register to add.
     */
    public void addRegReg(byte first, byte second) {
        //count the result using a bigger type (short), so we see that a carry occurs
        short result = (short) ((registry.VReg[first] & 0xFF) + (registry.VReg[second] & 0xFF));

        //carry stored in VF
        if ((result & 0x0FF00) >> 8 > 0) {
            registry.VReg[0x0F] = 1;
        } else {
            registry.VReg[0x0F] = 0;
        }

        //store lower byte in register
        registry.VReg[first] = (byte) (result & 0x0FF);
    }

    /**
     * 8xy5 - SUB Vx, Vy.
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     * Subtract Vy from Vx, store result in Vx. If Vx > Vy, set VF to 1, otherwise 0.
     * @param first Register to subtract from.
     * @param second Register to subtract.
     */
    public void subRegReg(byte first, byte second) {
        //count the result using a bigger type (short), so we are able to see that a carry occurs
        short result = (short) ((registry.VReg[first] & 0xFF) - (registry.VReg[second] & 0xFF));

        short vx = (short) (registry.VReg[first] & 0xFF);
        short vy = (short) (registry.VReg[second] & 0xFF);

        //carry stored in VF
        if (vx > vy) {
            registry.VReg[0x0F] = 1;
        } else {
            registry.VReg[0x0F] = 0;
        }

        //store lower byte in register
        registry.VReg[first] = (byte) (result & 0x0FF);
    }

    /**
     * 8xy6 - SHR Vx {, Vy}.
     * Set Vx = Vx SHR 1.
     * Shift register Vx right. Least significant bit of Vx is stored in VF.
     * @param reg Register to shift right.
     */
    public void shiftRight(byte reg) {
        //store least significant byte in VF
        registry.VReg[0xF] = (byte) (registry.VReg[reg] & 0x1);

        //casting register to a bigger type, getting rid of sign with 0xFF
        short val = (short) (registry.VReg[reg] & 0xFF);

        /// >>> means zero fill right shift
        registry.VReg[reg] = (byte) (val >>> 1);
    }

    /**
     * 8xy7 - SUBN Vx, Vy.
     * Set Vx = Vy - Vx, set VF = NOT borrow.
     * Subtract Vx from Vy, store result in Vx. If Vy > Vx, set VF to 1, otherwise 0.
     * @param first Register to subtract.
     * @param second Register to subtract from.
     */
    public void subNegativeRegReg(byte first, byte second) {
        //count the result using a bigger type (short), so we are able to see that a carry occurs
        short result = (short) ((registry.VReg[second] & 0xFF) - (registry.VReg[first] & 0xFF));

        short vx = (short) (registry.VReg[first] & 0xFF);
        short vy = (short) (registry.VReg[second] & 0xFF);

        //carry stored in VF
        if (vy > vx) {
            registry.VReg[0x0F] = 1;
        } else {
            registry.VReg[0x0F] = 0;
        }

        //store lower byte in register
        registry.VReg[first] = (byte) (result & 0x0FF);
    }

    /**
     * 8xyE - SHL Vx {, Vy}.
     * Set Vx= Vx SHL 1
     * Shift register Vx left. Most significant bit of Vx is stored in VF.
     * @param reg Register to shift left.
     */
    public void shiftLeft(byte reg) {
        //store most significant byte in VF
        registry.VReg[0xF] = (byte) ((registry.VReg[reg] & 0x80) >> 7);

        //casting register to a bigger type, getting rid of sign with 0xFF
        short val = (short) (registry.VReg[reg] & 0xFF);

        //shifting left
        registry.VReg[reg] = (byte) (val << 1);
    }

    /**
     * 9xy0 - SNE Vx, Vy.
     * Skip next instruction, if Vx != Vx.
     * If regs are not equal, increment PC by 2.
     * @param first First register to compare.
     * @param second Second register to compare.
     */
    public void skipNotEqualRegs(byte first, byte second) {
        if (registry.VReg[first] != registry.VReg[second]) {
            registry.PC = (short) (registry.PC + 2);
        }
    }

    /**
     * Annn - LD I, addr.
     * The value of register I is set to nnn.
     * @param value Value to store in register I.
     */
    public void setIReg(short value) {
        registry.IReg = value;
    }

    /**
     * Bnnn - JP V0, addr.
     * Jump to location nnn + V0.
     * @param address Address of jump destination, without the value of V0.
     */
    public void jumpAddV0(short address) {
        //using 0xFFFF and 0xFF to get rid of the sing
        registry.PC = (short) ((address & 0xFFFF) + (registry.VReg[0] & 0xFF));
    }

    /**
     * Cxkk - RND Vx, byte.
     * Set Vx = random byte AND kk.
     * Generates a random number from 0 to 255 which is then ANDed with value kk and then stored in Vx.
     * @param reg Register to store generated value.
     * @param value Value to AND random number with.
     */
    public void rand(byte reg, byte value) {
        registry.VReg[reg] = (byte) (randomGen.nextInt(256) & value);
    }

    /**
     * Dxyn - DRW Vx, Vy, nibble.
     * Display n-byte sprite starting at memory location stored in I register at (Vx, Vy), set VF = collision.
     * @param xReg Number of the register that holds the X position of the sprite to draw.
     * @param yReg Number of the register that holds the Y position of the sprite to draw.
     * @param numberOfBytes Number of bytes from the memory to draw, beginning from the address stored in the I register.
     */
    public void draw(byte xReg, byte yReg, byte numberOfBytes) {
        boolean collision = display.drawSprite(registry.VReg[xReg], registry.VReg[yReg], registry.IReg, numberOfBytes);
        if (collision) {
            registry.VReg[0xF] = 1;
        } else {
            registry.VReg[0xF] = 0;
        }
    }

    /**
     * Ex9E - SKP Vx
     * Skip next instruction, if key with value of Vx is pressed.
     * @param reg Register that holds the value of the key.
     */
    public void skipKeyPressed(byte reg) {
        if (keyboard.getKey(registry.VReg[reg])) {
            registry.PC = (short) (registry.PC + 2);
        }
    }

    /**
     * ExA1 - SKNP Vx
     * Skip next instruction, if key with value of Vx is not pressed.
     * @param reg Register that holds the value of the key.
     */
    public void skipKeyNotPressed(byte reg) {
        if (!keyboard.getKey(registry.VReg[reg])) {
            registry.PC = (short) (registry.PC + 2);
        }
    }
}

