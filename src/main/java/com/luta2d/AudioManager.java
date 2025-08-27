package com.luta2d;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private Clip currentMusic;
    private Clip currentSound;
    private Map<String, String> audioFiles;
    private boolean isMuted = false;
    
    private AudioManager() {
        audioFiles = new HashMap<>();
        loadAudioFiles();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    private void loadAudioFiles() {
        // Músicas de fundo
        audioFiles.put("univille", "/audio/univille.wav");
        audioFiles.put("menu", "/audio/menu_music.wav");
        audioFiles.put("characters", "/audio/character_select_music.wav");
        audioFiles.put("arenas", "/audio/arena_selection.wav");
        audioFiles.put("tutorial", "/audio/tutorial.wav");
        
        // Músicas das arenas
        for (int i = 2; i <= 9; i++) {
            audioFiles.put("arena" + i, "/audio/arenas_music/arena" + i + ".wav");
        }
        
        // Música fixa arena5.wav na raiz de audio
        audioFiles.put("arena5", "/audio/arena5.wav");
        
        // Sons dos personagens
        audioFiles.put("anna", "/audio/Anna.wav");
        audioFiles.put("cesar", "/audio/Cesar.wav");
        audioFiles.put("erik", "/audio/Erik.wav");
        audioFiles.put("giovana", "/audio/Giovana.wav");
        audioFiles.put("jean", "/audio/Jean.wav");
        audioFiles.put("william", "/audio/William.wav");
    }
    
    public void playMusic(String musicName, boolean loop) {
        if (isMuted) return;
        
        stopCurrentMusic();
        
        String resourcePath = audioFiles.get(musicName);
        if (resourcePath == null) {
            System.err.println("Arquivo de música não encontrado: " + musicName);
            return;
        }
        
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                getClass().getResource(resourcePath)
            );
            currentMusic = AudioSystem.getClip();
            currentMusic.open(audioInputStream);
            
            if (loop) {
                currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentMusic.start();
            }
        } catch (Exception e) {
            System.err.println("Erro ao reproduzir música " + musicName + ": " + e.getMessage());
        }
    }
    
    public void playSound(String soundName) {
        if (isMuted) return;
        
        String resourcePath = audioFiles.get(soundName);
        if (resourcePath == null) {
            System.err.println("Arquivo de som não encontrado: " + soundName);
            return;
        }
        
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                getClass().getResource(resourcePath)
            );
            Clip soundClip = AudioSystem.getClip();
            soundClip.open(audioInputStream);
            soundClip.start();
        } catch (Exception e) {
            System.err.println("Erro ao reproduzir som " + soundName + ": " + e.getMessage());
        }
    }
    
    public void stopCurrentMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
            currentMusic.close();
            currentMusic = null;
        }
    }
    
    public void stopCurrentSound() {
        if (currentSound != null && currentSound.isRunning()) {
            currentSound.stop();
            currentSound.close();
            currentSound = null;
        }
    }
    
    public void stopAllAudio() {
        stopCurrentMusic();
        stopCurrentSound();
    }
    
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            stopAllAudio();
        }
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public void toggleMute() {
        setMuted(!isMuted);
    }
    
    // Toca música e executa um callback ao terminar
    public void playMusicWithCallback(String musicName, boolean loop, Runnable onEnd) {
        if (isMuted) return;
        stopCurrentMusic();
        String resourcePath = audioFiles.get(musicName);
        if (resourcePath == null) {
            System.err.println("Arquivo de música não encontrado: " + musicName);
            return;
        }
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                getClass().getResource(resourcePath)
            );
            currentMusic = AudioSystem.getClip();
            currentMusic.open(audioInputStream);
            if (loop) {
                currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentMusic.start();
            }
            if (onEnd != null && !loop) {
                currentMusic.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        currentMusic.close();
                        onEnd.run();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Erro ao reproduzir música " + musicName + ": " + e.getMessage());
        }
    }
} 