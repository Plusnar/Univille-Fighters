package com.luta2d;

public class Character {
    private String name;
    private int id; // ID para identificar o personagem, pode ser 1 para o primeiro jogador, 2 para o segundo, etc.
    private int health; // Saúde do personagem (exemplo)
    private double attackDamage;
    private int moveSpeed;
    private int jumpHeight;
    private int attackRange;
    private double blockReduction;

    // Construtor com nome e ID
    public Character(String name, int id) {
        this.name = name;
        this.id = id;
        this.health = 300; // Aumentado de 275 para 300
        this.attackDamage = 15.0; // Aumentado de 1.0 para 15.0
        this.moveSpeed = 8;
        this.jumpHeight = 30;
        this.attackRange = 200;
        this.blockReduction = 0.5; // 50% do dano quando bloqueando (aumentado de 0.01)
    }

    // Getter para o nome
    public String getName() {
        return name;
    }

    // Setter para o nome
    public void setName(String name) {
        this.name = name;
    }

    // Getter para o ID
    public int getId() {
        return id;
    }

    // Setter para o ID
    public void setId(int id) {
        this.id = id;
    }

    // Getter para a saúde
    public int getHealth() {
        return health;
    }

    // Método para reduzir a saúde do personagem
    public void takeDamage(int damageAmount) {
        this.health -= damageAmount;
        if (this.health < 0) {
            this.health = 0; // Garante que a saúde não seja negativa
        }
    }

    // Método para restaurar a saúde do personagem
    public void restoreHealth() {
        this.health = 300; // Restaura para o valor máximo
    }

    // Método para causar dano a outro personagem
    public void attack(Character opponent) {
        opponent.takeDamage((int) (this.attackDamage));
    }

    // Getter para o dano
    public double getAttackDamage() {
        return attackDamage;
    }

    // Setter para o dano
    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    // Novos getters e setters
    public int getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(int moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public int getJumpHeight() {
        return jumpHeight;
    }

    public void setJumpHeight(int jumpHeight) {
        this.jumpHeight = jumpHeight;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public double getBlockReduction() {
        return blockReduction;
    }

    public void setBlockReduction(double blockReduction) {
        this.blockReduction = blockReduction;
    }

    // Métodos para obter dano específico por tipo de ataque
    public double getPunchDamage() {
        return attackDamage * 1.0; // Dano base
    }

    public double getKickDamage() {
        return attackDamage * 1.5; // Chute causa 50% mais dano
    }

    public double getSpecialAttackDamage() {
        return attackDamage * 4.0; // Golpe especial causa 4x mais dano
    }

    public double getUltimateDamage() {
        return attackDamage * 6.0; // Golpe final causa 6x mais dano
    }
}
