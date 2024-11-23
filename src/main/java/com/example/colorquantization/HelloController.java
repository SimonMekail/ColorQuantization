package com.example.colorquantization;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.awt.*;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import java.awt.Graphics2D;
import java.awt.image.IndexColorModel;
import java.util.ResourceBundle;
import java.util.Set;

import static com.example.colorquantization.MedianCutQuantizer.findClosestColor;

public class HelloController implements Initializable {
    File selectedFile;
    private Stage stage;
    private Scene scene;
    private Parent root;
    List<Color> usedColors;
    int colorNum;
    @FXML
    private ImageView orginalImage;
    @FXML
    private Slider myslider;
    @FXML
    private ImageView quantizationImage;
    @FXML
    private ImageView indexedImage;
    @FXML
    private ChoiceBox<String> choiceBox ;
    @FXML
    private Label time;
    @FXML private Label label;
    private final String[] choices = {"Kmean","Popularity","MedianCut"};

    @FXML
    protected void selectImage(ActionEvent event) throws IOException {

        orginalImage.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        selectedFile = fileChooser.showOpenDialog(stage);

        Image image = new Image(selectedFile.toURI().toString());
        orginalImage.setImage(image);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)  {

            colorNum = (int) myslider.getValue();
            label.setText(""+colorNum);

            myslider.valueProperty().addListener(new ChangeListener<Number>() {

                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {

                    colorNum = (int) myslider.getValue();
                    label.setText(""+colorNum);
                    try {
                        getChoices();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        choiceBox.getItems().addAll(choices);
        choiceBox.setOnAction(event -> {
            try {
                getChoices();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void getChoices() throws IOException {

        String choice = choiceBox.getValue();

        quantizationImage.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        if (choice.equals("Kmean")){
            long startTime = System.nanoTime();

            BufferedImage kMeanImage = ImageIO.read(new File(selectedFile.toURI()));
            KMeanColorQuantizer kMeanColorQuantizerquantizer = new KMeanColorQuantizer(kMeanImage, colorNum);
            BufferedImage quantizedImage = kMeanColorQuantizerquantizer.quantize();
            ImageIO.write(quantizedImage, "jpg", new File("Output.jpg"));
            Image kMeanOutput = new Image(new FileInputStream("Output.jpg"));
            quantizationImage.setImage(kMeanOutput);
            usedColors = kMeanColorQuantizerquantizer.getQuantizedColors();

            long duration = (System.nanoTime() - startTime)/1000000;
            time.setText("" + duration + "ms");

        }

        if (choice.equals("Popularity")){
            long startTime = System.nanoTime();


            BufferedImage popularityImage = ImageIO.read(new File(selectedFile.toURI()));
            BufferedImage outputImage = PopularityColorQuantization.quantize(popularityImage, colorNum);

            ImageIO.write(outputImage, "jpg", new File("Output.jpg"));
            Image popularityImageOutput = new Image(new FileInputStream("Output.jpg"));
            quantizationImage.setImage(popularityImageOutput);

            usedColors = PopularityColorQuantization.getQuantizedColors(outputImage,colorNum);

            long duration = (System.nanoTime() - startTime)/1000000;
            time.setText("" + duration + "ms");

        }

        if (choice.equals("MedianCut")) {
            long startTime = System.nanoTime();

            BufferedImage medianCutImage = ImageIO.read(new File(selectedFile.toURI()));

            List<Color> quantizedColors = MedianCutQuantizer.quantize(medianCutImage, colorNum);

            BufferedImage quantizedImage = new BufferedImage(
                    medianCutImage.getWidth(), medianCutImage.getHeight(), BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < medianCutImage.getHeight(); y++) {
                for (int x = 0; x < medianCutImage.getWidth(); x++) {
                    Color originalColor = new Color(medianCutImage.getRGB(x, y));
                    Color quantizedColor = findClosestColor(originalColor, quantizedColors);

                    quantizedImage.setRGB(x, y, quantizedColor.getRGB());
                }
            }

            ImageIO.write(quantizedImage, "jpg", new File("Output.jpg"));
            Image medianCutOutput = new Image(new FileInputStream("Output.jpg"));
            quantizationImage.setImage(medianCutOutput);
            usedColors = MedianCutQuantizer.getQuantizedColors(medianCutImage, colorNum);

            long duration = (System.nanoTime() - startTime)/1000000;
            time.setText("" + duration + "ms");
        }

    }
    @FXML
    protected void convertToIndexedImage (ActionEvent event) throws IOException {

        BufferedImage image = ImageIO.read(new File("Output.jpg"));

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        result.getGraphics().drawImage(image,0,0,null);

        ImageIO.write(result, "jpg", new File("indexed.jpg"));

        Image indexed_Image_Show = new Image(new FileInputStream("indexed.jpg"));
        indexedImage.setImage(indexed_Image_Show);
    }
    @FXML
    protected void saveIndexedImage(ActionEvent event) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = new File("indexed.jpg");

        byte[] imageData = new byte[(int) selectedFile.length()];
        FileInputStream input = new FileInputStream(selectedFile);
        input.read(imageData);
        input.close();

        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile != null) {

            FileOutputStream output = new FileOutputStream(saveFile);
            output.write(imageData);
            output.close();
        }
    }
    @FXML
    protected void displayColorPallete(ActionEvent event) throws IOException {

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        for (int i = 0; i < usedColors.size(); i++) {
            Color color = usedColors.get(i);
            Rectangle rectangle = new Rectangle(50, 50, javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
            gridPane.add(rectangle, i % 4, i / 4);
        }

        Stage colorPalleteStage = new Stage();
        Scene scene = new Scene(gridPane);
        colorPalleteStage.setScene(scene);
        colorPalleteStage.show();
    }
    @FXML
    protected void displayColorHistogram(ActionEvent event) throws FileNotFoundException {

        int NUM_BINS = 256;
        Image image = new Image(new FileInputStream("Output.jpg"));
        PixelReader pixelReader = image.getPixelReader();

        int[] redHistogram = new int[NUM_BINS];
        int[] greenHistogram = new int[NUM_BINS];
        int[] blueHistogram = new int[NUM_BINS];

        for (int y = 0; y < image.getHeight(); y++) {

            for (int x = 0; x < image.getWidth(); x++) {

                javafx.scene.paint.Color color = pixelReader.getColor(x, y);
                int redIndex = (int) (color.getRed() * (NUM_BINS - 1));
                int greenIndex = (int) (color.getGreen() * (NUM_BINS - 1));
                int blueIndex = (int) (color.getBlue() * (NUM_BINS - 1));
                redHistogram[redIndex]++;
                greenHistogram[greenIndex]++;
                blueHistogram[blueIndex]++;
            }
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> redChart = new BarChart<>(xAxis, yAxis);
        BarChart<String, Number> greenChart = new BarChart<>(xAxis, yAxis);
        BarChart<String, Number> blueChart = new BarChart<>(xAxis, yAxis);

        redChart.setTitle("Red Histogram");
        greenChart.setTitle("Green Histogram");
        blueChart.setTitle("Blue Histogram");

        XYChart.Series<String, Number> redSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> greenSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> blueSeries = new XYChart.Series<>();

        for (int i = 0; i < NUM_BINS; i++) {
            redSeries.getData().add(new XYChart.Data<>(String.valueOf(i), redHistogram[i]));
            greenSeries.getData().add(new XYChart.Data<>(String.valueOf(i), greenHistogram[i]));
            blueSeries.getData().add(new XYChart.Data<>(String.valueOf(i), blueHistogram[i]));
        }

        redChart.getData().add(redSeries);
        greenChart.getData().add(greenSeries);
        blueChart.getData().add(blueSeries);


        Stage colorHistogramStage = new Stage();
        Scene scene = new Scene(new javafx.scene.layout.HBox(redChart, greenChart, blueChart), 800, 600);
        colorHistogramStage.setTitle("Color Histogram");
        colorHistogramStage.setScene(scene);
        colorHistogramStage.show();
    }
    @FXML
    protected void switchToSearch(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("search.fxml"));

        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void exit(ActionEvent event) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setContentText("");

        if (alert.showAndWait().get() == ButtonType.OK){
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.close();
        }
    }
}

