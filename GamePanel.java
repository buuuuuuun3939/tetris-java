import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.Timer;

class GamePanel extends JPanel implements KeyListener {

    // ブロックの位置
    private int blockX = 90;
    private int blockY = 0;
    private int score = 0;
    private final int BLOCK_SIZE = 30;
    private final int PANEL_WIDTH = 400;
    private final int PANEL_HEIGHT = 600;
    private static final int SPAWN_ROW = 0;
    private static final int SPAWN_COL = 3;
    private boolean gameOver = false;
    private boolean paused = false;

    private Board board;
    private Timer timer;
    private Tetromino tetromino;
    private Tetromino nextTetromino;
    private BgmPlayer bgmPlayer;

    public GamePanel() {
        board = new Board();
        bgmPlayer = new BgmPlayer();
        bgmPlayer.play();

        tetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();
        spawnTetromino();

        setPreferredSize(new Dimension(
            Board.COLS * Board.BLOCK_SIZE + 150,
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
                score += board.clearFullRows() * 100;

                blockX = 90;
                blockY = 0;

                spawnTetromino();

                if (!canSpawn()) {
                    gameOver = true;
                    bgmPlayer.stop();
                    timer.stop();
                }
            }

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBoard(g);
        drawCurrentTetromino(g);
        drawNextTetromino(g);
        drawScore(g);
        drawGameOver(g);
        drawPaused(g);
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

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (canMoveDown()) {
                blockY += Board.BLOCK_SIZE;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (canRotate()) {
                tetromino.rotate();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            restartGame();
        }

        if (e.getKeyCode() == KeyEvent.VK_P && !gameOver) {
            if (paused) {
                paused = false;
                timer.start();
                bgmPlayer.play();
            } else {
                paused = true;
                timer.stop();
                bgmPlayer.stop();
            }

            repaint();
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
        int[][] shape = tetromino.getShape();
        int currentRow = blockY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int nextRow = currentRow + row + 1;
                int boardCol = currentCol + col;

                // 下端に到達した
                if (nextRow >= Board.ROWS) {
                    return false;
                }

                // 下のマスに固定ブロックがある
                if (board.getBlock(nextRow, boardCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean canMoveLeft() {
        int[][] shape = tetromino.getShape();
        int currentRow = blockY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int boardRow = currentRow + row;
                int nextCol = currentCol + col - 1;

                // Boardの左端に到達した
                if (nextCol < 0) {
                    return false;
                }

                // 左のマスに固定ブロックがある
                if (board.getBlock(boardRow, nextCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }


    private boolean canMoveRight() {
        int[][] shape = tetromino.getShape();
        int currentRow = blockY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int boardRow = currentRow + row;
                int nextCol = currentCol + col + 1;

                // Boardの右端に到達した
                if (nextCol >= Board.COLS) {
                    return false;
                }

                // 右のマスに固定ブロックがある
                if (board.getBlock(boardRow, nextCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean canRotate() {
        int[][] rotatedShape = tetromino.getRotatedShape();
        int currentRow = blockY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;

        for (int row = 0; row < rotatedShape.length; row++) {
            for (int col = 0; col < rotatedShape[row].length; col++) {

                if (rotatedShape[row][col] == 0) {
                    continue;
                }

                int boardRow = currentRow + row;
                int boardCol = currentCol + col;

                // Boardの外に出る
                if (boardRow < 0 || boardRow >= Board.ROWS
                    || boardCol < 0 || boardCol >= Board.COLS) {
                    return false;
                }

                // 回転先に固定ブロックがある
                if (board.getBlock(boardRow, boardCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {

                int x = col * Board.BLOCK_SIZE;
                int y = row * Board.BLOCK_SIZE;
                Color color = board.getBlock(row, col);

                if (color != null) {
                    // 固定されたブロック
                    g.setColor(color);
                    g.fillRect(
                        x,
                        y,
                        Board.BLOCK_SIZE,
                        Board.BLOCK_SIZE
                    );
                } else {
                    // 空いているマス
                    g.setColor(Color.WHITE);
                    g.fillRect(
                        x,
                        y,
                        Board.BLOCK_SIZE,
                        Board.BLOCK_SIZE
                    );

                    g.setColor(Color.BLACK);
                    g.drawRect(
                        x,
                        y,
                        Board.BLOCK_SIZE - 1,
                        Board.BLOCK_SIZE - 1
                    );
                }
            }
        }
    }

    private void drawScore(Graphics g) {
        int x = Board.COLS * Board.BLOCK_SIZE + 20;
        int y = 30;

        g.setColor(Color.BLACK);
        g.drawString("SCORE", x, y);
        g.drawString(String.valueOf(score), x, y + 25);
    }

    private void lockBlock() {
        int currentRow = blockY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;
        int[][] shape = tetromino.getShape();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    board.setBlock(currentRow + r, currentCol + c ,tetromino.getColor());
                }
            }
        }
    }

    private boolean canSpawn() {
        int[][] shape = tetromino.getShape();
        int startRow = 0;
        int startCol = blockX / Board.BLOCK_SIZE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                // 形状がない部分は無視
                if (shape[row][col] == 0) {
                    continue;
                }

                int boardRow = startRow + row;
                int boardCol = startCol + col;

                if (boardRow >= Board.ROWS) {
                    return false;
                }

                if (boardCol < 0 || boardCol >= Board.COLS) {
                    return false;
                }

                if (board.getBlock(boardRow, boardCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    private void spawnTetromino() {
        blockX = SPAWN_COL * Board.BLOCK_SIZE;
        blockY = SPAWN_ROW * Board.BLOCK_SIZE;

        tetromino = nextTetromino;
        nextTetromino = Tetromino.createRandom();
    }

    private void drawNextTetromino(Graphics g) {
        int nextX = Board.COLS * Board.BLOCK_SIZE + 30;
        int nextY = 150;
        int[][] shape = nextTetromino.getShape();

        // 次のミノの表示のためのラベルを描画
        g.setColor(Color.BLACK);
        g.drawString("Next", Board.COLS * Board.BLOCK_SIZE + 20, 100);

        g.setColor(nextTetromino.getColor());

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int x = nextX + col * Board.BLOCK_SIZE;
                int y = nextY + row * Board.BLOCK_SIZE;

                g.fillRect(
                    x,
                    y,
                    Board.BLOCK_SIZE,
                    Board.BLOCK_SIZE
                );
            }
        }
    }

    public void drawCurrentTetromino(Graphics g) {
        int[][] shape = tetromino.getShape();
        g.setColor(tetromino.getColor());

        // 現在のブロックを描画
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int x = blockX + col * Board.BLOCK_SIZE;
                int y = blockY + row * Board.BLOCK_SIZE;

                g.fillRect(
                    x,
                    y,
                    Board.BLOCK_SIZE,
                    Board.BLOCK_SIZE
                );
            }
        }
    }

    private void drawGameOver(Graphics g) {
        if (gameOver) {
            String message = "GAME OVER";
            int textWidth = g.getFontMetrics().stringWidth(message);

            int x = (Board.COLS * Board.BLOCK_SIZE - textWidth) / 2;
            int y = Board.ROWS * Board.BLOCK_SIZE / 2;

            g.setColor(Color.BLACK);
            g.drawString(message, x, y);
        }
    }

    private void drawPaused(Graphics g) {
        if (paused) {
            String message = "PAUSED";

            int textWidth = g.getFontMetrics().stringWidth(message);

            int x = (Board.COLS * Board.BLOCK_SIZE - textWidth) / 2;
            int y = Board.ROWS * Board.BLOCK_SIZE / 2;

            g.setColor(Color.BLACK);
            g.drawString(message, x, y);
        }
    }

    private void restartGame() {

        // 盤面を空にする
        board.clear();
        score = 0;
        gameOver = false;

        // ミノを作り直す
        tetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();

        // ミノの位置を初期位置に戻す
        blockX = SPAWN_COL * Board.BLOCK_SIZE;
        blockY = SPAWN_ROW * Board.BLOCK_SIZE;

        bgmPlayer.play();
        timer.start();

        // 画面を更新
        repaint();
    }
}
