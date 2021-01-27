package chip8;

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
    public byte delayTimerReg;

    /**
     * A registry for sound timer.
     */
    public byte soundTimerReg;

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
