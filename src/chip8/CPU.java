package chip8;

public class CPU {

    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;

    short currentInstr;

    public CPU(Memory memory, Registry registry, Display display, Keyboard keyboard) {
        this.memory = memory;
        this.registry = registry;
        this.display = display;
        this.keyboard = keyboard;

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
                }
//                else if ((currentInstr & 0x0F) == 0x06) {
//                    //8xy6 - Set Vx = Vx SHR 1, store least significant bit in VF
//                    shiftRight((byte) ((currentInstr & 0x0F00) >> 8));
//                } else if ((currentInstr & 0x0F) == 0x07) {
//                    //8xy7 - Set Vx = Vy - Vx, set VF = NOT borrow
//                    subNegativeRegReg((byte) ((currentInstr & 0x0F00) >> 8), (byte) ((currentInstr & 0x00F0) >> 4));
//                } else if ((currentInstr & 0x0F) == 0x0E) {
//                    //8xyE - Set Vx = Vx SHL 1, store most significant bit on VF
//                    shiftLeft((byte) ((currentInstr & 0x0F00) >> 8));
//                }

                break;



            default:
                System.out.println();
                break;
        }
    }

    /**
     * 00E0 - CLS
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
     * 00EE - RET
     * Return from a subroutine.
     * Sets the program counter to the address at the top of the stack, then decrements the SP.
     */
    public void returnSubroutine() {
        registry.PC = memory.getStack(registry.SP);
        registry.SP = (byte) (registry.SP - 1);
    }

    /**
     * 1nnn - JP addr
     * Sets the program counter to nnn.
     * @param adress Adress of the jump location.
     */
    public void jump(short adress) {
        registry.PC = adress;
    }

    /**
     * 2nnn - CALL addr
     * Call subroutine at nnn.
     * Increments the SP, puts the current PC on the top of the stack.
     */
    public void callSubroutine(short address) {
        registry.SP = (byte) (registry.SP + 1); //increment the SP
        memory.setStack(registry.SP, registry.PC); //set stack[SP] = PC
        registry.PC = address;
    }

    /**
     * 3xkk - SE Vx, byte
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
     * 4xkk - SN Vx, byte
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
     * 5xy0 - SE Vx, Vy
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
     * 6xkk - LD Vx, byte
     * Set Vx = kk.
     * @param reg Register to set.
     * @param val Value to set.
     */
    public void setRegVal(byte reg, byte val) {
        registry.VReg[reg] = val;
    }

    /**
     * 7xkk - ADD Vx, byte
     * Set Vx = Vx + kk.
     * Adds the value kk to the value of register Vx, stores the result in Vx.
     * @param reg Register to add to.
     * @param val Value to add.
     */
    public void addRegVal(byte reg, byte val) {
        registry.VReg[reg] = (byte) (registry.VReg[reg] + val);
    }

    /**
     * 8xy0 - LD Vx, Vy
     * Set Vx = Vy.
     * Stores the value of register Vy in register Vx.
     * @param first Register to store value in.
     * @param second Register with the value to store.
     */
    public void setRegReg(byte first, byte second) {
        registry.VReg[first] = registry.VReg[second];
    }

    /**
     * 8xy1 - OR Vx, Vy
     * Set Vx = Vx OR Vy.
     * Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx.
     * @param first First register to OR.
     * @param second Second register to OR.
     */
    public void orRegReg(byte first, byte second) {
        registry.VReg[first] = (byte) (registry.VReg[first] | registry.VReg[second]);
    }

    /**
     * 8xy2 - AND Vx, Vy
     * Set Vx = Vx AND Vy.
     * Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
     * @param first First register to AND.
     * @param second Second register to AND.
     */
    public void andRegReg(byte first, byte second) {
        registry.VReg[first] = (byte) (registry.VReg[first] & registry.VReg[second]);
    }

    /**
     * 8xy3 - XOR Vx, Vy
     * Set Vx = Vx XOR Vy.
     * Performs a bitwise XOR on the values of Vx and Vy, then stores the result in Vx.
     * @param first First register to XOR.
     * @param second Second register to XOR.
     */
    public void xorRegReg(byte first, byte second) {
        registry.VReg[first] = (byte) (registry.VReg[first] ^ registry.VReg[second]);
    }

    /**
     * 8xy4 - ADD Vx, Vy
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
     * 8xy5 - SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     * Subtract Vy from Vx, store result in Vx. If Vx > Vy, set VF to 1, otherwise 0.
     * @param first Register to subtract from.
     * @param second Register to subtract.
     */
    public void subRegReg(byte first, byte second) {
        //count the result using a bigger type (short), so we see that a carry occurs
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
}

