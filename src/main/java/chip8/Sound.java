package chip8;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Class representing chip-8's sound system. According to the references, it plays only one, custom tone.
 * Inspired by michaelarnauts's chip-8's sound system.
 * https://github.com/michaelarnauts/chip8-java/blob/master/Source/src/be/khleuven/arnautsmichael/chip8/Sound.java
 */
public class Sound {

    private AudioFormat af;
    private SourceDataLine sdl;

    /**
     * Whether the sound is playing right now.
     */
    private boolean isPlaying;

    /**
     * Reference to the PlayThread object.
     */
    private PlayThread playThread;

    /**
     * Sound playing thread.
     */
    private Thread thread;

    /**
     * Buffer that holds the information about the tone.
     */
    private byte[] buffer = new byte[256];

    /**
     * Default constructor. Creates the interface for sound playing and fills the tone buffer.
     * @throws LineUnavailableException If AudioSystem's source data line is not available (due to restrictions?).
     */
    public Sound() throws LineUnavailableException {

        af = new AudioFormat(44100f, 8, 1, true, false);
        sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        isPlaying = false;
        playThread = new PlayThread();

        int ampl = 160;
        byte curr = 80;

        for (int i = 0; i < 255; i++) {
            buffer[i] = curr;
            curr += 1;
            if (curr < ampl) {
                curr = 80;
            }
        }
    }

    /**
     * Begin sound playing.
     */
    public void startSound() {
        if (!isPlaying) {
            isPlaying = true;
            thread = new Thread(playThread);
            thread.start();
        }
    }

    /**
     * Stop sound playing.
     */
    public void stopSound() {
        isPlaying = false;
    }

    /**
     * Class implementing the runnable interface. Used as a thread for sound playing.
     */
    class PlayThread implements Runnable {

        @Override
        public void run() {
            sdl.start();

            while (isPlaying) {
                sdl.write(buffer, 0, buffer.length);
            }

            sdl.stop();
            sdl.flush();
        }
    }
}
