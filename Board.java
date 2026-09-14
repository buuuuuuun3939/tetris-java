public class Board {
    public static final int ROWS = 20;
    public static final int COLS = 10;
    public static final int BLOCK_SIZE = 30;

    private int[][] board = new int[ROWS][COLS];

    public int getWidth() {
        return COLS * BLOCK_SIZE;
    }

    public int getHeight() {
        return ROWS * BLOCK_SIZE;
    }
}
