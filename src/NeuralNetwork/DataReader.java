package neuralnetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Project: Assign1
 * Name: Graham Burgsma
 * Created on 03 March, 2016
 */
public class DataReader {


    public static ArrayList<DataSample> readSkinData() {
        Scanner scanner;

        ArrayList<DataSample> dataArrayList = new ArrayList<>();

        try {
            scanner = new Scanner(new File("Skin_NonSkin.csv"));

            while (scanner.hasNext()) {
                DataSample dataSample = new DataSample();

                String[] dataString = scanner.next().split(",");

                for (int i = 0; i < dataString.length - 1; i++) {
                    dataSample.data.add(Double.parseDouble(dataString[i]) / 255);
                }

                dataSample.classification = dataString[dataString.length - 1];

                dataArrayList.add(dataSample);
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return dataArrayList;
    }
}
