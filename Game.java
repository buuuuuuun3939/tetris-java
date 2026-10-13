import java.awt.Color;
import java.util.Objects;

/**
 * Holds the initial game state and exposes it to callers without exposing
 * mutable model objects. Game rules are added here in later migration steps.
 */
public class Game {
    private static final int SPAWN_ROW = 0;
    private static final int SPAWN_COL = 3;

    private final Board board;
    private final TetrominoSource tetrominoSource;

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
        this(createBagSource());
    }

    /** Creates a game with a replaceable piece source, useful for tests. */
    public Game(TetrominoSource tetrominoSource) {
        this.tetrominoSource = Objects.requireNonNull(tetrominoSource);
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

    /* Temporary accessors for moving GamePanel state before moving its rules. */
    Board boardForTransition() {
        return board;
    }

    Tetromino currentTetrominoForTransition() {
        return currentTetromino;
    }

    Tetromino nextTetrominoForTransition() {
        return nextTetromino;
    }

    Tetromino holdTetrominoForTransition() {
        return holdTetromino;
    }

    Tetromino nextPieceForTransition() {
        return nextPiece();
    }

    void setCurrentTetrominoForTransition(Tetromino tetromino) {
        currentTetromino = tetromino;
    }

    void setNextTetrominoForTransition(Tetromino tetromino) {
        nextTetromino = tetromino;
    }

    void setHoldTetrominoForTransition(Tetromino tetromino) {
        holdTetromino = tetromino;
    }

    void setCurrentPositionForTransition(int row, int col) {
        currentRow = row;
        currentCol = col;
    }

    void setScoreForTransition(int score) {
        this.score = score;
    }

    void setLevelForTransition(int level) {
        this.level = level;
    }

    void setHoldUsedForTransition(boolean holdUsed) {
        this.holdUsed = holdUsed;
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
