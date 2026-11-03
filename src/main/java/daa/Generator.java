package daa;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public final class Generator {

    public static final long SEED = 20261102L;
    public static final String DNA = "ACGT";
    public static final String LOG_PATTERN = "ERROR svc=search code=503";

    private Generator() { }


    public static Random rng(String type, int n) {
        return new Random(SEED * 31 + type.hashCode() * 1_000_003L + n);
    }

    public static String random(Random r, int len, String alphabet) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }

    public static String repeat(char c, int len) {
        char[] arr = new char[len];
        Arrays.fill(arr, c);
        return new String(arr);
    }

    public static String[] editPair(String type, int n) {
        switch (type) {
            case "random":
                return new String[]{random(new Random(SEED + 1), n, DNA), random(new Random(SEED + 2), n, DNA)};
            case "identical": {
                String a = random(new Random(SEED + 1), n, DNA);
                return new String[]{a, a};
            }
            case "disjoint":
                return new String[]{repeat('a', n), repeat('b', n)};
            default:
                throw new IllegalArgumentException("unknown edit-distance input: " + type);
        }
    }

    public static String[] searchCase(String type, int n, int m) {
        Random r = rng(type, n);
        switch (type) {
            case "random":
                return new String[]{random(r, n, DNA), random(r, m, DNA)};
            case "log":
                return new String[]{logText(r, n), LOG_PATTERN};
            case "worst":
                return new String[]{repeat('A', n), repeat('A', m - 1) + "B"};
            case "all_match":
                return new String[]{repeat('A', n), repeat('A', m)};
            default:
                throw new IllegalArgumentException("unknown search input: " + type);
        }
    }

    private static final String[] SERVICES = {"auth", "search", "payments", "map"};

    static String logText(Random r, int n) {
        StringBuilder sb = new StringBuilder(n + 80);
        while (sb.length() < n) {
            double u = r.nextDouble();
            String level;
            String code;
            if (u < 0.80) {
                level = "INFO";
                code = "200";
            } else if (u < 0.95) {
                level = "WARN";
                code = "404";
            } else {
                level = "ERROR";
                code = r.nextBoolean() ? "500" : "503";
            }
            String svc = SERVICES[r.nextInt(SERVICES.length)];
            sb.append(String.format(Locale.ROOT, "2026-11-02 %02d:%02d:%02d %s svc=%s code=%s\n",
                    r.nextInt(24), r.nextInt(60), r.nextInt(60), level, svc, code));
        }
        sb.setLength(n);
        return sb.toString();
    }
}
