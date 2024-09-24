import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {

    private Clip clip;

    public SoundPlayer(String soundFileName) {
        try {
            File soundFile = new File(soundFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Jouer le son une seule fois
    public void play() {
        if (clip != null) {
            clip.setFramePosition(0); // Revenir au début
            clip.start();
        }
    }

    // Jouer le son en boucle
    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Arrêter le son
    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}