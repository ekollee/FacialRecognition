package neuralnetwork;

import com.google.common.collect.Lists;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project: Assign1
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 18 February, 2016
 */

public class NeuralNetwork2 {

    Layer2 inputLayer;
    ArrayList<Layer2> hiddenLayers = new ArrayList<>();
    Layer2 outputLayer;
    ArrayList<Layer2> allLayers = new ArrayList<>();

    private double learningRate, deltaWeight, momentumRate;
    private int epochs, folds, numHiddenNodes, numHiddenLayers, numInputNodes, numOutputNodes;
    private ActivationFunction activationFunction;

    public NeuralNetwork2(int inputCount, int hiddenCount, int hiddenLayersCount, int outputCount, double learningRate, double momentumRate, ArrayList<DataSample> dataArrayList, ActivationFunction activationFunction) {
        this.learningRate = learningRate;
        this.momentumRate = momentumRate;
        this.activationFunction = activationFunction;
        this.numHiddenLayers = hiddenLayersCount;
        this.numInputNodes = inputCount;
        this.numOutputNodes = outputCount;
        this.numHiddenNodes = hiddenCount;

        RandomGenerator.getInstance().createNewRandom();

        for (int i = 0; i < hiddenLayersCount; i++) {
            hiddenLayers.add(new Layer2(hiddenCount));
        }

        inputLayer = new Layer2(inputCount);
        outputLayer = new Layer2(outputCount);

        HashSet<String> classifications = dataArrayList.stream().map(sample -> sample.classification).collect(Collectors.toCollection(HashSet::new));

        ArrayList<String> classificationArrayList = new ArrayList<>(classifications);

        for (int i = 0; i < outputCount; i++) {
            outputLayer.neurons.get(i).classification = classificationArrayList.get(i);
        }

        assembleNetwork();

        allLayers.add(inputLayer);
        allLayers.addAll(hiddenLayers);
        allLayers.add(outputLayer);
    }


    public Results runNetwork(int epochs, int folds, ArrayList<DataSample> dataArrayList) {
        this.epochs = epochs;
        this.folds = folds;

        Collections.shuffle(dataArrayList, RandomGenerator.getInstance().random);

        ArrayList<Results> resultsArrayList = new ArrayList<>(folds);
        ArrayList<DataSample> trainingSet = new ArrayList<>();
        ArrayList<DataSample> testingSet = new ArrayList<>();

        int partitionSize = folds == 1 ? dataArrayList.size() / 2 : dataArrayList.size() / folds;
        List<List<DataSample>> partitions = Lists.partition(dataArrayList, partitionSize);

        for (int i = 0; i < folds; i++) {
            Results results = new Results();
            trainingSet.clear();
            testingSet.clear();

            //Creating testing and training sets for k-fold cross validation
            for (int j = 0; j < partitions.size(); j++) {
                if (i == j) {
                    testingSet.addAll(partitions.get(j));
                } else {
                    trainingSet.addAll(partitions.get(j));
                }
            }

            for (int k = 0; k < epochs; k++) {
                int correct = 0;

                //Training
                for (DataSample dataSample : trainingSet) {
                    forwardPass(dataSample.data);
                    if (checkError(dataSample.classification))
                        correct++;
                    backPropagation();
                }

                results.training.add((double) correct / (double) trainingSet.size());
                correct = 0;

                //Testing
                for (DataSample dataSample : testingSet) {
                    forwardPass(dataSample.data);
                    if (checkError(dataSample.classification))
                        correct++;
                }

                Collections.shuffle(trainingSet, RandomGenerator.getInstance().random);
                Collections.shuffle(testingSet, RandomGenerator.getInstance().random);

                results.testing.add((double) correct / (double) testingSet.size());
            }
            resultsArrayList.add(results);
        }

        return averageResults(resultsArrayList);
    }

    //Take the average from an Arraylist of Results for Cross-validation
    private Results averageResults(ArrayList<Results> results) {
        Results finalResults = new Results();

        for (int i = 0; i < results.size(); i++) {
            if (i == 0) {
                finalResults.training = results.get(i).training;
                finalResults.testing = results.get(i).testing;
            } else {
                for (int j = 0; j < results.get(i).training.size(); j++) {
                    finalResults.training.set(j, finalResults.training.get(j) + results.get(i).training.get(j));
                }
                for (int j = 0; j < results.get(i).testing.size(); j++) {
                    finalResults.testing.set(j, finalResults.testing.get(j) + results.get(i).testing.get(j));
                }
            }
        }

        for (int i = 0; i < finalResults.testing.size(); i++) {
            finalResults.testing.set(i, finalResults.testing.get(i) / (double) (results.size()));
        }

        for (int i = 0; i < finalResults.training.size(); i++) {
            finalResults.training.set(i, finalResults.training.get(i) / (double) (results.size()));
        }

        return finalResults;
    }

