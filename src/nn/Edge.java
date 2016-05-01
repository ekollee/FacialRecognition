package neuralnetwork;

/**
 * Project: Assign1
 * Name: Graham Burgsma
 * Created on 22 February, 2016
 */

public class Edge {

    public double weight;
    Neuron neuronFront;

    public Edge(Neuron neuronFront) {
        this.neuronFront = neuronFront;

        this.weight = RandomGenerator.getInstance().random.doubles(-0.5, 0.5).iterator().nextDouble();
    }
}
