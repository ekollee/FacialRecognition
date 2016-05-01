import neuralnetwork.DataReader;
import neuralnetwork.NeuralNetwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Project: FacialRecognition
 * Name: grahamburgsma
 * Created on 21 April, 2016
 */
public class ImageProcessor {

    private BufferedImage image, originalImage;
    private List<MinMaxCoord> faceList = Collections.synchronizedList(new ArrayList<>());

    public ImageProcessor(String imageFileName) {
        try {
            importImage(new File(imageFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static BufferedImage importImageAndReturn(File file) throws IOException {
        return ImageIO.read(file);
    }

    public static void saveImage(BufferedImage image, String fileName) {
        File outputfile = new File(fileName);
        try {
            ImageIO.write(image, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    void importImage(File file) throws IOException {
        image = ImageIO.read(file);
        originalImage = ImageIO.read(file);
    }

    BufferedImage getImage() {
        return image;
    }

    BufferedImage getOriginalImageCopy() {
        ColorModel cm = originalImage.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = originalImage.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    void detectSkin() {
        SkinNeuralNetwork neuralNetwork = SkinNeuralNetwork.getInstance();
        NeuralNetwork.Results results = neuralNetwork.runNetwork(5, 2, DataReader.readSkinData());

        neuralNetwork.printResults(results);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                if (neuralNetwork.isSkin(rgb)) {
                    image.setRGB(x, y, Color.white.getRGB());
                } else {
                    image.setRGB(x, y, Color.black.getRGB());
                }
            }
        }
    }

    void fillHoles(int searchColour, int fillColour) {
        boolean visited[][] = new boolean[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (searchColour == image.getRGB(x, y) && !visited[y][x]) {
                    explore(0, y, x, visited, searchColour, fillColour);
                }
            }
        }
    }

    void floodFillInit(int searchColour, int fillColour) {
        boolean visited[][] = new boolean[image.getHeight()][image.getWidth()];

        floodFill(new Point(0, 0), visited, searchColour, fillColour);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (searchColour == image.getRGB(x, y) && !visited[y][x]) {
                    image.setRGB(x, y, fillColour);
                }
            }
        }
    }

    void floodFill(Point point, boolean[][] visited, int searchColour, int fillColour) {
        int rowNbr[] = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
        int colNbr[] = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

        Queue<Point> queue = new LinkedBlockingQueue<>();
        queue.add(point);

        while (!queue.isEmpty()) {
            Point tempPoint = queue.remove();

            if (image.getRGB(tempPoint.x, tempPoint.y) == searchColour && !visited[tempPoint.y][tempPoint.x]) {
                visited[tempPoint.y][tempPoint.x] = true;
                if (point.x != 0 || point.y != 0)
                    image.setRGB(tempPoint.x, tempPoint.y, fillColour);

                for (int k = 0; k < 8; ++k) {
                    if (isSafe(tempPoint.y + rowNbr[k], tempPoint.x + colNbr[k], visited, searchColour)) {
                        queue.add(new Point(tempPoint.x + colNbr[k], tempPoint.y + rowNbr[k]));
                    }
                }
            }
        }
    }

    private boolean explore(int currentSize, int y, int x, boolean visited[][], int searchColour, int fillColour) {
        if (currentSize >= 300)
            return false;

        int rowNbr[] = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
        int colNbr[] = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

        visited[y][x] = true;
        currentSize++;

        for (int k = 0; k < 8; ++k)
            if (isSafe(y + rowNbr[k], x + colNbr[k], visited, searchColour)) {
                if (explore(currentSize, y + rowNbr[k], x + colNbr[k], visited, searchColour, fillColour))
                    image.setRGB(x, y, fillColour);
                else {
                    image.setRGB(x, y, searchColour);
                    return false;
                }
            }
        image.setRGB(x, y, fillColour);
        return true;
    }

    private boolean isSafe(int y, int x, boolean visited[][], int searchColour) {
        return (y >= 0) && (y < image.getHeight()) &&
                (x >= 0) && (x < image.getWidth()) &&
                (image.getRGB(x, y) == searchColour && !visited[y][x]);
    }

    private BufferedImage edgeDetection() {
        BufferedImage greyImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage edgeImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        colorConvert.filter(originalImage, greyImage);

        int count, averageVertical, averageHorizontal;
        int arrayVertical[] = {1, 2, 1, 0, 0, 0, -1, -2, -1};
        int arrayHorizontal[] = {1, 0, -1, 2, 0, -2, 1, 0, -1};

        for (int x = 0; x < greyImage.getWidth(); x++) {
            for (int y = 0; y < greyImage.getHeight(); y++) {
                count = 0;
                averageVertical = 0;
                averageHorizontal = 0;
                for (int i = x - 1; i <= x + 1; i++) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (j > 0 && j < greyImage.getHeight() && i > 0 && i < greyImage.getWidth()) {
                            averageVertical += getRed(greyImage.getRGB(i, j)) * arrayVertical[count];
                            averageHorizontal += getRed(greyImage.getRGB(i, j)) * arrayHorizontal[count];
                        }
                        count++;
                    }
                }

                averageVertical = averageVertical < 0 ? 0 : averageVertical > 255 ? 255 : averageVertical;
                averageHorizontal = averageHorizontal < 0 ? 0 : averageHorizontal > 255 ? 255 : averageHorizontal;

                int newColor = (int) Math.sqrt(Math.pow(averageVertical, 2) + Math.pow(averageHorizontal, 2));

                newColor = newColor < 0 ? 0 : newColor > 255 ? 255 : newColor;

                edgeImage.setRGB(x, y, new Color(newColor, newColor, newColor).getRGB());
            }
        }

        return edgeImage;
    }

    void overlayEdgeDetectionImage() {
        BufferedImage edgeImage = edgeDetection();

        for (int y = 0; y < edgeImage.getHeight(); y++) {
            for (int x = 0; x < edgeImage.getWidth(); x++) {
                if (image.getRGB(x, y) == Color.white.getRGB() && getRed(edgeImage.getRGB(x, y)) > 50) {
                    image.setRGB(x, y, Color.black.getRGB());
                }
            }
        }

        fillHoles(Color.black.getRGB(), Color.white.getRGB());
        fillHoles(Color.white.getRGB(), Color.black.getRGB());
    }

    void findWhiteBlobDimensions() {
        boolean visited[][] = new boolean[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (Color.white.getRGB() == image.getRGB(x, y) && !visited[y][x]) {
                    faceList.add(floodFillGetDimensions(new Point(x, y), visited, Color.white.getRGB(), new MinMaxCoord()));
                }
            }
        }
    }

