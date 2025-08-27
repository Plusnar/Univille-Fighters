package com.luta2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.io.File;
import java.util.Random;

public class ArenaScreen extends JPanel implements ActionListener, KeyListener {
    private final GameWindow window;
    private final Timer timer;
    private final Player player1;
    private final Player player2;
    private final HashSet<Integer> keysPressed = new HashSet<>();
    private final Image background;
    private final String arenaName;

    // Sistema de rounds
    private int currentRound = 1;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private boolean roundEnded = false;
    private boolean matchEnded = false;
    private long roundEndTime = 0;
    private long matchEndTime = 0;
    private String winnerName = "";
    private boolean showWinnerMessage = false;
    private String roundWinnerName = ""; // Vencedor do round atual
    private boolean showRoundWinnerMessage = false; // Mostra mensagem de vitória do round
    private Timer roundTimer;
    private Timer matchTimer;

    // Sistema de countdown
    private Timer countdownTimer;
    private int countdown = 3;
    private JLabel countdownLabel;
    private boolean gameStarted = false;

    // Base dimensions (1920x1080)
    private static final int BASE_WIDTH = 1920;
    private static final int BASE_HEIGHT = 1080;
    private static final int GROUND_Y_POSITION = BASE_HEIGHT - 224; // Posição Y do chão
    private static final int PLAYER_HEIGHT = 400; // Altura do player
    
    // Posições iniciais dos jogadores
    private static final int PLAYER1_INITIAL_X = 450;
    private static final int PLAYER2_INITIAL_X = 1350;
    private static final int PLAYER_INITIAL_Y = GROUND_Y_POSITION - PLAYER_HEIGHT;

    public ArenaScreen(GameWindow window, Character char1, Character char2, String arenaName) {
        System.out.println("[DEBUG] ArenaScreen criado!");
        this.window = window;
        this.arenaName = arenaName;
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);
        
        // A posição Y inicial do jogador é calculada para que seus "pés" toquem o chão.
        int playerInitialY = GROUND_Y_POSITION - PLAYER_HEIGHT;

        // Initialize players with base positions
        player1 = new Player(char1, PLAYER1_INITIAL_X, PLAYER_INITIAL_Y, GROUND_Y_POSITION);
        player2 = new Player(char2, PLAYER2_INITIAL_X, PLAYER_INITIAL_Y, GROUND_Y_POSITION);
        player1.setOpponent(player2);
        player2.setOpponent(player1);

        // Música fixa da arena: arena5.wav
        AudioManager.getInstance().playMusic("arena5", true);

        // Carrega a imagem de fundo da arena
        String backgroundResource = "/images/arenas/" + arenaName.toLowerCase().replace(" ", "_") + "_background.png";
        java.net.URL bgUrl = getClass().getResource(backgroundResource);
        if (bgUrl != null) {
            background = new ImageIcon(bgUrl).getImage();
        } else {
            System.err.println("Imagem de fundo da arena não encontrada: " + backgroundResource);
            background = null;
        }

        timer = new Timer(1000 / 60, this);
        timer.start();

