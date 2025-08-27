package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TutorialScreen extends JPanel {
    private final GameWindow window;
    private final Image backgroundImage;

    public TutorialScreen(GameWindow window) {
        this.window = window;
        setLayout(null);
        setFocusable(true);
        setBackground(Color.BLACK);

        // Inicia a música do tutorial
        AudioManager.getInstance().playMusic("tutorial", true);

        // Carrega a imagem de fundo do tutorial
        Image tempImage = null;
        try {
            java.net.URL url = getClass().getResource("/images/tutorial_screen.png");
            if (url != null) {
                tempImage = new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar tutorial_screen.png: " + e.getMessage());
        }
        this.backgroundImage = tempImage;

        // ESC para voltar ao menu
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.switchTo(new MenuScreen(window));
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Habilita anti-aliasing para melhor qualidade visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Desenha a imagem de fundo do tutorial
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback: fundo preto se a imagem não carregar
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Texto de fallback
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String text = "Tutorial - Pressione ESC para voltar";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = getHeight() / 2;
            g2d.drawString(text, textX, textY);
        }
    }
} 