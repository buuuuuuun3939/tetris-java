import java.awt.Color;
import java.util.Objects;
import java.util.function.Supplier;

/** Holds game state and screen-independent game rules. */
public class Game {
    private static final int SPAWN_ROW = 0;
    private static final int SPAWN_COL = 3;

    private final Board board;
    private final Supplier<TetrominoSource> tetrominoSourceFactory;
    private TetrominoSource tetrominoSource;

    private Tetromino currentTetromino;
    private Tetromino nextTetromino;
    private Tetromino holdTetromino;
    private int currentRow;
    private int currentCol;
    private int score;
    private int level;
    private boolean holdUsed;
    private GameState gameState;

    /** Creates a game using the existing randomized 7-bag. */
    public Game() {
        this(Game::createBagSource);
    }

    /** Creates a game with a replaceable piece source, useful for tests. */
    public Game(TetrominoSource tetrominoSource) {
        this(() -> Objects.requireNonNull(tetrominoSource));
    }

    private Game(Supplier<TetrominoSource> tetrominoSourceFactory) {
        this.tetrominoSourceFactory = Objects.requireNonNull(tetrominoSourceFactory);
        this.tetrominoSource = Objects.requireNonNull(tetrominoSourceFactory.get());
        this.board = new Board();
        this.score = 0;
        this.level = 1;
        this.holdUsed = false;
        this.gameState = GameState.PLAYING;
        this.currentRow = SPAWN_ROW;
        this.currentCol = SPAWN_COL;

        this.currentTetromino = nextPiece();
        this.nextTetromino = nextPiece();
    }

    private static TetrominoSource createBagSource() {
        TetrominoBag bag = new TetrominoBag();
        return () -> new Tetromino(bag.next());
    }

    private Tetromino nextPiece() {
        return Objects.requireNonNull(
            tetrominoSource.next(),
            "TetrominoSource.next() must not return null"
        );
    }

