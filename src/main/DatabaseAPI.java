package main;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DatabaseAPI {
    public static final String MONGO_URI = String.format(
            "mongodb+srv://%s:%s@question-pool.u0e876j.mongodb.net/?retryWrites=true&w=majority",
            Main.ENV.get("MONGO_USERNAME"),
            Main.ENV.get("MONGO_PASSWORD")
    );

    private static final MongoClient client = MongoClients.create(MONGO_URI);
    private static final MongoDatabase database = client.getDatabase("question_pool");

    public static void reset() {
        database.drop();
    }

    public static void archiveTestDocument(TestDocument document) {
        System.out.println("[ARCHIVE DOCUMENT] " + document.getName());
        final MongoCollection<Document> collection = database.getCollection(document.getName());
        for (Question question: document.getQuestions())
            archiveQuestion(question, collection);
    }

    @SuppressWarnings("All")
    private static void archiveQuestion(Question question, MongoCollection<Document> collection) {
        try {
            Document conversion = Document.parse(question.toJSON().toString());
            collection.insertOne(conversion);
        } catch (MongoWriteException mwe) {
            mwe.printStackTrace();
        }
    }
}
