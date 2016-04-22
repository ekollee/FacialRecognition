package neuralnetwork;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Eric
 */
public class NeuralNetwork {

    public static NeuralNetwork INSTANCE;

    double correct;
    double testingCorrect;

    double learningRate;
    double momentum;
    boolean tanhFunction;

    List<String> possibleOutputs;
    List<Layer> layers;
    List<String[]> trainingSet;
    List<String[]> testingSet;
    List<Double> trainingPerformance;
    List<Double> testingPerformance;

    private NeuralNetwork(List<String[]> trainingSet, int epochs, double learningRate, double momentum, boolean tanhFunction, List<Integer> hiddenLayerSize) {

        this.trainingSet = new ArrayList<>();
        this.trainingSet.addAll(trainingSet);
        this.learningRate = learningRate;
        this.momentum = momentum;
        this.tanhFunction = tanhFunction;

        testingSet = new ArrayList<>();
        testingPerformance = new ArrayList<>();
        trainingPerformance = new ArrayList<>();
        possibleOutputs = new ArrayList<>();
        for (String[] example : this.trainingSet) {
            if (!possibleOutputs.contains(example[example.length - 1])) {
                possibleOutputs.add(example[example.length - 1]);   //Find all the classifications

            }
        }

        initializeNetwork(hiddenLayerSize);

        Collections.shuffle(this.trainingSet);
        double trainingSetHalf = this.trainingSet.size() * 0.5;
        for (int j = 0; j < (int) trainingSetHalf; j++) {
            testingSet.add(this.trainingSet.remove(j));
        }

        for (int i = 0; i < epochs; i++) {
            correct = 0;
            testingCorrect = 0;


            Collections.shuffle(this.trainingSet);
            Collections.shuffle(testingSet);
            for (String[] example : this.trainingSet) {
                if (forwardPass(example))
                    correct++;
                backPropError(example[example.length - 1]);
                updateWeights();
            }

            for (String[] example : this.testingSet) {
                if (forwardPass(example))
                    testingCorrect++;
            }


            double accuracy = correct / this.trainingSet.size();
            double testingAccuracy = testingCorrect / testingSet.size();

            testingPerformance.add(testingAccuracy);
            trainingPerformance.add(accuracy);

        }


    }

    public static NeuralNetwork getInstance() {
        if (INSTANCE == null) {
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
            INSTANCE = ANN;
        }
        return INSTANCE;
    }

    private void initializeNetwork(List<Integer> hiddenLayerSize) {
        layers = new ArrayList<>();

        layers.add(new Layer(trainingSet.get(0).length - 1)); //Input Layer
        for (int i = 0; i < hiddenLayerSize.size(); i++) {
            layers.add(new Layer(hiddenLayerSize.get(i))); //Hidden Layers
        }

        Layer outputLayer = new Layer(possibleOutputs.size());
        for (int i = 0; i < possibleOutputs.size(); i++) {
            outputLayer.neurons.get(i).classification = possibleOutputs.get(i);
        }

        layers.add(outputLayer); //Add output layer

        for (int i = 0; i < layers.size() - 1; i++) {
            for (Neuron backNode : layers.get(i).neurons) {
                for (Neuron frontNode : layers.get(i + 1).neurons) {
                    double randomWeight = randomDouble(-0.5, 0.5);
                    frontNode.links.add(new Link(backNode, frontNode, randomWeight));
                }
            }
        }

    }

    private boolean forwardPass(String[] example) {
        for (int i = 0; i < layers.get(0).neurons.size(); i++) {
            layers.get(0).neurons.get(i).value = Double.parseDouble(example[i]); //Set value of input layer
        }

        for (int i = 1; i < layers.size(); i++) {
            for (Neuron neuron : layers.get(i).neurons) {
                neuron.updateValue(tanhFunction);
            }
        }

        return Collections.max(layers.get(layers.size() - 1).neurons, new Neuron()).classification.equals(example[example.length - 1]);

    }

    private void backPropError(String expectedClassification) {
        boolean result = Collections.max(layers.get(layers.size() - 1).neurons, new Neuron()).classification.equals(expectedClassification);

        for (Neuron outputNeuron : layers.get(layers.size() - 1).neurons) {
            if (outputNeuron.classification.equals(expectedClassification)) {
                //Expected to fire, error = 1-Neuron output
                outputNeuron.error = (1 - outputNeuron.value);
            } else {
                //Not expected to fire, error = 0 - Neuron Output
                outputNeuron.error = (0 - outputNeuron.value);
            }
        }

        for (int i = layers.size() - 1; i > 0; i--) {
            for (Neuron neuron : layers.get(i).neurons) {
                for (Link link : neuron.links) {
                    link.backNode.error += link.weight * link.frontNode.error;
                }
            }
        }
        //   return result;

    }

    private void updateWeights() {
        for (Layer layer : layers) {
            for (Neuron neuron : layer.neurons) {
                for (Link link : neuron.links) {
                    //Update all weights
                    link.updateWeight(learningRate, momentum, tanhFunction);
                }
            }
        }
    }

    public void printStatistics() {
        System.out.println("Learning Rate: " + learningRate);
        System.out.println("Momentum Rate: " + momentum);
        System.out.println(tanhFunction ? "Tanh" : "Sigmoid");
        System.out.println("Training Data:");
        for (int i = 0; i < trainingPerformance.size(); i++) {
            System.out.println("Epoch: " + i + " Accuracy: " + trainingPerformance.get(i) * 100);
        }
        System.out.println();
        System.out.println("Testing Data:");
        for (int i = 0; i < testingPerformance.size(); i++) {
            System.out.println("Epoch: " + i + " Accuracy: " + testingPerformance.get(i) * 100);
        }
        System.out.println("Best Training Result: " + Collections.max(trainingPerformance));
        System.out.println("Best Testing Result: " + Collections.max(testingPerformance));
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

    private static double randomDouble(double min, double max) {
        Random r = new Random();
        return min + (max - min) * r.nextDouble();
    }

}
