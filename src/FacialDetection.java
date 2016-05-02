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
 * Name: Eric
 * Created on 20 April, 2016
 */

public class FacialDetection {


    public FacialDetection() {
        List<EigenFace> faces = EigenFaceFactory.loadEigenFaces("eigenfaces");
        EigenFace meanFace = EigenFaceFactory.meanEigenFace(faces);
        GUIView.getInstance().setImage(meanFace.image);
        ImageProcessor.saveImage(meanFace.image,"meanEigenFace.jpg");
        ImageProcessor imageProcessor = new ImageProcessor("testImage1.jpg");
        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.detectSkin();

        ImageProcessor.saveImage(imageProcessor.getImage(), "neuralnet.jpg");
        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.floodFillInit(Color.black.getRGB(), Color.white.getRGB());
        ImageProcessor.saveImage(imageProcessor.getImage(), "blackfill.jpg");

        imageProcessor.fillHoles(Color.white.getRGB(), Color.black.getRGB());
        ImageProcessor.saveImage(imageProcessor.getImage(), "whitefill.jpg");

        GUIView.getInstance().setImage(imageProcessor.getImage());

//
        imageProcessor.findWhiteBlobDimensions();
//
        GUIView.getInstance().setImage(imageProcessor.getImage());
    }



    public static void main(String[] args) {
        new FacialDetection();
    }

}
