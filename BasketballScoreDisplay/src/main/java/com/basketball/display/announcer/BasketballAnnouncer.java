package com.basketball.display.announcer;

/**
 * @author zhucha
 * @Title TODO
 * @Description TODO
 * @Date 2025/8/23
 */
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BasketballAnnouncer {

    private static final ThreadPoolExecutor executor =
        (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

    // 比赛状态变量
    private static int homeScore = 0;
    private static int awayScore = 0;
    private static int quarter = 1;
    private static int timeRemaining = 600; // 10分钟

    /**
     * 激情篮球解说风格播报
     */
    public static void announce(String text, boolean excited) {
        executor.execute(() -> {
            try {
                String command;
                if (excited) {
                    // 激动语气：提高音量和语速
                    command = String.format(
                        "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                            "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$speak.Volume = 100; " +
                            "$speak.Rate = 3; " +
                            "$speak.Speak('%s');\"",
                        text.replace("'", "''")
                    );
                } else {
                    // 正常播报
                    command = String.format(
                        "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                            "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$speak.Volume = 85; " +
                            "$speak.Rate = 1; " +
                            "$speak.Speak('%s');\"",
                        text.replace("'", "''")
                    );
                }

                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();

            } catch (IOException | InterruptedException e) {
                System.err.println("播报失败: " + e.getMessage());
            }
        });
    }

    /**
     * 得分播报 - 带激情效果
     */
    public static void announceScore(String team, int points, String scoreType) {
        String announcement;
        boolean excited = false;

        switch (scoreType) {
            case "3point":
                announcement = String.format("三分球！%s队命中三分！", team);
                excited = true;
                break;
            case "dunk":
                announcement = String.format("暴扣！%s队精彩灌篮！", team);
                excited = true;
                break;
            case "fastbreak":
                announcement = String.format("快攻得分！%s队漂亮的快攻！", team);
                excited = true;
                break;
            default:
                announcement = String.format("%s队得分，%d分", team, points);
                excited = (points >= 2);
        }

        announce(announcement, excited);
    }

    /**
     * 比分更新播报
     */
    public static void updateScore(String team, int points) {
        if ("home".equals(team)) {
            homeScore += points;
        } else {
            awayScore += points;
        }

        String scoreUpdate = String.format("当前比分：%d比%d", homeScore, awayScore);
        boolean excited = Math.abs(homeScore - awayScore) <= 5; // 比分接近时激动

        announce(scoreUpdate, excited);
    }

    /**
     * 比赛时间播报
     */
    public static void announceTime() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;

        String timeAnnouncement;
        boolean excited = false;

        if (timeRemaining <= 60) {
            timeAnnouncement = String.format("最后%d秒！", timeRemaining);
            excited = true;
        } else if (timeRemaining <= 120) {
            timeAnnouncement = "比赛还剩2分钟！";
            excited = true;
        } else {
            timeAnnouncement = String.format("第%d节，剩余时间：%d分%d秒", quarter, minutes, seconds);
        }

        announce(timeAnnouncement, excited);
    }

    /**
     * 犯规播报
     */
    public static void announceFoul(String player, String team, int foulCount) {
        String announcement;
        if (foulCount >= 5) {
            announcement = String.format("犯规！%s队%s球员5犯毕业！", team, player);
        } else {
            announcement = String.format("%s队%s球员犯规，个人第%d次犯规", team, player, foulCount);
        }
        announce(announcement, true);
    }

    /**
     * 暂停播报
     */
    public static void announceTimeout(String team) {
        String announcement = String.format("%s队请求暂停", team);
        announce(announcement, false);
    }

    /**
     * 节间休息播报
     */
    public static void announceQuarterEnd() {
        String announcement = String.format("第%d节比赛结束！当前比分：%d比%d",
            quarter, homeScore, awayScore);
        announce(announcement, true);
        quarter++;
        timeRemaining = 600;
    }

    /**
     * 比赛结束播报
     */
    public static void announceGameEnd() {
        String winner = homeScore > awayScore ? "龙都F4" : "暴风队";
        if (homeScore == awayScore) {
            announce("比赛结束！平局！进入加时赛！", true);
        } else {
            String announcement = String.format("比赛结束！%s获胜！最终比分：%d比%d",
                winner, homeScore, awayScore);
            announce(announcement, true);
        }
    }

    /**
     * 精彩瞬间播报
     */
    public static void announceHighlight(String highlight) {
        String[] excitedPhrases = {
            "太精彩了！", "难以置信！", "漂亮！", "好球！", "完美配合！"
        };
        String randomPhrase = excitedPhrases[(int)(Math.random() * excitedPhrases.length)];

        announce(randomPhrase + highlight, true);
    }

    public static void main(String[] args) {
        // 模拟一场比赛
        System.out.println("=== 篮球比赛语音播报演示 ===");

        try {
            Thread.sleep(1000);
            announce("比赛开始！", true);

            Thread.sleep(2000);
            announceScore("红", 2, "normal");
            updateScore("home", 2);

            Thread.sleep(1500);
            announceScore("蓝", 3, "3point");
            updateScore("away", 3);

            Thread.sleep(1500);
            announceScore("红", 2, "dunk");
            updateScore("home", 2);

            Thread.sleep(1000);
            announceTime();

            Thread.sleep(1500);
            announceFoul("张三", "蓝", 3);

            Thread.sleep(1200);
            announceTimeout("红");

            Thread.sleep(1000);
            announceScore("蓝", 2, "fastbreak");
            updateScore("away", 2);

            Thread.sleep(2000);
            announceQuarterEnd();

            Thread.sleep(3000);
            announceGameEnd();

            // 等待所有播报完成
            Thread.sleep(5000);
            executor.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
