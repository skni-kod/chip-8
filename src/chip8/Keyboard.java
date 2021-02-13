package chip8;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

    private boolean[] pressedKeys = new boolean[16];

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed:" + e.toString());
        setKey(e.getKeyCode(), true, false);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("Key released:" + e.toString());
        setKey(e.getKeyCode(), false, false);
    }

    /**
     * Sets a single key as pressed or released, depending on the value.
     * Default mapping or hex mapping can be used.
     * @param keyCode Value representing the key. If useHex is false, use default java KeyEvent keycodes.
     *                If useHex is true, use hex values from 0x0 to 0xF (default chip-8 keyboard).
     * @param value True to set key as pressed, false to set key as released.
     * @param useHex True if keyCode is passed as a hex value, false if keyCode is passed as a KeyEvent keycode.
     * @return
     */
    public boolean setKey(int keyCode, boolean value, boolean useHex) {

        if (useHex) {
            switch (keyCode) {
                case 0x0:
                    pressedKeys[0xD] = value;

                case 0x1:
                    pressedKeys[0x0] = value;

                case 0x2:
                    pressedKeys[0x1] = value;

                case 0x3:
                    pressedKeys[0x2] = value;

                case 0x4:
                    pressedKeys[0x4] = value;

                case 0x5:
                    pressedKeys[0x5] = value;

                case 0x6:
                    pressedKeys[0x6] = value;

                case 0x7:
                    pressedKeys[0x8] = value;

                case 0x8:
                    pressedKeys[0x9] = value;

                case 0x9:
                    pressedKeys[0xA] = value;

                case 0xA:
                    pressedKeys[0xC] = value;

                case 0xB:
                    pressedKeys[0xE] = value;

                case 0xC:
                    pressedKeys[0x3] = value;

                case 0xD:
                    pressedKeys[0x7] = value;

                case 0xE:
                    pressedKeys[0xB] = value;

                case 0xF:
                    pressedKeys[0xF] = value;

                default:
                    return false;
            }
        } else {
            switch (keyCode) {

                case KeyEvent.VK_1:
                    pressedKeys[0] = value;
                    break;

                case KeyEvent.VK_2:
                    pressedKeys[1] = value;
                    break;

                case KeyEvent.VK_3:
                    pressedKeys[2] = value;
                    break;

                case KeyEvent.VK_4:
                    pressedKeys[3] = value;
                    break;

                case KeyEvent.VK_Q:
                    pressedKeys[4] = value;
                    break;

                case KeyEvent.VK_W:
                    pressedKeys[5] = value;
                    break;

                case KeyEvent.VK_E:
                    pressedKeys[6] = value;
                    break;

                case KeyEvent.VK_R:
                    pressedKeys[7] = value;
                    break;

                case KeyEvent.VK_A:
                    pressedKeys[8] = value;
                    break;

                case KeyEvent.VK_S:
                    pressedKeys[9] = value;
                    break;

                case KeyEvent.VK_D:
                    pressedKeys[10] = value;
                    break;

                case KeyEvent.VK_F:
                    pressedKeys[11] = value;
                    break;

                case KeyEvent.VK_Z:
                    pressedKeys[12] = value;
                    break;

                case KeyEvent.VK_X:
                    pressedKeys[13] = value;
                    break;

                case KeyEvent.VK_C:
                    pressedKeys[14] = value;
                    break;

                case KeyEvent.VK_V:
                    pressedKeys[15] = value;
                    break;

                default:
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns whether the key with a certain value is pressed or not.
     * @param value Value of the key.
     * @return True if pressed, otherwise false.
     */
    public boolean getKey(int value) {
        switch (value) {
            case 0x0:
                return pressedKeys[0xD];

            case 0x1:
                return pressedKeys[0x0];

            case 0x2:
                return pressedKeys[0x1];

            case 0x3:
                return pressedKeys[0x2];

            case 0x4:
                return pressedKeys[0x4];

            case 0x5:
                return pressedKeys[0x5];

            case 0x6:
                return pressedKeys[0x6];

            case 0x7:
                return pressedKeys[0x8];

            case 0x8:
                return pressedKeys[0x9];

            case 0x9:
                return pressedKeys[0xA];

            case 0xA:
                return pressedKeys[0xC];

            case 0xB:
                return pressedKeys[0xE];

            case 0xC:
                return pressedKeys[0x3];

            case 0xD:
                return pressedKeys[0x7];

            case 0xE:
                return pressedKeys[0xB];

            case 0xF:
                return pressedKeys[0xF];

            default:
                return false;
        }
    }

}

