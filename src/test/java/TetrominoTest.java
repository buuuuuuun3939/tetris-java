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

    private static final Map<TetrominoType, int[][]> expectedRotatedShapes = Map.of(
        TetrominoType.I, new int[][] {{1}, {1}, {1}, {1}},
        TetrominoType.O, new int[][] {{1, 1}, {1, 1}}, // O型は回転しても形状が変わらない
        TetrominoType.T, new int[][] {{1, 0}, {1, 1}, {1, 0}},
        TetrominoType.S, new int[][] {{1, 0}, {1, 1}, {0, 1}},
        TetrominoType.Z, new int[][] {{0, 1}, {1, 1}, {1, 0}},
        TetrominoType.J, new int[][] {{1, 1}, {1, 0}, {1, 0}},
        TetrominoType.L, new int[][] {{1, 0}, {1, 0}, {1, 1}}
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
    @DisplayName("TET-002: 各ミノの回転後形状が時計回り90度")
    void eachTypeReturnsExpectedClockwiseRotatedShape() {
        // TODO: TetrominoTestの設計書にある回転後の期待値と比較する。
        for (TetrominoType type : TetrominoType.values()) {
            Tetromino tetromino = new Tetromino(type);
            int[][] actualRotatedShape = tetromino.getRotatedShape();
            int[][] expectedRotatedShape = expectedRotatedShapes.get(type); // 期待される回転後形状

            // 回転後形状の列数と値を比較する
            for (int row = 0; row < expectedRotatedShape.length; row++) {
                assertArrayEquals(expectedRotatedShape[row], actualRotatedShape[row], "Mismatch in rotated shape for type: " + type);
            }
        }
    }

    @Test
    @DisplayName("TET-003: 4回転すると初期形状に戻る")
    void fourRotationsRestoreInitialShape() {
        // TODO: 初期形状を保存し、rotate()を4回呼び出して比較する。
        for (TetrominoType type : TetrominoType.values()) {
            Tetromino tetromino = new Tetromino(type);
            int[][] initialShape = tetromino.getShape();

            // 4回回転させる
            for (int i = 0; i < 4; i++) {
                tetromino.rotate();
            }

            int[][] finalShape = tetromino.getShape();

            // 初期形状と最終形状を比較する
            for (int row = 0; row < initialShape.length; row++) {
                assertArrayEquals(initialShape[row], finalShape[row], "Mismatch after 4 rotations for type: " + type);
            }
        }
    }

    @Test
    @DisplayName("TET-004: 回転後形状の取得では現在形状が変化しない")
    void gettingRotatedShapeDoesNotChangeCurrentShape() {
        // TODO: getRotatedShape()の呼び出し前後で現在形状を比較する。

        for (TetrominoType type : TetrominoType.values()) {
            Tetromino tetromino = new Tetromino(type);
            int[][] initialShape = tetromino.getShape();

            // 回転後形状を取得する
            int[][] rotatedShape = tetromino.getRotatedShape();

            // 現在形状が変化していないことを確認する
            int[][] currentShapeAfterRotation = tetromino.getShape();
            for (int row = 0; row < initialShape.length; row++) {
                assertArrayEquals(initialShape[row], currentShapeAfterRotation[row], "Current shape changed after getting rotated shape for type: " + type);
            }
        }
    }
}
