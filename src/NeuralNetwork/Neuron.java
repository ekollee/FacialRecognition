package neuralnetwork;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Project: Assign1
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 18 February, 2016
 */

public class Neuron implements Comparator<Neuron> {

    public ArrayList<Edge> edges;
    public double value, error;
    public String classification;

    public Neuron() {
        edges = new ArrayList<>();
    }

    public double inputWeightedSum() {
        double sum = 0;

        for (Edge edge : edges) {
            sum += edge.neuronFront.value * edge.weight;
        }
        return sum;
    }

    public void connect(ArrayList<Neuron> neurons) {
        for (Neuron neuron : neurons) {
            edges.add(new Edge(neuron));
        }
    }

    @Override
    public int compare(Neuron o1, Neuron o2) {
        return Double.compare(o1.value, o2.value);
    }
}
