import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Project: FacialRecognition
 * Course: COSC 4P76
 * Name: Graham Burgsma
 * Created on 23 April, 2016
 */
public class GUIView extends JFrame {

    private static GUIView ourInstance = new GUIView();
    private JLabel imageLabel = new JLabel();
    private JPanel panel = new JPanel();

    public GUIView() {
        setTitle("Face Detection");
        setResizable(false);

        BorderLayout borderLayout = new BorderLayout();
        JPanel panel = new JPanel(borderLayout);
        panel.setLayout(borderLayout);

        borderLayout.addLayoutComponent(panel, BorderLayout.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);
        add(panel);

        panel.setVisible(true);
        setVisible(true);
    }

    public static GUIView getInstance() {
        return ourInstance;
    }

    public void setImage(BufferedImage image) {
        int width = 800, height;

        height = (int) (image.getHeight() / ((double) image.getWidth() / (double) width));
        setSize(width, height);

        panel.setSize(width, height);

        imageLabel.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_DEFAULT)));
    }
}
