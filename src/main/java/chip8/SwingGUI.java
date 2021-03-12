package chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

/**
 * Swing GUI implementing the Display interface to represent chip-8's screen.
 */
public class SwingGUI implements Display {
    /**
     * 32 x 64 screen.
     */
    private boolean screen[][];

    private BufferedImage screenImg;

    /**
     * Width of a single pixel on the screen.
     */
    private int pixelWidth;

    /**
     * Height of a single pixel on the screen.
     */
    private int pixelHeight;

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
     * Whether sprites should overlap on the screen, when they reach the border.
     * Some chip-8 programs are written to overlap sprites only, if the whole sprite is beyond the screen.
     * References aren't certain, whether this should be a standard. Most programs use overlapping mode by default.
     */
    private boolean overlappingMode;

    /**
     * Warning boolean that is set when a pixel was drawn outside of the screen bounds,
     * and screen overlapping is turned off. This may be a desired effect (for ex. BLITZ game), or a bug.
     */
    private boolean warningSet = false;

    /**
     * Constructor creating a new JFrame and DrawBoard.
     * @param pixelSize Size of a single pixel on the screen.
     * @param memory Reference to chip's memory.
     */
    public SwingGUI(int pixelSize, Memory memory, Keyboard keyboard, boolean overlappingMode) {
        this.pixelWidth = pixelSize;
        this.pixelHeight = pixelSize;

        screen = new boolean[64][32];

        this.overlappingMode = overlappingMode;

        this.memory = memory;
        this.keyboard = keyboard;
    }

    /**
     * Initializes the display by creating a Swing window with the screen.
     */
    public void createGUI() {
        frame = new JFrame("Chip-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addKeyListener(keyboard);

        frame.setResizable(true);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                pixelWidth = (int) e.getComponent().getSize().getWidth() / 64;
                pixelHeight = (int) e.getComponent().getSize().getHeight() / 32;
                screenImg = new BufferedImage(pixelWidth * 64, pixelHeight * 32, BufferedImage.TYPE_INT_RGB);
            }
        });

        drawBoard = new DrawBoard();
        drawBoard.setPreferredSize(new Dimension(pixelWidth * 64, pixelHeight * 32));
        frame.getContentPane().add(drawBoard);

        frame.setSize(new Dimension(pixelWidth * 64 + 16, pixelHeight * 32 + 64));
        frame.setVisible(true);

        screenImg = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);

        frame.pack();
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

        if (!overlappingMode) {
            if (x < 0 || x > 63 || y < 0 || y > 31) {
                if (!warningSet) {
                    System.out.println("Warning! Screen coordinate out out bounds, overlapping turned off.");
                    warningSet = true;
                }
                return false;
            }
        }

        while (x < 0) {
            x = (64 + x) % 64;

        }

        while (y < 0) {
            y = (32 + y) % 32;
        }

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
            screen[x % 64][y % 32] = value;
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

    /**
     * @return A 2D boolean array representing current screen.
     */
    public boolean[][] getScreen() {
        boolean[][] screenCopy = new boolean[64][32];
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screenCopy[x][y] = screen[x][y];
            }
        }
        return screenCopy;
    }

    /**
     * Render a single frame on the screen.
     */
    public void render() {
        frame.repaint();
    }

    /**
     * DrawBoard class overriding the painComponent. Draws the contents of the screen to the JPanel visible in JFrame.
     */
    private class DrawBoard extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(pixelWidth * 64, pixelHeight * 32);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int x = 0; x < screenImg.getWidth(); x++) {
                for (int y = 0; y < screenImg.getHeight(); y++) {
                    screenImg.setRGB(x, y, screen[x / pixelWidth][y / pixelHeight] ? 0xFFFFFF : 0x0);
                }
            }

            Graphics2D g2d = (Graphics2D) g.create();

            g2d.drawImage(screenImg, 0, 0, this);

            g2d.dispose();
        }
    }

}
