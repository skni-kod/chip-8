package chip8;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Key;

public class Main {

    public static void main(String[] args) {

        String filename = "./test_roms/c8_test.c8";

        Memory memory = new Memory();
        int size = 0;
        try {
            size = memory.loadFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Disassembler disassembler = new Disassembler(memory);
        disassembler.disassembleToFile((short) 0x200, size, "./test_roms/c8_test.c8" + ".txt");

        Chip8 chip8 = new Chip8(filename);
        chip8.loop();

    }
}
