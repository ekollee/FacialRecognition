package neuralnetwork;

import java.util.Random;

/**
 * Project: Assign1
 * Name: Graham Burgsma
 * Created on 01 March, 2016
 */

//This class is used to have consistent results over multiple runs by using a seed value and only one Random generator
public class RandomGenerator {
    private static RandomGenerator ourInstance = new RandomGenerator();
    public Random random;

    private RandomGenerator() {
        random = new Random();
    }

    public static RandomGenerator getInstance() {
        return ourInstance;
    }

    public void createNewRandom() {
        random = new Random(6);
    }
}
