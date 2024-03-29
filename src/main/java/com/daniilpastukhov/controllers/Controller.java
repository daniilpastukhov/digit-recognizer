package com.daniilpastukhov.controllers;

import com.daniilpastukhov.Main;
import com.daniilpastukhov.models.Model;
import com.daniilpastukhov.database.NoSqlDatabase;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * JavaFX UI Controller.
 */
public class Controller {
    private NoSqlDatabase database;
    private Model model;
    private final int LINE_WIDTH = 7; // Width of the drawn line
    private Boolean verbose = false; // Prints more information if set to true

    /**
     * Constructor to pass model and db to controller.
     *
     * @param model Trained model.
     * @param db    Initialized database.
     */
    public Controller(Model model, NoSqlDatabase db) {
        this.model = model;
        database = db;
    }

    @FXML
    private Canvas canvas;
    public Button clearButton;
    public Button predictButton;
    public Text predictionLabel;
    public Button retrainButton;
    public TextField correctDigitField;

    /**
     * Fills canvas with black color when application launches.
     */
    @FXML
    void initialize() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 150, 150);
    }

    /**
     * Predicts digit and sets predicted digit to the prediction label.
     *
     * @param actionEvent Event.
     */
    public void predictDigit(ActionEvent actionEvent) {
        double[] pixels = getPixels();

        if (verbose) {
            System.out.println(Arrays.toString(pixels));
            for (int y = 0; y < 28; y++) {
                for (int x = 0; x < 28; x++) {
                    System.out.printf("%3d ", (int) pixels[(y * 28) + x]);
                }
                System.out.println();
            }
            System.out.println(model.getModel().predict(pixels));
        }

        predictionLabel.setText("Prediciton: " + model.getModel().predict(pixels));
    }

    /**
     * Strokes on canvas after mouse button press.
     *
     * @param mouseEvent Event.
     */
    public void handleCanvasPress(MouseEvent mouseEvent) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(LINE_WIDTH);
        gc.setStroke(Color.WHITE);
        gc.beginPath();
        gc.moveTo(mouseEvent.getX(), mouseEvent.getY());
        gc.stroke();
    }

    /**
     * Strokes on canvas while mouse button is pressed.
     *
     * @param mouseEvent Event.
     */
    public void handleCanvasDrag(MouseEvent mouseEvent) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(LINE_WIDTH);
        gc.setStroke(Color.WHITE);
        gc.lineTo(mouseEvent.getX(), mouseEvent.getY());
        gc.stroke();
    }

    /**
     * Clears canvas after mouse right button click.
     *
     * @param mouseEvent Event.
     */
    public void handleCanvasClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            predictionLabel.setText("Prediction: ?");
            clear();
        }
    }

    /**
     * Clears canvas and clears prediction label.
     */
    public void clear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 150, 150);
        predictionLabel.setText("Prediction: ?");
    }

    /**
     * Actualize model's weights with new image and add new image to database.
     *
     * @param actionEvent Event.
     */
    public void retrainWithNewImage(ActionEvent actionEvent) {
        double[] pixels = getPixels();
        int label = Integer.parseInt(correctDigitField.getText());
        if (verbose) System.out.println(label);

        database.addImage(pixels, label);
        model.update(pixels, label);
        Main.serialize("model.ser", model);
    }

    /**
     * Scales image from "CANVAS_WIDTH"x"CANVAS_HEIGHT" to 28x28.
     *
     * @param canvas Canvas.
     * @return Scaled image.
     */
    private BufferedImage getScaledImage(Canvas canvas) {
        int CANVAS_WIDTH = 150;
        int CANVAS_HEIGHT = 150;
        WritableImage writableImage = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.snapshot(null, writableImage);
        Image tmp = SwingFXUtils.fromFXImage(writableImage, null).getScaledInstance(28, 28, Image.SCALE_DEFAULT);
        BufferedImage scaledImg = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = scaledImg.getGraphics();
        graphics.drawImage(tmp, 0, 0, null);
        graphics.dispose();
        return scaledImg;
    }

    /**
     * Returns array of 784 elements, each one corresponds to a pixel of the drawn scaled image.
     *
     * @return Array of double values with size 784.
     */
    private double[] getPixels() {
        BufferedImage img = getScaledImage(canvas);
        double[] pixels = new double[28 * 28];
        int index = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                pixels[index++] = img.getRGB(x, y) & 0xFF;
            }
        }

        return pixels;
    }
}
