public class Main {

    public static void main(String[] args) {
        Memory memory = new Memory();
        Registry registry = new Registry();

        try {
            memory.loadFile("flightrunner.ch8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        memory.printMemory((short) 0x200, (short) 0x400);
    }
}
