import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: FacialRecognition
 * Name: erickollee
 * Created on 30 April, 2016
 */
public class EigenFaceFactory {


    private EigenFaceFactory() {

    }


    public static List<EigenFace> generateEigenFaces(int numEigenFaces, int numTrainingFaces, String directory) {
        List<EigenFace> eigenFaces = new ArrayList<>();
        List<BufferedImage> images = new ArrayList<>();
        File dir = new File(directory);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int count = 0;
            for (File child : directoryListing) {
                // Do something with child
                try {
                    images.add(ImageProcessor.importImageAndReturn(child));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                count++;
                if (count == numTrainingFaces)
                    break;
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

        double[][] greyScaleEigenMatrix = createGreyscaleEigenMatrix(images);
        double[][] meanGreyScaleEigenMatrix = meanEigenFace(greyScaleEigenMatrix);
        double[][] adjustedGreyscaleEigenMatrix = adjustedGreyscaleEigenMatrix(greyScaleEigenMatrix, meanGreyScaleEigenMatrix);
        RealMatrix covarianceMatrix = new Array2DRowRealMatrix(multiplicar(adjustedGreyscaleEigenMatrix, transpose(adjustedGreyscaleEigenMatrix)));
        org.apache.commons.math3.linear.SingularValueDecomposition svd = new org.apache.commons.math3.linear.SingularValueDecomposition(covarianceMatrix);
        double[][] normalizedEigenVectors = normalizeEigenVectors(svd.getU().getData());

        int imageSize = (int) Math.sqrt(normalizedEigenVectors.length);
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);

        for (int k = 0; k < numEigenFaces; k++) {


            for (int y = 0; y < imageSize; y++) {
                for (int x = 0; x < imageSize; x++) {
                    int greyScale = (int) normalizedEigenVectors[y * imageSize + x][k];
                    image.setRGB(y, x, new Color(greyScale, greyScale, greyScale).getRGB());
                }
            }
            eigenFaces.add(new EigenFace(image));
        }
        return eigenFaces;
    }

    public static List<EigenFace> loadEigenFaces(String directory){
        List <EigenFace> images = new ArrayList<>();
        File dir = new File(directory);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {

            for (File child : directoryListing) {
                // Do something with child
                try {
                    images.add(new EigenFace(ImageProcessor.importImageAndReturn(child)));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        return images;
    }

    private static double[][] createGreyscaleEigenMatrix(List<BufferedImage> images) {
        double[][] greyscaleMatrix;
        greyscaleMatrix = new double[images.get(0).getHeight() * images.get(0).getWidth()][images.size()];
        for (int i = 0; i < images.get(0).getHeight(); i++) {
            for (int j = 0; j < images.get(0).getWidth(); j++) {
                for (int k = 0; k < images.size(); k++) {
                    greyscaleMatrix[i * images.get(0).getHeight() + j][k] = ImageProcessor.getBlue(images.get(k).getRGB(i, j));
                }
            }
        }
        return greyscaleMatrix;
    }

    private static double[][] meanEigenFace(double[][] greyscaleMatrix) {
        double[][] meanFace = new double[greyscaleMatrix.length][1];

        for (int i = 0; i < greyscaleMatrix.length; i++) {
            int rowSum = 0;
            for (int j = 0; j < greyscaleMatrix[0].length; j++) {
                rowSum += greyscaleMatrix[i][j];
            }
            meanFace[i][0] = rowSum / greyscaleMatrix[0].length;
        }
        return meanFace;
    }

    public static EigenFace meanEigenFace(List<EigenFace> faces) {
        EigenFace meanFace = new EigenFace(new BufferedImage(faces.get(0).image.getWidth(),faces.get(0).image.getHeight(),faces.get(0).image.getType()));
        double [][] averageGreyscale = new double[faces.get(0).image.getWidth()][faces.get(0).image.getHeight()];
        for (int i = 0; i < faces.size(); i++) {
            for (int j = 0; j < faces.get(i).image.getWidth(); j++) {
                for (int k = 0; k < faces.get(i).image.getHeight(); k++) {
                    averageGreyscale[j][k] = ImageProcessor.getBlue(faces.get(i).image.getRGB(j, k));
                }
            }
        }
        for (int i = 0; i < averageGreyscale.length; i++) {
            for (int j = 0; j < averageGreyscale[0].length; j++) {
                averageGreyscale[i][j] /= faces.size();
                meanFace.image.setRGB(i,j,new Color((int)Math.round(averageGreyscale[i][j]), (int)Math.round(averageGreyscale[i][j]), (int)Math.round(averageGreyscale[i][j])).getRGB());

            }

        }


        return meanFace;

    }


    private static double[][] adjustedGreyscaleEigenMatrix(double[][] greyScaleEigenMatrix, double[][] meanGreyScaleEigenMatrix) {
        double[][] adjustedGreyscaleEigenMatrix = new double[greyScaleEigenMatrix.length][greyScaleEigenMatrix[0].length];
        for (int i = 0; i < greyScaleEigenMatrix.length; i++) {
            for (int j = 0; j < greyScaleEigenMatrix[0].length; j++) {
                adjustedGreyscaleEigenMatrix[i][j] = greyScaleEigenMatrix[i][j] - meanGreyScaleEigenMatrix[i][0];
            }
        }
        return adjustedGreyscaleEigenMatrix;
    }

    private static double[][] normalizeEigenVectors(double[][] eigenVectors) {

        for (int i = 0; i < eigenVectors[0].length; i++) { //columns
            double smallest = Double.MAX_VALUE;
            double largest = Double.MIN_VALUE;
            int smallestRow = 0, largestRow = 0;
            for (int j = 0; j < eigenVectors.length; j++) { //rows
                if (eigenVectors[j][i] < smallest) {
                    smallest = eigenVectors[j][i];
                    smallestRow = j;
                }
                if (eigenVectors[j][i] > largest) {
                    largest = eigenVectors[j][i];
                    largestRow = j;
                }
            }
            for (int j = 0; j < eigenVectors.length; j++) {
                eigenVectors[j][i] = 255 * (eigenVectors[j][i] - smallest) / (largest - smallest);
            }
            eigenVectors[smallestRow][i] = 0;
            eigenVectors[largestRow][i] = 255;
        }
        return eigenVectors;
    }

    private static double[][] transpose(double[][] m) {
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    private static double[][] multiplicar(double[][] A, double[][] B) {

        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;

        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        double[][] C = new double[aRows][bColumns];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                C[i][j] = 0.00000;
            }
        }

        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }
}
