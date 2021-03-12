package chip8;

/**
 * Class representing chip-8's registry - all the registers, timers and pointers.
 */
public class Registry {

    /**
     * 16 8-bit registers from V0 to VX.
     */
    public byte[] VReg;

    /**
     * A 16-bit I register.
     */
    public short IReg;

    /**
     * A registry for delay timer.
     */
    public byte DT;

    /**
     * A registry for sound timer.
     */
    public byte ST;

    /**
     * Program counter registry.
     */
    public short PC;

    /**
     * Stack pointer registry.
     */
    public byte SP;

    public Registry() {
        VReg = new byte[16];
    }

}