    void mergeFaces() {
        while (true) {
            if (mergeFaces(10, 5, 3))
                break;
        }
    }

    void drawFaceBoxesOnImage(BufferedImage image) {
        for (MinMaxCoord face : faceList)
            drawRectangle(image, face);
    }

    MinMaxCoord floodFillGetDimensions(Point point, boolean[][] visited, int searchColour, MinMaxCoord coord) {
        int rowNbr[] = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
        int colNbr[] = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

        Queue<Point> queue = new LinkedBlockingQueue<>();
        queue.add(point);

        while (!queue.isEmpty()) {
            Point tempPoint = queue.remove();

            if (image.getRGB(tempPoint.x, tempPoint.y) == searchColour && !visited[tempPoint.y][tempPoint.x]) {
                visited[tempPoint.y][tempPoint.x] = true;
                coord.update(tempPoint.x, tempPoint.y);

                for (int k = 0; k < 8; ++k)
                    if (isSafe(tempPoint.y + rowNbr[k], tempPoint.x + colNbr[k], visited, searchColour))
                        queue.add(new Point(tempPoint.x + colNbr[k], tempPoint.y + rowNbr[k]));
            }
        }

        return coord;
    }

    void drawRectangle(BufferedImage image, MinMaxCoord coord) {
        for (int i = coord.minX; i <= coord.maxX; i++) {
            for (int j = 0; j < 3; j++) {
                try {
                    image.setRGB(i, coord.minY - j, Color.red.getRGB());
                    image.setRGB(i, coord.maxY + j, Color.red.getRGB());
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }
            }
        }

        for (int i = coord.minY; i <= coord.maxY; i++) {
            for (int j = 0; j < 3; j++) {
                try {
                    image.setRGB(coord.minX - j, i, Color.red.getRGB());
                    image.setRGB(coord.maxX + j, i, Color.red.getRGB());
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }
            }
        }
    }

    boolean mergeFaces(int vertical, int horizontal, double sizeFactor) {
        for (int i = 0; i < faceList.size(); i++) {
            MinMaxCoord face = faceList.get(i);
            for (int i1 = 0; i1 < faceList.size(); i1++) {
                MinMaxCoord face2 = faceList.get(i1);
                if (face != face2 && polygonTouching(vertical, horizontal, face, face2) && getPloygonArea(face2) < getPloygonArea(face) / sizeFactor) {
                    if (face2.minX < face.minX)
                        face.minX = face2.minX;
                    if (face2.minY < face.minY)
                        face.minY = face2.minY;
                    if (face2.maxX > face.maxX)
                        face.maxX = face2.maxX;
                    if (face2.maxY > face.maxY)
                        face.maxY = face2.maxY;
                    faceList.remove(face2);
                    return false;
                }
            }
        }
        return true;
    }

    List<BufferedImage> getFaceImages() {
        List<BufferedImage> faceImages = new ArrayList<>();

        for (MinMaxCoord face : faceList) {
            faceImages.add(image.getSubimage(face.minX, face.minY, face.getWidth(), face.getHeight()));
        }

        return faceImages;
    }

    int getPloygonArea(MinMaxCoord face) {
        return (face.maxX - face.minX) * (face.maxY - face.minY);
    }

    boolean polygonTouching(int vertical, int horizontal, MinMaxCoord poly1, MinMaxCoord poly2) {

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setLocation(poly1.minX - horizontal, poly1.minY - vertical);
        rectangle1.setSize(poly1.maxX - poly1.minX + horizontal, poly1.maxY - poly1.minY + vertical);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setLocation(poly2.minX - horizontal, poly2.minY - vertical);
        rectangle2.setSize(poly2.maxX - poly2.minX + horizontal, poly2.maxY - poly2.minY + vertical);

        return rectangle1.intersects(rectangle2);
//        return poly1.minX - horizontal < poly2.maxX + horizontal && poly1.maxX + horizontal > poly2.minX - horizontal && poly1.minY - vertical < poly2.maxY + vertical && poly1.maxY + vertical > poly2.minY - vertical;
    }

    class MinMaxCoord {

        int minX, minY, maxX, maxY;

        public MinMaxCoord() {
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;
        }

        int getWidth() {
            return maxX - minX;
        }

        int getHeight() {
            return maxY - minY;
        }

        public void update(int x, int y) {
            if (x < minX)
                minX = x;
            if (y < minY)
                minY = y;
            if (x > maxX)
                maxX = x;
            if (y > maxY)
                maxY = y;
        }

        @Override
        public String toString() {
            return minX + "\t" + minY + "\t" + maxX + "\t" + maxY;
        }
    }
}
