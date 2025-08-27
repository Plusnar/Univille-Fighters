package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class ArenaSelectionScreen extends JPanel implements ActionListener {
    private final GameWindow window;
    private final Image background;
    private final List<ArenaButton> arenaButtons = new ArrayList<>();
    private String selectedArena = null;
    private boolean hoverPlayed = false;
    private JLabel previewLabel;
    private JLabel arenaNameLabel;
    private JButton backButton;
    private Timer countdownTimer;
    private int countdown = 3;
    private JLabel countdownLabel;
    private JLayeredPane layeredPane;
    private JLabel titleLabel;
    private final Character player1Character;
    private final Character player2Character;
    private boolean fightStarted = false;

    // Nomes das arenas
    private final String[] arenaNames = {
        "Arena 2", "Arena 3",
        "Arena 4", "Arena 5", "Arena 6",
        "Arena 7", "Arena 8", "Arena 9"
    };

    public ArenaSelectionScreen(GameWindow window, Character player1Character, Character player2Character) {
        this.window = window;
        this.player1Character = player1Character;
        this.player2Character = player2Character;
        setLayout(new BorderLayout());
        setFocusable(true);

        // Inicia a música de seleção de arena
        AudioManager.getInstance().playMusic("arena_selection", true);

        java.net.URL bgUrl = getClass().getResource("/images/smoke.gif");
        if (bgUrl != null) {
            background = new ImageIcon(bgUrl).getImage();
        } else {
            background = null;
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

        // Create title label
        titleLabel = new JLabel("SELECIONE A ARENA DE LUTA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBounds(0, 30, getWidth(), 60);
        layeredPane.add(titleLabel, JLayeredPane.PALETTE_LAYER);

        // Create preview panel
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(null);
        previewPanel.setOpaque(false);
        previewPanel.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(previewPanel, JLayeredPane.PALETTE_LAYER);

        previewLabel = new JLabel();
        previewLabel.setBounds(0, 0, getWidth(), getHeight());
        previewPanel.add(previewLabel);

        // Create arena name label
        arenaNameLabel = new JLabel("Select Arena", SwingConstants.CENTER);
        arenaNameLabel.setFont(new Font("Arial", Font.BOLD, 32));
        arenaNameLabel.setForeground(Color.WHITE);
        arenaNameLabel.setBounds(0, 100, getWidth(), 40);
        layeredPane.add(arenaNameLabel, JLayeredPane.PALETTE_LAYER);

        int cols = 4;
        int rows = 2;
        int width = 300;
        int height = 150;
        int gap = 0;

        int panelWidth = 1280;
        int panelHeight = 720;

        if (window.getWidth() > 0 && window.getHeight() > 0) {
            panelWidth = window.getWidth();
            panelHeight = window.getHeight();
        }

        int totalWidth = cols * width + (cols - 1) * gap;
        int startX = (panelWidth - totalWidth) / 2;
        int startY = panelHeight - (rows * (height + gap)) - 50;

        for (int i = 0; i < arenaNames.length; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = startX + col * (width + gap);
            int y = startY + row * (height + gap);

            ArenaButton btn = new ArenaButton(arenaNames[i], x, y, width, height, i);
            arenaButtons.add(btn);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedArena = btn.name;
                    btn.setSelected(true);
                    startCountdown();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!hoverPlayed) {
                        hoverPlayed = true;
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverPlayed = false;
                }
            });

            layeredPane.add(btn, JLayeredPane.PALETTE_LAYER);
        }

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
    public void removeNotify() {
        super.removeNotify();
    }

    private void startCountdown() {
        countdownLabel.setVisible(true);
        countdown = 3;
        countdownLabel.setText(String.valueOf(countdown));

        countdownTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown > 0) {
                countdownLabel.setText(String.valueOf(countdown));
            } else {
                countdownTimer.stop();
                countdownLabel.setVisible(false);
                startFight();
            }
        });
        countdownTimer.start();
    }

    private void startFight() {
        if (fightStarted) return;
        fightStarted = true;
        window.switchTo(new ArenaScreen(window, player1Character, player2Character, selectedArena));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Implementação do ActionListener se necessário
    }

    private static class ArenaButton extends JLabel {
        private final String name;
        private boolean isSelected = false;
        private Color borderColor = Color.BLACK;
        private final int index;
        private ImageIcon arenaIcon;

        public ArenaButton(String name, int x, int y, int width, int height, int index) {
            this.name = name;
            this.index = index;
            setBounds(x, y, width, height);
            loadArenaImage(width, height);
            setBorder(BorderFactory.createLineBorder(borderColor, 3));
            setOpaque(false);
        }

        private void loadArenaImage(int width, int height) {
            try {
                String imagePath = "/images/arenas/" + name.toLowerCase().replace(" ", "_") + ".png";
                java.net.URL arenaUrl = getClass().getResource(imagePath);
                if (arenaUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(arenaUrl);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    arenaIcon = new ImageIcon(scaledImage);
                    setIcon(arenaIcon);
                } else {
                    setBackground(new Color(100, 100, 100));
                    setOpaque(true);
                }
            } catch (Exception e) {
                setBackground(new Color(100, 100, 100));
                setOpaque(true);
            }
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
            borderColor = selected ? Color.GREEN : Color.BLACK;
            setBorder(BorderFactory.createLineBorder(borderColor, 3));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isSelected) {
                g.setColor(new Color(0, 255, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
} 