
package com.inaoe.rna.view;

import com.inaoe.rna.Hopfield;
import com.inaoe.rna.utils.TSPUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

public class RootLayoutController implements Initializable {

    @FXML
    private Label lLengthHopfield;

    @FXML
    private Label lIsValid;

    @FXML
    private Label lLengthNearest;

    @FXML
    private Label lLengthOptimal;

    @FXML
    private Pane pHopfield;

    @FXML
    private Pane pNearest;

    @FXML
    private Pane pOptimal;

    @FXML
    private TextField tfN;

    @FXML
    private TextField tfA;

    @FXML
    private TextField tfB;

    @FXML
    private TextField tfC;

    @FXML
    private TextField tfD;

    @FXML
    private TextField tfNPrime;

    @FXML
    private Label lTimeHopfield;

    @FXML
    private Label lTimeNearest;

    @FXML
    private Label lTimeOptimal;

    private final int paddingPane = 30;
    private final float radiusNode = 7;
    private List<double[]> coordinates;
    private List<int[]> coordsNorm;
    private Stage stage;
    private Canvas canvasHopfield;
    private Canvas canvasNearest;
    private Canvas canvasOptimal;
    private Hopfield hopfield;
    private double A;
    private double B;
    private double C;
    private double D;
    private int n;
    private double nPrime;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initHopfield();
        initNearest();
        initOptimal();
    }

    public void initHopfield() {
        canvasHopfield = new Canvas(pHopfield.getWidth(), pHopfield.getHeight());
        pHopfield.getChildren().add(canvasHopfield);
    }

    public void initNearest() {
        canvasNearest = new Canvas(pNearest.getWidth(), pNearest.getHeight());
        pNearest.getChildren().add(canvasNearest);
    }

    public void initOptimal() {
        canvasOptimal = new Canvas(pOptimal.getWidth(), pOptimal.getHeight());
        pOptimal.getChildren().add(canvasOptimal);
    }

    private void drawNodesHopfield() {
        clearCanvasHopfield(canvasHopfield);
        initHopfield();

        GraphicsContext gc = canvasHopfield.getGraphicsContext2D();
        drawNodes(gc);
    }

    private void drawNodesNearest() {
        clearCanvasHopfield(canvasNearest);
        initNearest();

        GraphicsContext gc = canvasNearest.getGraphicsContext2D();
        drawNodes(gc);
    }

    private void drawNodesOptimal() {
        clearCanvasHopfield(canvasOptimal);
        initOptimal();

        GraphicsContext gc = canvasOptimal.getGraphicsContext2D();
        drawNodes(gc);
    }

    private void drawNodes(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        for (int[] coordinate : coordsNorm) {
            gc.fillOval(coordinate[0], coordinate[1], radiusNode, radiusNode);
        }
    }

    private void clearCanvasHopfield(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getWidth());
    }

    @FXML
    public void onActionbGenerate() {
        setParameters();
        coordinates = TSPUtils.generateInstance(n);

        double xMin = coordinates.get(0)[0];
        double xMax = coordinates.get(0)[0];
        double yMin = coordinates.get(0)[1];
        double yMax = coordinates.get(0)[1];
        for (double[] coordinate : coordinates) {
            if (coordinate[0] < xMin) {
                xMin = coordinate[0];
            }
            if (coordinate[0] > xMax) {
                xMax = coordinate[0];
            }
            if (coordinate[1] < yMin) {
                yMin = coordinate[1];
            }
            if (coordinate[1] > yMax) {
                yMax = coordinate[1];
            }
        }
        coordsNorm = TSPUtils.normNodes(coordinates, xMin, xMax, yMin, yMax,
                (int) pHopfield.getWidth() - paddingPane * 2, (int) pHopfield.getHeight() - paddingPane * 2);

        int h = (int) pHopfield.getHeight() - paddingPane;
        for (int[] coordinate : coordsNorm) {
            coordinate[0] += paddingPane;
            coordinate[1] = h - coordinate[1];
        }
        drawNodesHopfield();
        drawNodesNearest();
        drawNodesOptimal();
    }

    private void setParameters() {
        this.A = Double.parseDouble(tfA.getText());
        this.B = Double.parseDouble(tfB.getText());
        this.C = Double.parseDouble(tfC.getText());
        this.D = Double.parseDouble(tfD.getText());
        this.n = Integer.parseInt(tfN.getText());
        this.nPrime = Double.parseDouble(tfNPrime.getText());
    }

    @FXML
    public void onActionbComputeHopfield() {
        drawNodesHopfield();
        var graph = TSPUtils.getAdjacencyMatrix(coordinates);

        hopfield = new Hopfield(n, graph);
        hopfield.setConstants(A, B, C, D, nPrime);

        Instant start = Instant.now();
        var tuple = hopfield.force();
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        int[] tour = (int[]) tuple[0];
        boolean isValid = (boolean) tuple[1];
        printTour(tour, canvasHopfield);
        var fitness = TSPUtils.fitnessFunction(graph, tour);
        lLengthHopfield.setText(String.format("Length: %.3f", fitness));
        lIsValid.setText("Valid: " + isValid);
        lTimeHopfield.setText(String.format("Time: %d millis", timeElapsed));
    }

    @FXML
    public void onActionbComputeNearest() {
        drawNodesNearest();

        var graph = TSPUtils.getAdjacencyMatrix(coordinates);

        Instant start = Instant.now();
        var tour = TSPUtils.nearestNeighbor(graph);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        printTour(tour, canvasNearest);

        var fitness = TSPUtils.fitnessFunction(graph, tour);
        lLengthNearest.setText(String.format("Length: %.3f", fitness));
        lTimeNearest.setText(String.format("Time: %d millis", timeElapsed));
    }

    @FXML
    public void onActionbComputeOptimal() {
        drawNodesOptimal();

        var graph = TSPUtils.getAdjacencyMatrix(coordinates);

        Instant start = Instant.now();
        var tour = TSPUtils.solveExact(graph);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        printTour(tour, canvasOptimal);

        var fitness = TSPUtils.fitnessFunction(graph, tour);
        lLengthOptimal.setText(String.format("Length: %.3f", fitness));
        lTimeOptimal.setText(String.format("Time: %d millis", timeElapsed));
    }

    @FXML
    public void onActionbComputeRandom() {
        drawNodesOptimal();

        var graph = TSPUtils.getAdjacencyMatrix(coordinates);

        Instant start = Instant.now();
        var tour = TSPUtils.randomTour(n);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        printTour(tour, canvasOptimal);

        var fitness = TSPUtils.fitnessFunction(graph, tour);
        lLengthOptimal.setText(String.format("Length: %.3f", fitness));
        lTimeOptimal.setText(String.format("Time: %d millis", timeElapsed));
    }

    private void printTour(int[] tour, Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int i = 0;
        for (; i < tour.length - 1; i++) {
            gc.strokeLine(coordsNorm.get(tour[i])[0] + radiusNode / 2, coordsNorm.get(tour[i])[1] + radiusNode / 2,
                    coordsNorm.get(tour[i + 1])[0] + radiusNode / 2, coordsNorm.get(tour[i + 1])[1] + radiusNode / 2);
        }
        gc.strokeLine(coordsNorm.get(tour[i])[0] + radiusNode / 2, coordsNorm.get(tour[i])[1] + radiusNode / 2,
                coordsNorm.get(tour[0])[0] + radiusNode / 2, coordsNorm.get(tour[0])[1] + radiusNode / 2);
    }

    @FXML
    public void onActionbConvergence() throws IOException {
        double[] convergence = hopfield.getEnergy().stream().mapToDouble(i -> i).toArray();
        SineGraph frame = new SineGraph(convergence);
        frame.setVisible(true);
    }
}
