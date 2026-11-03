package daa;
import java.util.ArrayList;
import java.util.List;
public final class KmpSearch implements StringSearch {
    private long cmp;
    private long memory;
    public static int[] lps(String p) {
        return buildLps(p, new long[1]);
    }

    private static int[] buildLps(String p, long[] counter) {
        int m = p.length();
        int[] lps = new int[m];
        int k = 0;
        long c = 0;
        for (int i = 1; i < m; i++) {
            while (k > 0) {
                c++;
                if (p.charAt(i) != p.charAt(k)) {
                    k = lps[k - 1];
                } else {
                    break;
                }
            }
            if (k == 0) {
                c++;
                if (p.charAt(i) == p.charAt(0)) k = 1;
            } else {
                k++;
            }
            lps[i] = k;
        }
        counter[0] += c;
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern must not be empty");
        }
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m > n) return result;

        long[] buildCounter = new long[1];
        int[] lps = buildLps(pattern, buildCounter);
        memory += m;
        long c = buildCounter[0];

        int q = 0;
        for (int i = 0; i < n; i++) {
            char t = text.charAt(i);
            while (q > 0) {
                c++;
                if (t != pattern.charAt(q)) {
                    q = lps[q - 1];
                } else {
                    break;
                }
            }
            if (q == 0) {
                c++;
                if (t == pattern.charAt(0)) q = 1;
            } else {
                q++;
            }
            if (q == m) {
                result.add(i - m + 1);
                q = lps[q - 1];
            }
        }
        cmp += c;
        return result;
    }

    @Override public long comparisons() { return cmp; }
    @Override public long mem() { return memory; }
    @Override public void reset() { cmp = 0; memory = 0; }
    @Override public String name() { return "search_kmp"; }
}
