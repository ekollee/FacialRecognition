import neuralnetwork.DataReader;
import neuralnetwork.DataSample;
import neuralnetwork.NeuralNetwork2;

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

    public void detectSkin() {
        SkinNeuralNetwork neuralNetwork = SkinNeuralNetwork.getInstance();
        NeuralNetwork2.Results results = neuralNetwork.runNetwork(10, 5, DataReader.readSkinData());

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
}
