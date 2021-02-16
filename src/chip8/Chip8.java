package chip8;

public class Chip8 {

    private CPU cpu;
    private Memory memory;
    private Registry registry;
    private Display display;
    private Keyboard keyboard;

    private Disassembler disassembler;


    public Chip8(String filename) {
        memory = new Memory();
        keyboard = new Keyboard();
        registry = new Registry();
        display = new Display(12, memory, keyboard);
        cpu = new CPU(memory, registry, display, keyboard);

        disassembler = new Disassembler(memory);


        int size;
        try {
            size = memory.loadFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        display.initDisplay();
    }

    public void loop() {

        final int TIMER_TICK = 1000 / 60;
        final int CPU_FREQ = 500;
        final int CPU_TICK = 1000 / CPU_FREQ;

        double nextTimerTick = System.currentTimeMillis();
        double nextCPUTick = System.currentTimeMillis();
        int loops = 0;

        while (true) {

//            if (registry.PC % 2 != 0) {
//                System.out.println("PC OUT OF ORDER");
//            }


            if (System.currentTimeMillis() > nextCPUTick) {

                cpu.fetch();

                disassembler.disassemble(registry.PC);

                cpu.incrementPC();

                cpu.decodeAndExecute();

                nextCPUTick += CPU_TICK;
            }

            if (System.currentTimeMillis() > nextTimerTick) {

                display.render();

                if ((registry.DT & 0xFF) > 0) {
                    registry.DT--;
                }

                if ((registry.ST & 0xFF) > 0) {
                    registry.ST--;
                }

                nextTimerTick += TIMER_TICK;
                loops++;
                System.out.println("Loop:" + loops);
            }

        }
    }
}
