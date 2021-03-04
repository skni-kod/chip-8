package chip8;

/**
 * Interface of the chip-8's screen.
 */
public interface Display {
    /**
     * Initializes the display by creating a GUI window with the screen.
     */
    void createGUI();

    /**
     * Sets a single pixel on the screen.
     * @param x The X position on the screen.
     * @param y The Y position on the screen.
     * @param value True to turn pixel on, otherwise false.
     * @param sprite True if drawing a sprite.
     * @return True if collision occurs, otherwise false.
     */
    boolean setPixel(int x, int y, boolean value, boolean sprite);

    /**
     * Gets a single pixel from the screen.
     * @param x The x position of the pixel on the screen.
     * @param y The y position of the pixel on the screen.
     * @return True if set, otherwise false.
     */
    boolean getPixel(int x, int y);

    /**
     * Gets a byte representing 8 pixels from the screen, beginning from the x and y position.
     * If x or y is bigger than the screen size, it overlaps to the other side of the screen (returns to the beginning).
     * @param x The x position of the most significant bit.
     * @param y The y position of the most significant bit.
     * @return Byte representing 8 pixels from the screen.
     */
    byte getByte(int x, int y);

    /**
     * Draws a sprite on the screen at x and y position, from the memory adress.
     * @param x The x position on the screen.
     * @param y The y position on the screen.
     * @param address Adress of the sprite's beginning in the memory.
     * @param numberOfBytes Number of bytes from the memory to draw, beginning from the address.
     * @return True if collision occurred, otherwise false.
     */
    boolean drawSprite(int x, int y, int address, int numberOfBytes);

    /**
     * @return A 2D boolean array representing current screen.
     */
    boolean[][] getScreen();

    /**
     * Render a single frame on the screen.
     */
    void render();

}
