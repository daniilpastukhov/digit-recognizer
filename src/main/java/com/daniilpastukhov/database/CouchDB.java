package com.daniilpastukhov.database;

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

/**
 * Implementation of CouchDB database functionality.
 */

public class CouchDB implements NoSqlDatabase {
    /**
     * Class, which represents a single image.
     */
    static class MnistImage {
        double[] pixels;
        int label;

        MnistImage(double[] pixels, int label) {
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

    private CouchDbConnector trainDb; // Train database instance
    private CouchDbConnector testDb; // Test database instance

    @Override
    public CouchDB init() {
        System.out.println("> Connecting to database.");
        try {
            HttpClient httpClient = new StdHttpClient.Builder()
                    .url("http://0.0.0.0:5984")
                    .username("admin")
                    .password("admin")
                    .build();

            CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

            System.out.println("> Connected to CouchDB database.");

            trainDb = dbInstance.createConnector("digits_train", false);
            testDb = dbInstance.createConnector("digits_test", false);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return this;
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

        List<String> trainIds = trainDb.getAllDocIds();
        List<String> testIds = testDb.getAllDocIds();

        parseDocuments(trainTable, trainIds, trainDb);
        parseDocuments(testTable, testIds, testDb);

        return new Pair<>(trainTable.smile().nominalDataset("label"), testTable.smile().nominalDataset("label"));
    }

    /**
     * Prepares rows for converting to tablesaw's Table.
     * Deletes ids from the
     *
     * @param table    Table to modify.
     * @param ids      List of ids for all images.
     * @param database Database to get images from.
     */
    private void parseDocuments(Table table, List<String> ids, CouchDbConnector database) {
        for (String id : ids) {
            MnistImage image = getImage(database, id);
            for (int i = 1; i <= 28; i++)
                for (int j = 1; j <= 28; j++)
                    table.doubleColumn(i + "x" + j).append(image.pixels[((i - 1) * 28) + j - 1]);

            table.intColumn("label").append(image.label);
        }
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

        map.put(key.toString(), value.toString());
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
