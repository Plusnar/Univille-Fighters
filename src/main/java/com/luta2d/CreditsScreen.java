package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

public class CreditsScreen extends JPanel {
    private final GameWindow window;
    private VideoPlayer videoPlayer;

    public CreditsScreen(GameWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setFocusable(true);
        setBackground(Color.BLACK);

        java.net.URL videoUrl = getClass().getResource("/videos/creditos.mp4");
        if (videoUrl != null) {
            videoPlayer = new VideoPlayer(videoUrl.toExternalForm());
            add(videoPlayer, BorderLayout.CENTER);
        } else {
            System.err.println("Vídeo de créditos não encontrado: /videos/creditos.mp4");
            add(new JLabel("Vídeo não encontrado: /videos/creditos.mp4", SwingConstants.CENTER), BorderLayout.CENTER);
        }

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
    public void addNotify() {
        super.addNotify();
        if (videoPlayer != null) {
            videoPlayer.play(() -> {
                // Ao terminar, volta para o menu
                SwingUtilities.invokeLater(() -> window.switchTo(new MenuScreen(window)));
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Habilita anti-aliasing para melhor qualidade visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (videoPlayer != null) {
            try {
                videoPlayer.stop(); // Garante que o vídeo pare ao sair da tela
            } catch (Exception e) {
                System.err.println("Erro ao parar vídeo de créditos: " + e.getMessage());
            }
        }
    }
} 