import java.io.IOException;
import java.util.ArrayList;

public class Main {
    final static String[] Paths = {
            "dataset/a/test.pdf"
    };

    public static void main(String[] args) {
        ArrayList<TestDocument> WrittenTests = new ArrayList<>();
        for (String TestPath: Paths)
            WrittenTests.add(new TestDocument(TestPath));
    }
}
