public class Tetromino {

    private int[][] shape = {
        {1, 1, 1, 1}
    };

    public void reset() {
        shape = new int[][] {
            {1, 1, 1, 1}
        };
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
