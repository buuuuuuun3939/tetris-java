import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BgmPlayer {
    private Clip clip;

    public void play() {
        try {
            File file = new File("./bgm/korobeiniki_piano.wav");

            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();

            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
