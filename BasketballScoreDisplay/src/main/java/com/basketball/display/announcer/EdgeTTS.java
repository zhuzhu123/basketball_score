package com.basketball.display.announcer;

/**
 * @author zhucha
 * @Title TODO
 * @Description TODO
 * @Date 2025/8/23
 */
import java.io.*;

public class EdgeTTS {
    public static void speak(String text, String voiceName) throws IOException {
        String script = String.format("""
            Add-Type -AssemblyName System.Speech;
            $speak = New-Object System.Speech.Synthesis.SpeechSynthesizer;
            $speak.SelectVoice("%s");
            $speak.Speak("%s");
            """,
            voiceName,
            text.replace("\"", "\\\"")
        );

        // 通过PowerShell调用
        Process process = new ProcessBuilder("powershell", "-Command", script).start();
    }

    public static void main(String[] args) throws IOException {
        // 可用语音列表（需Edge浏览器）：
        // 中文男声: "Microsoft Yunxi Online (Natural)" - 带情绪的青年音
        // 英文激情: "Microsoft Guy Online (Natural)" - 体育解说风格
        speak("三分球！比赛进入加时赛！", "Microsoft Yunxi Online (Natural)");
    }
}
