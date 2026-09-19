public class Board {
    public static final int ROWS = 20;
    public static final int COLS = 10;
    public static final int BLOCK_SIZE = 30;

    private int[][] board = new int[ROWS][COLS];

    public static int getWidth() {
        return COLS * BLOCK_SIZE;
    }

    public int getHeight() {
        return ROWS * BLOCK_SIZE;
    }

    public void setBlock(int row, int col) {
        board[row][col] = 1;
    }

    public int getBlock(int row, int col) {
        return board[row][col];
    }

    public boolean isRowFull(int row) {
        for (int col = 0; col < COLS; col++) {
            if (board[row][col] == 0) {
                return false;
            }
        }

        return true;
    }

    public void clearRow(int row) {
        // 上の行を下にずらす
        for (int r = row; r > 0; r--) {
            for (int col = 0; col < COLS; col++) {
                board[r][col] = board[r - 1][col];
            }
        }

        // 一行空にする
        for (int col = 0; col < COLS; col++) {
            board[0][col] = 0;
        }
    }

    public int clearFullRows() {
        int clearedRows = 0;

        for (int row = ROWS - 1; row >= 0; row--) {
            if (isRowFull(row)) {
                clearRow(row);

                clearedRows++;
                row++;
            }
        }

        return clearedRows;
    }
}
