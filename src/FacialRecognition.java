
/**
 * Project: FacialRecognition
 * Name: Eric
 * Created on 20 April, 2016
 */

public class FacialRecognition {

    public FacialRecognition() {
        ImageProcessor imageProcessor = new ImageProcessor("testImage1.jpg");

        imageProcessor.detectSkin();
    }

    public static void main(String[] args) {
        new FacialRecognition();
    }
}
