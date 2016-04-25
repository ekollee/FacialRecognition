import neuralnetwork.DataReader;
import neuralnetwork.DataSample;
import neuralnetwork.NeuralNetwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

/**
 * Project: FacialRecognition
 * Name: grahamburgsma
 * Created on 21 April, 2016
 */
public class ImageProcessor {

    private BufferedImage image, originalImage;

    public ImageProcessor(String imageFileName) {
        try {
            importImage(new File(imageFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void importImage(File file) throws IOException {
        image = ImageIO.read(file);
        originalImage = ImageIO.read(file);
    }

    BufferedImage getImage() {
        return image;
    }

    void detectSkin() {
        SkinNeuralNetwork neuralNetwork = SkinNeuralNetwork.getInstance();
        NeuralNetwork.Results results = neuralNetwork.runNetwork(5, 2, DataReader.readSkinData());

        neuralNetwork.printResults(results);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                DataSample dataSample = new DataSample();
                dataSample.data.add(getRed(rgb) / 255.0);
                dataSample.data.add(getGreen(rgb) / 255.0);
                dataSample.data.add(getBlue(rgb) / 255.0);

                if (neuralNetwork.forwardPass(dataSample.data).equals("1")) {
                    image.setRGB(x, y, Color.white.getRGB());
                } else {
                    image.setRGB(x, y, Color.black.getRGB());
                }
            }
        }

        saveImage(image, "blackWhite.jpg");
    }


    void fillHoles(int searchColour, int fillColour) {
        boolean visited[][] = new boolean[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (searchColour == image.getRGB(x, y) && !visited[y][x]) {
                    explore(0, y, x, visited, searchColour, fillColour);
                }
            }
        }
    }


    private boolean explore(int currentSize, int y, int x, boolean visited[][], int searchColour, int fillColour) {
        if (currentSize >= 100)
            return false;

        int rowNbr[] = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
        int colNbr[] = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

        visited[y][x] = true;
        currentSize++;

        for (int k = 0; k < 8; ++k)
            if (isSafe(y + rowNbr[k], x + colNbr[k], visited, searchColour)) {
                if (explore(currentSize, y + rowNbr[k], x + colNbr[k], visited, searchColour, fillColour))
                    image.setRGB(x, y, fillColour);
                else {
                    image.setRGB(x, y, searchColour);
                    return false;
                }
            }
        image.setRGB(x, y, fillColour);
        return true;
    }

    private boolean isSafe(int y, int x, boolean visited[][], int searchColour) {
        return (y >= 0) && (y < image.getHeight()) &&
                (x >= 0) && (x < image.getWidth()) &&
                (image.getRGB(x, y) == searchColour && !visited[y][x]);
    }

    private BufferedImage edgeDetection() {
        BufferedImage greyImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage edgeImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        colorConvert.filter(originalImage, greyImage);

        saveImage(greyImage, "greyImage.jpg");

        int count, averageVertical, averageHorizontal;
        int arrayVertical[] = {1, 2, 1, 0, 0, 0, -1, -2, -1};
        int arrayHorizontal[] = {1, 0, -1, 2, 0, -2, 1, 0, -1};

        for (int x = 0; x < greyImage.getWidth(); x++) {
            for (int y = 0; y < greyImage.getHeight(); y++) {
                count = 0;
                averageVertical = 0;
                averageHorizontal = 0;
                for (int i = x - 1; i <= x + 1; i++) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (j > 0 && j < greyImage.getHeight() && i > 0 && i < greyImage.getWidth()) {
                            averageVertical += getRed(greyImage.getRGB(i, j)) * arrayVertical[count];
                            averageHorizontal += getRed(greyImage.getRGB(i, j)) * arrayHorizontal[count];
                        }
                        count++;
                    }
                }

                averageVertical = averageVertical < 0 ? 0 : averageVertical > 255 ? 255 : averageVertical;
                averageHorizontal = averageHorizontal < 0 ? 0 : averageHorizontal > 255 ? 255 : averageHorizontal;

                int newColor = (int) Math.sqrt(Math.pow(averageVertical, 2) + Math.pow(averageHorizontal, 2));

                newColor = newColor < 0 ? 0 : newColor > 255 ? 255 : newColor;

                edgeImage.setRGB(x, y, new Color(newColor, newColor, newColor).getRGB());
            }
        }

        return edgeImage;
    }

    void overlayEdgeDetectionImage() {
        BufferedImage edgeImage = edgeDetection();

        saveImage(image, "before.jpg");
        saveImage(edgeImage, "edgeBefore.jpg");

        for (int y = 0; y < edgeImage.getHeight(); y++) {
            for (int x = 0; x < edgeImage.getWidth(); x++) {
                if (image.getRGB(x, y) == Color.white.getRGB() && getRed(edgeImage.getRGB(x, y)) > 50) {
                    image.setRGB(x, y, Color.black.getRGB());
                }
            }
        }

        saveImage(image, "middle.jpg");

        fillHoles(Color.black.getRGB(), Color.white.getRGB());
        fillHoles(Color.white.getRGB(), Color.black.getRGB());

        saveImage(image, "after.jpg");
    }

    void saveImage(BufferedImage image, String fileName) {
        File outputfile = new File(fileName);
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private int getBlue(int rgb) {
        return rgb & 0xFF;
    }
}
