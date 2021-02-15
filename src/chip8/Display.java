package chip8;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Display {
    /**
     * 32 x 64 screen.
     */
    private boolean screen[][];

    /**
     * Size of a single pixel on the screen.
     */
    private int pixelSize;

    /**
     * Reference to the memory.
     */
    private Memory memory;

    /**
     * Reference to the keyboard.
     */
    private Keyboard keyboard;

    /**
     * Swing JFrame.
     */
    private JFrame frame;

    /**
     * JPanel chip-8's screen is drawn on.
     */
    private DrawBoard drawBoard;

    /**
     * Constructor creating a new JFrame and DrawBoard.
     * @param pixelSize Size of a single pixel on the screen.
     * @param memory Reference to chip's memory.
     */
    public Display(int pixelSize, Memory memory, Keyboard keyboard) {
        this.pixelSize = pixelSize;

        screen = new boolean[64][32];

        this.memory = memory;
        this.keyboard = keyboard;
    }

    public void initDisplay() {
        frame = new JFrame("Chip-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addKeyListener(keyboard);

        drawBoard = new DrawBoard();
        drawBoard.setPreferredSize(new Dimension(pixelSize * 64, pixelSize * 32));
        frame.getContentPane().add(drawBoard);

        frame.setSize(new Dimension(pixelSize * 64 + 32, pixelSize * 32 + 64));
        frame.setVisible(true);
    }

    /**
     * Sets a single pixel on the screen.
     * @param x The X position on the screen.
     * @param y The Y position on the screen.
     * @param value True to turn pixel on, otherwise false.
     * @param sprite True if drawing a sprite.
     * @return True if collision occurs, otherwise false.
     */
    public boolean setPixel(int x, int y, boolean value, boolean sprite) {
//        if (x < 0 || x > 63) {
//            System.out.println("Screen X coordinate out of range.");
//        } else if (y < 0 || y > 31) {
//            System.out.println("Screen Y coordinate out of range.");
//        }
        //drawing sprites or normal pixels
        //when drawing sprites, pixels are XORed on the screen
        //when not drawing a sprite, pixels are just set to the value
        if (sprite) {
            //getting the initial value of the pixel
            boolean prevVal = screen[x % 64][y % 32];
            //XOR-ing the value on the screen
            screen[x % 64][y % 32] = value ^ screen[x % 64][y % 32];

            //returns true if pixel was erased - a collision occurred, otherwise false.
            return prevVal && !screen[x % 64][y % 32];
        } else {
            screen[x][y] = value;
            return false;
        }
    }

    /**
     * Gets a single pixel from the screen.
     * @param x The x position of the pixel on the screen.
     * @param y The y position of the pixel on the screen.
     * @return True if set, otherwise false.
     */
    public boolean getPixel(int x, int y) {
        return screen[x][y];
    }

    /**
     * Gets a byte representing 8 pixels from the screen, beginning from the x and y position.
     * If x or y is bigger than the screen size, it overlaps to the other side of the screen (returns to the beginning).
     * @param x The x position of the most significant bit.
     * @param y The y position of the most significant bit.
     * @return Byte representing 8 pixels from the screen.
     */
    public byte getByte(int x, int y) {
        byte screenValue = 0;
        for (int i = 0; i < 8; i++) {
            boolean isSet = this.getPixel((byte) (x + i) % 64, (byte) y % 32);
            if (isSet) {
                screenValue = (byte) (screenValue | 1);
            }
            //shift left to make room for the next bit
            if (i != 7) {
                screenValue = (byte) (screenValue << 1);
            }
        }
        return screenValue;
    }

    /**
     * Draws a sprite on the screen at x and y position, from the memory adress.
     * @param x The x position on the screen.
     * @param y The y position on the screen.
     * @param address Adress of the sprite's beginning in the memory.
     * @param numberOfBytes Number of bytes from the memory to draw, beginning from the address.
     * @return True if collision occurred, otherwise false.
     */
    public boolean drawSprite(int x, int y, int address, int numberOfBytes) {
        boolean collision = false;
        //for each of the bytes
        for (int i = 0; i < numberOfBytes; i++) {
            //get the byte from the memory
            byte value = memory.get((short) (address + i));
            //for each bit from the byte, draw the value to the screen
            for (int j = 0; j < 8; j++) {
                //collision is ORed with the return value of setPixel
                collision = collision | this.setPixel(x + (7 - j), y + i, (value & (0x1 << j)) != 0, true);
            }
        }
        return collision;
    }

    public boolean[][] getScreen() {
        boolean[][] screenCopy = new boolean[64][32];
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screenCopy[x][y] = screen[x][y];
            }
        }
        return screenCopy;
    }

    public void render() {
        frame.repaint();
    }

    /**
     * DrawBoard class overriding the painComponent. Draws the contents of the screen to the JPanel visible in JFrame.
     */
    private class DrawBoard extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(pixelSize * 64, pixelSize * 32);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int x = 0; x < screen.length; x++) {
                for (int y = 0; y < screen[x].length; y++) {
                    if (screen[x][y]) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
                }
            }
        }
    }

}
