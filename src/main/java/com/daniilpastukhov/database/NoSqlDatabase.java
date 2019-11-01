package com.daniilpastukhov.database;

import javafx.util.Pair;
import smile.data.AttributeDataset;

import java.io.IOException;

public interface NoSqlDatabase {
    NoSqlDatabase init();

    void closeConnection();

    Pair<AttributeDataset, AttributeDataset> getDatasets() throws IOException;

    void addImage(double[] pixels, int label);
}
