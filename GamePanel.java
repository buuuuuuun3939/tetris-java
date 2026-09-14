import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.Timer;

class GamePanel extends JPanel implements KeyListener {

    // ブロックの位置
    private int blockX = 90;
    private int blockY = 120;
    private final int BLOCK_SIZE = 30;
    private final int PANEL_WIDTH = 400;
    private final int PANEL_HEIGHT = 600;
    private Board board;
    private Timer timer;

    public GamePanel() {
        board = new Board();
        setPreferredSize(new Dimension(
            Board.COLS * Board.BLOCK_SIZE,
            Board.ROWS * Board.BLOCK_SIZE
        ));

        setFocusable(true);
        addKeyListener(this);

        // 500ミリ秒ごとに処理する
        timer = new Timer(500, e -> {
            if (canMoveDown()) {
                blockY += BLOCK_SIZE;
            }
            repaint();

        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Boardを描画
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {

                int x = col * Board.BLOCK_SIZE;
                int y = row * Board.BLOCK_SIZE;

                g.drawRect(
                    x,
                    y,
                    Board.BLOCK_SIZE,
                    Board.BLOCK_SIZE
                );
            }
        }

        // I字ブロック
        g.fillRect(blockX, blockY, Board.BLOCK_SIZE, Board.BLOCK_SIZE);
        g.fillRect(blockX + Board.BLOCK_SIZE, blockY,
                   Board.BLOCK_SIZE, Board.BLOCK_SIZE);
        g.fillRect(blockX + Board.BLOCK_SIZE * 2, blockY,
                   Board.BLOCK_SIZE, Board.BLOCK_SIZE);
        g.fillRect(blockX + Board.BLOCK_SIZE * 3, blockY,
                   Board.BLOCK_SIZE, Board.BLOCK_SIZE);

        //Tetromino tetromino = new Tetromino();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (blockX - Board.BLOCK_SIZE >= 0) {
                blockX -= Board.BLOCK_SIZE;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (blockX + Board.BLOCK_SIZE * 4 < Board.getWidth()) {
                blockX += Board.BLOCK_SIZE;
            }
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private     boolean canMoveDown() {
        return blockY + Board.BLOCK_SIZE < board.getHeight();
    }
}
