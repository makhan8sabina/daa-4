package daa;
import java.util.ArrayList;
import java.util.List;

public final class NaiveSearch implements StringSearch {

    private long cmp;

    @Override
    public List<Integer> search(String text, String pattern) {
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern must not be empty");
        }
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        for (int s = 0; s + m <= n; s++) {
            int j = 0;
            while (j < m) {
                cmp++;
                if (text.charAt(s + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) result.add(s);
        }
        return result;
    }

    @Override public long comparisons() { return cmp; }
    @Override public long mem() { return 0; }
    @Override public void reset() { cmp = 0; }
    @Override public String name() { return "search_naive"; }
}
