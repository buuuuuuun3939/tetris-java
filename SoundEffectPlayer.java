import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundEffectPlayer {
    private Clip lockClip;
    private Clip clearClip;
    private Clip gameOverClip;
    private Clip rotateClip;
    private Clip moveClip;
    private Clip dropClip;

    public SoundEffectPlayer() {
        lockClip = loadClip("./bgm/lock.wav");
        clearClip = loadClip("./bgm/clear.wav");
        gameOverClip = loadClip("./bgm/gameover.wav");
        rotateClip = loadClip("./bgm/rotate.wav");
        moveClip = loadClip("./bgm/move.wav");
        dropClip = loadClip("./bgm/drop.wav");
    }

    private Clip loadClip(String path) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                new File(path))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        } catch (Exception e) {
            // 効果音が読み込めなくてもゲームは続行する
            return null;
        }
    }

    public void playLock() {
        play(lockClip);
    }

    public void playClear() {
        play(clearClip);
    }

    public void playGameOver() {
        play(gameOverClip);
    }

    public void playRotate() {
        play(rotateClip);
    }

    public void playMove() {
        play(moveClip);
    }

    public void playDrop() {
        play(dropClip);
    }

    private void play(Clip clip) {
        if (clip == null) {
            return;
        }

        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }
}
