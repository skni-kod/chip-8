package chip8;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Key;

public class Main {

    public static void main(String[] args) {

        Chip8 chip8 = new Chip8();
        chip8.loop();

//        memory.printMemory((short) 0x200, (short) 0x400);

//        Keyboard keyboard = new Keyboard();
//
//        Display display = new Display(12, memory, keyboard);
//
//        display.drawSprite(0, 0, Memory.SPRITE_9);
//        display.drawSprite(5, 0, Memory.SPRITE_9);
//        display.drawSprite(10, 0, Memory.SPRITE_7);

//        Disassembler disassembler = new Disassembler(memory);
//        disassembler.disassembleToFile((short) 0x200, size, "Fishie.txt");

    }
}
