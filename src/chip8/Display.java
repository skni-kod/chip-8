package chip8;

import javax.swing.*;
import java.awt.*;

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
    public Display(int pixelSize, Memory memory) {
        this.pixelSize = pixelSize;

        screen = new boolean[64][32];

        this.memory = memory;

        frame = new JFrame("Chip-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawBoard = new DrawBoard();
        drawBoard.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(drawBoard);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    //TODO return collisions
    /**
     * Sets a single pixel on the screen.
     * @param x The X position on the screen.
     * @param y The Y position on the screen.
     * @param value True to turn on, False to turn off.
     */
    void setPixel(int x, int y, boolean value) {
        if (x < 0 || x > 63) {
            System.out.println("Screen X coordinate out of range.");
        } else if (y < 0 || y > 31) {
            System.out.println("Screen Y coordinate out of range.");
        } else {
            screen[x][y] = value;
        }
    }

    void drawSprite(int x, int y, int adress) {
        for (int i = 0; i < 5; i++) {
            byte value = memory.get((byte) (adress + i));
            for (int j = 0; j < 8; j++) {
                this.setPixel(x + (7 - j), y + i, (value & (0x1 << j)) != 0);
            }
        }
    }

    void render() {
        frame.repaint();
    }

    /**
     * DrawBoard class overriding the painComponent. Draws the contents of the screen to the JPanel visible in JFrame.
     */
    private class DrawBoard extends JPanel {
        @Override
        public Dimension getPreferredSize() {
            //return super.getPreferredSize();
            return new Dimension(800, 600);

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
