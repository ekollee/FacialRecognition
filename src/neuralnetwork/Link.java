/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork;

/**
 * @author Eric
 */
public class Link {

    Neuron frontNode;
    Neuron backNode;
    double weight;
    double deltaWeight = 0;

    //  1 / (1 + Math.exp(-value))
    public Link(Neuron backNode, Neuron frontNode, double weight) {
        this.backNode = backNode;
        this.frontNode = frontNode;
        this.weight = weight;
    }

    public void updateWeight(double learningRate, double momentum, boolean tanhFuntion) {
        if (tanhFuntion) {
            deltaWeight = learningRate * frontNode.error * 4 * (Math.pow(Math.cosh(frontNode.sum), 2) / Math.pow(Math.cosh(2 * frontNode.sum) + 1, 2)) * backNode.value + momentum * deltaWeight;
        } else {
            deltaWeight = learningRate * frontNode.error * (Math.exp(frontNode.sum) / Math.pow((1 + Math.exp(frontNode.sum)), 2)) * backNode.value + momentum * deltaWeight;
        }

        weight += deltaWeight;
    }

}
