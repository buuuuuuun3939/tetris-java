import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    private static TetrominoSource fixedPieces(TetrominoType... types) {
        Deque<Tetromino> pieces = new ArrayDeque<>();
        for (TetrominoType type : types) {
            pieces.addLast(new Tetromino(type));
        }
        return pieces::removeFirst;
    }
}
