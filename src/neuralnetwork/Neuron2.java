package neuralnetwork;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Project: Assign1
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 18 February, 2016
 */

public class Neuron2 implements Comparator<Neuron2> {

    public ArrayList<Edge> edges;
    public double value, error;
    public String classification;

    public Neuron2() {
        edges = new ArrayList<>();
    }

    public double inputWeightedSum() {
        double sum = 0;

        for (Edge edge : edges) {
            sum += edge.neuronFront.value * edge.weight;
        }
        return sum;
    }

    public void connect(ArrayList<Neuron2> neurons) {
        for (Neuron2 neuron : neurons) {
            edges.add(new Edge(neuron));
        }
    }

    @Override
    public int compare(Neuron2 o1, Neuron2 o2) {
        return Double.compare(o1.value, o2.value);
    }
}
