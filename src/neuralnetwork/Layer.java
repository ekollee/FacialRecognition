package neuralnetwork;

import java.util.ArrayList;

/**
 * Project: Assign1
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 18 February, 2016
 */

public class Layer {

    ArrayList<Neuron> neurons = new ArrayList<Neuron>();

    public Layer(int count) {
        for (int i = 0; i < count; i++) {
            neurons.add(new Neuron());
        }
    }

    public int count() {
        return neurons.size();
    }

}