package com.luta2d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Font;
import javax.swing.Timer;

public class Player {
    private Character character;
    private int x, y;
    private int width = 400, height = 400;
    private int vy = 0;
    private boolean jumping = false;
    private boolean crouching = false;
    private boolean blocking = false;
    private boolean facingRight = true;
    private final int groundY;
    private static Image characterImage; // Imagem estática compartilhada entre todos os jogadores
    private Player opponent; // Referência ao oponente
    
    // Configurações de movimento
    private final int moveSpeed;
    private final int jumpHeight;
    private final double attackDamage;
    private final double blockReduction;
    private final int gravity = 2;
    private final int maxFallSpeed = 20;
    private final int jumpStartSpeed = -25;
    private final int attackCooldown = 500; // 500ms entre ataques
    private long lastAttackTime = 0;
    private final int specialAttackCooldown = 20000; // 20 segundos de cooldown para o especial
    private long lastSpecialAttackTime = 0;
    private final int attackAnimationDuration = 500; // ms for attack animation to have priority
    private long lastDamageTime = 0;
    private final int damageAnimationDuration = 400; // ms for hit stun animation
    private boolean knockedOut = false;
    private boolean isKnockedBack = false;
    private long knockbackEndTime = 0;
    private int knockbackVx = 0;
    private double rotationAngle = 0;
    private double rotationSpeed = 0;
    
    // Sistema de finalização
    private boolean canBeFinished = false;
    private boolean isBeingFinished = false;
    private long finishStartTime = 0;
    private final int finishAnimationDuration = 3000; // 3 segundos para a finalização
    private boolean finalKnockback = false;
    private int finalKnockbackVx = 0;
    private int finalKnockbackVy = 0;

    // Novo sistema de animação por spritesheet
    private class Animation {
        Image spritesheet;
        int frameWidth;
        int frameHeight;
        int frameCount;
        int frameDuration; // Duração específica para cada animação
    }
    private Map<String, Animation> animations = new HashMap<>();
    private String currentAnimation = "parado";
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final int defaultFrameDuration = 50; // ms por frame (padrão)
    private String characterFolder;

    // Efeito de sangue
    private static class BloodEffect {
        int x, y;
        int currentFrame = 0;
        long lastFrameTime = 0;
        int frameCount;
        int frameWidth;
        int frameHeight;
        BufferedImage spritesheet;
        boolean finished = false;
        public BloodEffect(int x, int y, BufferedImage spritesheet, int frameCount, int frameWidth, int frameHeight) {
            this.x = x;
            this.y = y;
            this.spritesheet = spritesheet;
            this.frameCount = frameCount;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.lastFrameTime = System.currentTimeMillis();
        }
    }
    private java.util.List<BloodEffect> bloodEffects = new ArrayList<>();
    private static BufferedImage bloodSpritesheet = null;
    private static int bloodFrameCount = 0;
    private static int bloodFrameWidth = 0;
    private static int bloodFrameHeight = 0;
    private static boolean bloodLoaded = false;

    private static Image shadowImage = null;

    // Sistema de números de dano flutuantes
    private static class DamageNumber {
        int x, y;
        int damage;
        long creationTime;
        int vy = -2; // Velocidade vertical (sobe)
        int vx = 0; // Velocidade horizontal (pode ser aleatória)
        boolean finished = false;
        Color color;
        
        public DamageNumber(int x, int y, int damage, boolean isSpecial) {
            this.x = x;
            this.y = y;
            this.damage = damage;
            this.creationTime = System.currentTimeMillis();
            this.vx = (int)(Math.random() * 6 - 3); // Movimento horizontal aleatório
            this.color = isSpecial ? Color.RED : Color.YELLOW; // Vermelho para golpes especiais
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += 1; // Gravidade faz o número descer
            if (System.currentTimeMillis() - creationTime > 2000) { // 2 segundos de vida
                finished = true;
            }
        }
    }
    private java.util.List<DamageNumber> damageNumbers = new ArrayList<>();

    // Sistema de projétil para Jean
    private static class Projectile {
        int x, y;
        int width = 300, height = 85; // Tamanho do giz.png
        boolean facingRight;
        int speed = 12;
        boolean active = false;
        long creationTime;
        final long maxLifetime = 3000; // 3 segundos de vida máxima
        
        public Projectile(int x, int y, boolean facingRight) {
            this.x = x;
            this.y = y;
            this.facingRight = facingRight;
            this.active = true;
            this.creationTime = System.currentTimeMillis();
        }
        
        public void update() {
            if (!active) return;
            
            // Move o projétil na direção que o Jean está olhando
            if (facingRight) {
                x += speed;
            } else {
                x -= speed;
            }
            
            // Verifica se o projétil expirou
            if (System.currentTimeMillis() - creationTime > maxLifetime) {
                active = false;
            }
        }
        
        public boolean isCollidingWith(Player player) {
            if (!active) return false;
            
            // Hitbox do projétil (giz)
            int projLeft = x;
            int projRight = x + width;
            int projTop = y;
            int projBottom = y + height;
            
            // Define a hitbox do jogador mais estreita (150px de largura) e centralizada
            int playerHitboxWidth = 150;
            int playerLeft = player.getX() + (player.getWidth() - playerHitboxWidth) / 2;
            int playerRight = playerLeft + playerHitboxWidth;
            int playerTop = player.getY();
            int playerBottom = player.getY() + player.getHeight();
            
            // Verifica colisão
            if (projRight >= playerLeft && projLeft <= playerRight &&
                projBottom >= playerTop && projTop <= playerBottom) {
                return true;
            }
            
            return false;
        }
    }
    
    // Sistema de raio para Anna
    private static class LightningEffect {
        int x, y;
        int width = 400, height = 1100; // Dimensões do raio.png
        boolean active = false;
        long creationTime;
        final long fallDuration = 800; // 800ms para o raio cair
        final long hitDuration = 300; // 300ms para o efeito de dano
        boolean hasHit = false;
        int groundY; // Adiciona groundY como campo da classe
        
