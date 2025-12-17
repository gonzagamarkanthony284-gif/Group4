package hpms.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {
    
    public static ImageIcon createRoundImageIcon(String imagePath, int width, int height) {
        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                return null;
            }
            
            File file = new File(imagePath);
            if (!file.exists()) {
                return null;
            }
            
            BufferedImage original = ImageIO.read(file);
            if (original == null) {
                return null;
            }
            
            // Resize the image while maintaining aspect ratio
            int newWidth = width;
            int newHeight = height;
            double aspectRatio = (double) original.getWidth() / original.getHeight();
            
            if (original.getWidth() > original.getHeight()) {
                newHeight = (int) (width / aspectRatio);
            } else {
                newWidth = (int) (height * aspectRatio);
            }
            
            Image scaled = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            
            // Create circular mask
            BufferedImage circleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleBuffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw the image centered
            int x = (width - newWidth) / 2;
            int y = (height - newHeight) / 2;
            
            // Create a circular clip
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, width, height));
            g2.drawImage(scaled, x, y, newWidth, newHeight, null);
            
            // Draw border
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(200, 200, 200));
            g2.drawOval(0, 0, width-1, height-1);
            
            g2.dispose();
            
            return new ImageIcon(circleBuffer);
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static ImageIcon resizeImage(ImageIcon icon, int width, int height) {
        if (icon == null) return null;
        
        Image img = icon.getImage();
        Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }
}
