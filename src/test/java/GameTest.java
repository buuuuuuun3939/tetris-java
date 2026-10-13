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
        leftGame.boardForTransition().setBlock(0, 2, Color.GRAY);
        assertFalse(leftGame.moveLeft());
        assertEquals(3, leftGame.getCurrentCol());

        Game rightGame = new Game(fixedPieces(TetrominoType.O, TetrominoType.I));
        rightGame.boardForTransition().setBlock(0, 5, Color.GRAY);
        assertFalse(rightGame.moveRight());
        assertEquals(3, rightGame.getCurrentCol());

        Game downGame = new Game(fixedPieces(TetrominoType.O, TetrominoType.I));
        downGame.boardForTransition().setBlock(2, 3, Color.GRAY);
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
        game.boardForTransition().setBlock(2, 2, Color.GRAY);
        game.boardForTransition().setBlock(2, 3, Color.GRAY);
        game.boardForTransition().setBlock(2, 4, Color.GRAY);

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
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));
        game.setCurrentPositionForTransition(Board.ROWS - 1, 3);

        Game.StepResult result = game.tick();

        assertTrue(result.isPieceLocked());
        assertEquals(0, result.getClearedRows());
        for (int col = 3; col < 7; col++) {
            assertEquals(Color.CYAN,
                game.boardForTransition().getBlock(Board.ROWS - 1, col));
        }
    }

    @Test
    void hardDropMovesPieceToBottomAndLocksIt() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));

        Game.StepResult result = game.hardDrop();

        assertTrue(result.isPieceLocked());
        assertEquals(Board.ROWS - 1, game.getCurrentRow());
        for (int col = 3; col < 7; col++) {
            assertEquals(Color.CYAN,
                game.boardForTransition().getBlock(Board.ROWS - 1, col));
        }
    }

    @Test
    void clearingLinesUpdatesScoreAndLevel() {
        Game game = new Game(fixedPieces(TetrominoType.I, TetrominoType.O));
        game.setCurrentPositionForTransition(Board.ROWS - 1, 3);

        for (int line = 0; line < 10; line++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (col < 3 || col > 6) {
                    game.boardForTransition().setBlock(
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
            assertNull(game.boardForTransition().getBlock(Board.ROWS - 1, col));
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
}
