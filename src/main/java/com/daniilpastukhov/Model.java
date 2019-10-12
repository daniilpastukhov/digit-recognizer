package com.daniilpastukhov;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import smile.classification.NeuralNetwork;
import smile.data.AttributeDataset;
import smile.validation.ConfusionMatrix;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.json.JsonReadOptions;
import tech.tablesaw.io.json.JsonReader;

import java.io.*;
import java.util.ArrayList;

/**
 * Model to predict digits.
 */

class Model implements Serializable {
    NeuralNetwork model;
    Database database;
    private final int epochs = 10;

    /**
     * Default constructor.
     */
    Model() {
        this.model = null;
        this.database = null;
    }

    /**
     * Sets database.
     *
     * @param database Initialized database.
     */
    void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * Trains model.
     *
     * @return Trained model.
     * @throws IOException If reading dataset wasn't successful.
     */
    Model train() throws IOException {
        System.out.println("> Creating datasets.");
//        AttributeDataset trainDf = getDatasetFromCsv("data/mnist_train.csv");
//        AttributeDataset testDf = getDatasetFromCsv("data/mnist_test.csv");
        AttributeDataset trainDf = getDataset(database.trainCollection);
        AttributeDataset testDf = getDataset(database.testCollection);
        System.out.println("> Datasets were created.");

        model = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, 784, 10);
        model.setLearningRate(0.4);
        model.setMomentum(0.5);

        System.out.println("> Starting learning process with " + epochs + " epochs.");
        for (int k = 0; k < epochs; k++) {
            model.learn(trainDf.x(), trainDf.labels());
            System.out.println("> > Epoch " + k);
        }

        System.out.println("> Learning process was finished.");

        System.out.println("> Starting evaluating process.");

        int[] pred = new int[testDf.labels().length];
        int correct = 0;
        int wrong = 0;

        for (int k = 0; k < testDf.labels().length; k++) {
            pred[k] = model.predict(testDf.x()[k]);
        }

        for (int k = 0; k < pred.length; k++) {
            if (testDf.labels()[k] == pred[k]) correct += 1;
            else wrong += 1;
        }

        System.out.println("> > Metrics: ");
        System.out.println("> > > Correct: " + correct + ", wrong: " + wrong);
        System.out.println("> > > Accuracy: " + 1.0 * correct / (correct + wrong));

        System.out.println("> Evaluating process was finished.");

        ConfusionMatrix confusionMatrix = new ConfusionMatrix(testDf.labels(), pred);
        System.out.println("> Confusion matrix:");
        System.out.println(confusionMatrix.toString());
        System.out.println();

        return this;
    }

    /**
     * Actualizes weights with new image.
     *
     * @param pixels 1d pixel array of size 784.
     * @param label  Image correct label.
     */
    void update(double[] pixels, int label) {
        model.learn(pixels, label);
    }

    /**
     * Parses dataset from json to Table format.
     *
     * @param collection MongoDB collection with data.
     * @return Dataset with nominal label.
     * @throws IOException If parsing wasn't successful.
     */
    private AttributeDataset getDataset(MongoCollection<Document> collection) throws IOException {
        ArrayList<String> rows = new ArrayList<>();

        for (Document doc : collection.find()) {
            rows.add(doc.toJson().replace(doc.toJson().substring(1, 46), ""));
        }

        JsonReader jr = new JsonReader();
        Table table = jr.read(JsonReadOptions.builderFromString("[" + String.join(",", rows) + "]").build());
        return table.smile().nominalDataset("label");
    }

    /**
     * Reads dataset from local csv files.
     * For faster debugging process.
     *
     * @param path Path to csv file.
     * @return Dataset with nominal dataset.
     * @throws IOException If file doesn't exist.
     */
    private AttributeDataset getDatasetFromCsv(String path) throws IOException {
        Table table = Table.read().csv(path);
        return table.smile().nominalDataset("label");
    }

    void gridSearch(int[] epochs, double[] learningRate, double[] momentum) {
        // TODO
    }
}
