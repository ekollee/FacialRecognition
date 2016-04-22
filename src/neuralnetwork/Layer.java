/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric
 */
public class Layer {

    List<Neuron> neurons;

    public Layer(int size) {
        neurons = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            neurons.add(new Neuron());
        }
    }

}
