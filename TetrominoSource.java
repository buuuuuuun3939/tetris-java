/** Supplies tetrominoes to a game. */
@FunctionalInterface
public interface TetrominoSource {
    Tetromino next();
}
