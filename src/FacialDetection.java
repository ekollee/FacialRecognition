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
    }

    public static void main(String[] args) {
        new FacialDetection();
    }
}
