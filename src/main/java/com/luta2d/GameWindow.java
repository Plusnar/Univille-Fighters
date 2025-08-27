package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowStateListener;
import java.io.File;

public class GameWindow extends JFrame {
    private JPanel currentScreen;
    private boolean isFullScreen = true;
    private static final int BASE_WIDTH = 1920;
    private static final int BASE_HEIGHT = 1080;
    private static final int MIN_WIDTH = 1280;
    private static final int MIN_HEIGHT = 720;
    private static final double ASPECT_RATIO = 16.0 / 9.0;
    private JLayeredPane layeredPane;

    public GameWindow() {
        setTitle("Univille Kombat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        // Create layered pane for overlays
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);
        
        // Add window state listener to handle resizing
        addWindowStateListener(e -> {
            if ((e.getOldState() & Frame.MAXIMIZED_BOTH) == 0 && 
                (e.getNewState() & Frame.MAXIMIZED_BOTH) != 0) {
                setFullScreen(true);
            }
        });

        // Add component listener to maintain aspect ratio
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isFullScreen) {
                    maintainAspectRatio();
                }
            }
        });

        setFullScreen(true);

        try {
            // Sempre mostrar o vídeo de introdução ao iniciar
            showIntro();
            
            // Aplica o cursor customizado
            java.net.URL cursorUrl = getClass().getResource("/images/cursor.png");
            if (cursorUrl != null) {
                CustomCursor.applyCursor(this, cursorUrl);
            } else {
                System.err.println("Arquivo de cursor não encontrado: /images/cursor.png");
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar recursos: " + e.getMessage());
            e.printStackTrace();
        }

        setVisible(true);
    }

    private void showIntro() {
        try {
            java.net.URL videoUrl = getClass().getResource("/videos/intro.mp4");
            if (videoUrl != null) {
                VideoPlayer videoPlayer = new VideoPlayer(videoUrl.toExternalForm());
                videoPlayer.setBounds(0, 0, getWidth(), getHeight());
                layeredPane.add(videoPlayer, JLayeredPane.DEFAULT_LAYER);
                currentScreen = videoPlayer;
                
                videoPlayer.play(() -> {
                    // Quando o vídeo terminar, mostrar o menu
                    SwingUtilities.invokeLater(this::showMenu);
                });
            } else {
                System.err.println("Vídeo de introdução não encontrado: /videos/intro.mp4");
                showMenu(); // Mostra o menu diretamente se o vídeo não for encontrado
            }
        } catch (Exception e) {
            System.err.println("Erro ao mostrar vídeo de introdução: " + e.getMessage());
            showMenu(); // Mostra o menu em caso de erro
        }
    }

    private void showMenu() {
        // Pequeno delay para garantir que o JavaFX seja limpo adequadamente
        Timer timer = new Timer(100, e -> {
            MenuScreen menuScreen = new MenuScreen(this);
            switchTo(menuScreen);
            ((Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void switchTo(JPanel newPanel) {
        // Para a música atual antes de trocar de tela
        AudioManager.getInstance().stopCurrentMusic();
        
        // Limpa o painel atual se for um VideoPlayer
        if (currentScreen instanceof VideoPlayer) {
            ((VideoPlayer) currentScreen).stop();
        }
        
        // Remove todos os componentes do layered pane
        layeredPane.removeAll();
        
        // Configura o novo painel para ocupar toda a tela
        newPanel.setBounds(0, 0, getWidth(), getHeight());
        newPanel.setPreferredSize(new Dimension(getWidth(), getHeight()));
        
        // Adiciona o novo painel
        layeredPane.add(newPanel, JLayeredPane.DEFAULT_LAYER);
        currentScreen = newPanel;
        
        // Revalida e repinta
        layeredPane.revalidate();
        layeredPane.repaint();
        newPanel.requestFocusInWindow();
        
        // Força um repaint adicional para garantir que a tela seja atualizada
        SwingUtilities.invokeLater(() -> {
            layeredPane.repaint();
            newPanel.repaint();
        });
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (fullScreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            device.setFullScreenWindow(this);
        } else {
            device.setFullScreenWindow(null);
            // Set initial size maintaining aspect ratio
            int width = BASE_WIDTH;
            int height = BASE_HEIGHT;
            setSize(width, height);
            setLocationRelativeTo(null);
        }
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public double getScaleFactor() {
        return Math.min(
            getWidth() / (double)BASE_WIDTH,
            getHeight() / (double)BASE_HEIGHT
        );
    }

    private void maintainAspectRatio() {
        int width = getWidth();
        int height = getHeight();
        
        // Calculate new dimensions maintaining aspect ratio
        if (width / (double)height > ASPECT_RATIO) {
            width = (int)(height * ASPECT_RATIO);
        } else {
            height = (int)(width / ASPECT_RATIO);
        }
        
        // Ensure minimum size
        width = Math.max(width, MIN_WIDTH);
        height = Math.max(height, MIN_HEIGHT);
        
        setSize(width, height);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}
