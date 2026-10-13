import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void boardDimensionsMatchRowsColumnsAndBlockSize() {
        Board board = new Board();

        assertEquals(Board.COLS * Board.BLOCK_SIZE, Board.getWidth());
        assertEquals(Board.ROWS * Board.BLOCK_SIZE, board.getHeight());
    }

    @Test
    void newBoardStartsEmpty() {
        Board board = new Board();

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                assertNull(board.getBlock(row, col),
                    "new board should be empty at row=" + row + ", col=" + col);
            }
        }
    }

    @Test
    void setBlockAndGetBlockUseRequestedCoordinates() {
        Board board = new Board();
        Color firstColor = Color.CYAN;
        Color secondColor = Color.ORANGE;

        board.setBlock(3, 2, firstColor);
        board.setBlock(4, 7, secondColor);

        assertEquals(firstColor, board.getBlock(3, 2));
        assertEquals(secondColor, board.getBlock(4, 7));
        assertNull(board.getBlock(3, 7));
    }

    @Test
    void emptyRowIsNotFull() {
        Board board = new Board();

        assertFalse(board.isRowFull(0));
    }

    @Test
    void partiallyFilledRowIsNotFull() {
        Board board = new Board();
        fillRow(board, 0, Color.CYAN);
        board.setBlock(0, Board.COLS - 1, null);

        assertFalse(board.isRowFull(0));
    }

    @Test
    void rowWithAllColumnsFilledIsFull() {
        Board board = new Board();
        fillRow(board, Board.ROWS - 1, Color.CYAN);

        assertTrue(board.isRowFull(Board.ROWS - 1));
    }

    @Test
    void clearRowMovesRowsDownAndLeavesRowsBelowUnchanged() {
        Board board = new Board();
        int clearedRow = 10;

        for (int row = 0; row < Board.ROWS; row++) {
            fillRow(board, row, rowColor(row));
        }

        board.clearRow(clearedRow);

        for (int col = 0; col < Board.COLS; col++) {
            assertNull(board.getBlock(0, col), "top row should become empty");

            for (int row = 1; row <= clearedRow; row++) {
                assertEquals(rowColor(row - 1), board.getBlock(row, col),
                    "row=" + row + " should contain the previous row");
            }

            for (int row = clearedRow + 1; row < Board.ROWS; row++) {
                assertEquals(rowColor(row), board.getBlock(row, col),
                    "rows below the cleared row should remain unchanged");
            }
        }
    }

    @Test
    void clearFullRowsReturnsZeroAndPreservesBoardWhenNoRowIsFull() {
        Board board = new Board();
        board.setBlock(0, 0, Color.RED);
        board.setBlock(7, 4, Color.BLUE);
        board.setBlock(Board.ROWS - 1, Board.COLS - 1, Color.GREEN);

        Color[][] before = copyBoard(board);

        assertEquals(0, board.clearFullRows());
        assertBoardEquals(before, board);
    }

    @Test
    void clearFullRowsRemovesSingleCompletedRow() {
        Board board = new Board();
        int completedRow = Board.ROWS - 1;
        fillRow(board, completedRow, Color.CYAN);
        board.setBlock(completedRow - 1, 0, Color.BLUE);

        assertEquals(1, board.clearFullRows());
        assertEquals(Color.BLUE, board.getBlock(completedRow, 0));
        assertFalse(board.isRowFull(completedRow));
    }

    @Test
    void clearFullRowsRemovesAdjacentCompletedRows() {
        Board board = new Board();
        int bottomRow = Board.ROWS - 1;
        fillRow(board, bottomRow - 1, Color.CYAN);
        fillRow(board, bottomRow, Color.CYAN);
        board.setBlock(bottomRow - 2, 0, Color.BLUE);
        board.setBlock(bottomRow - 3, 1, Color.GREEN);

        assertEquals(2, board.clearFullRows());
        assertEquals(Color.GREEN, board.getBlock(bottomRow - 1, 1));
        assertEquals(Color.BLUE, board.getBlock(bottomRow, 0));
        assertFalse(board.isRowFull(bottomRow - 1));
        assertFalse(board.isRowFull(bottomRow));
    }

    @Test
    void clearFullRowsRemovesNonAdjacentCompletedRowsAndKeepsRowsBetweenThem() {
        Board board = new Board();
        int upperCompletedRow = 15;
        int bottomCompletedRow = Board.ROWS - 1;
        fillRow(board, upperCompletedRow, Color.CYAN);
        fillRow(board, bottomCompletedRow, Color.CYAN);
        board.setBlock(17, 0, Color.BLUE);
        board.setBlock(14, 1, Color.GREEN);

        assertEquals(2, board.clearFullRows());

        // The row between the two cleared rows is preserved and falls one row.
        assertEquals(Color.BLUE, board.getBlock(18, 0));
        // Rows above both cleared rows fall by two rows in total.
        assertEquals(Color.GREEN, board.getBlock(16, 1));
        assertFalse(board.isRowFull(upperCompletedRow));
        assertFalse(board.isRowFull(bottomCompletedRow));
    }

    @Test
    void clearRemovesEveryBlock() {
        Board board = new Board();
        board.setBlock(0, 0, Color.RED);
        board.setBlock(10, 5, Color.BLUE);
        board.setBlock(Board.ROWS - 1, Board.COLS - 1, Color.GREEN);

        board.clear();

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                assertNull(board.getBlock(row, col),
                    "board should be empty at row=" + row + ", col=" + col);
            }
        }
    }

    private static void fillRow(Board board, int row, Color color) {
        for (int col = 0; col < Board.COLS; col++) {
            board.setBlock(row, col, color);
        }
    }

    private static Color rowColor(int row) {
        int shade = row + 1;
        return new Color(shade, shade, shade);
    }

    private static Color[][] copyBoard(Board board) {
        Color[][] copy = new Color[Board.ROWS][Board.COLS];
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                copy[row][col] = board.getBlock(row, col);
            }
        }
        return copy;
    }

    private static void assertBoardEquals(Color[][] expected, Board actual) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                assertEquals(expected[row][col], actual.getBlock(row, col),
                    "board changed at row=" + row + ", col=" + col);
            }
        }
    }
}
