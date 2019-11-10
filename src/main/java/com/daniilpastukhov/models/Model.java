package com.daniilpastukhov.models;

import javafx.util.Pair;
import smile.classification.NeuralNetwork;
import smile.data.AttributeDataset;
import smile.validation.ConfusionMatrix;

import java.io.IOException;
import java.io.Serializable;

/**
 * Model to predict digits.
 */

public class Model implements Serializable {
    private NeuralNetwork model;

    public Model() {
        this.model = null;
    }

    /**
     * Trains model.
     *
     * @param datasets Pair containing train and test datasets. The key is train dataset, the value is test dataset.
     * @return Trained model.
     * @throws IOException If reading dataset wasn't successful.
     */
    public Model train(Pair<AttributeDataset, AttributeDataset> datasets) throws IOException {
        AttributeDataset trainDf = datasets.getKey();
        AttributeDataset testDf = datasets.getValue();

        model = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, 784, 10);
        model.setLearningRate(0.4);
        model.setMomentum(0.9);

        int epochs = 20;
        System.out.println("> Starting learning process with " + epochs + " epochs.");
        for (int k = 1; k <= epochs; k++) {
            model.learn(trainDf.x(), trainDf.labels());
            System.out.println("> > Epoch " + k);
        }

        model.learn(trainDf.x(), trainDf.labels());

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
    public void update(double[] pixels, int label) {
        model.learn(pixels, label);
    }

    // Getters and setters

    public NeuralNetwork getModel() {
        return model;
    }

    public void setModel(NeuralNetwork model) {
        this.model = model;
    }
}
