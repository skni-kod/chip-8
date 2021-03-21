package chip8;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Class representing chip-8's keyboard.
 */
public class Keyboard implements KeyListener {

    /**
     * An array of booleans representing chip-8's keyboard.
     * Chip-8's keyboard:
     * 1|2|3|C
     * 4|5|6|D
     * 7|8|9|E
     * A|0|B|F
     * Modern keyboard mapping:
     * 1|2|3|4
     * Q|W|E|R
     * A|S|D|F
     * Z|X|C|V
     * Keys are mapped from modern to chip-8's representation using getProperKeyCode(int keyCode) method.
     * It is crucial to note that each key's indexes in the array are their hexadecimal values,
     * for example: key "A" is held in pressedKeys[0xA], key "2" is held in pressedKeys[0x2].
     * Each key holds a value indicating, whether it is currently pressed or not.
     */
    private boolean[] pressedKeys = new boolean[16];

    /**
     * Number of the currently pressed keys.
     */
    private int currentlyPressedCount = 0;

    /**
     * KeyCode of the most recently used key.
     */
    private int lastUsed = 0;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        setKey(e.getKeyCode(), true, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false, true);
    }

    /**
     * Sets a single key's value - pressed or released. If useJavaKeyCodes is true,
     * uses Java KeyEvent's keycodes, otherwise uses chip 8's key values from 0 to F.
     * @param keyCode KeyCode representing a single key.
     * @param value True to set key as pressed, False to set key as released.
     * @param useJavaKeyCodes If true, method uses mapped Java KeyEvent's keycodes, otherwise uses chip 8's key values
     *                        from 0 to F.
     * @return True, if key value was set, otherwise false.
     */
    public boolean setKey(int keyCode, boolean value, boolean useJavaKeyCodes) {
        if (useJavaKeyCodes) {
            keyCode = getProperKeyCode(keyCode);
        }

        if (keyCode >= 0x0 && keyCode <= 0xF) {
            boolean prev = pressedKeys[keyCode];
            pressedKeys[keyCode] = value;

            if (prev != pressedKeys[keyCode]) {
                if (value) {
                    lastUsed = keyCode;
                    currentlyPressedCount++;
                } else {
                    lastUsed = keyCode;
                    currentlyPressedCount--;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Returns a value of a single key.
     * @param keyCode Keycode of the key (from 0x0 to 0xF).
     * @return True if key is pressed, False if key is released.
     */
    public boolean getKey(int keyCode) {
        return pressedKeys[keyCode];
    }

    /**
     * Given a mapped Java KeyEvent keycode, returns a proper keycode (from 0x0 to 0xF).
     * @param keyCode Java KeyEvent keycode.
     * @return Proper keycode from 0x0 to 0xF if key is properly mapped, otherwise -1.
     */
    private int getProperKeyCode(int keyCode) {
        switch (keyCode) {

            case KeyEvent.VK_1:
                return 0x1;

            case KeyEvent.VK_2:
                return 0x2;

            case KeyEvent.VK_3:
                return 0x3;

            case KeyEvent.VK_4:
                return 0xC;

            case KeyEvent.VK_Q:
                return 0x4;

            case KeyEvent.VK_W:
                return 0x5;

            case KeyEvent.VK_E:
                return 0x6;

            case KeyEvent.VK_R:
                return 0xD;

            case KeyEvent.VK_A:
                return 0x7;

            case KeyEvent.VK_S:
                return 0x8;

            case KeyEvent.VK_D:
                return 0x9;

            case KeyEvent.VK_F:
                return 0xE;

            case KeyEvent.VK_Z:
                return 0xA;

            case KeyEvent.VK_X:
                return 0x0;

            case KeyEvent.VK_C:
                return 0xB;

            case KeyEvent.VK_V:
                return 0xF;

            default:
                return -1;
        }
    }

    /**
     * Waits for a single pressed key.
     * @return KeyCode of the most recent key used.
     */
    public int waitForKey() {
        try {
            while (currentlyPressedCount == 0) {
                Thread.sleep(0);
            }
            return lastUsed;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

}

