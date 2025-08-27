package com.luta2d;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javax.swing.*;
import java.awt.*;

public class VideoPlayer extends JPanel {
    private final String videoPath;
    private Runnable onVideoEnd;
    private JFXPanel jfxPanel;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isDisposed = false;

    public VideoPlayer(String videoPath) {
        this.videoPath = videoPath;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            if (!isDisposed) {
                StackPane root = new StackPane();
                Scene scene = new Scene(root);
                jfxPanel.setScene(scene);
            }
        });
    }

    public void play(Runnable onVideoEnd) {
        if (isDisposed) return;
        this.onVideoEnd = onVideoEnd;
        try {
            Platform.runLater(() -> {
                if (isDisposed) return;
                try {
                    Media media = new Media(videoPath);
                    mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.setFitWidth(getWidth());
                    mediaView.setFitHeight(getHeight());
                    if (jfxPanel.getScene() != null && jfxPanel.getScene().getRoot() instanceof StackPane) {
                        StackPane root = (StackPane) jfxPanel.getScene().getRoot();
                        root.getChildren().clear();
                        root.getChildren().add(mediaView);
                    }
                    mediaPlayer.setOnEndOfMedia(() -> {
                        if (!isDisposed) {
                            cleanup();
                        }
                    });
                    isPlaying = true;
                    mediaPlayer.play();
                } catch (Exception e) {
                    System.err.println("Erro ao iniciar vídeo: " + e.getMessage());
                    handleError();
                }
            });
        } catch (Exception e) {
            System.err.println("Erro ao preparar vídeo: " + e.getMessage());
            handleError();
        }
    }

    private void handleError() {
        cleanup();
        if (onVideoEnd != null && !isDisposed) {
            SwingUtilities.invokeLater(onVideoEnd);
        }
    }

    private void cleanup() {
        isPlaying = false;
        if (mediaPlayer != null && !isDisposed) {
            Platform.runLater(() -> {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao limpar MediaPlayer: " + e.getMessage());
                } finally {
                    mediaPlayer = null;
                }
            });
        }
        if (onVideoEnd != null && !isDisposed) {
            SwingUtilities.invokeLater(onVideoEnd);
        }
    }

    public void stop() {
        if (!isDisposed) {
            cleanup();
        }
    }

    @Override
    public void removeNotify() {
        isDisposed = true;
        cleanup();
        super.removeNotify();
    }
} 