    private void assembleNetwork() {
        for (int i = 0; i < hiddenLayers.size(); i++) {
            for (Neuron2 neuron : hiddenLayers.get(i).neurons) {

                if (i == 0) {
                    neuron.connect(inputLayer.neurons);
                } else {
                    neuron.connect(hiddenLayers.get(i - 1).neurons);
                }
            }
        }
        for (Neuron2 neuron : outputLayer.neurons) {
            neuron.connect(hiddenLayers.get(hiddenLayers.size() - 1).neurons);
        }
    }

    public String forwardPass(ArrayList<Double> inputs) {
        //Setup input neurons (not actually neurons, but it makes it easier)
        for (int i = 0; i < inputLayer.neurons.size(); i++) {
            inputLayer.neurons.get(i).value = inputs.get(i);
        }

        //Forward pass, weighted sum for hidden layers and output layer
        for (int i = 1; i < allLayers.size(); i++) {
            for (Neuron2 neuron : allLayers.get(i).neurons) {
                neuron.value = activationFunction(neuron.inputWeightedSum());
            }
        }

        return Collections.max(outputLayer.neurons, new Neuron2()).classification;
    }

    public void backPropagation() {
        //Error backwards
        for (int i = allLayers.size() - 1; i > 0; i--) {
            for (Neuron2 neuron : allLayers.get(i).neurons) {
                for (Edge edge : neuron.edges) {
                    edge.neuronFront.error = edge.weight * neuron.error;
                }
            }
        }

        //Update weights
        for (int i = 1; i < allLayers.size(); i++) {
            for (Neuron2 neuron : allLayers.get(i).neurons) {
                for (Edge edge : neuron.edges) {
                    deltaWeight = learningRate * neuron.error * activationFunctionDerivative(neuron.inputWeightedSum()) * edge.neuronFront.value + momentumRate * deltaWeight;
                    edge.weight += deltaWeight;
                }
            }
        }
    }

    public boolean checkError(String classification) {
        Neuron2 max = Collections.max(outputLayer.neurons, new Neuron2());

        for (Neuron2 neuron : outputLayer.neurons) {
            if (classification.equals(neuron.classification)) {
                neuron.error = 1 - neuron.value;
            } else {
                neuron.error = 0 - neuron.value;
            }
        }

        return max.classification.equals(classification);
    }

    private double activationFunction(double value) {
        switch (activationFunction) {
            case LOGISTIC:
                return sigmoidFunction(value);
            case TANH:
                return tanhFunction(value);
            default:
                return sigmoidFunction(value);
        }
    }

    private double activationFunctionDerivative(double value) {
        switch (activationFunction) {
            case LOGISTIC:
                return sigmoidFunctionDerivative(value);
            case TANH:
                return tanhFunctionDerivative(value);
            default:
                return sigmoidFunctionDerivative(value);
        }
    }

    private double sigmoidFunction(double value) {
        return 1 / (1 + Math.exp(-value));
    }

    private double sigmoidFunctionDerivative(double value) {
        return Math.exp(value) / Math.pow(Math.exp(value) + 1, 2);
    }

    private double tanhFunction(double value) {
        return Math.tanh(value);
    }

    private double tanhFunctionDerivative(double value) {
        return 1 - Math.pow(Math.tanh(value), 2);
    }

    public void printResults(Results results) {
        DecimalFormat df = new DecimalFormat("0.00000");

        System.out.println(" - - Run Complete - - ");
        System.out.println("Epochs: " + epochs);
        System.out.println("Cross-Validation Folds: " + folds);

        System.out.println("Learning Rate: " + learningRate);
        System.out.println("Momentum Rate: " + momentumRate);

        System.out.println("Activation Function: " + activationFunction.toString());

        System.out.println("Hidden Layers: " + numHiddenLayers);
        System.out.println("Hidden Neurons: " + numHiddenNodes);

        System.out.println("Inputs: " + numInputNodes);
        System.out.println("Output Neurons: " + numOutputNodes);

        System.out.println("\nHighest Training Result: " + df.format(Collections.max(results.training)));
        System.out.println("Highest Testing Result: " + df.format(Collections.max(results.testing)));
    }

    public enum ActivationFunction {
        LOGISTIC, TANH
    }

    public class Results {
        ArrayList<Double> training;
        ArrayList<Double> testing;

        public Results() {
            training = new ArrayList<>();
            testing = new ArrayList<>();
        }

        public double[] getTrainingArray() {
            return training.stream().mapToDouble(Double::doubleValue).toArray();
        }

        public double[] getTestingArray() {
            return testing.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }
}
