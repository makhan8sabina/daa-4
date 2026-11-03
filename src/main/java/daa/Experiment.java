package daa;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Experiment {

    static final int[] REC_SIZES = {3, 4, 6, 8, 10, 11, 12, 13, 14};
    static final int[] DP_SIZES = {250, 500, 1000, 2000, 4000};
    static final int[] SEARCH_SIZES = {50_000, 100_000, 200_000, 400_000, 800_000};
    static final int PATTERN_LEN = 500;

    static final String[] ED_INPUTS = {"random", "identical", "disjoint"};
    static final String[] SEARCH_INPUTS = {"random", "log", "worst", "all_match"};

    static final int RUNS = 5;
    static final int RUNS_RECURSIVE = 3;
    static final int REC_SINGLE_RUN_FROM = 13;

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        if (out.getParent() != null) Files.createDirectories(out.getParent());

        warmUp();

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out))) {
            w.println("algorithm,input,n,m,time_ms,ops,mem,result,runs");


            EditDistance rec = new EditDistanceRecursive();
            for (String input : ED_INPUTS) {
                for (int n : REC_SIZES) {
                    runEdit(rec, input, n, n >= REC_SINGLE_RUN_FROM ? 1 : RUNS_RECURSIVE, false, w);
                }
            }

            EditDistance[] dp = {new EditDistanceTable(), new EditDistanceTwoRows()};
            for (EditDistance ed : dp) {
                for (String input : ED_INPUTS) {
                    for (int n : DP_SIZES) runEdit(ed, input, n, RUNS, true, w);
                }
            }
            StringSearch[] searchers = {new NaiveSearch(), new KmpSearch()};
            for (StringSearch s : searchers) {
                for (String input : SEARCH_INPUTS) {
                    for (int n : SEARCH_SIZES) runSearch(s, input, n, RUNS, w);
                }
            }
        }
        System.out.println("Written " + out.toAbsolutePath());
    }

    private static void runEdit(EditDistance ed, String input, int n, int runs, boolean untimedRun, PrintWriter w) {
        String[] ab = Generator.editPair(input, n);
        int[] res = new int[1];
        if (untimedRun) ed.distance(ab[0], ab[1]);
        double ms = medianMs(() -> {
            ed.reset();
            res[0] = ed.distance(ab[0], ab[1]);
        }, runs);
        row(w, ed.name(), input, n, ab[1].length(), ms, ed.ops(), ed.mem(), res[0], runs);
    }

    private static void runSearch(StringSearch s, String input, int n, int runs, PrintWriter w) {
        String[] tp = Generator.searchCase(input, n, PATTERN_LEN);
        int[] res = new int[1];
        s.search(tp[0], tp[1]);
        double ms = medianMs(() -> {
            s.reset();
            res[0] = s.search(tp[0], tp[1]).size();
        }, runs);
        row(w, s.name(), input, n, tp[1].length(), ms, s.comparisons(), s.mem(), res[0], runs);
    }

    private static void row(PrintWriter w, String alg, String input, int n, int m, double ms,
                            long ops, long mem, long result, int runs) {
        String line = String.format(Locale.ROOT, "%s,%s,%d,%d,%.4f,%d,%d,%d,%d",
                alg, input, n, m, ms, ops, mem, result, runs);
        w.println(line);
        w.flush();
        System.out.println(line);
    }

    static double medianMs(Runnable task, int runs) {
        double[] t = new double[runs];
        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            task.run();
            t[i] = (System.nanoTime() - start) / 1e6;
        }
        Arrays.sort(t);
        return t[runs / 2];
    }


    private static void warmUp() {
        List<EditDistance> dp = List.of(new EditDistanceTable(), new EditDistanceTwoRows());
        for (String type : ED_INPUTS) {
            String[] ab = Generator.editPair(type, 300);
            for (int i = 0; i < 20; i++) for (EditDistance e : dp) e.distance(ab[0], ab[1]);
            String[] small = Generator.editPair(type, 8);
            EditDistance r = new EditDistanceRecursive();
            for (int i = 0; i < 2000; i++) r.distance(small[0], small[1]);
        }
        List<StringSearch> ss = List.of(new NaiveSearch(), new KmpSearch());
        for (String type : SEARCH_INPUTS) {
            String[] tp = Generator.searchCase(type, 100_000, 100);
            for (int i = 0; i < 5; i++) for (StringSearch s : ss) s.search(tp[0], tp[1]);
        }
    }
}
