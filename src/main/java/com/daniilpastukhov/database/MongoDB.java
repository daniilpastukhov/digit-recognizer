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
import java.util.List;

/**
 * Implementation of MongoDB database functionality.
 */

public class MongoDB implements NoSqlDatabase {
    private String dbUri;
    private static MongoCollection<Document> trainCollection;
    private static MongoCollection<Document> testCollection;

    public MongoDB(String uri) {
        this.dbUri = uri;
    }

    public MongoDB init() {
        System.out.println("> Connecting to database.");
        MongoClient mongoClient = new MongoClient(new MongoClientURI(dbUri));
        System.out.println("> Connected to MongoDB database.");
        MongoDatabase database = mongoClient.getDatabase("digits");
        trainCollection = database.getCollection("digits_train");
        testCollection = database.getCollection("digits_test");
        System.out.println("> Collections were extracted.");
        return this;
    }

    @Override
    public Pair<AttributeDataset, AttributeDataset> getDatasets() throws IOException {
        ArrayList<String> trainRows = new ArrayList<>();
        ArrayList<String> testRows = new ArrayList<>();

        for (Document doc : trainCollection.find())
            trainRows.add(doc.toJson().replace(doc.toJson().substring(1, 46), ""));
        for (Document doc : testCollection.find())
            testRows.add(doc.toJson().replace(doc.toJson().substring(1, 46), ""));

        JsonReader jr = new JsonReader();
        Table trainTable = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", trainRows) + "]").build());
        Table testTable = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", testRows) + "]").build());

        List<String> colNames = new ArrayList<>();
        for (int i = 1; i <= 28; i++)
            for (int j = 1; j <= 28; j++)
                colNames.add(i + "x" + j);

        Table sortedTrainTable = Table.create().addColumns(trainTable.column("label"));
        Table sortedTestTable = Table.create().addColumns(testTable.column("label"));

        for (String col : colNames) {
            sortedTrainTable.addColumns(trainTable.column(col));
            sortedTestTable.addColumns(testTable.column(col));
        }

        return new Pair<>(sortedTrainTable.smile().nominalDataset("label"), sortedTestTable.smile().nominalDataset("label"));
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
