import neuralnetwork.DataReader;
import neuralnetwork.DataSample;
import neuralnetwork.NeuralNetwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Project: FacialRecognition
 * Name: grahamburgsma
 * Created on 21 April, 2016
 */
public class ImageProcessor {

    private BufferedImage image;

    public ImageProcessor(String imageFileName) {
        try {
            importImage(new File(imageFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importImage(File file) throws IOException {
        image = ImageIO.read(file);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void detectSkin() {
        SkinNeuralNetwork neuralNetwork = SkinNeuralNetwork.getInstance();
        NeuralNetwork.Results results = neuralNetwork.runNetwork(10, 5, DataReader.readSkinData());

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


    public void fillHoles(int searchColour, int fillColour) {
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
        if (currentSize >= 250)
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
        CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
        edgeDetector.setSourceImage(image);
        edgeDetector.process();
        return edgeDetector.getEdgesImage();
    }

    public void saveImage(BufferedImage image, String fileName) {
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
