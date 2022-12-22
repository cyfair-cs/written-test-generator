import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<TestDocument> writtenTests = new ArrayList<>();
        File dataset = new File("dataset"), output = new File("output");

        for (File document: dataset.listFiles())
            writtenTests.add(new TestDocument(document, document.getName().replaceAll(".pdf","")));

        for (File previous_json: output.listFiles())
            previous_json.delete();

        // Convert the Test Documents to JSON Documents
        for (TestDocument document: writtenTests) {
            try (FileWriter out = new FileWriter("output/" + document.getName() + ".json")) {
                out.write(document.toJSON().toString(4));
                out.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
