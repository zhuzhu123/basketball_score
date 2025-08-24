package com.basketball.display;

/**
 * @author zhucha
 * @Title TODO
 * @Description TODO
 * @Date 2025/8/23
 */
import java.io.IOException;

public class WindowsTTS {

    /**
     * 使用 PowerShell 进行语音播报（最稳定）
     */
    public static void speak(String text) {
        try {
            // 处理特殊字符
            String safeText = text.replace("\"", "\\\"")
                .replace("'", "''")
                .replace("$", "\\$");

            // 构建 PowerShell 命令
            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.Speak('%s');\"", safeText);

            // 执行命令
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("语音播报失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 带音量、语速控制的版本
     */
    public static void speakWithControl(String text, int volume, int rate) {
        try {
            String safeText = text.replace("\"", "\\\"")
                .replace("'", "''")
                .replace("$", "\\$");

            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.Volume = %d; " +
                    "$speak.Rate = %d; " +
                    "$speak.Speak('%s');\"", volume, rate, safeText);

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

        } catch (Exception e) {
            System.err.println("语音播报失败: " + e.getMessage());
        }
    }

    /**
     * 异步语音播报（不阻塞主线程）
     */
    public static void speakAsync(String text) {
        new Thread(() -> speak(text)).start();
    }

    public static void main(String[] args) {
        // 简单测试
        speak("系统启动成功，欢迎使用语音播报功能");

        // 播报多条消息
        String[] messages = {
            "第一条测试消息",
            "当前分数：80比75",
            "比赛时间剩余2分钟",
            "红队请求暂停"
        };

        for (String message : messages) {
            speak(message);
            try {
                Thread.sleep(1500); // 等待上一条播报完成
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 带控制的播报
        speakWithControl("重要提示：比赛即将结束", 100, 2);
    }
}
