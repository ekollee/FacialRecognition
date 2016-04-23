import neuralnetwork.DataReader;
import neuralnetwork.DataSample;
import neuralnetwork.NeuralNetwork2;

import java.util.ArrayList;

/**
 * Project: FacialRecognition
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 22 April, 2016
 */
public class SkinNeuralNetwork extends NeuralNetwork2 {

    private static final int INPUT_NODES = 3;
    private static final int HIDDEN_NODES = 8;
    private static final int HIDDEN_LAYERS = 1;
    private static final int OUTPUT_NODES = 2;
    private static final double LEARNING_RATE = 0.002;
    private static final double MOMENTUM_RATE = 0.5;
    private static final ActivationFunction ACTIVATION_FUNCTION = ActivationFunction.LOGISTIC;

    private static SkinNeuralNetwork ourInstance = new SkinNeuralNetwork(INPUT_NODES, HIDDEN_NODES, HIDDEN_LAYERS, OUTPUT_NODES, LEARNING_RATE, MOMENTUM_RATE, DataReader.readSkinData(), ACTIVATION_FUNCTION);

    public SkinNeuralNetwork(int inputCount, int hiddenCount, int hiddenLayersCount, int outputCount, double learningRate, double momentumRate, ArrayList<DataSample> dataArrayList, NeuralNetwork2.ActivationFunction activationFunction) {
        super(inputCount, hiddenCount, hiddenLayersCount, outputCount, learningRate, momentumRate, dataArrayList, activationFunction);
    }

    public static SkinNeuralNetwork getInstance() {
        return ourInstance;
    }

}
