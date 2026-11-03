package daa;
public final class EditDistanceTable implements EditDistance {

    private long cells;
    private long memory;

    @Override
    public int distance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] d = new int[n + 1][m + 1];
        memory += (long) (n + 1) * (m + 1);

        for (int i = 0; i <= n; i++) d[i][0] = i;
        for (int j = 0; j <= m; j++) d[0][j] = j;

        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                cells++;
                if (ca == b.charAt(j - 1)) {
                    d[i][j] = d[i - 1][j - 1];
                } else {
                    d[i][j] = 1 + Math.min(d[i - 1][j], Math.min(d[i][j - 1], d[i - 1][j - 1]));
                }
            }
        }
        return d[n][m];
    }

    @Override public long ops() { return cells; }
    @Override public long mem() { return memory; }
    @Override public void reset() { cells = 0; memory = 0; }
    @Override public String name() { return "edit_table"; }
}