    public Color[][] getBoardSnapshot() {
        Color[][] snapshot = new Color[Board.ROWS][Board.COLS];
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                snapshot[row][col] = board.getBlock(row, col);
            }
        }
        return snapshot;
    }

    public int[][] getCurrentShape() {
        return copyShape(currentTetromino.getShape());
    }

    public Color getCurrentColor() {
        return currentTetromino.getColor();
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int getCurrentCol() {
        return currentCol;
    }

    public int[][] getNextShape() {
        return copyShape(nextTetromino.getShape());
    }

    public Color getNextColor() {
        return nextTetromino.getColor();
    }

    public int[][] getHoldShape() {
        return holdTetromino == null ? null : copyShape(holdTetromino.getShape());
    }

    public Color getHoldColor() {
        return holdTetromino == null ? null : holdTetromino.getColor();
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHoldUsed() {
        return holdUsed;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean moveLeft() {
        if (gameState != GameState.PLAYING) {
            return false;
        }
        if (!canPlaceCurrentAt(currentRow, currentCol - 1)) {
            return false;
        }
        currentCol--;
        return true;
    }

    public boolean moveRight() {
        if (gameState != GameState.PLAYING) {
            return false;
        }
        if (!canPlaceCurrentAt(currentRow, currentCol + 1)) {
            return false;
        }
        currentCol++;
        return true;
    }

    /** Moves down one board cell. Returns false when blocked. */
    public boolean moveDown() {
        if (gameState != GameState.PLAYING) {
            return false;
        }
        if (!canMoveDownFromRow(currentRow)) {
            return false;
        }
        currentRow++;
        return true;
    }

    /**
     * Rotates clockwise. If needed, tries shifting one cell right and then
     * one cell left, matching the existing wall adjustment behavior.
     */
    public boolean rotate() {
        if (gameState != GameState.PLAYING) {
            return false;
        }
        int[][] rotatedShape = currentTetromino.getRotatedShape();
        int[] candidateCols = {currentCol, currentCol + 1, currentCol - 1};

        for (int candidateCol : candidateCols) {
            if (canPlace(rotatedShape, currentRow, candidateCol)) {
                currentTetromino.rotate();
                currentCol = candidateCol;
                return true;
            }
        }

        return false;
    }

    /** Advances the game by one automatic gravity step. */
    public StepResult tick() {
        if (gameState != GameState.PLAYING) {
            return StepResult.moved();
        }
        if (moveDown()) {
            return StepResult.moved();
        }
        return lockCurrentPiece();
    }

    /** Drops the current piece as far as possible, then locks it. */
    public StepResult hardDrop() {
        if (gameState != GameState.PLAYING) {
            return StepResult.moved();
        }
        while (moveDown()) {
            // Continue until the next cell is blocked.
        }
        return lockCurrentPiece();
    }

    private StepResult lockCurrentPiece() {
        int[][] shape = currentTetromino.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    board.setBlock(
                        currentRow + row,
                        currentCol + col,
                        currentTetromino.getColor()
                    );
                }
            }
        }

        int clearedRows = board.clearFullRows();
        score += clearedRows * 100;
        level = score / 1000 + 1;
        holdUsed = false;
        spawnNextPiece();
        return StepResult.locked(clearedRows);
    }

    /** Stores the current piece or swaps it with the held piece. */
    public boolean hold() {
        if (gameState != GameState.PLAYING || holdUsed) {
            return false;
        }

        if (holdTetromino == null) {
            holdTetromino = currentTetromino;
            spawnNextPiece();
        } else {
            Tetromino heldPiece = holdTetromino;
            holdTetromino = currentTetromino;
            holdTetromino.reset();
            currentTetromino = heldPiece;
            currentRow = SPAWN_ROW;
            currentCol = SPAWN_COL;
            setGameOverIfCurrentPieceCannotSpawn();
        }

        holdUsed = true;
        return true;
    }

    /** Restarts with an empty board and a new pair of pieces. */
    public void restart() {
        board.clear();
        score = 0;
        level = 1;
        holdTetromino = null;
        holdUsed = false;
        gameState = GameState.PLAYING;
        tetrominoSource = Objects.requireNonNull(tetrominoSourceFactory.get());
        currentRow = SPAWN_ROW;
        currentCol = SPAWN_COL;
        currentTetromino = nextPiece();
        nextTetromino = nextPiece();
    }

    private void spawnNextPiece() {
        currentTetromino = nextTetromino;
        nextTetromino = nextPiece();
        currentRow = SPAWN_ROW;
        currentCol = SPAWN_COL;
        setGameOverIfCurrentPieceCannotSpawn();
    }

    private void setGameOverIfCurrentPieceCannotSpawn() {
        if (!canPlaceCurrentAt(currentRow, currentCol)) {
            gameState = GameState.GAME_OVER;
        }
    }

    /** Result of one gravity step or a hard drop. */
    public static final class StepResult {
        private final boolean pieceLocked;
        private final int clearedRows;

        private StepResult(boolean pieceLocked, int clearedRows) {
            this.pieceLocked = pieceLocked;
            this.clearedRows = clearedRows;
        }

        private static StepResult moved() {
            return new StepResult(false, 0);
        }

        private static StepResult locked(int clearedRows) {
            return new StepResult(true, clearedRows);
        }

        public boolean isPieceLocked() {
            return pieceLocked;
        }

        public int getClearedRows() {
            return clearedRows;
        }
    }

    /** Returns whether the current piece can move down from the given row. */
    boolean canMoveDownFromRow(int row) {
        return canPlaceCurrentAt(row + 1, currentCol);
    }

    private boolean canPlaceCurrentAt(int row, int col) {
        return canPlace(currentTetromino.getShape(), row, col);
    }

    private boolean canPlace(int[][] shape, int row, int col) {
        for (int shapeRow = 0; shapeRow < shape.length; shapeRow++) {
            for (int shapeCol = 0; shapeCol < shape[shapeRow].length; shapeCol++) {
                if (shape[shapeRow][shapeCol] == 0) {
                    continue;
                }

                int boardRow = row + shapeRow;
                int boardCol = col + shapeCol;
                if (boardRow < 0 || boardRow >= Board.ROWS
                        || boardCol < 0 || boardCol >= Board.COLS) {
                    return false;
                }
                if (board.getBlock(boardRow, boardCol) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /* Package-private setup hooks used by deterministic game tests. */
    Board boardForTest() {
        return board;
    }

    void setCurrentPositionForTest(int row, int col) {
        currentRow = row;
        currentCol = col;
    }

    void setGameStateForTransition(GameState gameState) {
        this.gameState = gameState;
    }

    private static int[][] copyShape(int[][] shape) {
        int[][] copy = new int[shape.length][];
        for (int row = 0; row < shape.length; row++) {
            copy[row] = shape[row].clone();
        }
        return copy;
    }
}
