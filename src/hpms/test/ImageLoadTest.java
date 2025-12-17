package hpms.test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoadTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Image Load Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        
        // Test loading images from resources folder
        String projectRoot = System.getProperty("user.dir");
        String resourcesPath = projectRoot + File.separator + "resources";
        
        String[] testImages = {
            "cardiology1.jpg",
            "er1.jpg", 
            "neurology1.jpg",
            "orthopedics1.jpg",
            "pediatrics1.jpg"
        };
        
        for (String imageName : testImages) {
            String imagePath = resourcesPath + File.separator + imageName;
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                if (image != null) {
                    Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    JLabel label = new JLabel(new ImageIcon(scaledImage));
                    label.setText(imageName);
                    label.setVerticalTextPosition(SwingConstants.BOTTOM);
                    label.setHorizontalTextPosition(SwingConstants.CENTER);
                    frame.add(label);
                    System.out.println("Successfully loaded: " + imagePath);
                } else {
                    System.err.println("Failed to load: " + imagePath + " - image is null");
                }
            } catch (IOException e) {
                System.err.println("Could not load image: " + imagePath + " - " + e.getMessage());
            }
        }
        
        frame.pack();
        frame.setVisible(true);
    }
}
