import neuralnetwork.NeuralNetwork;
import sun.applet.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Project: FacialRecognition
 * Name: Eric
 * Created on 20 April, 2016
 */

public class FacialRecognition {

    public FacialRecognition() {
        NeuralNetwork.getInstance().printStatistics();

    }







    public static void main(String[] args) {
        new FacialRecognition();
    }
}
