package com.basketball.display.announcer;

/**
 * @author zhucha
 * @Title TODO
 * @Description TODO
 * @Date 2025/8/23
 */
import java.io.IOException;

public class BasketballCommentator {

    /**
     * 使用不同的语音角色进行播报
     */
    public static void announceWithVoice(String text, String voiceName, int rate) {
        try {
            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.SelectVoice('%s'); " +
                    "$speak.Rate = %d; " +
                    "$speak.Volume = 100; " +
                    "$speak.Speak('%s');\"",
                voiceName, rate, text.replace("'", "''")
            );

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("语音播报失败: " + e.getMessage());
            // 失败时使用默认语音
            backupAnnounce(text);
        }
    }

    /**
     * 获取系统可用的语音列表
     */
    public static void listAvailableVoices() {
        try {
            String command = "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "$speak.GetInstalledVoices() | ForEach-Object { $_.VoiceInfo.Name }\"";

            Process process = Runtime.getRuntime().exec(command);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );

            System.out.println("=== 可用的语音角色 ===");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();

        } catch (Exception e) {
            System.err.println("获取语音列表失败");
        }
    }

    /**
     * 备用播报方案
     */
    private static void backupAnnounce(String text) {
        try {
            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.Speak('%s');\"",
                text.replace("'", "''")
            );
            Runtime.getRuntime().exec(command).waitFor();
        } catch (Exception e) {
            System.err.println("所有语音播报方法都失败了");
        }
    }

    /**
     * 篮球比赛专用播报角色
     */
    public static void announceGameEvent(String eventType, String message) {
        switch (eventType) {
            case "SCORE" -> {
                // 使用激动的中年男性声音播报得分
                announceWithVoice(message, "Microsoft David Desktop", 3);
            }
            case "FOUL" -> {
                // 使用严肃的声音播报犯规
                announceWithVoice(message, "Microsoft Zira Desktop", 1);
            }
            case "TIMEOUT" -> {
                // 使用清晰的女声播报暂停
                announceWithVoice("暂停，" + message, "Microsoft Huihui Desktop", 0);
            }
            case "QUARTER_END" -> {
                // 使用庄重的声音播报节末
                announceWithVoice(message, "Microsoft David Desktop", 2);
            }
            case "GAME_END" -> {
                // 使用激情的声音播报比赛结束
                announceWithVoice(message, "Microsoft David Desktop", 4);
            }
            default -> announceWithVoice(message, "Microsoft David Desktop", 2);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 首先查看可用的语音
        listAvailableVoices();

        Thread.sleep(2000);

        // 测试不同的语音角色
        System.out.println("\n=== 测试不同语音角色 ===");

        announceGameEvent("SCORE", "三分球！红队远投命中！");
        Thread.sleep(3000);

        announceGameEvent("FOUL", "犯规！蓝队5号球员个人第三次犯规");
        Thread.sleep(3000);

        announceGameEvent("TIMEOUT", "红队请求暂停");
        Thread.sleep(3000);

        announceGameEvent("QUARTER_END", "第一节比赛结束，当前比分20比18");
        Thread.sleep(3000);

        announceGameEvent("GAME_END", "比赛结束！红队以85比80获胜！");
    }
}
