import neuralnetwork.NeuralNetwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public void detectSkin() {
        NeuralNetwork ANN = NeuralNetwork.getInstance();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                String [] sample = {Double.toString(getRed(rgb)/255.0), Double.toString(getGreen(rgb)/255.0), Double.toString(getBlue(rgb)/255.0)};
                if (ANN.getClassification(sample).equals("1")){
                    image.setRGB(x, y, Color.white.getRGB());
                } else {
                    image.setRGB(x, y, Color.black.getRGB());
                }
            }
        }

        displayImage(image);
    }

    public void displayImage(BufferedImage image) {
        File outputfile = new File("testOutput.jpg");
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

    private static ArrayList readData(String filename) throws IOException {
        BufferedReader reader;
        String inputString;
        String splitChar = ",";
        ArrayList<String[]> result = new ArrayList<>();

        reader = new BufferedReader(new FileReader(filename));
        while ((inputString = reader.readLine()) != null) {

            String[] data = inputString.split(splitChar);
            result.add(data);
        }

        reader.close();

        return result;
    }
}
