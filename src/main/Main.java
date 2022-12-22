import java.io.FileWriter;
import java.util.ArrayList;

public class Main {
    final static String dataset = "dataset/a/";

    final static String[] tests = {
            "test.pdf : Test Question"
    };

    public static void main(String[] args) {
        ArrayList<TestDocument> writtenTests = new ArrayList<>();
        for (String test: tests){
            String[] tokens = test.split(" : ");
            writtenTests.add(new TestDocument(tokens[0], tokens[1]));
        }

        // Convert the Test Documents to JSON Arrays
        for (TestDocument document: writtenTests) {
            try (FileWriter out = new FileWriter())
        }
    }
}
