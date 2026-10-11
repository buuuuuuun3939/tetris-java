import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TetrominoBagTest {

    @Test
    void eachBagContainsEveryTetrominoExactlyOnce() {
        TetrominoBag bag = new TetrominoBag();

        for (int bagNumber = 0; bagNumber < 2; bagNumber++) {
            Set<TetrominoType> types = new HashSet<>();
            for (int i = 0; i < TetrominoType.values().length; i++) {
                types.add(bag.next());
            }

            assertEquals(TetrominoType.values().length, types.size());
            assertEquals(new HashSet<>(java.util.Arrays.asList(TetrominoType.values())), types);
        }
    }
}
