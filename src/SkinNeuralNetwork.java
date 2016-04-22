import neuralnetwork.NeuralNetwork;

import java.util.List;

/**
 * Project: FacialRecognition
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 22 April, 2016
 */
public class SkinNeuralNetwork extends NeuralNetwork {

    public SkinNeuralNetwork(List<String[]> trainingSet, int epochs, double learningRate, double momentum, boolean tanhFunction, List<Integer> hiddenLayerSize) {
        super(trainingSet, epochs, learningRate, momentum, tanhFunction, hiddenLayerSize);
    }

    public boolean isRGBSkin(int red, int green, int blue) {
        String [] sample = {Double.toString(red/255.0), Double.toString(green/255.0), Double.toString(blue/255.0)};

        return getClassification(sample).equals("1");
    }
}
