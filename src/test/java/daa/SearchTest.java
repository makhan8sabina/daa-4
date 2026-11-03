package daa;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTest {
    private static StringSearch[] impls() {
        return new StringSearch[]{new NaiveSearch(), new KmpSearch()};
    }

    private static void check(String text, String pattern, List<Integer> expected) {
        for (StringSearch s : impls()) {
            assertEquals(expected, s.search(text, pattern), s.name() + ": \"" + pattern + "\" in \"" + text + "\"");
        }
    }

    private static List<Integer> oracle(String text, String pattern) {
        List<Integer> res = new ArrayList<>();
        int from = 0;
        while (true) {
            int idx = text.indexOf(pattern, from);
            if (idx < 0) break;
            res.add(idx);
            from = idx + 1;
        }
        return res;
    }

    @Test
    void referenceMatches() {
        check("ABABDABACDABABCABAB", "ABABCABAB", List.of(10));
        check("AAAAA", "AA", List.of(0, 1, 2, 3));
        check("ABABABA", "ABA", List.of(0, 2, 4));
    }

    @Test
    void noMatchGivesEmptyList() {
        check("ABCABC", "XYZ", List.of());
        check("", "A", List.of());
    }

    @Test
    void emptyPatternThrows() {
        for (StringSearch s : impls()) {
            assertThrows(IllegalArgumentException.class, () -> s.search("ABC", ""), s.name());
            assertThrows(IllegalArgumentException.class, () -> s.search("", ""), s.name());
        }
    }

    @Test
    void patternLongerThanTextGivesEmptyList() {
        check("AB", "ABC", List.of());
        check("", "ABC", List.of());
    }

    @Test
    void matchAtTheVeryEnd() {
        check("xxabc", "abc", List.of(2));
        check("abc", "abc", List.of(0));
    }

    @Test
    void naiveAndKmpAgreeOnRandomTexts() {
        Random r = new Random(7);
        NaiveSearch naive = new NaiveSearch();
        KmpSearch kmp = new KmpSearch();
        for (int test = 0; test < 100; test++) {
            String text = Generator.random(r, r.nextInt(61), "ab");
            String pattern = Generator.random(r, 1 + r.nextInt(5), "ab");
            List<Integer> a = naive.search(text, pattern);
            List<Integer> b = kmp.search(text, pattern);
            assertEquals(a, b, "text=\"" + text + "\" pattern=\"" + pattern + "\"");
            assertEquals(oracle(text, pattern), b, "oracle, text=\"" + text + "\" pattern=\"" + pattern + "\"");
        }
    }

    @Test
    void kmpNeverNeedsMoreThanTwiceTheInput() {
        String[][] cases = {
                Generator.searchCase("worst", 5000, 50),
                Generator.searchCase("random", 5000, 50),
                Generator.searchCase("all_match", 5000, 50),
                Generator.searchCase("log", 5000, 50)
        };
        for (String[] tp : cases) {
            KmpSearch kmp = new KmpSearch();
            kmp.search(tp[0], tp[1]);
            long bound = 2L * (tp[0].length() + tp[1].length());
            assertTrue(kmp.comparisons() <= bound, "comparisons " + kmp.comparisons() + " > " + bound);
        }
    }

    @Test
    void naiveWorstCaseIsQuadratic() {
        NaiveSearch naive = new NaiveSearch();
        String[] tp = Generator.searchCase("worst", 1000, 10);
        naive.search(tp[0], tp[1]);
        assertEquals((long) (1000 - 10 + 1) * 10, naive.comparisons());
    }
}
