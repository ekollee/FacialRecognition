package neuralnetwork;

import java.util.ArrayList;

/**
 * Project: Assign1
 * Name: Graham Burgsma
 * Created on 03 March, 2016
 */

public class DataSample {
    public ArrayList<Double> data = new ArrayList<>();
    public String classification;

    @Override
    public String toString() {
        return data.toString();
    }
}