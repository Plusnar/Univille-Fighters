package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class CharacterSelectionScreen extends JPanel implements ActionListener {
    private final GameWindow window;
    private final Image background;
    private final List<CharacterButton> characterButtons = new ArrayList<>();
    private int currentPlayer = 1;
    private Character selected1 = null;
    private Character selected2 = null;
    private boolean hoverPlayed = false;
    private JLabel previewLabel;
    private JLabel previewNameLabel;
    private JLabel playerIndicator;
    private Timer countdownTimer;
    private int countdown = 3;
    private JLabel countdownLabel;
    private JLabel sidePhotoLabel;
    private JLayeredPane layeredPane;
    private JLabel bigPhotoLabel1;
    private JLabel bigPhotoLabel2;
    private ImageIcon currentBigPhoto1;
    private ImageIcon currentBigPhoto2;
    private Timer fadeTimer1;
    private Timer fadeTimer2;
    private float fadeAlpha1 = 1.0f;
    private float fadeAlpha2 = 1.0f;
    private ImageIcon nextBigPhoto1;
    private ImageIcon nextBigPhoto2;
    
    // Cores para seleção: preta (padrão), azul (Player 1), vermelha (Player 2)
    
    // Variáveis para transição suave
    private Image versusBackground;
    private boolean transitionStarted = false;
    private float transitionAlpha = 0.0f;
    private Timer transitionTimer;
    private Timer arenaTimer;
    private boolean iconsHidden = false;

    public CharacterSelectionScreen(GameWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setFocusable(true);
        setBackground(Color.BLACK); // Define fundo preto

        // Inicia a música de seleção de personagens
        AudioManager.getInstance().playMusic("characters", true);

        // Carrega a imagem de fundo smoke.gif
        java.net.URL bgUrl = getClass().getResource("/images/smoke.gif");
        if (bgUrl != null) {
            background = new ImageIcon(bgUrl).getImage();
        } else {
            System.err.println("Imagem de fundo não encontrada: /images/smoke.gif");
            background = null;
        }

        // Carrega a imagem versus.png para a transição
        java.net.URL versusUrl = getClass().getResource("/images/versus.png");
        if (versusUrl != null) {
            versusBackground = new ImageIcon(versusUrl).getImage();
        } else {
            versusBackground = null;
        }

        // Criar o JLayeredPane
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane, BorderLayout.CENTER);

        // Create countdown label
        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 72));
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setBounds(0, 50, getWidth(), 100);
        countdownLabel.setVisible(false);
        layeredPane.add(countdownLabel, JLayeredPane.MODAL_LAYER);

        // Create preview panel for Player 1 (left side)
        JPanel previewPanel1 = new JPanel();
        previewPanel1.setLayout(null);
        previewPanel1.setOpaque(false);
        previewPanel1.setBounds(0, 0, getWidth()/2, getHeight());
        layeredPane.add(previewPanel1, JLayeredPane.PALETTE_LAYER);

        previewLabel = new JLabel();
        previewLabel.setBounds(0, 0, 640, 720);
        previewPanel1.add(previewLabel);

        // Create preview panel for Player 2 (right side)
        JPanel previewPanel2 = new JPanel();
        previewPanel2.setLayout(null);
        previewPanel2.setOpaque(false);
        previewPanel2.setBounds(getWidth()/2, 0, getWidth()/2, getHeight());
        layeredPane.add(previewPanel2, JLayeredPane.PALETTE_LAYER);

        sidePhotoLabel = new JLabel();
        sidePhotoLabel.setBounds(0, 0, 640, 720);
        previewPanel2.add(sidePhotoLabel);

        // Create player indicator with background
        JPanel indicatorPanel = new JPanel();
        indicatorPanel.setOpaque(false);
        indicatorPanel.setBounds(0, 0, getWidth(), 60);
        layeredPane.add(indicatorPanel, JLayeredPane.PALETTE_LAYER);

        playerIndicator = new JLabel("Player 1 - Select Your Character", SwingConstants.CENTER);
        playerIndicator.setFont(new Font("Arial", Font.BOLD, 32));
        playerIndicator.setForeground(Color.WHITE);
        indicatorPanel.add(playerIndicator);

        String[] names = {
                "Anna", "William", "Cesar", "Jean",
                "Giovana", "Erik"
        };

        // Configuração para grade 6x1 (uma linha horizontal com 6 personagens)
        int cols = 6; // Alterado de 9 para 6
        int rows = 1;
        int width = 150;
        int height = 150;
        int gap = 20; // Adiciona um pequeno espaço entre os ícones

        int panelWidth = 1280;
        int panelHeight = 720;

        if (window.getWidth() > 0 && window.getHeight() > 0) {
            panelWidth = window.getWidth();
            panelHeight = window.getHeight();
        }

        // Calcula o tamanho total e centraliza horizontalmente
        int totalWidth = cols * width + (cols - 1) * gap;
        int startX = (panelWidth - totalWidth) / 2;
        int startY = panelHeight - height - 80; // Posiciona na parte inferior da tela com mais espaço

        for (int i = 0; i < names.length; i++) {
            int col = i % cols;
            int x = startX + col * (width + gap);
            int y = startY;

            CharacterButton btn = new CharacterButton(names[i], x, y, width, height, i);
            characterButtons.add(btn);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentPlayer == 1 && selected1 == null) {
                        selected1 = new Character(btn.name, 1);
                        btn.setSelected(true, Color.BLUE); // Azul para Player 1
                        currentPlayer = 2;
                        playerIndicator.setText("Player 2 - Select Your Character");
                    } else if (currentPlayer == 2 && selected2 == null && !btn.name.equals(selected1.getName())) {
                        selected2 = new Character(btn.name, 2);
                        btn.setSelected(true, Color.RED); // Vermelho para Player 2
                        startCountdown();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (currentPlayer == 1 && selected1 == null) {
                        startFadePhoto(1, btn.name);
                    } else if (currentPlayer == 2 && selected2 == null && !btn.name.equals(selected1 != null ? selected1.getName() : "")) {
                        startFadePhoto(2, btn.name);
                    }
                    // Toca o som do personagem ao passar o mouse
                    AudioManager.getInstance().playSound(btn.name.toLowerCase());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (currentPlayer == 1 && selected1 == null) {
                        startFadePhoto(1, null);
                    } else if (currentPlayer == 2 && selected2 == null && !btn.name.equals(selected1 != null ? selected1.getName() : "")) {
                        startFadePhoto(2, null);
                    }
                    // Para o som do personagem ao sair do hover
                    AudioManager.getInstance().stopCurrentSound();
                }
            });

            layeredPane.add(btn, JLayeredPane.PALETTE_LAYER);
        }

        // Adicionar as fotos grandes dos personagens
        bigPhotoLabel1 = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (currentBigPhoto1 != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, fadeAlpha1));
                    // Player 1: esquerda, espelha horizontalmente
                    int w = getWidth();
                    int h = getHeight();
                    g2d.drawImage(currentBigPhoto1.getImage(), w, h - w, -w, w, null);
                    g2d.dispose();
                }
                if (nextBigPhoto1 != null && fadeAlpha1 < 1.0f) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f - fadeAlpha1));
                    int w = getWidth();
                    int h = getHeight();
                    g2d.drawImage(nextBigPhoto1.getImage(), w, h - w, -w, w, null);
                    g2d.dispose();
                }
            }
        };
        bigPhotoLabel2 = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (currentBigPhoto2 != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, fadeAlpha2));
                    // Player 2: direita, NÃO espelha, desenha normalmente
                    int w = getWidth();
                    int h = getHeight();
                    g2d.drawImage(currentBigPhoto2.getImage(), 0, h - w, w, w, null);
                    g2d.dispose();
                }
                if (nextBigPhoto2 != null && fadeAlpha2 < 1.0f) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f - fadeAlpha2));
                    int w = getWidth();
                    int h = getHeight();
                    g2d.drawImage(nextBigPhoto2.getImage(), 0, h - w, w, w, null);
                    g2d.dispose();
                }
            }
        };
        // Adiciona as labels ao layeredPane, entre o fundo e os ícones
        bigPhotoLabel1.setOpaque(false);
        bigPhotoLabel2.setOpaque(false);
        // Adiciona na camada abaixo dos ícones, acima do fundo
        layeredPane.add(bigPhotoLabel1, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(bigPhotoLabel2, JLayeredPane.DEFAULT_LAYER);

        // Ajusta o tamanho das labels ao redimensionar a tela
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();
                bigPhotoLabel1.setBounds(0, 0, w / 2, h);
                bigPhotoLabel2.setBounds(w / 2, 0, w / 2, h);
            }
        });
        // Inicializa o tamanho correto
        int w = layeredPane.getWidth();
        int h = layeredPane.getHeight();
        bigPhotoLabel1.setBounds(0, 0, w / 2, h);
        bigPhotoLabel2.setBounds(w / 2, 0, w / 2, h);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.setFullScreen(false);
                }
            }
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        repositionCharacterButtons();
    }

    private void repositionCharacterButtons() {
        int cols = 6;
        int width = 150;
        int height = 150;
        int gap = 20;
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // Calcula o tamanho total e centraliza horizontalmente
        int totalWidth = cols * width + (cols - 1) * gap;
        int startX = (panelWidth - totalWidth) / 2;
        int startY = panelHeight - height - 80;
        
        for (int i = 0; i < characterButtons.size(); i++) {
            int col = i % cols;
            int x = startX + col * (width + gap);
            int y = startY;
            characterButtons.get(i).setBounds(x, y, width, height);
        }
    }

    private void showSelectionMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Character Selected", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showConfirmationDialog() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Ready to fight?\n" + selected1.getName() + " vs " + selected2.getName(),
            "Start Fight",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            startFight();
        } else {
            // Reset selections
            selected1 = null;
            selected2 = null;
            currentPlayer = 1;
            playerIndicator.setText("Player 1 - Select Your Character");
            for (CharacterButton btn : characterButtons) {
                btn.setSelected(false, Color.BLACK); // Volta para borda preta
            }
        }
    }

    private void startCountdown() {
        if (transitionStarted) return; // Evita múltiplas chamadas
        transitionStarted = true;
        
        // Esconde os ícones suavemente
        hideIconsSmoothly();
        
        // Inicia a transição do fundo
        startBackgroundTransition();
        
        // Timer para ir para a tela de seleção da arena após 5 segundos
        arenaTimer = new Timer(5000, e -> {
            arenaTimer.stop();
            startFight();
        });
        arenaTimer.start();
    }

    private void startFight() {
        window.switchTo(new ArenaSelectionScreen(window, selected1, selected2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Desenha o fundo smoke.gif
        if (background != null) {
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback para fundo preto se a imagem não carregar
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Se a transição começou, desenha o fundo versus.png com transparência
        if (transitionStarted && versusBackground != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transitionAlpha));
            g2d.drawImage(versusBackground, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Implementação do ActionListener se necessário
    }

    private static class CharacterButton extends JLabel {
        private final String name;
        private boolean isSelected = false;
        private Color borderColor = Color.BLACK; // Borda preta por padrão
        private final int index;
        private ImageIcon characterIcon;
        private static ImageIcon frameIcon = null; // Imagem de fundo compartilhada

        public CharacterButton(String name, int x, int y, int width, int height, int index) {
            this.name = name;
            this.index = index;
            setBounds(x, y, width, height);
            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(borderColor, 2));
            
            // Carrega a imagem de fundo (frame.png) apenas uma vez
            if (frameIcon == null) {
                loadFrameImage(width, height);
            }
            
            // Carrega a imagem do personagem
            loadCharacterImage(width, height);
        }

        private void loadCharacterImage(int width, int height) {
            try {
                String imagePath = "/images/characters/" + name.toLowerCase() + ".png";
                java.net.URL charUrl = getClass().getResource(imagePath);
                if (charUrl != null) {
                    characterIcon = new ImageIcon(charUrl);
                    Image scaledImage = characterIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    characterIcon = new ImageIcon(scaledImage);
                    setIcon(characterIcon);
                } else {
                    System.out.println("Erro ao carregar imagem: " + imagePath);
                }
            } catch (Exception e) {
                System.out.println("Erro ao carregar imagem do personagem " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void loadFrameImage(int width, int height) {
            try {
                String framePath = "/images/frame.png";
                java.net.URL frameUrl = getClass().getResource(framePath);
                if (frameUrl != null) {
                    ImageIcon originalFrame = new ImageIcon(frameUrl);
                    Image scaledFrame = originalFrame.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    frameIcon = new ImageIcon(scaledFrame);
                    System.out.println("Frame carregado com sucesso: " + framePath + " (" + width + "x" + height + ")");
                } else {
                    System.out.println("Erro ao carregar frame: " + framePath);
                }
            } catch (Exception e) {
                System.out.println("Erro ao carregar frame: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void setSelected(boolean selected, Color color) {
            isSelected = selected;
            if (selected) {
                borderColor = color; // Azul para Player 1, Vermelho para Player 2
            } else {
                borderColor = Color.BLACK; // Volta para borda preta
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // 1. Desenha o frame como fundo (mais atrás)
            if (frameIcon != null && frameIcon.getImage() != null) {
                g2d.drawImage(frameIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }

            // 2. Desenha a imagem do personagem por cima do frame
            if (characterIcon != null && characterIcon.getImage() != null) {
                // Desenha o personagem centralizado no frame
                int charWidth = getWidth();
                int charHeight = getHeight();
                g2d.drawImage(characterIcon.getImage(), 0, 0, charWidth, charHeight, this);
            } else {
                // Se não houver imagem do personagem, desenha um retângulo com o nome
                g2d.setColor(new Color(128, 128, 128, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String text = name;
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                int textY = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(text, textX, textY);
            }

            // 3. Desenha a borda por último (mais alta na pilha)
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(isSelected ? 6 : 3)); // Borda mais grossa quando selecionado
            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    // Métodos auxiliares para fade e troca de foto
    private void startFadePhoto(int player, String charName) {
        if (player == 1) {
            ImageIcon newPhoto = (charName != null) ? loadBigPhoto(charName) : null;
            if (newPhoto == currentBigPhoto1) return;
            nextBigPhoto1 = newPhoto;
            if (fadeTimer1 != null && fadeTimer1.isRunning()) fadeTimer1.stop();
            fadeAlpha1 = 1.0f;
            fadeTimer1 = new Timer(15, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fadeAlpha1 -= 0.07f;
                    if (fadeAlpha1 <= 0f) {
                        fadeAlpha1 = 1.0f;
                        currentBigPhoto1 = nextBigPhoto1;
                        nextBigPhoto1 = null;
                        fadeTimer1.stop();
                    }
                    bigPhotoLabel1.repaint();
                }
            });
            fadeTimer1.start();
        } else {
            ImageIcon newPhoto = (charName != null) ? loadBigPhoto(charName) : null;
            if (newPhoto == currentBigPhoto2) return;
            nextBigPhoto2 = newPhoto;
            if (fadeTimer2 != null && fadeTimer2.isRunning()) fadeTimer2.stop();
            fadeAlpha2 = 1.0f;
            fadeTimer2 = new Timer(15, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fadeAlpha2 -= 0.07f;
                    if (fadeAlpha2 <= 0f) {
                        fadeAlpha2 = 1.0f;
                        currentBigPhoto2 = nextBigPhoto2;
                        nextBigPhoto2 = null;
                        fadeTimer2.stop();
                    }
                    bigPhotoLabel2.repaint();
                }
            });
            fadeTimer2.start();
        }
    }

    private void setBigPhoto(int player, String charName) {
        if (player == 1) {
            currentBigPhoto1 = loadBigPhoto(charName);
            nextBigPhoto1 = null;
            fadeAlpha1 = 1.0f;
            bigPhotoLabel1.repaint();
        } else {
            currentBigPhoto2 = loadBigPhoto(charName);
            nextBigPhoto2 = null;
            fadeAlpha2 = 1.0f;
            bigPhotoLabel2.repaint();
        }
    }

    private ImageIcon loadBigPhoto(String charName) {
        try {
            // As imagens devem estar em /images/characters/<nome>/foto.png no classpath
            java.net.URL url = getClass().getResource("/images/characters/" + charName.toLowerCase() + "/foto.png");
            if (url == null) return null;
            return new ImageIcon(url);
        } catch (Exception e) {
            return null;
        }
    }

    private void hideIconsSmoothly() {
        // Timer para esconder os ícones gradualmente
        Timer hideTimer = new Timer(50, new ActionListener() {
            private float iconAlpha = 1.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                iconAlpha -= 0.05f; // Diminui a opacidade gradualmente
                if (iconAlpha <= 0f) {
                    iconAlpha = 0f;
                    iconsHidden = true;
                    ((Timer) e.getSource()).stop();
                }
                
                // Aplica a opacidade aos ícones
                for (CharacterButton btn : characterButtons) {
                    btn.setVisible(iconAlpha > 0f);
                }
                
                // Esconde também o indicador do jogador
                if (playerIndicator != null) {
                    playerIndicator.setVisible(iconAlpha > 0f);
                }
                
                repaint();
            }
        });
        hideTimer.start();
    }

    private void startBackgroundTransition() {
        // Timer para transição suave do fundo
        transitionTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transitionAlpha += 0.02f; // Aumenta a opacidade gradualmente
                if (transitionAlpha >= 1.0f) {
                    transitionAlpha = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            }
        });
        transitionTimer.start();
    }
}
