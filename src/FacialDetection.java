import java.awt.*;

/**
 * Project: FacialRecognition
 * Name: Eric
 * Created on 20 April, 2016
 */

public class FacialDetection {

    public FacialDetection() {
        ImageProcessor imageProcessor = new ImageProcessor("testImage1.jpg");
        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.detectSkin();

        GUIView.getInstance().setImage(imageProcessor.getImage());

        imageProcessor.fillHoles(Color.black.getRGB(), Color.white.getRGB());
        GUIView.getInstance().setImage(imageProcessor.getImage());
        imageProcessor.fillHoles(Color.white.getRGB(), Color.black.getRGB());
        GUIView.getInstance().setImage(imageProcessor.getImage());
    }

    public static void main(String[] args) {
        new FacialDetection();
    }
}
