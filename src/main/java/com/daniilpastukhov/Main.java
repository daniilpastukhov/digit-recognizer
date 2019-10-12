package com.daniilpastukhov;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.*;

/**
 * Main class that launches JavaFX UI and occasionally ANN learning.
 */

public class Main extends Application {
    BooleanProperty ready = new SimpleBooleanProperty(false);

    private void longStart() {
        //simulate long init in background
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int max = 10;
                for (int i = 1; i <= max; i++) {
                    Thread.sleep(1000);
                    // Send progress to preloader
                    notifyPreloader(new Preloader.ProgressNotification(((double) i)/max));
                }
                // After init is ready, the app is ready to be shown
                // Do this before hiding the preloader stage to prevent the
                // app from exiting prematurely
                ready.setValue(Boolean.TRUE);

                notifyPreloader(new Preloader.StateChangeNotification(
                        Preloader.StateChangeNotification.Type.BEFORE_START));

                return null;
            }
        };
        new Thread(task).start();
    }


    /**
     * Trains model or load it in case it was trained already, then renders UI.
     * @param primaryStage Primary stage.
     * @throws IOException If training was't successful.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        longStart();
        primaryStage.setScene(new Scene(new Label("Application started"),
                400, 400));

        ready.addListener(new ChangeListener<Boolean>(){
            public void changed(
                    ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (Boolean.TRUE.equals(t1)) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            File yourFile = new File("model.ser");
                            Model model = null;
                            Database database = new Database().init();

                            if (!yourFile.exists()) {
                                try {
                                    model = trainModel(database);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                model = deserialize("model.ser");
                                model.setDatabase(database);
                            }

                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui.fxml"));

                            Controller controller = new Controller(model, database);
                            fxmlLoader.setController(controller);

                            Parent root = null;
                            try {
                                root = fxmlLoader.load();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            primaryStage.setScene(new Scene(root, 600, 400));
                            primaryStage.setTitle("Digit recognizer");
                            primaryStage.show();
                        }
                    });
                }
            }
        });;

//        File yourFile = new File("model.ser");
//        Model model;
//        Database database = new Database().init();
//
//        if (!yourFile.exists()) {
////            Group root = new Group();
////            Scene trainingScene = new Scene(root, 150, 100);
////            primaryStage.setScene(trainingScene);
////            primaryStage.setTitle("Training neural network");
////
////            ProgressBar progressBar = new ProgressBar(0);
////            HBox hb = new HBox();
////            hb.setSpacing(5);
////            hb.setAlignment(Pos.CENTER);
////            hb.getChildren().addAll(progressBar);
////            progressBar.setProgress(0.25);
////            trainingScene.setRoot(hb);
////            primaryStage.show();
//
//            model = trainModel(database);
////            progressBar.setProgress(1);
//        } else {
//            model = deserialize("model.ser");
//            model.setDatabase(database);
//        }
//
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui.fxml"));
//
//        Controller controller = new Controller(model, database);
//        fxmlLoader.setController(controller);
//
//        Parent root = fxmlLoader.load();
//
//        primaryStage.setScene(new Scene(root, 600, 400));
//        primaryStage.setTitle("Digit recognizer");
//        primaryStage.show();
    }

    /**
     * Serializes object.
     * @param path Where to serialize object.
     * @param cls Object to serialize.
     */
    static <T> void serialize(String path, T cls) {
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
     * @param database Initialized database instance.
     * @return Model with set database.
     * @throws IOException If parsing was unsuccessful.
     */
    private Model trainModel(Database database) throws IOException {
        Model model = new Model().train();
        serialize("model.ser", model);
        model.setDatabase(database);
        return model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
