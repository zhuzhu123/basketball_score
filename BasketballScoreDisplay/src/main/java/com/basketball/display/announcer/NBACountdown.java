package com.basketball.display.announcer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class NBACountdown {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("🏀 NBA倒计时器 🏀");
        System.out.print("请输入倒计时时间（秒）: ");
        int seconds = scanner.nextInt();

        startCountdown(seconds);
    }

    public static void startCountdown(int seconds) {
        try {
            System.out.println("⏰ 倒计时开始: " + seconds + "秒");

            for (int i = seconds; i > 0; i--) {
                System.out.printf("\r剩余时间: %02d:%02d", i / 60, i % 60);
                TimeUnit.SECONDS.sleep(1);
            }

            System.out.println("\n🏀 时间到！比赛结束！");
            playBuzzerSound();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("倒计时被中断");
        }
    }

    public static void playBuzzerSound() {
        try {
            // 尝试播放自定义声音文件
            File soundFile = new File("G:\\DownLoad\\buzzer.wav");
            if (soundFile.exists()) {
                playCustomSound(soundFile);
            } else {
                // 如果没有找到文件，使用系统蜂鸣
                System.out.println("⚠️ 未找到声音文件，使用系统蜂鸣");
                System.out.println("\7\7\7"); // ASCII蜂鸣字符
            }
        } catch (Exception e) {
            System.err.println("播放声音出错: " + e.getMessage());
        }
    }

    private static void playCustomSound(File soundFile)
        throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();

        // 等待声音播放完成
        while (!clip.isRunning()) Thread.sleep(10);
        while (clip.isRunning()) Thread.sleep(10);

        clip.close();
        audioStream.close();
    }
}