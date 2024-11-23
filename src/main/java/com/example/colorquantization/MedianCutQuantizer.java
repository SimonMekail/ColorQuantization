package com.example.colorquantization;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianCutQuantizer {

    private static class ColorCube {
        private int minX, minY, minZ, maxX, maxY, maxZ;
        private final List<Color> colors = new ArrayList<>();

        public ColorCube(List<Color> colors) {
            int[] minValues = {255, 255, 255};
            int[] maxValues = {0, 0, 0};

            for (Color color : colors) {
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                if (r < minValues[0]) minValues[0] = r;
                if (g < minValues[1]) minValues[1] = g;
                if (b < minValues[2]) minValues[2] = b;

                if (r > maxValues[0]) maxValues[0] = r;
                if (g > maxValues[1]) maxValues[1] = g;
                if (b > maxValues[2]) maxValues[2] = b;

                this.colors.add(color);
            }

            this.minX =minValues[0];
            this.minY = minValues[1];
            this.minZ = minValues[2];
            this.maxX = maxValues[0];
            this.maxY = maxValues[1];
            this.maxZ = maxValues[2];
        }

        public int getRangeIndex() {
            int rRange = maxX - minX;
            int gRange = maxY - minY;
            int bRange = maxZ - minZ;

            if (rRange >= gRange && rRange >= bRange) {
                return 0;
            } else if (gRange >= rRange && gRange >= bRange) {
                return 1;
            } else {
                return 2;
            }
        }

        public List<Color> getColors() {
            return colors;
        }

        public Color getAverageColor() {
            int totalR = 0, totalG = 0, totalB = 0;

            for (Color color : colors) {
                totalR += color.getRed();
                totalG += color.getGreen();
                totalB += color.getBlue();
            }

            int size = colors.size();

            int avgR = totalR / size;
            int avgG = totalG / size;
            int avgB = totalB / size;

            return new Color(avgR, avgG, avgB);
        }

        public List<ColorCube> splitCube() {
            int rangeIndex = getRangeIndex();

            List<Color> sortedColors = new ArrayList<>(colors);
            Collections.sort(sortedColors, (c1, c2) -> {
                if (rangeIndex == 0) {
                    return Integer.compare(c1.getRed(), c2.getRed());
                } else if (rangeIndex == 1) {
                    return Integer.compare(c1.getGreen(), c2.getGreen());
                } else {
                    return Integer.compare(c1.getBlue(), c2.getBlue());
                }
            });
            int midIndex = sortedColors.size() / 2;

            List<Color> colors1 = sortedColors.subList(0, midIndex);
            List<Color> colors2 = sortedColors.subList(midIndex, sortedColors.size());

            ColorCube cube1 = new ColorCube(colors1);
            ColorCube cube2 = new ColorCube(colors2);

            List<ColorCube> result = new ArrayList<>();
            result.add(cube1);
            result.add(cube2);

            return result;
        }
    }

    public static List<Color> quantize(BufferedImage image, int numColors) {
        List<Color> result = new ArrayList<>();

        List<Color> allColors = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                allColors.add(color);
            }
        }

        ColorCube rootCube =new ColorCube(allColors);
        List<ColorCube> cubes = new ArrayList<>();
        cubes.add(rootCube);

        while (cubes.size() < numColors) {
            ColorCube cubeToSplit = null;
            double maxVolume = 0;

            for (ColorCube cube : cubes) {
                int volume = (cube.maxX - cube.minX + 1) * (cube.maxY - cube.minY + 1) * (cube.maxZ - cube.minZ + 1);

                if (volume > maxVolume) {
                    maxVolume = volume;
                    cubeToSplit = cube;
                }
            }

            cubes.remove(cubeToSplit);
            List<ColorCube> newCubes = cubeToSplit.splitCube();
            cubes.addAll(newCubes);
        }

        for (ColorCube cube : cubes) {
            result.add(cube.getAverageColor());
        }

        return result;
    }

    public static List<Color> getQuantizedColors(BufferedImage image, int numColors) {
        List<Color> quantizedColors = quantize(image, numColors);
        return quantizedColors;
    }

    public static Color findClosestColor(Color color, List<Color> colors) {
        // Find the color in the list of quantized colors that is closest to the original color
        Color closestColor = null;
        double minDistance = Double.MAX_VALUE;

        for (Color c : colors) {
            double distance = getDistance(color, c);

            if (distance < minDistance) {
                minDistance = distance;
                closestColor = c;
            }
        }

        return closestColor;
    }

    private static double getDistance(Color c1, Color c2) {
        // Compute the Euclidean distance between two colors in RGB space
        double rdiff = c1.getRed() - c2.getRed();
        double gdiff = c1.getGreen() - c2.getGreen();
        double bdiff = c1.getBlue() - c2.getBlue();

        return Math.sqrt(rdiff * rdiff + gdiff * gdiff + bdiff * bdiff);
    }

}