package com.daniilpastukhov;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Database class.
 */

class Database {
    MongoCollection<Document> trainCollection;
    MongoCollection<Document> testCollection;

    /**
     * Initializes database.
     *
     * @return Initialized database instance.
     */
    Database init() {
        System.out.println("> Connection to database.");
        MongoClientURI uri = new MongoClientURI("mongodb+srv://admin:admin@digit-classification-l3sse.mongodb.net/admin?retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(uri);
        System.out.println("> Connected to database.");
        System.out.println("> Extracting collections.");
        MongoDatabase database = mongoClient.getDatabase("digits");
        trainCollection = database.getCollection("digits_train");
        testCollection = database.getCollection("digits_test");
        System.out.println("> Collections were extracted.");
        return this;
    }

    /**
     * Adds image to database.
     *
     * @param document Image represented by Document class.
     */
    void addImage(Document document) {
        trainCollection.insertOne(document);
    }

//    private Table getTable(MongoCollection<Document> collection) throws IOException {
//        ArrayList<String> rows = new ArrayList<>();
//
//        for (Document doc : collection.find()) {
//            rows.add(doc.toJson().replace(doc.toJson().substring(1, 46), ""));
//        }
//
//        JsonReader jr = new JsonReader();
//        return jr.read(JsonReadOptions.builderFromString("[" + String.join(",", rows) + "]").build());
//    }
}
