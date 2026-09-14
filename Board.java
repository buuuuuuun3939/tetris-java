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
}
