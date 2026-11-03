package daa;
import java.util.List;
public interface StringSearch {
    List<Integer> search(String text, String pattern);
    long comparisons();
    long mem();
    void reset();
    String name();
}
