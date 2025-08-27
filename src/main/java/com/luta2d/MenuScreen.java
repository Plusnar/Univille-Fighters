package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import javax.swing.Icon;

public class MenuScreen extends JPanel {
    private final GameWindow window;
    private final Image backgroundImage;
    private final JButton tutorialButton;
    private final JButton playButton;
    private final JButton creditsButton;

    public MenuScreen(GameWindow window) {
        this.window = window;
        setLayout(null);
        setFocusable(true);

        // Inicia o sistema de som
        AudioManager audioManager = AudioManager.getInstance();
        // Toca o título uma vez e só inicia a música do menu após terminar
        audioManager.playMusicWithCallback("univille", false, () -> {
            audioManager.playMusic("menu", true);
        });

        // Carrega a imagem de fundo
        java.net.URL url = getClass().getResource("/images/fundo_menu.png");
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        } else {
            System.err.println("Imagem de fundo não encontrada: /images/fundo_menu.png");
            backgroundImage = null;
        }

        // Configuração dos botões
        tutorialButton = new JButton();
        playButton = new JButton();
        creditsButton = new JButton();

        // Carrega a imagem do botão Tutorial
        java.net.URL tutorialUrl = getClass().getResource("/images/tutorial.png");
        if (tutorialUrl != null) {
            ImageIcon tutorialIcon = new ImageIcon(tutorialUrl);
            tutorialButton.setIcon(tutorialIcon);
        } else {
            System.err.println("Imagem do botão tutorial não encontrada: /images/tutorial.png");
            tutorialButton.setText("Tutorial");
        }

        // Carrega a imagem do botão Jogar
        java.net.URL playUrl = getClass().getResource("/images/jogar.png");
        if (playUrl != null) {
            ImageIcon playIcon = new ImageIcon(playUrl);
            playButton.setIcon(playIcon);
        } else {
            System.err.println("Imagem do botão jogar não encontrada: /images/jogar.png");
            playButton.setText("Jogar");
        }

        // Carrega a imagem do botão Créditos
        java.net.URL creditsUrl = getClass().getResource("/images/creditos.png");
        if (creditsUrl != null) {
            ImageIcon creditsIcon = new ImageIcon(creditsUrl);
            creditsButton.setIcon(creditsIcon);
        } else {
            System.err.println("Imagem do botão créditos não encontrada: /images/creditos.png");
            creditsButton.setText("Créditos");
        }

        // Configuração visual dos botões
        setupButton(tutorialButton);
        setupButton(playButton);
        setupButton(creditsButton);

        // Adiciona efeitos sonoros de hover
        addHoverSound(tutorialButton);
        addHoverSound(playButton);
        addHoverSound(creditsButton);

        // Posicionamento dos botões
        positionButtons();

        // Ações dos botões
        playButton.addActionListener(e -> {
            window.switchTo(new CharacterSelectionScreen(window));
        });

        tutorialButton.addActionListener(e -> {
            window.switchTo(new TutorialScreen(window));
        });

        creditsButton.addActionListener(e -> {
            window.switchTo(new CreditsScreen(window));
        });

        // ESC para sair de tela cheia
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.setFullScreen(false);
                }
            }
        });
    }

    private void setupButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(128, 14, 14, 150));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        add(button);
    }

    private void addHoverSound(JButton button) {
        // Removido som de hover
    }

    private void positionButtons() {
        int buttonWidth = 300;
        int buttonHeight = 100;
        int spacing = 30;
        
        // Usa as dimensões do painel ou valores padrão se ainda não estiver definido
        int panelWidth = getWidth() > 0 ? getWidth() : 1920;
        int panelHeight = getHeight() > 0 ? getHeight() : 1080;
        
        int startY = panelHeight - buttonHeight - 100;

        // Centraliza os botões horizontalmente
        int totalWidth = (buttonWidth * 3) + (spacing * 2);
        int startX = (panelWidth - totalWidth) / 2;

        // Posiciona cada botão individualmente
        tutorialButton.setBounds(startX, startY, buttonWidth, buttonHeight);
        playButton.setBounds(startX + buttonWidth + spacing, startY, buttonWidth, buttonHeight);
        creditsButton.setBounds(startX + (buttonWidth + spacing) * 2, startY, buttonWidth, buttonHeight);

        // Força o repaint para garantir que os botões sejam exibidos corretamente
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Habilita anti-aliasing para melhor qualidade visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Desenha o fundo
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            setBackground(Color.BLACK);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Força o layout e repaint quando o painel for adicionado
        SwingUtilities.invokeLater(() -> {
            positionButtons();
            repaint();
        });
    }

    @Override
    public void doLayout() {
        super.doLayout();
        positionButtons();
    }
}
