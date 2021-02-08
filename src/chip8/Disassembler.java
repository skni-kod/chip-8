package chip8;

/**
 * Class representing a simple linear chip-8 program disassembler.
 * It's not very effective as it treats binary data stored in ROM (sprites) as opcodes.
 */
public class Disassembler {

    Memory memory;

    public Disassembler(Memory memory) {
        this.memory = memory;
    }

    public void disassemble(short PC) {
        // Two bytes of the instruction
        byte first = memory.get(PC);
        byte second = memory.get((short) (PC + 1));

        // The first nibble of the instruction
        byte firstNib = (byte) ((first >> 4) & 0xF);

        // A full 2 byte instruction held in short
        short instr = (short) (((first & 0xFF) << 8) | (second & 0xFF));


        System.out.print(String.format("%03X %02X %02X ", PC, first, second));

        switch (firstNib) {
            case 0x00:
                if ((instr & 0xFFFF) == 0x00E0) {
                    //00E0 - Clears the screen
                    System.out.println("CLS");
                } else if ((instr & 0xFFFF) == 0x00EE) {
                    //00EE - Returns from a subroutine
                    System.out.println("RET");
                } else {
                    System.out.println();
                }
                break;

            case 0x01:
                //1nnn - Jump to location nnn.
                System.out.println(String.format("JP %03x", (instr & 0x0FFF)));
                break;

            case 0x02:
                //2nnn - Call subroutine at nnn.
                System.out.println(String.format("CALL %03x", (instr & 0x0FFF)));
                break;

            case 0x03:
                //3xkk - Skip next instruction if Vx = kk
                System.out.println(String.format("SE V%01x, %02x", (instr & 0x0F00) >> 8, (instr & 0x00FF)));
                break;

            case 0x04:
                //4xkk - Skip next instruction if Vx != kk
                System.out.println(String.format("SNE V%01x, %02x", (instr & 0x0F00) >> 8, (instr & 0x00FF)));
                break;

            case 0x05:
                //5xy0 - Skip next instruction if Vx = Vy
                System.out.println(String.format("SE V%01x, V%02x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                break;

            case 0x06:
                //6xkk - Set Vx = kk
                System.out.println(String.format("LD V%01x, %02x", (instr & 0x0F00) >> 8, (instr & 0x00FF)));
                break;

            case 0x07:
                //7xkk - Set Vx = Vx + kk
                System.out.println(String.format("ADD V%01x, %02x", (instr & 0x0F00) >> 8, (instr & 0x00FF)));
                break;

            case 0x08:
                if ((instr & 0x0F) == 0x00) {
                    //8xy0 - Set Vx = Vy
                    System.out.println(String.format("LD V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x01) {
                    //8xy1 - Set Vx = Vx or Vy
                    System.out.println(String.format("OR V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x02) {
                    //8xy2 - Vx = Vx AND Vy
                    System.out.println(String.format("AND V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x03) {
                    //8xy3 - Set Vx = Vx XOR Vy
                    System.out.println(String.format("XOR V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x04) {
                    //8xy4 - Set Vx = Vx + Vy, set VF = carry
                    System.out.println(String.format("ADD V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x05) {
                    //8xy5 - Set Vx = Vx - Vy, set VF = NOT borrow
                    System.out.println(String.format("SUB V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x06) {
                    //8xy6 - Set Vx = Vx SHR 1, store least significant bit in VF
                    System.out.println(String.format("SHR V%01x {, V%01x}", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x07) {
                    //8xy7 - Set Vx = Vy - Vx, set VF = NOT borrow
                    System.out.println(String.format("SUBN V%01x, V%01x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else if ((instr & 0x0F) == 0x0E) {
                    //8xyE - Set Vx = Vx SHL 1, store most significant bit on VF
                    System.out.println(String.format("SHL V%01x {, V%01x}", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                } else {
                    System.out.println();
                }
                break;

            case 0x09:
                //9xy0 - Skip next instruction if Vx != Vy
                System.out.println(String.format("SNE V%01x, V%02x", (instr & 0x0F00) >> 8, (instr & 0x00F0) >> 4));
                break;

            case 0x0A:
                //Annn - set I = nnn
                System.out.println(String.format("LD I, %03x", (instr & 0x0FFF)));
                break;

            case 0x0B:
                //Bnnn - Jump to location nnn + V0
                System.out.println(String.format("JP V0, %03x", (instr & 0x0FFF)));
                break;

            case 0x0C:
                //Cxkk - Set Vx = random byte AND kk
                System.out.println(String.format("RND V%01x, %02x", (instr & 0x0F00) >> 8, (instr & 0x00FF)));
                break;

            case 0x0D:
                //Dxyn - Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision
                System.out.println(String.format("RND V%01x, %02x", (instr & 0x0F00) >> 8, (instr & 0x00FF)));
                break;

            case 0x0E:
                if ((instr & 0xFF) == 0x9E) {
                    //Ex9E - Skip next instruction if key with value of Vx is pressed.
                    System.out.println(String.format("SKP V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0xA1) {
                    //ExA1 - Skip next instruction if key with the value of Vx is not pressed.
                    System.out.println(String.format("SKNP V%01x", (instr & 0x0F00) >> 8));
                } else {
                    System.out.println();
                }
                break;

            case 0x0F:
                if ((instr & 0xFF) == 0x07) {
                    //Fx07 - Set Vx = delay timer value.
                    System.out.println(String.format("LD V%01x, DT", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x0A) {
                    //Fx0A - Wait for a key press, store the value of the key in Vx.
                    System.out.println(String.format("LD V%01x, K", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x15) {
                    //Fx15 - Set delay timer = Vx
                    System.out.println(String.format("LD DT, V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x18) {
                    //Fx18 - Set sound timer = Vx
                    System.out.println(String.format("LD ST, V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x1E) {
                    //Fx1E - Set I = I + Vx
                    System.out.println(String.format("ADD I, V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x29) {
                    //Fx29 - Set I = location of sprite for digit Vx
                    System.out.println(String.format("LD F, V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x33) {
                    //Fx33 - Store BCD representation of Vx in memory locations I, I+1, I+2
                    System.out.println(String.format("LD B, V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x55) {
                    //Fx55 - Store registers V0 through Vx in memory starting at location I.
                    System.out.println(String.format("LD [I], V%01x", (instr & 0x0F00) >> 8));
                } else if ((instr & 0xFF) == 0x65) {
                    //Fx65 - Read registers V0 through Vx from memory starting at location I.
                    System.out.println(String.format("LD V%01x, [I]", (instr & 0x0F00) >> 8));
                } else {
                    System.out.println();
                }
                break;

            default:
                System.out.println();
                break;
        }
    }
}
