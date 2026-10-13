import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.Timer;

class GamePanel extends JPanel implements KeyListener {

    private Game game;

    private static final int UI_X =
        Board.COLS * Board.BLOCK_SIZE + 30;

    private static final int SCORE_Y = 50;
    private static final int LEVEL_Y = 130;
    private static final int NEXT_Y = 220;
    private static final int HOLD_Y = 350;
    private static final int MINO_Y_OFFSET = 20;

    private Timer timer;
    private BgmPlayer bgmPlayer;
    private SoundEffectPlayer soundEffectPlayer;

    public GamePanel() {
        game = new Game();
        bgmPlayer = new BgmPlayer();
        soundEffectPlayer = new SoundEffectPlayer();
        bgmPlayer.play();

        setPreferredSize(new Dimension(
            Board.COLS * Board.BLOCK_SIZE + 200,
            Board.ROWS * Board.BLOCK_SIZE
        ));

        setFocusable(true);
        addKeyListener(this);

        // 500ミリ秒ごとに処理する
        timer = new Timer(500, e -> {
            if (game.getGameState() != GameState.PLAYING) {
                return;
            }

            Game.StepResult result = game.tick();
            if (result.isPieceLocked()) {
                handlePieceLocked(result, false);
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
            if (game.hold() && game.getGameState() == GameState.GAME_OVER) {
                handleGameOver();
            }
            repaint();
            return;
        }

        // GAME_OVER中はRキーでリスタートできるようにする
        if (e.getKeyCode() == KeyEvent.VK_R
                && game.getGameState() == GameState.GAME_OVER) {
            restartGame();
            return;
        }

        // Pキーで一時停止・再開
        if (e.getKeyCode() == KeyEvent.VK_P
                && game.getGameState() != GameState.GAME_OVER) {
            togglePause();
            return;
        }

        // PLAYING中以外は通常操作を受け付けない
        if (game.getGameState() != GameState.PLAYING) {
            return;
        }


        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (game.moveLeft()) {
                soundEffectPlayer.playMove();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (game.moveRight()) {
                soundEffectPlayer.playMove();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            game.moveDown();
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (game.rotate()) {
                soundEffectPlayer.playRotate();
            }
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

    private int getDropInterval() {
       return Math.max(100, 500 - (game.getLevel() - 1) * 50);
    }

    private void startGame() {
        game.resume();
        timer.start();
        bgmPlayer.play();
        repaint();
    }

    private void stopGame() {
        game.pause();
        timer.stop();
        bgmPlayer.stop();
        repaint();
    }

    private void handleGameOver() {
        timer.stop();
        bgmPlayer.stop();
        soundEffectPlayer.playGameOver();
    }

    private void togglePause() {
        if (game.getGameState() == GameState.PAUSED) {
            startGame();
        } else if (game.getGameState() == GameState.PLAYING) {
            stopGame();
        }
    }

    private void drawBoard(Graphics g) {
        Color[][] board = game.getBoardSnapshot();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {

                int x = col * Board.BLOCK_SIZE;
                int y = row * Board.BLOCK_SIZE;
                Color color = board[row][col];

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

        g.setColor(Color.BLACK);
        g.drawString("SCORE", UI_X, SCORE_Y);
        g.drawString(String.valueOf(game.getScore()), UI_X, SCORE_Y + 25);

        g.drawString("LEVEL", UI_X, LEVEL_Y);
        g.drawString(String.valueOf(game.getLevel()), UI_X, LEVEL_Y + 25);
    }

    private void handlePieceLocked(Game.StepResult result, boolean hardDropped) {
        if (hardDropped) {
            soundEffectPlayer.playDrop();
        } else {
            soundEffectPlayer.playLock();
        }

        if (result.getClearedRows() > 0) {
            soundEffectPlayer.playClear();
        }
        timer.setDelay(getDropInterval());

        if (game.getGameState() == GameState.GAME_OVER) {
            handleGameOver();
        }

    }

    private void hardDrop() {
        Game.StepResult result = game.hardDrop();
        handlePieceLocked(result, true);
        repaint();
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
        int[][] shape = game.getCurrentShape();
        int ghostRow = game.getGhostRow();
        int currentCol = game.getCurrentCol();

        g.setColor(Color.LIGHT_GRAY);

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int x = (currentCol + col) * Board.BLOCK_SIZE;
                int y = (ghostRow + row) * Board.BLOCK_SIZE;

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

        g.setColor(game.getNextColor());

        // 次のミノの表示開始位置を計算
        int[][] shape = game.getNextShape();
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
        int[][] shape = game.getCurrentShape();
        g.setColor(game.getCurrentColor());

        // 現在のブロックを描画
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                int x = game.getCurrentCol() * Board.BLOCK_SIZE + col * Board.BLOCK_SIZE;
                int y = game.getCurrentRow() * Board.BLOCK_SIZE + row * Board.BLOCK_SIZE;

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
        if (game.getGameState() == GameState.GAME_OVER) {
            String message = "GAME OVER";
            String scoreMessage = "SCORE: " + game.getScore();
            String restartMessage = "Press R to Restart";

            int boardWidth = Board.COLS * Board.BLOCK_SIZE;
            int boardHeight = Board.ROWS * Board.BLOCK_SIZE;

            // 盤面を半透明の黒で覆う
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, boardWidth, boardHeight);

            // 文字色
            g.setColor(Color.WHITE);

            g.setFont(new java.awt.Font(
                "SansSerif",
                java.awt.Font.BOLD,
                28
            ));
            int messageWidth = g.getFontMetrics().stringWidth(message);
            g.drawString(
                message,
                (boardWidth - messageWidth) / 2,
                boardHeight / 2 - 30
            );

            g.setFont(new java.awt.Font(
                "SansSerif",
                java.awt.Font.PLAIN,
                18
            ));
            int scoreWidth = g.getFontMetrics().stringWidth(scoreMessage);
            g.drawString(
                scoreMessage,
                (boardWidth - scoreWidth) / 2,
                boardHeight / 2
            );

            g.setFont(new java.awt.Font(
                "SansSerif",
                java.awt.Font.PLAIN,
                14
            ));
            int restartWidth = g.getFontMetrics().stringWidth(restartMessage);
            g.drawString(
                restartMessage,
                (boardWidth - restartWidth) / 2,
                boardHeight / 2 + 30
            );
        }
    }

    private void drawPaused(Graphics g) {
        if (game.getGameState() == GameState.PAUSED) {
            int boardWidth = Board.COLS * Board.BLOCK_SIZE;
            int boardHeight = Board.ROWS * Board.BLOCK_SIZE;

            // 半透明の黒い背景
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, boardWidth, boardHeight);

            String message = "PAUSED";
            String resumeMessage = "Press P to Resume";

            int centerX = boardWidth / 2;
            int centerY = boardHeight / 2;

            // PAUSEDを大きく太字で表示
            g.setColor(Color.WHITE);
            g.setFont(new java.awt.Font(
                "SansSerif",
                java.awt.Font.BOLD,
                28
            ));

            int messageWidth = g.getFontMetrics().stringWidth(message);
            g.drawString(
                message,
                centerX - messageWidth / 2,
                centerY - 10
            );

            // 再開方法を表示
            g.setFont(new java.awt.Font(
                "SansSerif",
                java.awt.Font.PLAIN,
                14
            ));

            int resumeWidth = g.getFontMetrics().stringWidth(resumeMessage);
            g.drawString(
                resumeMessage,
                centerX - resumeWidth / 2,
                centerY + 25
            );
        }
    }

    private void drawHoldTetromino(Graphics g) {
        if (game.getHoldShape() == null) {
            return;
        }

        g.setColor(Color.BLACK);
        g.drawString("HOLD", UI_X, HOLD_Y);

        // HOLDのミノの表示開始位置を計算
        int[][] shape = game.getHoldShape();
        int startX = calcTetrominoCenteringOffset(shape);
        int startY = HOLD_Y + MINO_Y_OFFSET;

        g.setColor(game.getHoldColor());

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
        game.restart();
        timer.setDelay(getDropInterval()); // タイマーの落下間隔を500msにリセット
        startGame();
    }

}
