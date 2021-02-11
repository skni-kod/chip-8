package chip8;

import javax.swing.*;
import java.awt.*;
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

    //TODO return collisions
    //TODO XOR-ing
    /**
     * Sets a single pixel on the screen.
     * @param x The X position on the screen.
     * @param y The Y position on the screen.
     * @param value True to turn on, False to turn off.
     */
    public void setPixel(int x, int y, boolean value, boolean sprite) {
        if (x < 0 || x > 63) {
            System.out.println("Screen X coordinate out of range.");
        } else if (y < 0 || y > 31) {
            System.out.println("Screen Y coordinate out of range.");
        }
        if (sprite) {
            //XOR-ing the value on the screen
            screen[x % 64][y % 32] = value ^ screen[x % 64][y % 32];
        } else {
            screen[x][y] = value;
        }
    }

    public boolean getPixel(int x, int y) {
        return screen[x][y];
    }

    /**
     * Draws a sprite on the screen at x and y position, from the memory adress.
     * @param x The x position on the screen.
     * @param y The y position on the screen.
     * @param adress Adress of the sprite's beginning in the memory.
     */
    public void drawSprite(int x, int y, int adress) {
        for (int i = 0; i < 5; i++) {
            byte value = memory.get((byte) (adress + i));
            for (int j = 0; j < 8; j++) {
                this.setPixel(x + (7 - j), y + i, (value & (0x1 << j)) != 0, true);
            }
        }
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