        public LightningEffect(int targetX, int groundY) {
            // Centraliza o raio na posição do alvo
            this.x = targetX - (width / 2);
            this.y = -height; // Começa acima da tela
            this.active = true;
            this.creationTime = System.currentTimeMillis();
            this.groundY = groundY; // Armazena o groundY
        }
        
        public void update() {
            if (!active) return;
            
            long elapsed = System.currentTimeMillis() - creationTime;
            
            if (elapsed < fallDuration) {
                // Calcula a posição Y durante a queda
                float progress = (float) elapsed / fallDuration;
                this.y = (int) (-height + (progress * (groundY + height)));
            } else if (elapsed < fallDuration + hitDuration) {
                // Raio atingiu o chão, mantém posição
                this.y = groundY;
                if (!hasHit) {
                    hasHit = true;
                }
            } else {
                // Efeito terminou
                active = false;
            }
        }
        
        public boolean isCollidingWith(Player player) {
            if (!active || !hasHit) return false;
            
            // Hitbox do raio - 100 pixels de largura centralizada
            int hitboxWidth = 100;
            int lightningCenterX = x + (width / 2);
            int lightningLeft = lightningCenterX - (hitboxWidth / 2);
            int lightningRight = lightningCenterX + (hitboxWidth / 2);
            int lightningTop = y;
            int lightningBottom = y + height;
            
            // Hitbox do jogador
            int playerHitboxWidth = 150;
            int playerLeft = player.getX() + (player.getWidth() - playerHitboxWidth) / 2;
            int playerRight = playerLeft + playerHitboxWidth;
            int playerTop = player.getY();
            int playerBottom = player.getY() + player.getHeight();
            
            // Verifica colisão
            return lightningRight >= playerLeft && lightningLeft <= playerRight &&
                   lightningBottom >= playerTop && lightningTop <= playerBottom;
        }
    }
    
    // Sistema de urso para Erik
    private static class BearEffect {
        int x, y;
        int width = 400, height = 987; // Dimensões do urso.png
        boolean active = false;
        long creationTime;
        final long fallDuration = 1400; // 1400ms para o urso cair (ainda mais lento)
        final long hitDuration = 500; // 500ms para o efeito de dano (ainda mais lento)
        boolean hasHit = false;
        int groundY; // Adiciona groundY como campo da classe
        
        public BearEffect(int targetX, int groundY) {
            // Centraliza o urso na posição do alvo
            this.x = targetX - (width / 2);
            this.y = -height; // Começa acima da tela
            this.active = true;
            this.creationTime = System.currentTimeMillis();
            this.groundY = groundY; // Armazena o groundY
        }
        
        public void update() {
            if (!active) return;
            
            long elapsed = System.currentTimeMillis() - creationTime;
            
            if (elapsed < fallDuration) {
                // Calcula a posição Y durante a queda
                float progress = (float) elapsed / fallDuration;
                this.y = (int) (-height + (progress * (groundY + height)));
            } else if (elapsed < fallDuration + hitDuration) {
                // Urso atingiu o chão, mantém posição
                this.y = groundY;
                if (!hasHit) {
                    hasHit = true;
                }
            } else {
                // Efeito terminou
                active = false;
            }
        }
        
        public boolean isCollidingWith(Player player) {
            if (!active || !hasHit) return false;
            
            // Hitbox do urso - 150 pixels de largura centralizada
            int hitboxWidth = 150;
            int bearCenterX = x + (width / 2);
            int bearLeft = bearCenterX - (hitboxWidth / 2);
            int bearRight = bearCenterX + (hitboxWidth / 2);
            int bearTop = y;
            int bearBottom = y + height;
            
            // Hitbox do jogador
            int playerHitboxWidth = 150;
            int playerLeft = player.getX() + (player.getWidth() - playerHitboxWidth) / 2;
            int playerRight = playerLeft + playerHitboxWidth;
            int playerTop = player.getY();
            int playerBottom = player.getY() + player.getHeight();
            
            // Verifica colisão
            return bearRight >= playerLeft && bearLeft <= playerRight &&
                   bearBottom >= playerTop && bearTop <= playerBottom;
        }
    }
    
    private Projectile currentProjectile = null;
    private static Image projectileImage = null; // Imagem do giz.png
    
    // Efeito de raio da Anna
    private LightningEffect currentLightning = null;
    private static Image lightningImage = null; // Imagem do raio.png

    // Efeito de urso do Erik
    private BearEffect currentBear = null;
    private static Image bearImage = null; // Imagem do urso.png

    // Aura especial do Cesar
    private static Image waveAuraImage = null;
    private boolean auraActive = false;
    private long auraStartTime = 0;
    private float auraAlpha = 1.0f;
    private final int auraDuration = 3000; // 3 segundos
    private final int auraFadeDuration = 500; // 0.5 segundos finais para fade

    private static class LapisProjectile {
        int x, y;
        int width = 300, height = 200; // Tamanho do lapis.png
        int speed = 16;
        boolean facingRight;
        boolean active = false;
        long creationTime;
        final long maxLifetime = 3000; // 3 segundos de vida máxima
        public LapisProjectile(int x, int y, boolean facingRight) {
            this.x = x;
            this.y = y;
            this.facingRight = facingRight;
            this.active = true;
            this.creationTime = System.currentTimeMillis();
        }
        public void update() {
            if (!active) return;
            if (facingRight) {
                x += speed;
            } else {
                x -= speed;
            }
            if (System.currentTimeMillis() - creationTime > maxLifetime) {
                active = false;
            }
        }
        public boolean isCollidingWith(Player player) {
            if (!active) return false;
            int projLeft = x;
            int projRight = x + width;
            int projTop = y;
            int projBottom = y + height;
            int playerHitboxWidth = 150;
            int playerLeft = player.getX() + (player.getWidth() - playerHitboxWidth) / 2;
            int playerRight = playerLeft + playerHitboxWidth;
            int playerTop = player.getY();
            int playerBottom = player.getY() + player.getHeight();
            return projRight >= playerLeft && projLeft <= playerRight &&
                   projBottom >= playerTop && projTop <= playerBottom;
        }
    }
    private LapisProjectile currentLapis = null;
    private static Image lapisImage = null; // Imagem do lapis.png

