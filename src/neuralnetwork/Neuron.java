/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Eric
 */
public class Neuron implements Comparator<Neuron> {

    double value;
    double error;
    double sum;
    String classification;
    List<Link> links;

    public Neuron() {
        links = new ArrayList<>();
        error = 0;
    }

    public void updateValue(boolean tanhFunction) {
        sum = 0;
        error = 0;
        for (Link link : links) {
            sum += link.backNode.value * link.weight;
        }
        if (tanhFunction) {
            value = Math.tanh(sum);
        } else {
            value = 1 / (1 + Math.exp(-sum));
        }

    }

    @Override
    public int compare(Neuron n1, Neuron n2) {
        return Double.compare(n1.value, n2.value);
    }

}
