package com.daniilpastukhov.database;

import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import javafx.util.Pair;

import smile.data.AttributeDataset;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.json.JsonReadOptions;
import tech.tablesaw.io.json.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of RethinkDB database functionality.
 */

public class RethinkDB implements NoSqlDatabase {
    private static com.rethinkdb.RethinkDB rethinkDB = com.rethinkdb.RethinkDB.r;
    private static Connection connection;

    @Override
    public RethinkDB init() {
        System.out.println("> Connecting to database.");
        connection = rethinkDB.connection().hostname("localhost").port(28015).connect();
        System.out.println("> Connected to RethinkDB database.");
        return this;
    }

    @Override
    public Pair<AttributeDataset, AttributeDataset> getDatasets() throws IOException {
        Cursor trainCursor = rethinkDB.db("digits").table("digits_train").without("id").map(ReqlExpr::toJsonString).run(connection);
        Cursor testCursor = rethinkDB.db("digits").table("digits_test").without("id").map(ReqlExpr::toJson).run(connection);

        ArrayList<String> trainRows = new ArrayList<>();
        ArrayList<String> testRows = new ArrayList<>();

        System.out.println(trainCursor.next().toString().replaceAll("\"(\\d+)\"", "$1"));

        for (Object doc : trainCursor) trainRows.add(doc.toString().replaceAll("\"(\\d+)\"", "$1"));
        for (Object doc : testCursor) testRows.add(doc.toString().replaceAll("\"(\\d+)\"", "$1"));

        JsonReader jr = new JsonReader();
        Table trainTable = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", trainRows) + "]").build());
        Table testTable = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", testRows) + "]").build());

        AttributeDataset trainDataset = trainTable.smile().nominalDataset("label");
        AttributeDataset testDataset = testTable.smile().nominalDataset("label");

        System.out.println(trainDataset.head(1));

        return new Pair<>(trainDataset, testDataset);
    }

    @Override
    public void addImage(double[] pixels, int label) {
        Map<String, Object> map = new HashMap<String, Object>();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        key.append("label,");
        value.append(label).append(",");
        for (int i = 1; i <= 28; i++) {
            for (int j = 1; j <= 28; j++) {
                key.append(i).append("x").append(j).append((i == 28 && j == 28) ? "" : ",");
                value.append((int) pixels[((i - 1) * 28) + j - 1]).append((i == 28 && j == 28) ? "" : ",");
            }
        }

        System.out.println(key);
        System.out.println(value);

        map.put(key.toString(), value.toString());
        rethinkDB.db("digits").table("digits_train").insert(map);
    }
}
