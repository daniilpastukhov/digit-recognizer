package com.daniilpastukhov.database;

import javafx.util.Pair;
import smile.data.AttributeDataset;

import java.io.IOException;

/**
 * Interface for NoSQL database.
 */

public interface NoSqlDatabase {
    /**
     * Establish connection with the database.
     *
     * @return Database instance.
     */
    NoSqlDatabase init();

    /**
     * Load and parse datasets from the database.
     *
     * @return Pair containing train and test datasets.
     * @throws IOException If table/collection doesn't exist.
     */
    Pair<AttributeDataset, AttributeDataset> getDatasets() throws IOException;

    /**
     * Add image to the database.
     *
     * @param pixels 2D matrix containing all pixels.
     * @param label  Digit, which is represented by the image.
     */
    void addImage(double[] pixels, int label);
}
