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
            } else {
                lockBlock();

                blockX = 90;
                blockY = 120;
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

                // マス目
                g.drawRect(
                    x,
                    y,
                    Board.BLOCK_SIZE - 1,
                    Board.BLOCK_SIZE - 1
                );

                // 固定されたブロック
                if (board.getBlock(row, col) == 1) {
                    g.fillRect(
                    x,
                    y,
                    Board.BLOCK_SIZE,
                    Board.BLOCK_SIZE
                );}
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
            if (canMoveLeft()) {
                blockX -= Board.BLOCK_SIZE;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (canMoveRight()) {
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

    private boolean canMoveDown() {
        int nextRow = (blockY + Board.BLOCK_SIZE) / Board.BLOCK_SIZE;
        int col = blockX / Board.BLOCK_SIZE;

        // Boardの一番下
        if (nextRow >= Board.ROWS) {
            return false;
        }

        // I字ミノの4マスを確認
        for (int i = 0; i < 4; i++) {
            if (board.getBlock(nextRow, col + i) == 1) {
                return false;
            }
        }

        return true;
    }

    private boolean canMoveLeft() {
        int row = blockY / Board.BLOCK_SIZE;
        int col = blockX / Board.BLOCK_SIZE;

        // Boardの左端
        if (col <= 0) {
            return false;
        }

        // I字ミノの左端を確認
        if (board.getBlock(row, col - 1) == 1) {
            return false;
        }

        return true;
    }


    private boolean canMoveRight() {
        int row = blockY / Board.BLOCK_SIZE;
        int col = blockX / Board.BLOCK_SIZE;

        // I字ミノの右端
        int rightCol = col + 3;

        // Boardの右端
        if (rightCol >= Board.COLS - 1) {
            return false;
        }

        // 右隣に固定ブロックがある
        if (board.getBlock(row, rightCol + 1) == 1) {
            return false;
        }

        return true;
    }

    private void lockBlock() {
        int row = blockY / Board.BLOCK_SIZE;
        int col = blockX / Board.BLOCK_SIZE;

        board.setBlock(row, col);
        board.setBlock(row, col + 1);
        board.setBlock(row, col + 2);
        board.setBlock(row, col + 3);
    }
}
