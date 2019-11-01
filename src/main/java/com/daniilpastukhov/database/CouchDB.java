package com.daniilpastukhov.database;

//import com.rethinkdb.gen.ast.ReqlExpr;
//import com.rethinkdb.net.Connection;
//import com.rethinkdb.net.Cursor;

import javafx.util.Pair;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import smile.data.AttributeDataset;
import tech.tablesaw.api.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CouchDB implements NoSqlDatabase {
    class MnistImage {
        double[] pixels;
        int label;

        public MnistImage(double[] pixels, int label) {
            this.pixels = pixels;
            this.label = label;
        }

        public int getLabel() {
            return label;
        }

        public void setLabel(int label) {
            this.label = label;
        }

        public double[] getPixels() {
            return pixels;
        }

        public void setPixels(double[] pixels) {
            this.pixels = pixels;
        }
    }

    private CouchDbInstance dbInstance;
    CouchDbConnector trainDb;
    CouchDbConnector testDb;

    @Override
    public CouchDB init() {
        System.out.println("> Connecting to database.");
        try {
            HttpClient httpClient = new StdHttpClient.Builder()
                    .url("http://localhost:5984")
                    .username("admin")
                    .password("admin")
                    .build();

            dbInstance = new StdCouchDbInstance(httpClient);

            System.out.println("> Connected to CouchDB database.");

            trainDb = dbInstance.createConnector("digits_train", false);
            testDb = dbInstance.createConnector("digits_test", false);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void closeConnection() {
    }

    @Override
    public Pair<AttributeDataset, AttributeDataset> getDatasets() throws IOException {
        Table trainTable = Table.create().addColumns(IntColumn.create("label"));
        Table testTable = Table.create().addColumns(IntColumn.create("label"));

        for (int i = 1; i <= 28; i++) {
            for (int j = 1; j <= 28; j++) {
                trainTable.addColumns(DoubleColumn.create(i + "x" + j));
                testTable.addColumns(DoubleColumn.create(i + "x" + j));
            }
        }

        System.out.println("noice");

        List<String> trainIds = trainDb.getAllDocIds();
        List<String> testIds = testDb.getAllDocIds();

        for (String id : trainIds) {
            MnistImage image = getImage(trainDb, id);
            for (int i = 1; i <= 28; i++)
                for (int j = 1; j <= 28; j++)
                    trainTable.doubleColumn(i + "x" + j).append(image.pixels[((i - 1) * 28) + j - 1]);

            trainTable.intColumn("label").append(image.label);
        }

        for (String id : testIds) {
            MnistImage image = getImage(testDb, id);
            for (int i = 1; i <= 28; i++)
                for (int j = 1; j <= 28; j++)
                    testTable.doubleColumn(i + "x" + j).append(image.pixels[((i - 1) * 28) + j - 1]);

            testTable.intColumn("label").append(image.label);
        }

        System.out.println("super");
        System.out.println(trainTable.sampleN(1));

        return new Pair<>(trainTable.smile().nominalDataset("label"), testTable.smile().nominalDataset("label"));
    }

    @Override
    public void addImage(double[] pixels, int label) {
        Map<String, Object> map = new HashMap<String, Object>();
        String key = "";
        String value = "";
        key += "label,";
        value += label + ",";
        for (int i = 1; i <= 28; i++)
            for (int j = 1; j <= 28; j++) {
                key += i + "x" + j + ((i == 28 && j == 28) ? "" : ",");
                value += (int) pixels[((i - 1) * 28) + j - 1] + ((i == 28 && j == 28) ? "" : ",");
            }

        System.out.println(key);
        System.out.println(value);

        map.put(key, value);
        trainDb.create(map);
    }

    private MnistImage getImage(CouchDbConnector connector, String id) {
        Map map = connector.find(Map.class, id);
        map.remove("_id");
        map.remove("_rev");
        String row = "";
        for (Object obj : map.keySet()) row = map.get(obj).toString();

        int label = Integer.parseInt(row.substring(0, 1));
        row = row.substring(2, row.length());

        String[] split = row.split(",");
        double[] pixels = new double[split.length];
        for (int i = 0; i < split.length; i++) pixels[i] = Double.parseDouble(split[i]);
        return new MnistImage(pixels, label);
    }
}
