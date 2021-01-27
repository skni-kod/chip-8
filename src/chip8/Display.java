package chip8;

import javax.swing.*;
import java.awt.*;

public class Display {
    /**
     * 32 x 64 screen.
     */
    private boolean screen[][];

    private int pixelSize = 32;

    private JFrame frame;
    private DrawBoard drawBoard;

    public Display(int pixelSize) {
        this.pixelSize = pixelSize;

        screen = new boolean[64][32];

        frame = new JFrame("Chip-8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawBoard = new DrawBoard();
        drawBoard.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(drawBoard);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    void setPixel(int x, int y, boolean value) {
        if (x < 0 || x > 32 || y < 0 || y > 32) {
            System.out.println("Screen coordinate out of range.");
        } else {
            screen[x][y] = value;
        }
    }

    void render() {
        frame.repaint();
    }

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
