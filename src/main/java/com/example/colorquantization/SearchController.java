package com.example.colorquantization;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static com.example.colorquantization.MedianCutQuantizer.findClosestColor;

public class SearchController {

    private Stage stage;
    private Scene scene;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField folderField;
    @FXML
    private TextField imageField;
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private ImageView matchedImageView;
    @FXML
    private Label score;
    @FXML
    private DatePicker date;
    @FXML
    private TextField size;
    File selectedFile;
    List<File> imageFiles = new ArrayList<>();

    HashMap<String, Double> imagesWithScore;
    ArrayList<java.awt.Color> selectedColors = new ArrayList<>();

    List<String> matchedImages;

    double initialMouseX, initialMouseY;
    double selectionStartX, selectionStartY;
    private ImageView imageCropView;
    private Rectangle selectionRectangle;

    int index = 0;

    @FXML
    protected void switchToImageQuantization(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void selectFolder(ActionEvent event) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File selectedFolder = directoryChooser.showDialog(stage);
        folderField.setText(selectedFolder.getAbsolutePath());

        File[] files = selectedFolder.listFiles();

        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif")) {
                imageFiles.add(file);
            }
        }
        System.out.println(imageFiles);
    }

    @FXML
    protected void restFiles(ActionEvent event) {

        imageFiles = new ArrayList<>();
    }

    @FXML
    protected void selectImage(ActionEvent event) {
        matchedImageView.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        selectedFile = fileChooser.showOpenDialog(stage);
        imageField.setText(selectedFile.getName());

    }

    @FXML
    protected void saveColors(ActionEvent event) {
        Color fxColor = colorPicker.getValue();

        int red = (int) (fxColor.getRed() * 255);
        int green = (int) (fxColor.getGreen() * 255);
        int blue = (int) (fxColor.getBlue() * 255);

        java.awt.Color color = new java.awt.Color(red, green, blue);
        selectedColors.add(color);

        System.out.println(selectedColors);
    }

    @FXML
    protected void restColors(ActionEvent event) {

        selectedColors = new ArrayList<>();

    }

    @FXML
    protected void seeColors(ActionEvent event) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        for (int i = 0; i < selectedColors.size(); i++) {
            java.awt.Color color = selectedColors.get(i);
            Rectangle rectangle = new Rectangle(50, 50, javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
            gridPane.add(rectangle, i % 4, i / 4);
        }

        Stage colorPalleteStage = new Stage();
        Scene scene = new Scene(gridPane);
        colorPalleteStage.setScene(scene);
        colorPalleteStage.show();

    }


    @FXML
    protected void search(ActionEvent event) throws IOException {

        imagesWithScore = new HashMap<>();
        matchedImages = new ArrayList<>();

        BufferedImage medianCutImage = ImageIO.read(new File(selectedFile.toURI()));

        List<java.awt.Color> quantizedColors = MedianCutQuantizer.quantize(medianCutImage, 32);

        BufferedImage quantizedImage = new BufferedImage(
                medianCutImage.getWidth(), medianCutImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < medianCutImage.getHeight(); y++) {
            for (int x = 0; x < medianCutImage.getWidth(); x++) {
                java.awt.Color originalColor = new java.awt.Color(medianCutImage.getRGB(x, y));
                java.awt.Color quantizedColor = findClosestColor(originalColor, quantizedColors);

                quantizedImage.setRGB(x, y, quantizedColor.getRGB());
            }
        }

        for (int i = 0; i < imageFiles.size(); i++) {

            BufferedImage medianCutImage1 = ImageIO.read(new File(imageFiles.get(i).toURI()));

            List<java.awt.Color> quantizedColors1 = MedianCutQuantizer.quantize(medianCutImage, 32);

            BufferedImage quantizedImage1 = new BufferedImage(
                    medianCutImage1.getWidth(), medianCutImage1.getHeight(), BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < medianCutImage1.getHeight(); y++) {
                for (int x = 0; x < medianCutImage1.getWidth(); x++) {
                    java.awt.Color originalColor = new java.awt.Color(medianCutImage1.getRGB(x, y));
                    java.awt.Color quantizedColor = findClosestColor(originalColor, quantizedColors1);

                    quantizedImage1.setRGB(x, y, quantizedColor.getRGB());
                }
            }

            imagesWithScore.put(imageFiles.get(i).toURI().getPath(), calcSimilarityScore
                    (calcLuminosityHistogram(quantizedImage), calcLuminosityHistogram(quantizedImage1)));

            System.out.println(imagesWithScore);
        }

        for (Map.Entry<String, Double> entry : imagesWithScore.entrySet()) {
            if (entry.getValue() >= 50) {
                matchedImages.add(entry.getKey());
            }
        }
        Image Image_Show = new Image(new FileInputStream(matchedImages.get(0)));
        matchedImageView.setImage(Image_Show);
        score.setText("" + imagesWithScore.get(matchedImages.get(0)));
    }

    @FXML
    protected void next(ActionEvent event) throws FileNotFoundException {


        if (index + 1 < matchedImages.size()) {
            index = index + 1;
            Image Image_Show = new Image(new FileInputStream(matchedImages.get(index)));
            matchedImageView.setImage(Image_Show);
            score.setText("" + imagesWithScore.get(matchedImages.get(index)));
        }
    }

    @FXML
    protected void previous(ActionEvent event) throws FileNotFoundException {


        if (index - 1 >= 0) {
            index = index - 1;
            Image Image_Show = new Image(new FileInputStream(matchedImages.get(index)));
            matchedImageView.setImage(Image_Show);

            score.setText("" + imagesWithScore.get(matchedImages.get(index)));
        }
    }


    @FXML
    protected void searchByColor(ActionEvent event) throws IOException {

        imagesWithScore = new HashMap<>();
        matchedImages = new ArrayList<>();

        for (int i = 0; i < imageFiles.size(); i++) {

            BufferedImage image = ImageIO.read(new File(imageFiles.get(i).toURI()));

            List<java.awt.Color> otherColors = MedianCutQuantizer.quantize(image,16);

            imagesWithScore.put(imageFiles.get(i).toURI().getPath(),calcSimilarityScore
                    (calcLuminosityHistogramColor(selectedColors), calcLuminosityHistogramColor(otherColors)));

            System.out.println(imagesWithScore.values());
        }


        for (Map.Entry<String, Double> entry : imagesWithScore.entrySet()) {
            if (entry.getValue() > 0) {
                matchedImages.add(entry.getKey());
            }
        }

        Image Image_Show = new Image(new FileInputStream(matchedImages.get(0)));
        matchedImageView.setImage(Image_Show);
        score.setText("" + imagesWithScore.get(matchedImages.get(0)));

    }

    @FXML
    protected void crop() {

        BorderPane root = new BorderPane();
        Pane imagePane = new Pane();
        imageCropView = new ImageView();
        imagePane.getChildren().add(imageCropView);
        root.setCenter(imagePane);

        Scene scene = new Scene(root, 800, 600);
        Stage primaryStage = new Stage();
        primaryStage.setScene(scene);

        Image image = new Image(selectedFile.toURI().toString());
        imageCropView.setImage(image);

        imageCropView.setOnMousePressed(event -> {
            initialMouseX = event.getX();
            initialMouseY = event.getY();
            selectionStartX = initialMouseX;
            selectionStartY = initialMouseY;
            if (selectionRectangle == null) {
                selectionRectangle = new Rectangle(initialMouseX, initialMouseY, 0, 0);
                selectionRectangle.setFill(null);
                selectionRectangle.setStrokeWidth(1);
                selectionRectangle.setStroke(javafx.scene.paint.Color.BLACK);
                imagePane.getChildren().add(selectionRectangle);
            } else {
                selectionRectangle.setX(initialMouseX);
                selectionRectangle.setY(initialMouseY);
                selectionRectangle.setWidth(0);
                selectionRectangle.setHeight(0);
            }
        });

        imageCropView.setOnMouseDragged(event -> {
            double x = Math.min(event.getX(), initialMouseX);
            double y = Math.min(event.getY(), initialMouseY);
            double width = Math.abs(event.getX() - initialMouseX);
            double height = Math.abs(event.getY() - initialMouseY);
            selectionRectangle.setX(x);
            selectionRectangle.setY(y);
            selectionRectangle.setWidth(width);
            selectionRectangle.setHeight(height);
        });


        imageCropView.setOnMouseReleased(event -> {
            if (event.getX() != initialMouseX && event.getY() != initialMouseY) {
                double x = Math.min(event.getX(), initialMouseX);
                double y = Math.min(event.getY(), initialMouseY);
                double width = Math.abs(event.getX() - initialMouseX);
                double height = Math.abs(event.getY() - initialMouseY);
                Rectangle2D viewport = new Rectangle2D(x, y, width, height);
                imageCropView.setViewport(viewport);
                selectionRectangle.setX(0);
                selectionRectangle.setY(0);
                selectionRectangle.setWidth(0);
                selectionRectangle.setHeight(0);

                try {
                    PixelReader pixelReader = imageCropView.getImage().getPixelReader();
                    WritableImage croppedImage = new WritableImage(pixelReader,
                            (int) viewport.getMinX(), (int) viewport.getMinY(),
                            (int) viewport.getWidth(), (int) viewport.getHeight());
                    File output = new File(System.getProperty("user.home"), "Desktop/cropped_image.png");
                    ImageIO.write(SwingFXUtils.fromFXImage(croppedImage, null), "png", output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                imagePane.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
            }
        });
        primaryStage.show();

    }


    @FXML
    protected void resize(ActionEvent event) throws IOException {

        BufferedImage originalImage = ImageIO.read(selectedFile);
        int newWidth = Integer.parseInt(width.getText());
        int newHeight = Integer.parseInt(height.getText());
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        ImageIO.write(resizedImage, "jpg", new File("resized.jpg"));


    }

    @FXML
    protected void filter(ActionEvent event) throws IOException {

        ArrayList<File> filteredImages = new ArrayList<>();

        for (File file : imageFiles) {
            LocalDate modifiedDate = LocalDate.ofEpochDay(file.lastModified() / 86400000L); // Convert milliseconds since epoch to days since epoch
            long fileSize = file.length();

            if (modifiedDate.isAfter(date.getValue()) && fileSize > Long.parseLong(size.getCharacters().toString())) {
                System.out.println(file.getName() + "  " + fileSize + "  bytes   " + modifiedDate);
                filteredImages.add(file);
            }
        }
        imageFiles = filteredImages;
        System.out.println(imageFiles);
    }



    public static int[] calcLuminosityHistogram(BufferedImage image) {
        int[] hist = new int[256];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                java.awt.Color color = new java.awt.Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                hist[lum]++;
            }
        }
        return hist;
    }

    public static double calcSimilarityScore(int[] hist1, int[] hist2) {
        double score = 0;
        for (int i = 0; i < hist1.length; i++) {
            score += Math.min(hist1[i], hist2[i]);
        }
        score /= Math.min(sum(hist1), sum(hist2));
        score *= 100;
        return score;
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static int[] calcLuminosityHistogramColor(List<java.awt.Color> colors) {
        int[] hist = new int[256];
        for (java.awt.Color color : colors) {
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            hist[lum]++;
        }
        return hist;
    }

}