package daa;
public interface EditDistance {
    int distance(String a, String b);
    long ops();
    long mem();
    void reset();
    String name();
}
