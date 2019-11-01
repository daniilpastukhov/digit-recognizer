package com.daniilpastukhov.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.util.Pair;
import org.bson.Document;
import smile.data.AttributeDataset;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.json.JsonReadOptions;
import tech.tablesaw.io.json.JsonReader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * MongoDB database class.
 */

class MongoDB implements NoSqlDatabase {
    private static MongoClient mongoClient;
    private String dbUri;
    MongoCollection<Document> trainCollection;
    MongoCollection<Document> testCollection;

    MongoDB(String uri) {
        this.dbUri = uri;
    }

    public MongoDB init() {
        System.out.println("> Connecting to database.");
        mongoClient = new MongoClient(new MongoClientURI(dbUri));
        System.out.println("> Connected to MongoDB database.");
        return this;
    }

    @Override
    public void closeConnection() {
        mongoClient.close();
    }

    @Override
    public Pair<AttributeDataset, AttributeDataset> getDatasets() throws IOException {
        MongoDatabase database = mongoClient.getDatabase("digits");
        trainCollection = database.getCollection("digits_train");
        testCollection = database.getCollection("digits_test");
        System.out.println("> Collections were extracted.");

        ArrayList<String> trainRows = new ArrayList<>();
        ArrayList<String> testRows = new ArrayList<>();

        System.out.println(trainCollection.find().first().toJson().replace(trainCollection.find().first().toJson().substring(1, 46), ""));

        for (Document doc : trainCollection.find())
            trainRows.add(doc.toJson().replace(doc.toJson().substring(1, 46), ""));
        for (Document doc : testCollection.find())
            testRows.add(doc.toJson().replace(doc.toJson().substring(1, 46), ""));

        JsonReader jr = new JsonReader();
        Table trainTable = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", trainRows) + "]").build());
        Table testTable = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", testRows) + "]").build());

        return new Pair<>(trainTable.smile().nominalDataset("label"), testTable.smile().nominalDataset("label"));
    }

    @Override
    public void addImage(double[] pixels, int label) {
        Document document = new Document();
        document.append("label", label);

        for (int i = 1; i <= 28; i++)
            for (int j = 1; j <= 28; j++)
                document.append(i + "x" + j, pixels[((i - 1) * 28) + j - 1]);

        trainCollection.insertOne(document);
    }
}
