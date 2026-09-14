import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.Timer;

class GamePanel extends JPanel implements KeyListener {

    // ブロックの位置
    private int blockX = 100;
    private int blockY = 100;
    private final int BLOCK_SIZE = 30;
    private Timer timer;

    public GamePanel() {

        setFocusable(true);
        addKeyListener(this);

        // 500ミリ秒ごとに処理する
        timer = new Timer(500, e -> {
            blockY += BLOCK_SIZE;

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ブロックを描画
        // g.fillRect(blockX, blockY, 30, 30);

        g.fillRect(blockX, blockY, 30, 30);
        g.fillRect(blockX + 30, blockY, 30, 30);
        g.fillRect(blockX + 60, blockY, 30, 30);
        g.fillRect(blockX + 90, blockY, 30, 30);

        //Tetromino tetromino = new Tetromino();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            blockX -= 30;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            blockX += 30;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            blockY += 30;
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