    // Clone da Giovana
    private static class GiovanaClone {
        int x, y;
        boolean facingRight;
        long creationTime;
        float alpha = 1.0f;
        static final long DURATION = 5000; // 5 segundos
        public GiovanaClone(int x, int y, boolean facingRight) {
            this.x = x;
            this.y = y;
            this.facingRight = facingRight;
            this.creationTime = System.currentTimeMillis();
        }
        public float getAlpha() {
            long elapsed = System.currentTimeMillis() - creationTime;
            if (elapsed >= DURATION) return 0f;
            return 1.0f - (float)elapsed / DURATION;
        }
        public boolean isAlive() {
            return System.currentTimeMillis() - creationTime < DURATION;
        }
    }
    private GiovanaClone giovanaClone = null;

    public Player(Character character, int x, int y, int groundY) {
        this.character = character;
        this.x = x;
        this.y = y;
        this.groundY = groundY;
        
        // Define a direção inicial com base na posição X (maior que o meio da tela, vira pra esquerda)
        if (x > 1920 / 2) {
            facingRight = false;
        }

        // Configurações baseadas no personagem
        this.moveSpeed = character.getMoveSpeed();
        this.jumpHeight = character.getJumpHeight();
        this.attackDamage = character.getAttackDamage();
        this.blockReduction = character.getBlockReduction();
        this.characterFolder = character.getName().toLowerCase();
        loadAnimations();
        
        // Carrega a imagem do personagem apenas uma vez
        if (characterImage == null) {
            try {
                characterImage = new ImageIcon(getClass().getResource("/images/char.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem do personagem: " + e.getMessage());
            }
        }

        // Carrega a sombra apenas uma vez
        if (shadowImage == null) {
            try {
                shadowImage = new ImageIcon(getClass().getResource("/images/sombra.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar sombra: " + e.getMessage());
            }
        }

        // Carrega a imagem do raio apenas uma vez
        if (lightningImage == null) {
            try {
                lightningImage = new ImageIcon(getClass().getResource("/images/effects/raio.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar raio.png: " + e.getMessage());
            }
        }

        // Carrega a imagem do giz para o projétil do Jean
        if (projectileImage == null) {
            try {
                projectileImage = new ImageIcon(getClass().getResource("/images/effects/giz.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar giz: " + e.getMessage());
            }
        }

        // Carrega a imagem da aura do Cesar apenas uma vez
        if (waveAuraImage == null) {
            try {
                waveAuraImage = new ImageIcon(getClass().getResource("/images/effects/wave.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar wave.png: " + e.getMessage());
            }
        }

        // Carrega a imagem do urso para o Erik apenas uma vez
        if (bearImage == null) {
            try {
                bearImage = new ImageIcon(getClass().getResource("/images/effects/urso.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar urso.png: " + e.getMessage());
            }
        }

        // Carrega a imagem do lápis para o William
        if (lapisImage == null) {
            try {
                lapisImage = new ImageIcon(getClass().getResource("/images/effects/lapis.png")).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar lapis.png: " + e.getMessage());
            }
        }
    }

    private void loadAnimations() {
        String[] anims = {"parado", "andando", "agachar", "pulo", "soco", "chute", "bloqueio", "golpe_especial", "tonto", "dano"};
        for (String anim : anims) {
            String path = String.format("/images/characters/%s/%s.png", characterFolder, anim);
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(path));
                Image spritesheet = icon.getImage();

                // Define as dimensões e calcula a contagem de frames dinamicamente
                int frameWidth = 400;
                int frameHeight = 400;
                int frameCount = 1; // Padrão de 1 frame se a imagem não carregar

                if (spritesheet != null && icon.getIconWidth() > 0) {
                    frameCount = icon.getIconWidth() / frameWidth;
                    // Adiciona um aviso se a largura não for um múltiplo perfeito
                    // if (icon.getIconWidth() % frameWidth != 0) {
                    //     System.err.println("Aviso: A largura do spritesheet " + path + " não é um múltiplo perfeito de " + frameWidth);
                    // }
                }
                
                // Define durações específicas para cada animação
                int frameDuration = defaultFrameDuration; // Duração padrão
                switch (anim) {
                    case "parado":
                        frameDuration = 50; // Animação parado padronizada
                        break;
                    case "andando":
                        frameDuration = 50; // Animação andando padronizada
                        break;
                    case "agachar":
                        frameDuration = 50; // Animação agachar padronizada
                        break;
                    case "pulo":
                        frameDuration = 50; // Animação pulo padronizada
                        break;
                    case "soco":
                        frameDuration = 50; // Animação soco padronizada
                        break;
                    case "chute":
                        frameDuration = 50; // Animação chute padronizada
                        break;
                    case "bloqueio":
                        frameDuration = 50; // Animação bloqueio padronizada
                        break;
                    case "golpe_especial":
                        frameDuration = 50; // Animação golpe especial padronizada
                        break;
                    case "tonto":
                        frameDuration = 50; // Animação tonto padronizada
                        break;
                    case "dano":
                        frameDuration = 50; // Animação dano padronizada
                        break;
                }
                
                Animation animation = new Animation();
                animation.spritesheet = spritesheet;
                animation.frameWidth = frameWidth;
                animation.frameHeight = frameHeight;
                animation.frameCount = frameCount;
                animation.frameDuration = frameDuration; // Duração específica para cada animação
                animations.put(anim, animation);
            } catch (Exception e) {
                // fallback: não encontrou spritesheet
                System.err.println("Aviso: Spritesheet não encontrado para " + characterFolder + "/" + anim);
            }
        }
        // fallback: se não houver spritesheet, usa char.png
        if (animations.get("parado") == null) {
            Animation fallback = new Animation();
            fallback.spritesheet = new ImageIcon(getClass().getResource("/images/char.png")).getImage();
            fallback.frameWidth = width;
            fallback.frameHeight = height;
            fallback.frameCount = 1;
            fallback.frameDuration = 50; // Duração padronizada para o fallback também
            animations.put("parado", fallback);
        }
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    private void performAttack(Player opponent, String animationName, double damageMultiplier) {
        if (isKnockedBack || knockedOut) return;
        if (opponent == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackCooldown) {
            return;
        }

        lastAttackTime = currentTime;
        setAnimation(animationName);

        // Se o oponente estiver agachado e o ataque for um soco, o ataque não acerta.
        if (opponent.isCrouching() && animationName.equals("soco")) {
            return; // O ataque erra, mas a animação do atacante e o cooldown ocorrem.
        }

        int distance = Math.abs(this.x - opponent.x);
        boolean isInRange = distance < character.getAttackRange();

        if (isInRange) {
            double finalDamage = character.getAttackDamage() * damageMultiplier;
            // Dano é reduzido em 50% se estiver bloqueando
            double damage = opponent.isBlocking() ?
                finalDamage * character.getBlockReduction() : finalDamage;
            opponent.takeDamage((int)damage, false); // Ataque normal
        }
    }

    // Métodos para atacar outro jogador
    public void punch(Player opponent) {
        if (knockedOut) return;
        performAttack(opponent, "soco", 1.0); // Usa o multiplicador base
    }

    public void kick(Player opponent) {
        if (knockedOut) return;
        performAttack(opponent, "chute", 1.5); // Chute causa 50% mais dano
    }

    public void specialAttack(Player opponent) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpecialAttackTime < specialAttackCooldown) {
            return; // Cooldown global para todos os especiais
        }

        switch (character.getName()) {
            case "Anna":
                lastAttackTime = currentTime;
                lastSpecialAttackTime = currentTime;
                setAnimation("golpe_especial");
                
                // Cria o efeito do raio na posição do oponente
                if (opponent != null) {
                    int targetX = opponent.getX() + (opponent.getWidth() / 2);
                    currentLightning = new LightningEffect(targetX, groundY);
                }
                break;

            case "Jean":
                if (currentProjectile != null && currentProjectile.active) {
                    return; // Impede novo projétil se um já estiver ativo
                }
                lastAttackTime = currentTime;
                lastSpecialAttackTime = currentTime;
                setAnimation("golpe_especial");
                
                // Lógica do projétil do Jean
                int projectileX = facingRight ? x + 100 : x;
                int projectileY = y + 60;
                currentProjectile = new Projectile(projectileX, projectileY, facingRight);
                break;

            case "Cesar":
                lastAttackTime = currentTime;
                lastSpecialAttackTime = currentTime;
                setAnimation("golpe_especial");

                // Ativa a aura
                auraActive = true;
                auraStartTime = System.currentTimeMillis();
                auraAlpha = 1.0f;

                // Lógica de ataque de curta distância do Cesar
                int attackRange = 400; // Alcance do especial
                int distanceX = Math.abs((this.x + this.width / 2) - (opponent.getX() + opponent.getWidth() / 2));
                boolean opponentIsInFront = (this.facingRight && opponent.getX() > this.x) || (!this.facingRight && opponent.getX() < this.x);

                if (distanceX < attackRange && opponentIsInFront && !opponent.isCrouching()) {
                    double damage = character.getSpecialAttackDamage(); // Usa o novo método
                    if (opponent.isBlocking()) {
                        damage *= character.getBlockReduction();
                    }
                    opponent.takeDamage((int)damage, true); // Golpe especial
                    opponent.applyKnockback(this.facingRight);
                }
                break;

            case "Erik":
                lastAttackTime = currentTime;
                lastSpecialAttackTime = currentTime;
                setAnimation("golpe_especial");
                
                // Cria o efeito do urso na posição do oponente
                if (opponent != null) {
                    int targetX = opponent.getX() + (opponent.getWidth() / 2);
                    currentBear = new BearEffect(targetX, groundY);
                }
                break;

            case "William":
                if (currentLapis != null && currentLapis.active) {
                    return; // Impede novo lápis se um já estiver ativo
                }
                lastAttackTime = currentTime;
                lastSpecialAttackTime = currentTime;
                setAnimation("golpe_especial");
                int lapisX = facingRight ? x + 100 : x;
                int lapisY = y + 100;
                currentLapis = new LapisProjectile(lapisX, lapisY, facingRight);
                break;
            
            case "Giovana":
                lastAttackTime = currentTime;
                lastSpecialAttackTime = currentTime;
                setAnimation("golpe_especial");
                // Cria o clone na posição atual
                giovanaClone = new GiovanaClone(x, y, facingRight);
                // Teleporta para uma posição aleatória do mapa (dentro dos limites)
                int minX = 0;
                int maxX = 1920 - width;
                int randomX = minX + (int)(Math.random() * (maxX - minX));
                this.x = randomX;
                // Mantém o mesmo Y (no chão)
                break;
            
            // Outros personagens podem ser adicionados aqui
        }
    }

    public boolean isAttacking() {
        return System.currentTimeMillis() - lastAttackTime < attackAnimationDuration;
    }

    public boolean isAttackingWithProjectile() {
        return currentProjectile != null && currentProjectile.active;
    }

    public boolean isAttackingWithLightning() {
        return currentLightning != null && currentLightning.active;
    }

    public boolean isAttackingWithBear() {
        return currentBear != null && currentBear.active;
    }

    public boolean isTakingDamage() {
        return System.currentTimeMillis() - lastDamageTime < damageAnimationDuration;
    }

    public void update() {
        // Knockback final (arremesso para fora da tela)
        if (finalKnockback) {
            x += finalKnockbackVx;
            y += finalKnockbackVy;
            finalKnockbackVy += 2; // Gravidade
            rotationAngle += 0.8; // Rotação mais rápida durante o voo
            return; // Não atualiza mais nada
        }

        if (isKnockedBack) {
            if (System.currentTimeMillis() > knockbackEndTime) {
                isKnockedBack = false;
                knockbackVx = 0;
                rotationAngle = 0;
                rotationSpeed = 0;
            } else {
                x += knockbackVx;
                rotationAngle += rotationSpeed;
            }
        }

        // Se estiver nocauteado, só mantém a animação tonto
        if (knockedOut) {
            setAnimation("tonto");
            // Não atualiza física nem animações de movimento
            Animation anim = animations.getOrDefault(currentAnimation, animations.get("parado"));
            if (anim != null && anim.frameCount > 1) {
                long now = System.currentTimeMillis();
                if (now - lastFrameTime > anim.frameDuration) {
                    currentFrame = (currentFrame + 1) % anim.frameCount;
                    lastFrameTime = now;
                }
            }
            return;
        }

        // --- Lógica de Física Corrigida ---

        // 1. Só aplica gravidade se o jogador estiver no ar.
        // Um jogador está no ar se sua posição Y for menor que o chão, ou se tiver velocidade para cima (início do pulo).
        if (y < groundY - height || vy < 0) {
            vy += gravity;
            if (vy > maxFallSpeed) {
                vy = maxFallSpeed; // Limita a velocidade de queda.
            }
        }

        // 2. Atualiza a posição Y com base na velocidade.
        y += vy;

        // 3. Verifica e corrige a colisão com o chão.
        // Isso garante que o jogador nunca caia através do chão.
        if (y >= groundY - height) {
            y = groundY - height; // Alinha o jogador perfeitamente com o chão.
            vy = 0;               // Para o movimento vertical ao tocar o chão.
            
            if (jumping) {
                jumping = false;
                if (currentAnimation.equals("pulo")) {
                    setAnimation("parado");
                }
            }
        }

        // --- Fim da Lógica de Física ---

        // Atualiza frame da animação
        Animation anim = animations.getOrDefault(currentAnimation, animations.get("parado"));
        if (anim != null && anim.frameCount > 1) {
            long now = System.currentTimeMillis();
            if (now - lastFrameTime > anim.frameDuration) {
                // Se for a animação de golpe especial e chegou ao fim, volta para "parado"
                if (currentAnimation.equals("golpe_especial") && currentFrame == anim.frameCount - 1) {
                    setAnimation("parado");
                } else {
                    // Lógica para segurar o último frame ao agachar ou bloquear
                    boolean holdFrame = (isCrouching() && currentAnimation.equals("agachar") && currentFrame == anim.frameCount - 1) ||
                                        (isBlocking() && currentAnimation.equals("bloqueio") && currentFrame == anim.frameCount - 1);

                    if (!holdFrame) {
                        currentFrame = (currentFrame + 1) % anim.frameCount;
                        lastFrameTime = now;
                    }
                }
            }
        }

        // Atualiza efeitos de sangue
        long now = System.currentTimeMillis();
        for (BloodEffect effect : bloodEffects) {
            if (!effect.finished && now - effect.lastFrameTime > 50) {
                effect.currentFrame++;
                effect.lastFrameTime = now;
                if (effect.currentFrame >= effect.frameCount) {
                    effect.finished = true;
                }
            }
        }
        bloodEffects.removeIf(e -> e.finished);
        
        // Atualiza números de dano flutuantes
        for (DamageNumber damageNum : damageNumbers) {
            damageNum.update();
        }
        damageNumbers.removeIf(d -> d.finished);
        
        // Atualiza projétil do Jean
        if (currentProjectile != null) {
            currentProjectile.update();
            
            // Verifica colisão com o oponente
            if (opponent != null && currentProjectile.isCollidingWith(opponent)) {
                // Se o oponente não estiver agachado, o projétil atinge
                if (!opponent.isCrouching()) {
                    double damage = character.getSpecialAttackDamage(); // Usa o novo método
                    if (opponent.isBlocking()) {
                        damage *= character.getBlockReduction();
                    }
                    opponent.takeDamage((int)damage, false);
                    opponent.applyKnockback(this.facingRight);
                    currentProjectile.active = false; // Desativa o projétil após colisão
                }
                // Se o oponente estiver agachado, o projétil passa reto e nada acontece aqui.
            }
            
            // Remove projétil inativo
            if (!currentProjectile.active) {
                currentProjectile = null;
            }
        }
        
        // Atualiza raio da Anna
        if (currentLightning != null) {
            currentLightning.update();
            
            // Verifica colisão com o oponente
            if (opponent != null && currentLightning.isCollidingWith(opponent)) {
                // Se o oponente não estiver agachado, o raio atinge
                if (!opponent.isCrouching()) {
                    double damage = character.getSpecialAttackDamage(); // Usa o novo método
                    if (opponent.isBlocking()) {
                        damage *= character.getBlockReduction();
                    }
                    opponent.takeDamage((int)damage, true); // Golpe especial
                    opponent.applyKnockback(this.facingRight);
                }
                // Se o oponente estiver agachado, o raio não atinge
            }
            
            // Remove raio inativo
            if (!currentLightning.active) {
                currentLightning = null;
            }
        }
        
        // Atualiza urso do Erik
        if (currentBear != null) {
            currentBear.update();
            
            // Verifica colisão com o oponente
            if (opponent != null && currentBear.isCollidingWith(opponent)) {
                // Se o oponente não estiver agachado, o urso atinge
                if (!opponent.isCrouching()) {
                    double damage = character.getSpecialAttackDamage(); // Usa o novo método
                    if (opponent.isBlocking()) {
                        damage *= character.getBlockReduction();
                    }
                    opponent.takeDamage((int)damage, true); // Golpe especial
                    opponent.applyKnockback(this.facingRight);
                }
                // Se o oponente estiver agachado, o urso não atinge
            }
            
            // Remove urso inativo
            if (!currentBear.active) {
                currentBear = null;
            }
        }

        // Atualiza lápis do William
        if (currentLapis != null) {
            currentLapis.update();
            if (opponent != null && currentLapis.isCollidingWith(opponent)) {
                if (!opponent.isCrouching()) {
                    double damage = character.getSpecialAttackDamage();
                    if (opponent.isBlocking()) {
                        damage *= character.getBlockReduction();
                    }
                    opponent.takeDamage((int)damage, true);
                    opponent.applyKnockback(true); // Sempre da direita
                    currentLapis.active = false;
                }
            }
            if (!currentLapis.active) {
                currentLapis = null;
            }
        }

        // Atualiza o clone da Giovana
        if (giovanaClone != null && !giovanaClone.isAlive()) {
            giovanaClone = null;
        }
    }

    // Métodos para mover o jogador
    public void moveLeft() {
        if (isKnockedBack || knockedOut) return;
        int nextX = x - moveSpeed;
        if (opponent == null || !isCollidingAt(nextX, opponent)) {
            x = nextX;
        }
        facingRight = false;
    }

    public void moveRight() {
        if (isKnockedBack || knockedOut) return;
        int nextX = x + moveSpeed;
        if (opponent == null || !isCollidingAt(nextX, opponent)) {
            x = nextX;
        }
        facingRight = true;
    }

    private boolean isCollidingAt(int nextX, Player other) {
        final int hitbox = 150;
        int myNextCenterX = nextX + this.width / 2;
        int opponentCenterX = other.getX() + other.getWidth() / 2;
        
        int currentDistance = Math.abs((this.x + this.width / 2) - opponentCenterX);
        int nextDistance = Math.abs(myNextCenterX - opponentCenterX);

        // A colisão só ocorre se o jogador estiver se movendo EM DIREÇÃO ao oponente
        // e se a nova posição violar a hitbox.
        if (nextDistance < currentDistance && nextDistance < hitbox) {
            return true;
        }
        
        return false;
    }

    public void jump() {
        if (isKnockedBack || knockedOut) return;
        if (!jumping) {
            vy = jumpStartSpeed;
            jumping = true;
            setAnimation("pulo");
        }
    }

    public void setCrouching(boolean crouching) {
        if (isKnockedBack || knockedOut) return;
        this.crouching = crouching;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public void setBlocking(boolean blocking) {
        if (isKnockedBack || knockedOut) return;
        this.blocking = blocking;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void updateAnimationState(boolean isMoving, boolean isCrouching, boolean isBlocking) {
        if (knockedOut) {
            setAnimation("tonto");
            return;
        }
        if (isTakingDamage() || isAttacking() || isAttackingWithProjectile() || isAttackingWithLightning() || isAttackingWithBear() || jumping) {
            return; // Higher priority animations are already playing
        }
        if (isBlocking) {
            setAnimation("bloqueio");
        } else if (isCrouching) {
            setAnimation("agachar");
        } else if (isMoving) {
            setAnimation("andando");
        } else {
            setAnimation("parado");
        }
    }

    // Métodos para desenhar o jogador
    public void draw(Graphics g) {
        // Desenha a sombra fixa no chão, centralizada horizontalmente em relação ao player
        if (shadowImage != null) {
            int shadowX = x;
            int shadowY = groundY - 400;
            g.drawImage(shadowImage, shadowX, shadowY, 400, 400, null);
        }

        Graphics2D g2dPlayer = (Graphics2D) g.create();
        if (isKnockedBack) {
            g2dPlayer.rotate(rotationAngle, x + width / 2, y + height / 2);
        }

        Animation anim = animations.getOrDefault(currentAnimation, animations.get("parado"));
        if (anim != null && anim.spritesheet != null) {
            int frameX = currentFrame * anim.frameWidth;

            // Define a área de origem (source) no spritesheet
            int sx1 = frameX;
            int sy1 = 0;
            int sx2 = sx1 + anim.frameWidth;
            int sy2 = anim.frameHeight;

            // Define a área de destino (destination) na tela
            int dx1 = x;
            int dy1 = y;
            int dx2 = x + width;
            int dy2 = y + height;

            // Se o personagem estiver virado para a direita, inverte o desenho
            if (facingRight && !isKnockedBack) { 
                dx1 = x + width;
                dx2 = x;
            }

            g2dPlayer.drawImage(anim.spritesheet, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        }
        g2dPlayer.dispose();

        // Desenha efeitos de sangue
        for (BloodEffect effect : bloodEffects) {
            if (!effect.finished) {
                int drawWidth = (int)(effect.frameWidth * 2.5);
                int drawHeight = (int)(effect.frameHeight * 2.5);
                int drawX = effect.x - (drawWidth - effect.frameWidth) / 2;
                int drawY = effect.y - (drawHeight - effect.frameHeight) / 2 - (int)(this.height * 0.2);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.85f));
                g2d.drawImage(effect.spritesheet, drawX, drawY, drawX + drawWidth, drawY + drawHeight,
                    0, 0, effect.frameWidth, effect.frameHeight, null);
                g2d.dispose();
            }
        }
        
        // Desenha o projétil do Jean
        if (currentProjectile != null && currentProjectile.active) {
            Graphics2D g2d = (Graphics2D) g.create();
            
            if (projectileImage != null) {
                // Se a imagem original (giz.png) aponta para a esquerda:
                if (currentProjectile.facingRight) {
                    // Espelha a imagem para que a ponta do giz aponte para a direita
                    g2d.drawImage(projectileImage, currentProjectile.x + currentProjectile.width, currentProjectile.y, 
                                 -currentProjectile.width, currentProjectile.height, null);
                } else {
                    // Desenha normalmente, pois o personagem e o giz já apontam para a esquerda
                    g2d.drawImage(projectileImage, currentProjectile.x, currentProjectile.y, 
                                 currentProjectile.width, currentProjectile.height, null);
                }
            } else {
                // Fallback: desenha um retângulo se a imagem não carregar
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(currentProjectile.x, currentProjectile.y, currentProjectile.width, currentProjectile.height);
            }
            
            g2d.dispose();
        }
        
        // Desenha o raio da Anna
        if (currentLightning != null && currentLightning.active) {
            Graphics2D g2d = (Graphics2D) g.create();
            
            if (lightningImage != null) {
                // Desenha o raio com efeito de transparência durante a queda
                long elapsed = System.currentTimeMillis() - currentLightning.creationTime;
                float alpha = 1.0f;
                
                if (elapsed < currentLightning.fallDuration) {
                    // Durante a queda, o raio fica mais brilhante
                    alpha = 0.8f + (0.2f * (float)elapsed / currentLightning.fallDuration);
                } else if (elapsed < currentLightning.fallDuration + currentLightning.hitDuration) {
                    // Durante o impacto, o raio fica muito brilhante
                    alpha = 1.0f;
                }
                
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(lightningImage, currentLightning.x, currentLightning.y, 
                             currentLightning.width, currentLightning.height, null);
            } else {
                // Fallback: desenha um retângulo azul se a imagem não carregar
                g2d.setColor(Color.CYAN);
                g2d.fillRect(currentLightning.x, currentLightning.y, currentLightning.width, currentLightning.height);
            }
            
            g2d.dispose();
        }

        // Desenha o urso do Erik
        if (currentBear != null && currentBear.active) {
            Graphics2D g2d = (Graphics2D) g.create();
            
            if (bearImage != null) {
                // Desenha o urso com efeito de transparência durante a queda
                long elapsed = System.currentTimeMillis() - currentBear.creationTime;
                float alpha = 1.0f;
                
                if (elapsed < currentBear.fallDuration) {
                    // Durante a queda, o urso fica mais brilhante
                    alpha = 0.8f + (0.2f * (float)elapsed / currentBear.fallDuration);
                } else if (elapsed < currentBear.fallDuration + currentBear.hitDuration) {
                    // Durante o impacto, o urso fica muito brilhante
                    alpha = 1.0f;
                }
                
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(bearImage, currentBear.x, currentBear.y, 
                             currentBear.width, currentBear.height, null);
            } else {
                // Fallback: desenha um retângulo marrom se a imagem não carregar
                g2d.setColor(new Color(139, 69, 19)); // Marrom
                g2d.fillRect(currentBear.x, currentBear.y, currentBear.width, currentBear.height);
            }
            
            g2d.dispose();
        }

        // Desenha a wave do especial do Cesar
        if (auraActive && character.getName().equals("Cesar") && waveAuraImage != null) {
            long elapsed = System.currentTimeMillis() - auraStartTime;
            // Duração total do efeito (pode ajustar se quiser)
            int efeitoTotal = auraDuration;
            if (elapsed < efeitoTotal) {
                float progress = Math.min(1f, elapsed / (float)efeitoTotal);
                // Tamanho inicial e final da wave
                int minSize = 400;
                int maxSize = 900;
                int size = (int)(minSize + (maxSize - minSize) * progress);
                // Alpha diminui conforme cresce
                float alpha = 1.0f - progress;
                Graphics2D g2dAura = (Graphics2D) g.create();
                g2dAura.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
                int auraX = x + width/2 - size/2;
                int auraY = y + height/2 - size/2;
                g2dAura.drawImage(waveAuraImage, auraX, auraY, size, size, null);
                g2dAura.dispose();
            } else {
                auraActive = false; // Garante que não desenhe mais após o tempo
            }
        }
        
        // Desenha o lápis do William
        if (currentLapis != null && currentLapis.active) {
            Graphics2D g2d = (Graphics2D) g.create();
            if (lapisImage != null) {
                if (currentLapis.facingRight) {
                    g2d.drawImage(lapisImage, currentLapis.x, currentLapis.y, currentLapis.width, currentLapis.height, null);
                } else {
                    // Espelha a imagem horizontalmente
                    g2d.drawImage(lapisImage, currentLapis.x + currentLapis.width, currentLapis.y, -currentLapis.width, currentLapis.height, null);
                }
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(currentLapis.x, currentLapis.y, currentLapis.width, currentLapis.height);
            }
            g2d.dispose();
        }
        
        // Desenha números de dano flutuantes
        for (DamageNumber damageNum : damageNumbers) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(damageNum.color);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            
            // Adiciona efeito de sombra
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(damageNum.damage), damageNum.x + 2, damageNum.y + 2);
            
            // Desenha o número principal
            g2d.setColor(damageNum.color);
            g2d.drawString(String.valueOf(damageNum.damage), damageNum.x, damageNum.y);
            
            g2d.dispose();
        }

        // Desenha o clone da Giovana
        if (giovanaClone != null && giovanaClone.isAlive()) {
            Animation cloneAnim = animations.getOrDefault("parado", null);
            if (cloneAnim != null && cloneAnim.spritesheet != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                float alpha = giovanaClone.getAlpha();
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
                int dx1 = giovanaClone.x;
                int dy1 = giovanaClone.y;
                int dx2 = giovanaClone.x + width;
                int dy2 = giovanaClone.y + height;
                int sx1 = 0;
                int sy1 = 0;
                int sx2 = cloneAnim.frameWidth;
                int sy2 = cloneAnim.frameHeight;
                if (giovanaClone.facingRight) {
                    g2d.drawImage(cloneAnim.spritesheet, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                } else {
                    g2d.drawImage(cloneAnim.spritesheet, dx1 + width, dy1, dx1, dy2, sx1, sy1, sx2, sy2, null);
                }
                g2d.dispose();
            }
        }
    }

    // Troca de animação
    public void setAnimation(String anim) {
        if (!currentAnimation.equals(anim)) {
            currentAnimation = anim;
            currentFrame = 0; // Reseta o frame ao mudar de animação
            lastFrameTime = System.currentTimeMillis();
        }
    }

    // Getters e setters
    public Character getCharacter() {
        return character;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void clearProjectile() {
        currentProjectile = null;
    }

    private void loadBloodSpritesheet() {
        if (bloodLoaded) return;
        try {
            bloodSpritesheet = javax.imageio.ImageIO.read(getClass().getResource("/images/effects/blood.png"));
            bloodFrameHeight = bloodSpritesheet.getHeight();
            // Supondo que cada frame tem a mesma largura do personagem
            bloodFrameWidth = bloodFrameHeight; // ou width, se preferir
            bloodFrameCount = bloodSpritesheet.getWidth() / bloodFrameWidth;
            bloodLoaded = true;
        } catch (Exception e) {
            bloodLoaded = true;
        }
    }

    // Sobrescrever takeDamage para adicionar efeito de sangue
    public void takeDamage(int damageAmount) {
        takeDamage(damageAmount, false);
    }

    // Sobrescrever takeDamage para adicionar efeito de sangue e números de dano
    public void takeDamage(int damageAmount, boolean isSpecial) {
        if (isTakingDamage() || knockedOut) return; // Previne "stun lock" e impede dano após nocaute
        character.takeDamage(damageAmount);
        if (character.getHealth() <= 0) {
            knockedOut = true;
            canBeFinished = true; // Pode ser finalizado
            setAnimation("tonto");
            lastDamageTime = System.currentTimeMillis();
            
            // Toca o som do personagem quando nocauteado
            String characterName = character.getName().toLowerCase();
            AudioManager.getInstance().playSound(characterName);
            return;
        }
        setAnimation("dano");
        lastDamageTime = System.currentTimeMillis();
        loadBloodSpritesheet();
        if (bloodSpritesheet != null && bloodFrameCount > 0) {
            int bx = x + width/2 - bloodFrameWidth/2;
            int by = y + height/2 - bloodFrameHeight/2;
            bloodEffects.add(new BloodEffect(bx, by, bloodSpritesheet, bloodFrameCount, bloodFrameWidth, bloodFrameHeight));
        }
        
        // Adiciona número de dano flutuante
        int damageX = x + width/2;
        int damageY = y + height/2;
        damageNumbers.add(new DamageNumber(damageX, damageY, damageAmount, isSpecial));
    }

    public void applyKnockback(boolean fromRight) {
        if (knockedOut || isKnockedBack) return; // Não pode sofrer novo knockback se já estiver no estado
        isKnockedBack = true;
        knockbackEndTime = System.currentTimeMillis() + 3000; // Duração de 3 segundos
        vy = -40; // Aplica um grande impulso vertical para "voar"
        knockbackVx = fromRight ? -20 : 20; // Arremessa na direção oposta ao projétil
        rotationSpeed = fromRight ? -0.5 : 0.5; // Define a velocidade de rotação
        setAnimation("dano");
    }

    // Métodos para a barra de cooldown do golpe especial
    public double getSpecialAttackProgress() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastSpecial = currentTime - lastSpecialAttackTime;
        
        if (timeSinceLastSpecial >= specialAttackCooldown) {
            return 1.0; // Cooldown completo
        }
        
        return (double) timeSinceLastSpecial / specialAttackCooldown;
    }

    public boolean isSpecialAttackReady() {
        return getSpecialAttackProgress() >= 1.0;
    }

    public long getTimeUntilSpecialReady() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastSpecial = currentTime - lastSpecialAttackTime;
        
        if (timeSinceLastSpecial >= specialAttackCooldown) {
            return 0; // Já está pronto
        }
        
        return specialAttackCooldown - timeSinceLastSpecial;
    }

    // Métodos para finalização
    public boolean canBeFinished() {
        return canBeFinished && !isBeingFinished;
    }

    public void finish(boolean fromRight) {
        if (!canBeFinished || isBeingFinished) return;
        
        isBeingFinished = true;
        finishStartTime = System.currentTimeMillis();
        setAnimation("dano");
        
        // Aplica knockback final após a animação com velocidade muito alta
        Timer finishTimer = new Timer(finishAnimationDuration, e -> {
            finalKnockback = true;
            finalKnockbackVx = fromRight ? -80 : 80; // Velocidade horizontal muito alta
            finalKnockbackVy = -100; // Velocidade vertical muito alta
            ((Timer)e.getSource()).stop();
        });
        finishTimer.setRepeats(false);
        finishTimer.start();
    }

    public boolean isBeingFinished() {
        return isBeingFinished;
    }

    public boolean isFinalKnockback() {
        return finalKnockback;
    }

    public void resetForNewRound() {
        // Reseta o personagem para o próximo round
        knockedOut = false;
        canBeFinished = false;
        isBeingFinished = false;
        finalKnockback = false;
        finalKnockbackVx = 0;
        finalKnockbackVy = 0;
        isKnockedBack = false;
        knockbackVx = 0;
        rotationAngle = 0;
        rotationSpeed = 0;
        vy = 0;
        jumping = false;
        crouching = false;
        blocking = false;
        setAnimation("parado");
        
        // Restaura a vida
        character.restoreHealth();
    }
}

