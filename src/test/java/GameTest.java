import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    @Test
    void startsWithExpectedInitialState() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));

        Color[][] board = game.getBoardSnapshot();
        for (Color[] row : board) {
            for (Color block : row) {
                assertNull(block);
            }
        }

        assertEquals(0, game.getScore());
        assertEquals(1, game.getLevel());
        assertEquals(0, game.getCurrentRow());
        assertEquals(3, game.getCurrentCol());
        assertEquals(GameState.PLAYING, game.getGameState());
        assertFalse(game.isHoldUsed());
        assertNull(game.getHoldShape());
        assertNull(game.getHoldColor());
    }

    @Test
    void usesProvidedPiecesForCurrentAndNextPiece() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));

        assertArrayEquals(new int[] {1, 1, 1, 1}, game.getCurrentShape()[0]);
        assertEquals(Color.CYAN, game.getCurrentColor());
        assertArrayEquals(new int[] {1, 1}, game.getNextShape()[0]);
        assertEquals(Color.YELLOW, game.getNextColor());
    }

    @Test
    void returnedShapesAndBoardAreSnapshots() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));

        int[][] currentShape = game.getCurrentShape();
        currentShape[0][0] = 9;
        assertArrayEquals(new int[] {1, 1, 1, 1}, game.getCurrentShape()[0]);

        Color[][] board = game.getBoardSnapshot();
        board[0][0] = Color.RED;
        assertNull(game.getBoardSnapshot()[0][0]);
    }

    @Test
    void movementUpdatesPositionOneCellAtATime() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));

        assertTrue(game.moveLeft());
        assertEquals(2, game.getCurrentCol());
        assertTrue(game.moveRight());
        assertEquals(3, game.getCurrentCol());
        assertTrue(game.moveDown());
        assertEquals(1, game.getCurrentRow());
    }

    @Test
    void movementStopsAtBoardEdges() {
        Game leftGame = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));
        while (leftGame.moveLeft()) {
            // Move to the left edge.
        }
        assertEquals(0, leftGame.getCurrentCol());
        assertFalse(leftGame.moveLeft());
        assertEquals(0, leftGame.getCurrentCol());

        Game rightGame = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));
        while (rightGame.moveRight()) {
            // Move to the right edge.
        }
        assertEquals(Board.COLS - 4, rightGame.getCurrentCol());
        assertFalse(rightGame.moveRight());
        assertEquals(Board.COLS - 4, rightGame.getCurrentCol());

        Game bottomGame = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));
        while (bottomGame.moveDown()) {
            // Move to the bottom edge.
        }
        assertEquals(Board.ROWS - 1, bottomGame.getCurrentRow());
        assertFalse(bottomGame.moveDown());
        assertEquals(Board.ROWS - 1, bottomGame.getCurrentRow());
    }

    @Test
    void movementStopsBeforeFixedBlocks() {
        Game leftGame = new Game(fixedPieces(TetrominoType.O, TetrominoType.I));
        leftGame.boardForTest().setBlock(0, 2, Color.GRAY);
        assertFalse(leftGame.moveLeft());
        assertEquals(3, leftGame.getCurrentCol());

        Game rightGame = new Game(fixedPieces(TetrominoType.O, TetrominoType.I));
        rightGame.boardForTest().setBlock(0, 5, Color.GRAY);
        assertFalse(rightGame.moveRight());
        assertEquals(3, rightGame.getCurrentCol());

        Game downGame = new Game(fixedPieces(TetrominoType.O, TetrominoType.I));
        downGame.boardForTest().setBlock(2, 3, Color.GRAY);
        assertFalse(downGame.moveDown());
        assertEquals(0, downGame.getCurrentRow());
    }

    @Test
    void rotationProducesClockwiseShape() {
        Game game = new Game(fixedPieces(TetrominoType.T, TetrominoType.I));

        assertTrue(game.rotate());

        int[][] expected = {
            {1, 0},
            {1, 1},
            {1, 0}
        };
        assertShapeEquals(expected, game.getCurrentShape());
        assertEquals(3, game.getCurrentCol());
    }

    @Test
    void rotationShiftsPieceLeftWhenNeededToStayInsideRightWall() {
        Game game = new Game(fixedPieces(TetrominoType.T, TetrominoType.I));

        assertTrue(game.rotate());
        for (int i = 0; i < 5; i++) {
            assertTrue(game.moveRight());
        }
        assertEquals(8, game.getCurrentCol());

        assertTrue(game.rotate());
        assertEquals(7, game.getCurrentCol());
    }

    @Test
    void rotationDoesNotChangePieceWhenEveryCandidateIsBlocked() {
        Game game = new Game(fixedPieces(TetrominoType.T, TetrominoType.I));
        int[][] initialShape = game.getCurrentShape();
        game.boardForTest().setBlock(2, 2, Color.GRAY);
        game.boardForTest().setBlock(2, 3, Color.GRAY);
        game.boardForTest().setBlock(2, 4, Color.GRAY);

        assertFalse(game.rotate());

        assertEquals(3, game.getCurrentCol());
        assertShapeEquals(initialShape, game.getCurrentShape());
    }

    @Test
    void tickMovesPieceDownWhenSpaceIsAvailable() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));

        Game.StepResult result = game.tick();

        assertFalse(result.isPieceLocked());
        assertEquals(0, result.getClearedRows());
        assertEquals(1, game.getCurrentRow());
        assertEquals(0, game.getScore());
    }

    @Test
    void tickLocksPieceWhenItCannotMoveDown() {
        Game game = new Game(fixedPieces(
            TetrominoType.I, TetrominoType.O, TetrominoType.T));
        game.setCurrentPositionForTest(Board.ROWS - 1, 3);

        Game.StepResult result = game.tick();

        assertTrue(result.isPieceLocked());
        assertEquals(0, result.getClearedRows());
        for (int col = 3; col < 7; col++) {
            assertEquals(Color.CYAN,
                game.boardForTest().getBlock(Board.ROWS - 1, col));
        }
        assertEquals(0, game.getCurrentRow());
    }

    @Test
    void hardDropMovesPieceToBottomAndLocksIt() {
        Game game = new Game(fixedPieces(
            TetrominoType.I, TetrominoType.O, TetrominoType.T));

        Game.StepResult result = game.hardDrop();

        assertTrue(result.isPieceLocked());
        for (int col = 3; col < 7; col++) {
            assertEquals(Color.CYAN,
                game.boardForTest().getBlock(Board.ROWS - 1, col));
        }
        assertEquals(0, game.getCurrentRow());
        assertEquals(Color.YELLOW, game.getCurrentColor());
    }

    @Test
    void clearingLinesUpdatesScoreAndLevel() {
        Game game = new Game(repeatingPieces(TetrominoType.I));

        for (int line = 0; line < 10; line++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (col < 3 || col > 6) {
                    game.boardForTest().setBlock(
                        Board.ROWS - 1, col, Color.GRAY);
                }
            }

            Game.StepResult result = game.hardDrop();
            assertTrue(result.isPieceLocked());
            assertEquals(1, result.getClearedRows());
        }

        assertEquals(1000, game.getScore());
        assertEquals(2, game.getLevel());
        for (int col = 0; col < Board.COLS; col++) {
            assertNull(game.boardForTest().getBlock(Board.ROWS - 1, col));
        }
    }

    @Test
    void holdStoresCurrentPieceAndPromotesNextPiece() {
        Game game = new Game(fixedPieces(
            TetrominoType.I, TetrominoType.O, TetrominoType.T));

        assertTrue(game.hold());

        assertEquals(Color.CYAN, game.getHoldColor());
        assertEquals(Color.YELLOW, game.getCurrentColor());
        assertEquals(Color.MAGENTA, game.getNextColor());
        assertEquals(0, game.getCurrentRow());
        assertEquals(3, game.getCurrentCol());
        assertTrue(game.isHoldUsed());
    }

    @Test
    void holdCanBeUsedAgainAfterLockAndSwapsPieces() {
        Game game = new Game(fixedPieces(
            TetrominoType.I, TetrominoType.O, TetrominoType.T,
            TetrominoType.S, TetrominoType.Z, TetrominoType.L));

        assertTrue(game.hold());
        assertFalse(game.hold());
        assertTrue(game.hardDrop().isPieceLocked());
        assertFalse(game.isHoldUsed());

        assertTrue(game.rotate());
        int[][] initialTShape = {
            {0, 1, 0},
            {1, 1, 1}
        };
        assertShapeEquals(new int[][] {{1, 0}, {1, 1}, {1, 0}},
            game.getCurrentShape());

        assertTrue(game.hold());

        assertEquals(Color.CYAN, game.getCurrentColor());
        assertEquals(Color.MAGENTA, game.getHoldColor());
        assertShapeEquals(initialTShape, game.getHoldShape());
        assertEquals(0, game.getCurrentRow());
        assertEquals(3, game.getCurrentCol());
    }

    @Test
    void blockedSpawnChangesGameStateToGameOver() {
        Game game = new Game(fixedPieces(
            TetrominoType.I, TetrominoType.O, TetrominoType.T));
        game.boardForTest().setBlock(1, 3, Color.GRAY);

        Game.StepResult result = game.tick();

        assertTrue(result.isPieceLocked());
        assertEquals(GameState.GAME_OVER, game.getGameState());
        assertEquals(Color.CYAN, game.boardForTest().getBlock(0, 3));
    }

    @Test
    void restartResetsGameStateAndCreatesFreshPieces() {
        Game game = new Game(fixedPieces(
            TetrominoType.I, TetrominoType.O, TetrominoType.T,
            TetrominoType.S, TetrominoType.Z, TetrominoType.L));
        for (int col = 0; col < Board.COLS; col++) {
            if (col < 3 || col > 6) {
                game.boardForTest().setBlock(Board.ROWS - 1, col, Color.GRAY);
            }
        }
        game.setCurrentPositionForTest(Board.ROWS - 1, 3);
        game.hardDrop();
        game.hold();
        game.boardForTest().setBlock(5, 5, Color.BLUE);
        game.setGameStateForTransition(GameState.PAUSED);

        game.restart();

        assertEquals(0, game.getScore());
        assertEquals(1, game.getLevel());
        assertEquals(GameState.PLAYING, game.getGameState());
        assertFalse(game.isHoldUsed());
        assertNull(game.getHoldShape());
        assertEquals(Color.RED, game.getCurrentColor());
        assertEquals(Color.ORANGE, game.getNextColor());
        assertEquals(0, game.getCurrentRow());
        assertEquals(3, game.getCurrentCol());
        for (Color[] row : game.getBoardSnapshot()) {
            for (Color block : row) {
                assertNull(block);
            }
        }
    }

    private static void assertShapeEquals(int[][] expected, int[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int row = 0; row < expected.length; row++) {
            assertArrayEquals(expected[row], actual[row]);
        }
    }

    private static TetrominoSource fixedPieces(TetrominoType... types) {
        Deque<Tetromino> pieces = new ArrayDeque<>();
        for (TetrominoType type : types) {
            pieces.addLast(new Tetromino(type));
        }
        return pieces::removeFirst;
    }

    private static TetrominoSource repeatingPieces(TetrominoType type) {
        return () -> new Tetromino(type);
    }
}
