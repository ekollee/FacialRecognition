
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: FacialRecognition
 * Name: Eric
 * Created on 20 April, 2016
 */

public class FacialDetection {
    List<ImageProcessor> images = new ArrayList<ImageProcessor>();

    public FacialDetection() {
        ImageProcessor imageProcessor = new ImageProcessor("testImage.jpg");
        GUIView.getInstance().setImage(imageProcessor.getImage());

        //  eigenFacesTest();

        imageProcessor.detectSkin();
        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.floodFillInit(Color.black.getRGB(), Color.white.getRGB());

//        imageProcessor.fillHoles(Color.black.getRGB(), Color.white.getRGB());
        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.fillHoles(Color.white.getRGB(), Color.black.getRGB());

        GUIView.getInstance().setImage(imageProcessor.getImage());
        imageProcessor.overlayEdgeDetectionImage();

        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.findWhiteBlobDimensions();

        GUIView.getInstance().setImage(imageProcessor.getImage());
    }

    private void eigenFacesTest() {

        File dir = new File("faces");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int count = 0;
            for (File child : directoryListing) {
                // Do something with child
                images.add(new ImageProcessor(child.getAbsolutePath()));
                count++;
                if (count == 100)
                    break;
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

        double[][] greyScaleEigenMatrix = createGreyscaleEigenMatrix();
        double[][] meanGreyScaleEigenMatrix = meanEigenface(greyScaleEigenMatrix);
        double[][] adjustedGreyscaleEigenMatrix = adjustedGreyscaleEigenMatrix(greyScaleEigenMatrix, meanGreyScaleEigenMatrix);
        RealMatrix covarianceMatrix = new Array2DRowRealMatrix(multiplicar(adjustedGreyscaleEigenMatrix, transpose(adjustedGreyscaleEigenMatrix)));
        org.apache.commons.math3.linear.SingularValueDecomposition svd = new org.apache.commons.math3.linear.SingularValueDecomposition(covarianceMatrix);
        double[][] normalizedEigenVectors = normalizeEigenVectors(svd.getU().getData());

        int imageSize = (int) Math.sqrt(normalizedEigenVectors.length);
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);

        for (int k = 0; k < 10; k++) {


            for (int y = 0; y < imageSize; y++) {
                for (int x = 0; x < imageSize; x++) {
                    // System.out.println(y * imageSize + x);
                    int greyScale = (int) normalizedEigenVectors[y * imageSize + x][k];
                    // System.out.println("Greyscale: " + greyScale);
                    image.setRGB(y, x, new Color(greyScale, greyScale, greyScale).getRGB());
                }
            }
            ImageProcessor.saveImage(image, "eigenface " + k + ".jpg");
        }

    }

    private double[][] createGreyscaleEigenMatrix() {
        double[][] greyscaleMatrix;
        greyscaleMatrix = new double[images.get(0).getImage().getHeight() * images.get(0).getImage().getWidth()][images.size()];
        for (int i = 0; i < images.get(0).getImage().getHeight(); i++) {
            for (int j = 0; j < images.get(0).getImage().getWidth(); j++) {
                for (int k = 0; k < images.size(); k++) {


                    greyscaleMatrix[i * images.get(0).getImage().getHeight() + j][k] = ImageProcessor.getBlue(images.get(k).getImage().getRGB(i, j));
                }

            }
        }
        return greyscaleMatrix;
    }

    private double[][] meanEigenface(double[][] greyscaleMatrix) {
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

    private double[][] adjustedGreyscaleEigenMatrix(double[][] greyScaleEigenMatrix, double[][] meanGreyScaleEigenMatrix) {
        double[][] adjustedGreyscaleEigenMatrix = new double[greyScaleEigenMatrix.length][greyScaleEigenMatrix[0].length];
        for (int i = 0; i < greyScaleEigenMatrix.length; i++) {
            for (int j = 0; j < greyScaleEigenMatrix[0].length; j++) {
                adjustedGreyscaleEigenMatrix[i][j] = greyScaleEigenMatrix[i][j] - meanGreyScaleEigenMatrix[i][0];
            }
        }
        return adjustedGreyscaleEigenMatrix;
    }

    private double[][] normalizeEigenVectors(double[][] eigenVectors) {

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

    public static double[][] transpose(double[][] m) {
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    public static double[][] multiplicar(double[][] A, double[][] B) {

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


    public static void main(String[] args) {
        new FacialDetection();
    }
}
