package chip8;

import java.util.Random;

public class CPU {

    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;

    private Random randomGen;

    short currentInstr;

    boolean loadStoreQuirk;
    boolean shiftQuirk;

    public CPU(Memory memory, Registry registry, Display display, Keyboard keyboard, boolean loadStoreQuirk, boolean shiftQuirk) {
        this.memory = memory;
        this.registry = registry;
        this.display = display;
        this.keyboard = keyboard;

        this.loadStoreQuirk = loadStoreQuirk;
        this.shiftQuirk = shiftQuirk;

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

//        System.out.println(String.format("Instruction:%04X", currentInstr));

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
                    shiftRight((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x07) {
                    //8xy7 - Set Vx = Vy - Vx, set VF = NOT borrow
                    subNegativeRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
                } else if ((currentInstr & 0x0F) == 0x0E) {
                    //8xyE - Set Vx = Vx SHL 1, store most significant bit on VF
                    shiftLeft((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
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

            case 0x0F:
                if ((currentInstr & 0xFF) == 0x07) {
                    //Fx07 - Set Vx = delay timer value.
                    setRegDT((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x0A) {
                    //Fx0A - Wait for a key press, store the value of the key in Vx.
                    waitKeySetReg((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x15) {
                    //Fx15 - Set delay timer = Vx
                    setDTReg((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x18) {
                    //Fx18 - Set sound timer = Vx
                    setSTReg((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x1E) {
                    //Fx1E - Set I = I + Vx
                    setIRegSum((byte)  ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x29) {
                    //Fx29 - Set I = location of sprite for digit Vx
                    setISpriteAddrReg((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x33) {
                    //Fx33 - Store BCD representation of Vx in memory locations I, I+1, I+2
                    setBCDRegI((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x55) {
                    //Fx55 - Store registers V0 through Vx(including) in memory starting at location I.
                    storeRegsAtI((byte) ((currentInstr & 0x0F00) >> 8));
                } else if ((currentInstr & 0xFF) == 0x65) {
                    //Fx65 - Read registers V0 through Vx from memory starting at location I.
                    loadRegsAtI((byte) ((currentInstr & 0x0F00) >> 8));
                }

                break;

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
     * Set Vx = Vy SHR 1 or Vx = Vx SHR 1.
     * Shift register Vy right, then store the result in Vx. Least significant bit of Vy is stored in VF.
     * If ShiftQuirk is true, Shift register Vx rather than Vy and then store the result in Vx.
     * @param regX Register to store the result in.
     * @param regY Register to shift right if ShiftQuirk is false.
     */
    public void shiftRight(byte regX, byte regY) {
        short val;

        //if shift quirk is turned on, reg Vx is the one shifted and saved in Vx
        //otherwise Vy is the one shifted and saved in Vx
        //references aren't consistent about this instruction
        if (shiftQuirk) {
            //store least significant byte in VF
            registry.VReg[0xF] = (byte) (registry.VReg[regX] & 0x1);

            //casting register to a bigger type, getting rid of sign with 0xFF
            val = (short) (registry.VReg[regX] & 0xFF);
        } else {
            //store least significant byte in VF
            registry.VReg[0xF] = (byte) (registry.VReg[regY] & 0x1);

            //casting register to a bigger type, getting rid of sign with 0xFF
            val = (short) (registry.VReg[regY] & 0xFF);
        }

        /// >>> means zero fill right shift
        registry.VReg[regX] = (byte) (val >>> 1);
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
     * Set Vx = Vy SHL 1 or Vx = Vx SHL 1.
     * Shift register Vy left, then store the result in Vx.. Most significant bit of Vx is stored in VF.
     * If ShiftQuirk is true, Shift register Vx rather than Vy and then store the result in Vx.
     * @param regX Register to store the result in.
     * @param regY Register to shift left, if ShiftQuirk is false.
     */
    public void shiftLeft(byte regX, byte regY) {
        short val;

        //if shift quirk is turned on, reg Vx is the one shifted and saved in Vx
        //otherwise Vy is the one shifted and saved in Vx
        //references aren't consistent about this instruction
        if (shiftQuirk) {
            //store most significant byte in VF
            registry.VReg[0xF] = (byte) ((registry.VReg[regX] & 0x80) >> 7);

            //casting register to a bigger type, getting rid of sign with 0xFF
            val = (short) (registry.VReg[regX] & 0xFF);
        } else {
            //store most significant byte in VF
            registry.VReg[0xF] = (byte) ((registry.VReg[regY] & 0x80) >> 7);

            //casting register to a bigger type, getting rid of sign with 0xFF
            val = (short) (registry.VReg[regY] & 0xFF);
        }

        //shifting left
        registry.VReg[regX] = (byte) (val << 1);
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

    /**
     * Fx07 - LD Vx, DT
     * Set Vx = delay timer value.
     * @param reg Register to set.
     */
    public void setRegDT(byte reg) {
        registry.VReg[reg] = (byte) registry.DT;
    }

    /**
     * Fx0A - LD Vx, K
     * Wait for a key press, store the value of the key in Vx.
     * @param reg Register to store the value in.
     */
    public void waitKeySetReg(byte reg) {
        registry.VReg[reg] = (byte) keyboard.waitForKey();
    }

    /**
     * Fx15 - LD DT, Vx
     * Set delay timer = Vx.
     * @param reg Register with the value to set DT to.
     */
    public void setDTReg(byte reg) {
        registry.DT = (byte) registry.VReg[reg];
    }

    /**
     * Fx18 - LD ST, Vx
     * Set sound timer = Vx.
     * @param reg Register with the value.
     */
    public void setSTReg(byte reg) {
        registry.ST = registry.VReg[reg];
    }

    /**
     * Fx1E - Add I, Vx
     * Set I = I + Vx.
     * @param reg Register to add to I.
     */
    public void setIRegSum(byte reg) {
        registry.IReg = (short) (registry.IReg + (registry.VReg[reg] & 0xFF));
    }

    /**
     * Fx29 - LD F, Vx
     * Set I = location of spite for digit stored in Vx.
     * @param reg Register that holds the hex digit which sprite's address to store in I.
     */
    public void setISpriteAddrReg(byte reg) {

        switch (registry.VReg[reg]) {

            case 0x0:
                registry.IReg = Memory.SPRITE_0;
                break;

            case 0x1:
                registry.IReg = Memory.SPRITE_1;
                break;

            case 0x2:
                registry.IReg = Memory.SPRITE_2;
                break;

            case 0x3:
                registry.IReg = Memory.SPRITE_3;
                break;

            case 0x4:
                registry.IReg = Memory.SPRITE_4;
                break;

            case 0x5:
                registry.IReg = Memory.SPRITE_5;
                break;

            case 0x6:
                registry.IReg = Memory.SPRITE_6;
                break;

            case 0x7:
                registry.IReg = Memory.SPRITE_7;
                break;

            case 0x8:
                registry.IReg = Memory.SPRITE_8;
                break;

            case 0x9:
                registry.IReg = Memory.SPRITE_9;
                break;

            case 0xA:
                registry.IReg = Memory.SPRITE_A;
                break;

            case 0xB:
                registry.IReg = Memory.SPRITE_B;
                break;

            case 0xC:
                registry.IReg = Memory.SPRITE_C;
                break;

            case 0xD:
                registry.IReg = Memory.SPRITE_D;
                break;

            case 0xE:
                registry.IReg = Memory.SPRITE_E;
                break;

            case 0xF:
                registry.IReg = Memory.SPRITE_F;
                break;

        }
    }

    /**
     * Fx33 - LD B, Vx
     * Store BCD representation of Vx in memory locations I, I+1, I+2.
     * Hundreds digit stored in memory at location I, tens digit at I+1 and ones digit at I + 2.
     * @param reg Register that holds the value.
     */
    public void setBCDRegI(byte reg) {
        //calculate BCD representation of the number
        //as always, java's signed types are certainly not helping us, so there's lot of byte masking
        byte value = (byte) (registry.VReg[reg] & 0xFF);
        byte ones = (byte) ((value & 0xFF) % 10);
        byte tens = (byte) (((value & 0xFF) - ones) % 100 / 10);
        byte hundreds = (byte) (((value & 0xFF) - ones - tens) % 1000 / 100);

        //set the memory using the I register
        memory.set(registry.IReg, hundreds);
        memory.set((short) (registry.IReg + 1), tens);
        memory.set((short) (registry.IReg + 2), ones);
    }

    /**
     * Fx55 - LD [I], Vx
     * Store register from V0 to Vx (included) in memory, starting at location stored in I.
     * Increment I (I = I + X + 1)
     * @param reg Index of the last register included.
     */
    public void storeRegsAtI(byte reg) {
        //copying the values of registry to the memory, starting at I
        for (int i = 0; i <= reg; i++) {
            //if loadStoreQuirk, I Register is incremented/modified when storing regs in memory
            //references aren't consistent about this instruction
            if (loadStoreQuirk) {
                memory.set((short) (registry.IReg + i), registry.VReg[i]);
            } else {
                memory.set((short) (registry.IReg), registry.VReg[i]);
                registry.IReg++;
            }
        }
    }

    /**
     * Fx65 - LD Vx, [I]
     * Read registers from V0 to Vx (included) from memory, starting at location stored in I.
     * Increment I (I = I + X + 1)
     * @param reg Index of the last register included.
     */
    public void loadRegsAtI(byte reg) {
        //loading the values from the memory to the registry, starting at I
        for (byte i = 0; i <= reg; i++) {
            //if loadStoreQuirk, I Register is not incremented/modified when loading regs from memory
            //references aren't consistent about this instruction
            if (loadStoreQuirk) {
                registry.VReg[i] = memory.get((short) (registry.IReg + i));
            } else {
                registry.VReg[i] = memory.get((short) (registry.IReg));
                registry.IReg++;
            }
        }
    }
}

