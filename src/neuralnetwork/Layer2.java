package neuralnetwork;

import java.util.ArrayList;

/**
 * Project: Assign1
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 18 February, 2016
 */

public class Layer2 {

    ArrayList<Neuron2> neurons = new ArrayList<Neuron2>();

    public Layer2(int count) {
        for (int i = 0; i < count; i++) {
            neurons.add(new Neuron2());
        }
    }

    public int count() {
        return neurons.size();
    }

}