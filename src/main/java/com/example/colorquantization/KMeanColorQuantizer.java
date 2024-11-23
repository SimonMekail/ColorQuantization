package com.example.colorquantization;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KMeanColorQuantizer {
    private BufferedImage image;
    private int numColors;
    private List<Color> centroids;
    private List<Color> quantizedColors;

    public KMeanColorQuantizer(BufferedImage image, int numColors) {
        this.image = image;
        this.numColors = numColors;
        this.centroids = new ArrayList<>();
        this.quantizedColors = new ArrayList<>();
    }

    public BufferedImage quantize() {
        List<Color> pixels = getPixels();

        // Initialize the centroids with random colors
        Random random = new Random();
        for (int i = 0; i < numColors; i++) {
            Color centroid = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            centroids.add(centroid);
        }

        // Run the k-means algorithm to find the cluster centroids
        boolean converged = false;
        while (!converged) {
            // Assign each pixel to the nearest centroid
            List<List<Color>> clusters = new ArrayList<>();
            for (int i = 0; i < numColors; i++) {
                clusters.add(new ArrayList<>());
            }
            for (Color pixel : pixels) {
                int nearestCentroidIndex =getNearestCentroidIndex(pixel);
                clusters.get(nearestCentroidIndex).add(pixel);
            }

            // Update the centroids based on the mean color of each cluster
            List<Color> newCentroids = new ArrayList<>();
            for (int i = 0; i < numColors; i++) {
                List<Color> cluster = clusters.get(i);
                if (cluster.isEmpty()) {
                    newCentroids.add(centroids.get(i));
                } else {
                    int redSum = 0;
                    int greenSum = 0;
                    int blueSum = 0;
                    for (Color pixel : cluster) {
                        redSum += pixel.getRed();
                        greenSum += pixel.getGreen();
                        blueSum += pixel.getBlue();
                    }
                    int redMean = redSum / cluster.size();
                    int greenMean = greenSum / cluster.size();
                    int blueMean = blueSum / cluster.size();
                    Color newCentroid = new Color(redMean, greenMean, blueMean);
                    newCentroids.add(newCentroid);
                }
            }

            // Check if the centroids have converged
            converged = true;
            for (int i = 0; i < numColors; i++) {
                if (!newCentroids.get(i).equals(centroids.get(i))){
                    converged = false;
                    break;
                }
            }

            // Update the centroids
            centroids = new ArrayList<>(newCentroids);
        }

        // Replace each pixel with the color of its nearest centroid
        BufferedImage quantizedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x, y));
                int nearestCentroidIndex = getNearestCentroidIndex(pixel);
                Color nearestCentroid = centroids.get(nearestCentroidIndex);
                quantizedImage.setRGB(x, y, nearestCentroid.getRGB());

                // Add the quantized color to the list of colors
                if (!quantizedColors.contains(nearestCentroid)) {
                    quantizedColors.add(nearestCentroid);
                }
            }
        }

        return quantizedImage;
    }

    public List<Color> getQuantizedColors() {
        return quantizedColors;
    }

    private List<Color> getPixels() {
        List<Color> pixels = new ArrayList<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixels.add(new Color(image.getRGB(x, y)));
            }
        }
        return pixels;
    }

    private int getNearestCentroidIndex(Color pixel) {
        int nearestCentroidIndex = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < numColors; i++) {
            Color centroid = centroids.get(i);
            double distance = getDistance(pixel, centroid);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestCentroidIndex = i;
            }
        }
        return nearestCentroidIndex;
    }

    private double getDistance(Color c1, Color c2) {
        double redDiff = c1.getRed() - c2.getRed();
        double greenDiff = c1.getGreen() - c2.getGreen();
        double blueDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }
}