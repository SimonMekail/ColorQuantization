package com.example.colorquantization;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class PopularityColorQuantization {

    public static BufferedImage quantize(BufferedImage image, int numColors) {
        // Create a frequency map to count the occurrence of each color
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                if (frequencyMap.containsKey(rgb)) {
                    frequencyMap.put(rgb, frequencyMap.get(rgb) + 1);
                } else {
                    frequencyMap.put(rgb, 1);
                }
            }
        }

        // Use a priority queue to sort colors by frequency
        PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(Map.Entry.comparingByValue());
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            queue.add(entry);
        }

        // Take the numColors most frequent colors
        Map<Integer, Color> colorMap = new HashMap<>();
        for (int i = 0; i < numColors; i++) {
            Map.Entry<Integer, Integer> entry = queue.poll();
            Color color = new Color(entry.getKey());
            colorMap.put(entry.getKey(), color);
        }

        // Create a list of the colors in the color map
        List<Color> colors = new ArrayList<>(colorMap.values());

        // Replace each color in the image with the closest color in the color map
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                Color closestColor = null;
                double minDistance = Double.MAX_VALUE;
                for (Color c : colorMap.values()) {
                    double distance = getDistance(color, c);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestColor = c;
                    }
                }
                result.setRGB(x, y, closestColor.getRGB());
            }
        }

        // Return the quantized image and the list of colors
        return result;
    }

    private static double getDistance(Color c1, Color c2) {
        double redDiff = c1.getRed() - c2.getRed();
        double greenDiff = c1.getGreen() - c2.getGreen();
        double blueDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    public static List<Color> getQuantizedColors(BufferedImage image, int numColors) {
        // Create a frequency map to count the occurrence of each color
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                if (frequencyMap.containsKey(rgb)) {
                    frequencyMap.put(rgb, frequencyMap.get(rgb) + 1);
                } else {
                    frequencyMap.put(rgb, 1);
                }
            }
        }

        // Use a priority queue to sort colors by frequency
        PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(Map.Entry.comparingByValue());
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            queue.add(entry);
        }

        // Take the numColors most frequent colors
        Map<Integer, Color> colorMap = new HashMap<>();
        for (int i = 0; i < numColors; i++) {
            Map.Entry<Integer, Integer> entry = queue.poll();
            Color color = new Color(entry.getKey());
            colorMap.put(entry.getKey(), color);
        }

        // Create a list of the colors in the color map
        List<Color> colors = new ArrayList<>(colorMap.values());

        return colors;
    }
}