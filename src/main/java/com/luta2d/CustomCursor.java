package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CustomCursor {

    public static void applyCursor(JFrame frame, java.net.URL imageUrl) {
        try {
            BufferedImage cursorImage = ImageIO.read(imageUrl);
            Point hotspot = new Point(0, 0); // onde o clique acontece (pode ajustar)
            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, hotspot, "CustomCursor");
            frame.setCursor(customCursor);
        } catch (Exception e) {
            System.out.println("Erro ao carregar cursor: " + e.getMessage());
        }
    }

    public static void applyCursorToComponent(Component component, java.net.URL imageUrl) {
        try {
            BufferedImage cursorImage = ImageIO.read(imageUrl);
            Point hotspot = new Point(0, 0);
            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, hotspot, "CustomCursor");
            component.setCursor(customCursor);
        } catch (Exception e) {
            System.out.println("Erro ao carregar cursor: " + e.getMessage());
        }
    }
}
