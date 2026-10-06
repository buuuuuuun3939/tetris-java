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
    private int level = 1;
    private final int BLOCK_SIZE = 30;
    private final int PANEL_WIDTH = 400;
    private final int PANEL_HEIGHT = 600;
    private static final int SPAWN_ROW = 0;
    private static final int SPAWN_COL = 3;
    private boolean holdUsed = false;
    private GameState gameState = GameState.PLAYING;

    private static final int UI_X =
        Board.COLS * Board.BLOCK_SIZE + 30;

    private static final int SCORE_Y = 50;
    private static final int LEVEL_Y = 130;
    private static final int NEXT_Y = 220;
    private static final int HOLD_Y = 350;
    private static final int MINO_Y_OFFSET = 20;

    private Board board;
    private Timer timer;
    private Tetromino tetromino;
    private Tetromino nextTetromino;
    private Tetromino holdTetromino;
    private BgmPlayer bgmPlayer;

    public GamePanel() {
        board = new Board();
        bgmPlayer = new BgmPlayer();
        bgmPlayer.play();

        tetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();
        spawnTetromino();

        setPreferredSize(new Dimension(
            Board.COLS * Board.BLOCK_SIZE + 200,
            Board.ROWS * Board.BLOCK_SIZE
        ));

        setFocusable(true);
        addKeyListener(this);

        // 500ミリ秒ごとに処理する
        timer = new Timer(500, e -> {
            if (gameState != GameState.PLAYING) {
                return;
            }

            if (canMoveDown()) {
                blockY += BLOCK_SIZE;
            } else {
                landTetromino();

                //　ミノ初期位置
                blockX = 90;
                blockY = 0;
            }

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBoard(g);
        drawGhostTetromino(g);  // 現在のミノよりもゴーストのミノを先に描画する
        drawCurrentTetromino(g);
        drawNextTetromino(g);
        drawHoldTetromino(g);
        drawScore(g);
        drawGameOver(g);
        drawPaused(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_C) {
            hold();
            repaint();
            return;
        }

        // GAME_OVER中はRキーでリスタートできるようにする
        if (e.getKeyCode() == KeyEvent.VK_R
                && gameState == GameState.GAME_OVER) {
            restartGame();
            return;
        }

        // Pキーで一時停止・再開
        if (e.getKeyCode() == KeyEvent.VK_P
                && gameState != GameState.GAME_OVER) {
            togglePause();
            return;
        }

        // PLAYING中以外は通常操作を受け付けない
        if (gameState != GameState.PLAYING) {
            return;
        }


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
            tryRotate();
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            hardDrop();
            return;
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
        return canMoveDownAt(blockY);
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

    private void tryRotate() {
        int originalX = blockX;

        // そのまま回転できるか試す
        if (canRotate()) {
            tetromino.rotate();
            repaint();

            return;
        }

        // 左端に寄っている場合の処理
        // 1マス右にずらして回転できるか試す
        blockX = originalX + Board.BLOCK_SIZE;
        if (canRotate()) {
            tetromino.rotate();
            repaint();

            return;
        }

        // 右端に寄っている場合の処理
        // 1マス左にずらして回転できるか試す
        blockX = originalX - Board.BLOCK_SIZE;
        if (canRotate()) {
            tetromino.rotate();
            repaint();

            return;
        }


        blockX = originalX;
    }

    private int getDropInterval() {
       return Math.max(100, 500 - (level - 1) * 50);
    }

    private void startGame() {
        gameState = GameState.PLAYING;
        timer.start();
        bgmPlayer.play();
        repaint();
    }

    private void stopGame() {
        gameState = GameState.PAUSED;
        timer.stop();
        bgmPlayer.stop();
        repaint();
    }

    private void setGameOver() {
        gameState = GameState.GAME_OVER;
        timer.stop();
        bgmPlayer.stop();
    }

    private void togglePause() {
        if (gameState == GameState.PAUSED) {
            startGame();
        } else if (gameState == GameState.PLAYING) {
            stopGame();
        }
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

    private void addScore(int clearedRows) {
        score += clearedRows * 100;
    }

    private void drawScore(Graphics g) {

        g.setColor(Color.BLACK);
        g.drawString("SCORE", UI_X, SCORE_Y);
        g.drawString(String.valueOf(score), UI_X, SCORE_Y + 25);

        g.drawString("LEVEL", UI_X, LEVEL_Y);
        g.drawString(String.valueOf(level), UI_X, LEVEL_Y + 25);
    }

    private void updateLevel() {
        level = score / 1000 + 1;
        timer.setDelay(getDropInterval());  // レベルに応じて落下速度を変更
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

    private void landTetromino() {
        lockBlock();

        int clearedRows = board.clearFullRows();
        addScore(clearedRows);
        updateLevel();

        spawnTetromino();
        holdUsed = false;

        if (!canSpawn()) {
            setGameOver();
        }

    }

    private void hardDrop() {
        while (canMoveDown()) {
            blockY += Board.BLOCK_SIZE;
        }

        landTetromino();
        repaint();
    }

    private void hold() {
        // すでにHOLDしていたら何もしない
        if (holdUsed) {
            return;
        }

        // HOLDが空の場合
        if (holdTetromino == null) {
            holdTetromino = tetromino;
            spawnTetromino();
        } else {
            // HOLDと現在のミノを交換
            Tetromino temp = holdTetromino;
            holdTetromino = tetromino;
            holdTetromino.reset(); // ホールドしたミノの形状を元に戻す
            tetromino = temp;

            blockX = SPAWN_COL * Board.BLOCK_SIZE;
            blockY = SPAWN_ROW * Board.BLOCK_SIZE;
        }

        holdUsed = true;

        if (!canSpawn()) {
            setGameOver();
        }
    }

    // 盤面外でミノの形状を中央に配置するためのオフセットを計算する
    private int calcTetrominoCenteringOffset(int[][] shape) {
        int minCol = shape[0].length;
        int maxCol = -1;

        for (int[] row : shape) {
            for (int col = 0; col < row.length; col++) {
                if (row[col] != 0) {
                    minCol = Math.min(minCol, col);
                    maxCol = Math.max(maxCol, col);
                }
            }
        }

        int pieceWidth = (maxCol - minCol + 1) * Board.BLOCK_SIZE;
        int sidePanelWidth = getWidth() - Board.getWidth();
        int offsetX = (sidePanelWidth - pieceWidth) / 2 - minCol * Board.BLOCK_SIZE;

        // offsetXを使用してミノを中央に配置する処理を行う

        return offsetX + Board.getWidth(); // 右側領域の左端からのオフセットを返す
    }

    private void drawGhostTetromino(Graphics g) {
        int ghostY = getGhostY();
        int[][] shape = tetromino.getShape();
        int currentRow = ghostY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;

        g.setColor(Color.LIGHT_GRAY);

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int x = (currentCol + col) * Board.BLOCK_SIZE;
                int y = (currentRow + row) * Board.BLOCK_SIZE;

                g.drawRect(
                    x,
                    y,
                    Board.BLOCK_SIZE - 1,
                    Board.BLOCK_SIZE - 1
                );
            }
        }
    }

    private void drawNextTetromino(Graphics g) {
        // 次のミノの表示のためのラベルを描画
        g.setColor(Color.BLACK);
        g.drawString("Next", UI_X, NEXT_Y);

        g.setColor(nextTetromino.getColor());

        // 次のミノの表示開始位置を計算
        int[][] shape = nextTetromino.getShape();
        int startX = calcTetrominoCenteringOffset(shape);
        int startY = NEXT_Y + MINO_Y_OFFSET;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int x = startX + col * Board.BLOCK_SIZE;
                int y = startY + row * Board.BLOCK_SIZE;

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
        if (gameState == GameState.GAME_OVER) {
            String message = "GAME OVER";
            int textWidth = g.getFontMetrics().stringWidth(message);

            int x = (Board.COLS * Board.BLOCK_SIZE - textWidth) / 2;
            int y = Board.ROWS * Board.BLOCK_SIZE / 2;

            g.setColor(Color.BLACK);
            g.drawString(message, x, y);
        }
    }

    private void drawPaused(Graphics g) {
        if (gameState == GameState.PAUSED) {
            String message = "PAUSED";

            int textWidth = g.getFontMetrics().stringWidth(message);

            int x = (Board.COLS * Board.BLOCK_SIZE - textWidth) / 2;
            int y = Board.ROWS * Board.BLOCK_SIZE / 2;

            g.setColor(Color.BLACK);
            g.drawString(message, x, y);
        }
    }

    private void drawHoldTetromino(Graphics g) {
        if (holdTetromino == null) {
            return;
        }

        g.setColor(Color.BLACK);
        g.drawString("HOLD", UI_X, HOLD_Y);

        // HOLDのミノの表示開始位置を計算
        int[][] shape = holdTetromino.getShape();
        int startX = calcTetrominoCenteringOffset(shape);;
        int startY = HOLD_Y + MINO_Y_OFFSET;

        g.setColor(holdTetromino.getColor());

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                g.fillRect(
                    startX + col * Board.BLOCK_SIZE,
                    startY + row * Board.BLOCK_SIZE,
                    Board.BLOCK_SIZE,
                    Board.BLOCK_SIZE
                );
            }
        }
    }

    private void restartGame() {
        board.clear();

        score = 0;

        // ミノを作り直す
        tetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();

        // ミノの位置を初期位置に戻す
        blockX = SPAWN_COL * Board.BLOCK_SIZE;
        blockY = SPAWN_ROW * Board.BLOCK_SIZE;

        startGame();
    }

    private int getGhostY() {
        int ghostY = blockY;

        while (canMoveDownAt(ghostY)) {
            ghostY += Board.BLOCK_SIZE;
        }

        return ghostY;
    }

    private boolean canMoveDownAt(int testY) {

        int[][] shape = tetromino.getShape();

        int currentRow = testY / Board.BLOCK_SIZE;
        int currentCol = blockX / Board.BLOCK_SIZE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int nextRow = currentRow + row + 1;
                int boardCol = currentCol + col;

                if (nextRow >= Board.ROWS) {
                    return false;
                }

                if (board.getBlock(nextRow, boardCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }
}
