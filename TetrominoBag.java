import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TetrominoBag {

    private List<TetrominoType> bag = new ArrayList<>();


    public TetrominoType next() {
        if (bag.isEmpty()) {
            for (TetrominoType type : TetrominoType.values()) {
                bag.add(type);
            }

            Collections.shuffle(bag);
        }

        return bag.remove(0);
    }
}