        // Inicializa o label do countdown
        countdownLabel = new JLabel("3", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 72));
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setBounds(0, 0, getWidth(), getHeight());
        add(countdownLabel);

        // Inicia o countdown
        countdownTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown > 0) {
                // Atualiza o label do countdown
                countdownLabel.setText(String.valueOf(countdown));
            } else {
                // Countdown terminou, inicia a luta
                countdownTimer.stop();
                countdownLabel.setVisible(false);
                gameStarted = true;
                startGame();
            }
        });
        countdownTimer.start();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Get current scale factor
        double scale = window.getScaleFactor();
        
        // Scale the graphics context
        g2d.scale(scale, scale);

        // Draw background
        g2d.drawImage(background, 0, 0, BASE_WIDTH, BASE_HEIGHT, this);

        // Draw players
        player1.draw(g2d);
        player2.draw(g2d);

        // Draw health bars
        g2d.setColor(Color.GREEN);
        g2d.fillRect(75, 45, player1.getCharacter().getHealth() * 3, 30);
        g2d.fillRect(BASE_WIDTH - 75 - player2.getCharacter().getHealth() * 3, 45, 
                    player2.getCharacter().getHealth() * 3, 30);

        // Draw special attack cooldown bars
        drawSpecialAttackBar(g2d, 75, 85, player1); // Player 1 (esquerda)
        drawSpecialAttackBar(g2d, BASE_WIDTH - 75 - 300, 85, player2); // Player 2 (direita)

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(player1.getCharacter().getName(), 75, 35);
        g2d.drawString(player2.getCharacter().getName(), BASE_WIDTH - 270, 35);

        // Desenha informações dos rounds
        drawRoundInfo(g2d);
        
        // Desenha mensagem de vitória do round
        if (showRoundWinnerMessage) {
            drawRoundWinnerMessage(g2d);
        }
        
        // Desenha mensagem de vitória do match
        if (showWinnerMessage) {
            drawWinnerMessage(g2d);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Se o round ou match terminou, não processa mais inputs
        if (roundEnded || matchEnded) {
            return;
        }

        boolean p1Moving = keysPressed.contains(KeyEvent.VK_A) || keysPressed.contains(KeyEvent.VK_D);
        if (keysPressed.contains(KeyEvent.VK_A)) player1.moveLeft();
        if (keysPressed.contains(KeyEvent.VK_D)) player1.moveRight();
        if (keysPressed.contains(KeyEvent.VK_W)) player1.jump();
        boolean p1Crouching = keysPressed.contains(KeyEvent.VK_S);
        player1.setCrouching(p1Crouching);
        boolean p1Blocking = keysPressed.contains(KeyEvent.VK_Q);
        player1.setBlocking(p1Blocking);

        boolean p2Moving = keysPressed.contains(KeyEvent.VK_LEFT) || keysPressed.contains(KeyEvent.VK_RIGHT);
        if (keysPressed.contains(KeyEvent.VK_LEFT)) player2.moveLeft();
        if (keysPressed.contains(KeyEvent.VK_RIGHT)) player2.moveRight();
        if (keysPressed.contains(KeyEvent.VK_UP)) player2.jump();
        boolean p2Crouching = keysPressed.contains(KeyEvent.VK_DOWN);
        player2.setCrouching(p2Crouching);
        boolean p2Blocking = keysPressed.contains(KeyEvent.VK_NUMPAD2);
        player2.setBlocking(p2Blocking);

        // Attack methods
        if (keysPressed.contains(KeyEvent.VK_SPACE)) player1.punch(player2);
        if (keysPressed.contains(KeyEvent.VK_F)) player1.kick(player2);
        if (keysPressed.contains(KeyEvent.VK_G)) player1.specialAttack(player2);

        if (keysPressed.contains(KeyEvent.VK_ENTER)) player2.punch(player1);
        if (keysPressed.contains(KeyEvent.VK_NUMPAD0)) player2.kick(player1);
        if (keysPressed.contains(KeyEvent.VK_NUMPAD1)) player2.specialAttack(player1);

        // Finalização - qualquer ataque pode finalizar um oponente nocauteado
        if (player2.canBeFinished()) {
            if (keysPressed.contains(KeyEvent.VK_SPACE) || keysPressed.contains(KeyEvent.VK_F) || keysPressed.contains(KeyEvent.VK_G)) {
                player2.finish(player1.isFacingRight());
            }
        }
        if (player1.canBeFinished()) {
            if (keysPressed.contains(KeyEvent.VK_ENTER) || keysPressed.contains(KeyEvent.VK_NUMPAD0) || keysPressed.contains(KeyEvent.VK_NUMPAD1)) {
                player1.finish(player2.isFacingRight());
            }
        }

        // Update animation states based on input
        player1.updateAnimationState(p1Moving, p1Crouching, p1Blocking);
        player2.updateAnimationState(p2Moving, p2Crouching, p2Blocking);

        player1.update();
        player2.update();

        // Keep players within bounds (exceto durante knockback final)
        if (!player1.isFinalKnockback()) {
            int leftLimit = 0;
            int rightLimit = (int)(BASE_WIDTH - player1.getWidth());
            player1.setX(Math.max(leftLimit, Math.min(rightLimit, player1.getX())));
        }
        if (!player2.isFinalKnockback()) {
            int leftLimit = 0;
            int rightLimit = (int)(BASE_WIDTH - player2.getWidth());
            player2.setX(Math.max(leftLimit, Math.min(rightLimit, player2.getX())));
        }

        // Verifica se um round terminou
        checkRoundEnd();

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keysPressed.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysPressed.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void drawRoundInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Round atual
        String roundText = "ROUND " + currentRound;
        FontMetrics fm = g2d.getFontMetrics();
        int roundX = (BASE_WIDTH - fm.stringWidth(roundText)) / 2;
        g2d.drawString(roundText, roundX, 35);
        
        // Placar
        String scoreText = player1Wins + " - " + player2Wins;
        int scoreX = (BASE_WIDTH - fm.stringWidth(scoreText)) / 2;
        g2d.drawString(scoreText, scoreX, 60);
    }

    private void drawWinnerMessage(Graphics2D g2d) {
        // Fundo semi-transparente
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);
        
        // Mensagem de vitória
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        String winnerText = winnerName + " VENCE!";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (BASE_WIDTH - fm.stringWidth(winnerText)) / 2;
        int textY = BASE_HEIGHT / 2;
        g2d.drawString(winnerText, textX, textY);
        
        // Subtexto
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        String subText = "Retornando ao menu...";
        int subX = (BASE_WIDTH - fm.stringWidth(subText)) / 2;
        g2d.drawString(subText, subX, textY + 60);
    }

    private void drawRoundWinnerMessage(Graphics2D g2d) {
        // Fundo semi-transparente
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);
        
        // Mensagem de vitória do round
        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String roundText = "ROUND " + currentRound + " - " + roundWinnerName + " VENCE!";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (BASE_WIDTH - fm.stringWidth(roundText)) / 2;
        int textY = BASE_HEIGHT / 2;
        g2d.drawString(roundText, textX, textY);
        
        // Subtexto
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        String subText = "Próximo round em 3 segundos...";
        int subX = (BASE_WIDTH - fm.stringWidth(subText)) / 2;
        g2d.drawString(subText, subX, textY + 50);
    }

    private void drawSpecialAttackBar(Graphics2D g2d, int x, int y, Player player) {
        int barWidth = 300;
        int barHeight = 15;
        
        // Desenha o fundo da barra (cinza escuro)
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x, y, barWidth, barHeight);
        
        // Calcula o progresso do cooldown
        double progress = player.getSpecialAttackProgress();
        int fillWidth = (int)(barWidth * progress);
        
        // Desenha a barra de progresso
        if (progress >= 1.0) {
            // Verde quando pronto
            g2d.setColor(Color.GREEN);
            
            // Adiciona efeito de brilho quando pronto
            long currentTime = System.currentTimeMillis();
            float glowIntensity = (float)(0.7 + 0.3 * Math.sin(currentTime * 0.01)); // Efeito pulsante
            g2d.setColor(new Color(0, (int)(255 * glowIntensity), 0));
        } else {
            // Laranja quando carregando
            g2d.setColor(new Color(255, 165, 0));
        }
        g2d.fillRect(x, y, fillWidth, barHeight);
        
        // Desenha a borda da barra
        if (progress >= 1.0) {
            // Borda brilhante quando pronto
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(3));
        } else {
            // Borda normal quando carregando
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
        }
        g2d.drawRect(x, y, barWidth, barHeight);
        
        // Desenha o texto "ESPECIAL"
        if (progress >= 1.0) {
            // Texto brilhante quando pronto
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
        } else {
            // Texto normal quando carregando
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
        }
        String text = "ESPECIAL";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(text)) / 2;
        int textY = y + (barHeight + fm.getAscent()) / 2;
        g2d.drawString(text, textX, textY);
        
        // Adiciona texto "PRONTO!" quando o golpe especial estiver disponível
        if (progress >= 1.0) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String readyText = "PRONTO!";
            FontMetrics fmReady = g2d.getFontMetrics();
            int readyX = x + (barWidth - fmReady.stringWidth(readyText)) / 2;
            int readyY = y + barHeight + 15;
            g2d.drawString(readyText, readyX, readyY);
        }
    }

    private void checkRoundEnd() {
        // Verifica se um jogador foi finalizado
        if (player1.isBeingFinished() || player2.isBeingFinished()) {
            if (!roundEnded) {
                roundEnded = true;
                roundEndTime = System.currentTimeMillis();
                
                // Determina o vencedor do round
                if (player1.isBeingFinished()) {
                    player2Wins++;
                    roundWinnerName = player2.getCharacter().getName();
                } else {
                    player1Wins++;
                    roundWinnerName = player1.getCharacter().getName();
                }
                
                // Mostra mensagem de vitória do round
                showRoundWinnerMessage = true;
                
                // Inicia timer para próximo round ou fim do match
                roundTimer = new Timer(3000, e -> {
                    showRoundWinnerMessage = false; // Esconde mensagem do round
                    if (currentRound < 3) {
                        startNewRound();
                    } else {
                        endMatch();
                    }
                    ((Timer)e.getSource()).stop();
                });
                roundTimer.setRepeats(false);
                roundTimer.start();
            }
        }
    }

    private void startNewRound() {
        currentRound++;
        roundEnded = false;
        showRoundWinnerMessage = false; // Esconde mensagem do round anterior
        
        // Reseta os jogadores para posições iniciais
        player1.resetForNewRound();
        player2.resetForNewRound();
        player1.setX(PLAYER1_INITIAL_X);
        player1.setY(PLAYER_INITIAL_Y);
        player2.setX(PLAYER2_INITIAL_X);
        player2.setY(PLAYER_INITIAL_Y);
    }

    private void endMatch() {
        matchEnded = true;
        matchEndTime = System.currentTimeMillis();
        showRoundWinnerMessage = false; // Esconde mensagem do round
        
        // Determina o vencedor final
        if (player1Wins > player2Wins) {
            winnerName = player1.getCharacter().getName();
        } else {
            winnerName = player2.getCharacter().getName();
        }
        
        // Mostra mensagem de vitória final
        showWinnerMessage = true;
        
        // Timer para voltar ao menu
        matchTimer = new Timer(5000, e -> {
            window.switchTo(new CharacterSelectionScreen(window));
            ((Timer)e.getSource()).stop();
        });
        matchTimer.setRepeats(false);
        matchTimer.start();
    }

    private void startGame() {
        // O jogo já está iniciado, apenas garante que o foco está correto
        requestFocusInWindow();
    }
}
