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
        setKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("Key released:" + e.toString());
        setKey(e.getKeyCode(), false);
    }

    boolean setKey(int keyCode, boolean value) {

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

        return true;
    }

}

