package chip8;

public class Main {

    public static void main(String[] args) {
        Memory memory = new Memory();
        Registry registry = new Registry();

//        try {
//            memory.loadFile("flightrunner.ch8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        memory.printMemory((short) 0x200, (short) 0x400);

        Display display = new Display(12, memory);

        display.drawSprite(0, 0, Memory.SPRITE_9);
        display.drawSprite(5, 0, Memory.SPRITE_9);
        display.drawSprite(10, 0, Memory.SPRITE_7);
    }

}
