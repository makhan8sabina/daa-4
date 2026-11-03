package daa;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EditDistanceTest {
    private static EditDistance[] impls() {
        return new EditDistance[]{new EditDistanceRecursive(), new EditDistanceTable(), new EditDistanceTwoRows()};
    }
    private static void check(String a, String b, int expected) {
        for (EditDistance ed : impls()) {
            assertEquals(expected, ed.distance(a, b), ed.name() + ": \"" + a + "\" -> \"" + b + "\"");
        }
    }

    @Test
    void referenceValues() {
        check("kitten", "sitting", 3);
        check("flaw", "lawn", 2);
        check("intention", "execution", 5);
        check("abc", "abc", 0);
    }

    @Test
    void emptyStrings() {
        check("", "abc", 3);
        check("abc", "", 3);
        check("", "", 0);
    }

    @Test
    void distanceIsSymmetric() {
        for (EditDistance ed : impls()) {
            assertEquals(ed.distance("kitten", "sitting"), ed.distance("sitting", "kitten"), ed.name());
        }
    }

    @Test
    void threeVersionsAgreeOnRandomShortStrings() {
        Random r = new Random(42);
        EditDistance[] all = impls();
        for (int test = 0; test < 100; test++) {
            String a = Generator.random(r, r.nextInt(9), "abc");
            String b = Generator.random(r, r.nextInt(9), "abc");
            int expected = all[1].distance(a, b);
            assertEquals(expected, all[0].distance(a, b), "recursive on \"" + a + "\", \"" + b + "\"");
            assertEquals(expected, all[2].distance(a, b), "two rows on \"" + a + "\", \"" + b + "\"");
        }
    }

    @Test
    void countersWork() {
        EditDistance table = new EditDistanceTable();
        EditDistance two = new EditDistanceTwoRows();
        table.distance("kitten", "sitting");
        two.distance("kitten", "sitting");
        assertEquals(6L * 7L, table.ops());
        assertEquals(6L * 7L, two.ops());
        assertEquals(7L * 8L, table.mem());
        assertEquals(2L * 8L, two.mem());
    }
}
