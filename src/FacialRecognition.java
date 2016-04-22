import neuralnetwork.NeuralNetwork;
import sun.applet.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Project: FacialRecognition
 * Name: Eric
 * Created on 20 April, 2016
 */

public class FacialRecognition {

    public FacialRecognition() {
        List<String[]> trainingSet = new ArrayList<>();
        try {
            trainingSet = readData("Skin_NonSkin.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int epoch = 20;
        double learningRate = .2;
        double momentum = 0;
        boolean tanh = false;
        List<Integer> hiddenLayers = new ArrayList<>();
        hiddenLayers.add(6);

        NeuralNetwork ANN = new NeuralNetwork(trainingSet, epoch, learningRate, momentum, tanh, hiddenLayers);
        ANN.printStatistics();
    }





    private static ArrayList readData(String filename) throws IOException {
        BufferedReader reader;
        String inputString;
        String splitChar = ",";
        ArrayList<String[]> result = new ArrayList<>();

        reader = new BufferedReader(new FileReader(filename));
        while ((inputString = reader.readLine()) != null) {

            // use comma as separator
            String[] data = inputString.split(splitChar);
            result.add(data);

        }

        reader.close();

        return result;
    }

    public static void main(String[] args) {
        new FacialRecognition();
    }
}
