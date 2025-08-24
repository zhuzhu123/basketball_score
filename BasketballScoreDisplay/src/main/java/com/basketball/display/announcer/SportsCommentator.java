package com.basketball.display.announcer;

/**
 * @author zhucha
 * @Title TODO
 * @Description TODO
 * @Date 2025/8/23
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SportsCommentator {

    // 预定义的解说员配置
    private static final Map<String, CommentatorConfig> COMMENTATORS = new HashMap<>();

    static {
        // 激情解说员（美式风格）
        COMMENTATORS.put("EXCITED", new CommentatorConfig("Microsoft David Desktop", 4, 100));
        // 专业解说员（冷静分析）
        COMMENTATORS.put("PROFESSIONAL", new CommentatorConfig("Microsoft Zira Desktop", 2, 90));
        // 中文解说员
        COMMENTATORS.put("CHINESE", new CommentatorConfig("Microsoft Huihui Desktop", 3, 100));
        // 庄重解说员（比赛结束）
        COMMENTATORS.put("FINAL", new CommentatorConfig("Microsoft David Desktop", 1, 100));
    }

    static class CommentatorConfig {
        String voiceName;
        int rate;
        int volume;

        CommentatorConfig(String voiceName, int rate, int volume) {
            this.voiceName = voiceName;
            this.rate = rate;
            this.volume = volume;
        }
    }

    /**
     * 使用指定解说员风格播报
     */
    public static void announceWithCommentator(String text, String commentatorStyle) {
        CommentatorConfig config = COMMENTATORS.getOrDefault(commentatorStyle,
            new CommentatorConfig("Microsoft David Desktop", 2, 100));

        try {
            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.SelectVoice('%s'); " +
                    "$speak.Rate = %d; " +
                    "$speak.Volume = %d; " +
                    "$speak.Speak('%s');\"",
                config.voiceName, config.rate, config.volume, text.replace("'", "''")
            );

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println(commentatorStyle + " 解说员播报失败");
            backupAnnounce(text);
        }
    }

    /**
     * 篮球比赛事件播报
     */
    public static void basketballAnnouncement(String event, String details) {
        switch (event) {
            case "JUMP_BALL" ->
                announceWithCommentator("跳球！比赛开始！", "EXCITED");
            case "SCORE_2" ->
                announceWithCommentator("两分命中！" + details, "EXCITED");
            case "SCORE_3" ->
                announceWithCommentator("三分球！" + details + "！", "EXCITED");
            case "DUNK" ->
                announceWithCommentator("暴扣！精彩的灌篮！" + details, "EXCITED");
            case "STEAL" ->
                announceWithCommentator("抢断！快速反击！" + details, "PROFESSIONAL");
            case "BLOCK" ->
                announceWithCommentator("盖帽！漂亮的防守！", "EXCITED");
            case "FOUL" ->
                announceWithCommentator("犯规，" + details, "PROFESSIONAL");
            case "TIMEOUT" ->
                announceWithCommentator("暂停，" + details, "PROFESSIONAL");
            case "QUARTER_END" ->
                announceWithCommentator("本节比赛结束，" + details, "FINAL");
            case "GAME_END" ->
                announceWithCommentator("比赛结束！" + details, "FINAL");
            default ->
                announceWithCommentator(details, "PROFESSIONAL");
        }
    }

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
            System.err.println("备用播报也失败了");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 演示不同的解说员风格
        basketballAnnouncement("JUMP_BALL", "");
        Thread.sleep(2000);

        basketballAnnouncement("SCORE_3", "红队8号球员远投命中");
        Thread.sleep(3000);

        basketballAnnouncement("DUNK", "蓝队23号球员强力灌篮");
        Thread.sleep(3000);

        basketballAnnouncement("STEAL", "红队防守成功");
        Thread.sleep(3000);

        basketballAnnouncement("FOUL", "蓝队5号球员个人第4次犯规");
        Thread.sleep(3000);

        basketballAnnouncement("GAME_END", "最终比分85比80，红队获胜");
    }
}