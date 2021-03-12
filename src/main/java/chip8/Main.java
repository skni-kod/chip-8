package chip8;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: java -jar chip8.jar (ROM_PATH) (-PARAMETERS)");
        }

        String filename = args[0];
        int cpuFreq = 500;
        boolean regGUIFlag = false;
        boolean loadStoreQuirk = false;
        boolean shiftQuirk = true;
        boolean overlappingMode = true;

        //parsing arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i])
            {
                case "-freq": {
                    if (i + 1 < args.length) {
                        cpuFreq = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                }

                case "-regGUI": {
                    regGUIFlag = true;
                    break;
                }

                case "-loadq": {
                    if (i + 1 < args.length) {
                        loadStoreQuirk = Boolean.parseBoolean(args[i + 1]);
                        i++;
                    }
                    break;
                }

                case "-shiftq": {
                    if (i + 1 < args.length) {
                        shiftQuirk = Boolean.parseBoolean(args[i + 1]);
                        i++;
                    }
                    break;
                }

                case "-overlap": {
                    if (i + 1 < args.length) {
                        overlappingMode = Boolean.parseBoolean(args[i + 1]);
                        i++;
                    }
                    break;
                }
            }
        }

        Chip8 chip8 = new Chip8(filename, cpuFreq, loadStoreQuirk, shiftQuirk, overlappingMode, regGUIFlag);
        chip8.loop();

    }

}
