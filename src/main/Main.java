import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<TestDocument> writtenTests = new ArrayList<>();
        File dataset = new File("dataset");

        for (File document: dataset.listFiles())
            writtenTests.add(new TestDocument(document, document.getName()));

//        File testDocument = dataset.listFiles()[0];
//        writtenTests.add(new TestDocument(testDocument, testDocument.getName().replaceAll(".pdf", "")));

        // Convert the Test Documents to JSON Arrays
        for (TestDocument document: writtenTests) {
            try (FileWriter out = new FileWriter("output/" + document.getName() + ".json")) {
                out.write(document.toJSON().toString());
                out.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
