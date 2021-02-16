package chip8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Class representing chip-8's memory.
 */
public class Memory {

    public static final byte SPRITE_0 = 0;
    public static final byte SPRITE_1 = 5;
    public static final byte SPRITE_2 = 10;
    public static final byte SPRITE_3 = 15;
    public static final byte SPRITE_4 = 20;
    public static final byte SPRITE_5 = 25;
    public static final byte SPRITE_6 = 30;
    public static final byte SPRITE_7 = 35;
    public static final byte SPRITE_8 = 40;
    public static final byte SPRITE_9 = 45;
    public static final byte SPRITE_A = 50;
    public static final byte SPRITE_B = 55;
    public static final byte SPRITE_C = 60;
    public static final byte SPRITE_D = 65;
    public static final byte SPRITE_E = 70;
    public static final byte SPRITE_F = 75;

    /**
     * A 2D array holding all the initial sprites representing hex digits from 0 to F,
     * each sprite 5 bytes long, 8x5 pixels.
     */
    private static final byte[][] sprites = new byte[][] {
            // "0" sprite
            {
                    (byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0
            },
            // "1" sprite
            {
                    (byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70
            },
            // "2" sprite
            {
                    (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0
            },
            // "3" sprite
            {
                    (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0
            },
            // "4" sprite
            {
                    (byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10
            },
            // "5" sprite
            {
                    (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0
            },
            // "6" sprite
            {
                    (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0
            },
            // "7" sprite
            {
                    (byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40
            },
            // "8" sprite
            {
                    (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0
            },
            // "9" sprite
            {
                    (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0
            },
            // "A" sprite
            {
                    (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90
            },
            // "B" sprite
            {
                    (byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0
            },
            // "C" sprite
            {
                    (byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0
            },
            // "D" sprite
            {
                    (byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0
            },
            // "E" sprite
            {
                    (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0
            },
            // "F" sprite
            {
                    (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80
            },
    };

    /**
     * 4096 Bytes of memory
     */
    private byte[] memory;

    /**
     * 16 16-bit values
     */
    private short[] stack;

    public Memory() {
        this.memory = new byte[4096];
        this.stack = new short[16];
        this.initMemory();
    }

    /**
     * Gets a single byte from the memory.
     * @param address Address of the value in the memory.
     * @return Value held under the address in the memory.
     */
    public byte get(short address) {
        if (address > 0xFFF) {
            System.out.println("Address " + address +  " out of range!");
            return 0x0;
        } else {
            return (byte) (memory[(short) address]);
        }
    }

    /**
     * Sets a single byte in the memory.
     * @param address Address of the cell to set.
     * @param value Value of the cell to set.
     * @return True if successful, otherwise false.
     */
    public boolean set(short address, byte value) {
        if (address > 0xFFF) {
            System.out.println("Address " + address +  " out of range!");
            return false;
        } else {
            memory[address] = value;
            return true;
        }
    }

    public short getStack(short address) {
        if (address > 0x0F) {
            System.out.println("Stack address " + address +  " out of range!");
            return 0x0;
        } else {
            return stack[address];
        }
    }

    public boolean setStack(short address, short value) {
        if (address > 0x0F) {
            System.out.println("Stack address " + address +  " out of range!");
            return false;
        } else {
            stack[address] = value;
            return true;
        }
    }

    /**
     * Initializes memory with default sprites from 0 to F, each 5 bytes long.
     */
    private void initMemory() {
        short pointer = 0x0;
        for (int i = 0; i < sprites.length; i++) {
            for (int j = 0; j < sprites[i].length; j++) {
                this.set(pointer, sprites[i][j]);
                pointer++;
            }
        }
    }

    /**
     * Loads a file to the memory.
     * @param path Path to the file.
     * @return Size of the loaded file or -1 if failed.
     */
    public int loadFile(String path) throws IOException {
        byte[] program = Files.readAllBytes(new File(path).toPath());
        if (program.length > 0xFFF - 0x200) {
            System.out.println("File's size too big to fit!");
            return -1;
        }
        short pointer = 0x200;
        for (byte x : program) {
            memory[pointer] = x;
            pointer++;
        }
        return program.length;
    }

    /**
     * Prints out the memory.
     */
    public void printMemory() {
        System.out.println("Chip8.Memory:");
        for (int i = 0; i < memory.length; i++) {
            System.out.println("0x" + Integer.toHexString(i).toUpperCase() + ":" + memory[i]);
        }
    }

    /**
     * Prints out a chunk of memory.
     * @param from Beginning adresss.
     * @param to End adress.
     */
    public void printMemory(short from, short to) {
        System.out.println("Chip8.Memory from " + from + " to " + to);
        if (from < 0 || from > 0xFFF || to < 0 || to > 0xFFF) {
            System.out.println("Address out of range!");
        }
        for (int i = from; i < to; i++) {
            System.out.println("0x" + Integer.toHexString(i).toUpperCase() + ":" + memory[i]);
        }
    }

    /**
     * Prints out the stack.
     */
    public void printStack() {
        System.out.println("Stack:");
        for (int i = 0; i < stack.length; i++) {
            System.out.println("0x" + Integer.toHexString(i).toUpperCase() + ":" + stack[i]);
        }
    }
}
