package daa;
public final class EditDistanceTwoRows implements EditDistance {

    private long cells;
    private long memory;

    @Override
    public int distance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        memory += 2L * (m + 1);

        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                cells++;
                if (ca == b.charAt(j - 1)) {
                    cur[j] = prev[j - 1];
                } else {
                    cur[j] = 1 + Math.min(prev[j], Math.min(cur[j - 1], prev[j - 1]));
                }
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[m];
    }

    @Override public long ops() { return cells; }
    @Override public long mem() { return memory; }
    @Override public void reset() { cells = 0; memory = 0; }
    @Override public String name() { return "edit_two_rows"; }
}
