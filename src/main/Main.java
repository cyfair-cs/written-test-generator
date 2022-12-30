package main;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Main {
    public static final Dotenv ENV = Dotenv.load();
    public static final List<TestDocument> writtenTests = new ArrayList<>();
    public static final File dataset = new File("dataset"),
            output = new File("output"),
            keys = new File("keys");

    public static void main(String[] args) {
        loadDocuments();
        loadJSONFromDocuments();

        System.out.println("\nArchiving documents...\n");
        DatabaseAPI.reset();
        for (TestDocument test: writtenTests)
            DatabaseAPI.archiveTestDocument(test);
    }

    private static void loadDocuments() {
        writtenTests.add(TestDocument.QUESTION_POOL);

        System.out.println("Loading documents...\n");
        for (File document: Objects.requireNonNull(dataset.listFiles())) {
            System.out.println("[LOAD TEST] " + document.getName());
            writtenTests.add(new TestDocument(document, document.getName().replaceAll(".pdf", "")));
        }

        // Load keys from key pdf files
        System.out.println("\nLoading external keyfiles...\n");
        for (File keyFile: Objects.requireNonNull(keys.listFiles())) {
            System.out.println("[LOAD KEY] " + keyFile.getName());
            String documentSource = keyFile.getName().replaceAll("_KEY\\.pdf","");
            try {
                PDDocument keyPDF = PDDocument.load(keyFile);
                String raw = new PDFTextStripper().getText(keyPDF);
                keyPDF.close();
                String contestSource = TestDocument.getVerboseContestName(documentSource);
                Optional<TestDocument> testFound = writtenTests.stream().filter(test -> test.getName().equals(contestSource)).findFirst();
                if (testFound.isPresent()) {
                    testFound.get().loadKeysFromText(raw);
//                    System.out.println(contestSource);
                }
                else {
                    System.out.println("Could not load keys for document: " + contestSource);
//                    System.out.println(raw);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadJSONFromDocuments() {
        System.out.println("\nDeleting previous JSON builds...\n");
        for (File previousJSON: Objects.requireNonNull(output.listFiles())) {
            System.out.println("[DELETE JSON] " + previousJSON.getName());
            previousJSON.delete();
        }

        // Convert the Test Documents to JSON Documents
        System.out.println("\nBuilding JSON files...\n");
        for (TestDocument document: writtenTests) {
            System.out.println("[BUILD JSON] " + document.getName());
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
