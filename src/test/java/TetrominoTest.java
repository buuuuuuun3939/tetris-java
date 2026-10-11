import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TetrominoTest {

    private static final Map<TetrominoType, int[][]> expectedShapes = Map.of(
        TetrominoType.I, new int[][] {{1, 1, 1, 1}},
        TetrominoType.O, new int[][] {{1, 1}, {1, 1}},
        TetrominoType.T, new int[][] {{0, 1, 0}, {1, 1, 1}},
        TetrominoType.S, new int[][] {{0, 1, 1}, {1, 1, 0}},
        TetrominoType.Z, new int[][] {{1, 1, 0}, {0, 1, 1}},
        TetrominoType.J, new int[][] {{1, 0, 0}, {1, 1, 1}},
        TetrominoType.L, new int[][] {{0, 0, 1}, {1, 1, 1}}
    );

    private static final Map<TetrominoType, Color> expectedColors = Map.of(
        TetrominoType.I, Color.CYAN,
        TetrominoType.O, Color.YELLOW,
        TetrominoType.T, Color.MAGENTA,
        TetrominoType.S, Color.GREEN,
        TetrominoType.Z, Color.RED,
        TetrominoType.J, Color.BLUE,
        TetrominoType.L, Color.ORANGE
    );

    @Test
    @DisplayName("TET-001: 各ミノの初期形状と色が定義どおり")
    void eachTypeHasExpectedInitialShapeAndColor() {
        for (TetrominoType type : TetrominoType.values()) {
            Tetromino tetromino = new Tetromino(type);

            int[][] actualShape = tetromino.getShape();
            int[][] expectedShape = expectedShapes.get(type); // 期待される形状

            // 形状の列数と値を比較する
            for (int row = 0; row < expectedShape.length; row++) {
                assertArrayEquals(expectedShape[row], actualShape[row], "Mismatch in shape for type: " + type);
            }

            Color actualColor = tetromino.getColor();
            Color expectedColor = expectedColors.get(type); // 期待される色
            assertEquals(expectedColor, actualColor, "Mismatch in color for type: " + type);
        }
    }

    @Test
    @Disabled("未実装: TET-002の回転後形状との比較を追加してください")
    @DisplayName("TET-002: 各ミノの回転後形状が時計回り90度")
    void eachTypeReturnsExpectedClockwiseRotatedShape() {
        // TODO: TetrominoTestの設計書にある回転後の期待値と比較する。
    }

    @Test
    @Disabled("未実装: TET-003の4回転後の比較を追加してください")
    @DisplayName("TET-003: 4回転すると初期形状に戻る")
    void fourRotationsRestoreInitialShape() {
        // TODO: 初期形状を保存し、rotate()を4回呼び出して比較する。
    }

    @Test
    @Disabled("未実装: TET-004の形状が変更されないことの確認を追加してください")
    @DisplayName("TET-004: 回転後形状の取得では現在形状が変化しない")
    void gettingRotatedShapeDoesNotChangeCurrentShape() {
        // TODO: getRotatedShape()の呼び出し前後で現在形状を比較する。
    }
}
