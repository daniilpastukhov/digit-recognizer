package com.daniilpastukhov;

import com.daniilpastukhov.controllers.Controller;
import com.daniilpastukhov.database.CouchDB;
import com.daniilpastukhov.database.MongoDB;
import com.daniilpastukhov.database.NoSqlDatabase;
import com.daniilpastukhov.database.RethinkDB;
import com.daniilpastukhov.models.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

/**
 * Main class that launches JavaFX UI and occasionally ANN learning.
 */

public class Main extends Application {
    /**
     * Trains model or load it in case it was trained already, then renders UI.
     *
     * @param primaryStage Primary stage.
     * @throws IOException If training was't successful.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        final String mongoUrl = "mongodb+srv://%61%64%6d%69%6e:%61%64%6d%69%6e@digit-classification-l3sse.mongodb.net/test?retryWrites=true&w=majority";
        NoSqlDatabase db = new MongoDB(mongoUrl).init();
//        NoSqlDatabase db = new CouchDB().init();
//        NoSqlDatabase db = new RethinkDB().init();

        File modelPath = new File("model.ser");
        Model model;

        if (!modelPath.exists()) model = trainModel(db);
        else model = deserialize("model.ser");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui.fxml"));

        Controller controller = new Controller(model, db);
        fxmlLoader.setController(controller);

        Parent root = fxmlLoader.load();

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("Digit recognizer");
        primaryStage.show();
    }

    /**
     * Serializes object.
     *
     * @param <T>  Any serializable class type.
     * @param path Where to serialize object.
     * @param cls  Object to serialize.
     */
    public static <T> void serialize(String path, T cls) {
        try {
            File yourFile = new File(path);
            yourFile.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(yourFile, false);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(cls);
            out.close();
            fileOut.close();
            System.out.println("> Serialized object is saved as " + path + ".");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * Deserializes object.
     *
     * @param path Path of serialized object.
     * @return Deserialized object.
     */
    private static <T> T deserialize(String path) {
        T obj = null;
        try {
            FileInputStream file = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(file);
            obj = (T) in.readObject();
            in.close();
            file.close();
        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        }

        return obj;
    }

    /**
     * Trains and serialises model as 'model.ser'.
     *
     * @param database Initialized database instance.
     * @return Model with set database.
     * @throws IOException If parsing was unsuccessful.
     */
    private Model trainModel(NoSqlDatabase database) throws IOException {
        Model model = new Model();
        model = model.train(database.getDatasets());
        serialize("model.ser", model);
        return model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
