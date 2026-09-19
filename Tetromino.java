import java.util.Random;

public class Tetromino {
    private TetrominoType type;
    private int[][] shape;
    private static final Random random = new Random();

    public Tetromino(TetrominoType type) {
        this.type = type;
        setShape();
    }

    public static Tetromino createRandom() {
        TetrominoType[] types = TetrominoType.values();
        int index = random.nextInt(types.length);

        return new Tetromino(types[index]);
    }

    private void setShape() {
        switch (type) {
            case I:
                shape = new int[][] {
                    {1, 1, 1, 1}
                };
                break;
            case O:
                shape = new int[][] {
                    {1, 1},
                    {1, 1}
                };
                break;
            case T:
                shape = new int[][] {
                    {0, 1, 0},
                    {1, 1, 1}
                };
                break;
            case S:
                shape = new int[][] {
                    {0, 1, 1},
                    {1, 1, 0}
                };
                break;
            case Z:
                shape = new int[][] {
                    {1, 1, 0},
                    {0, 1, 1}
                };
                break;
            case J:
                shape = new int[][] {
                    {1, 0, 0},
                    {1, 1, 1}
                };
                break;
            case L:
                shape = new int[][] {
                    {0, 0, 1},
                    {1, 1, 1}
                };
                break;
        }
    }

    public void reset() {
        setShape();
    }

    public int[][] getShape() {
        return shape;
    }

    public void rotate() {
        shape = getRotatedShape();
    }

    // 回転後の形状を、shapeを変更せずに取得する
    public int[][] getRotatedShape() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                rotated[col][rows - 1 - row] = shape[row][col];
            }
        }

        return rotated;
    }
}
