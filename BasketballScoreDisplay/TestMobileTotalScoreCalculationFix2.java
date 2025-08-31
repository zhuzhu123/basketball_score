import java.util.HashMap;
import java.util.Map;

/**
 * 测试手机侧总分计算错误的第二次修复
 * 主要解决checkQuarterEnd和nextQuarter重复累加的问题
 */
public class TestMobileTotalScoreCalculationFix2 {
    
    // 模拟节次比分数据类
    static class QuarterScore {
        int homeScore;
        int awayScore;
        
        QuarterScore(int homeScore, int awayScore) {
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }
        
        @Override
        public String toString() {
            return homeScore + ":" + awayScore;
        }
    }
    
    // 模拟手机侧比赛状态
    static class MobileMatchState {
        int currentQuarter = 1;
        int homeScore = 0;
        int awayScore = 0;
        int totalHomeScore = 0;
        int lastTotalHomeScore = 0;
        int totalAwayScore = 0;
        int lastTotalAwayScore = 0;
        Map<Integer, QuarterScore> quarterHistory = new HashMap<>();
        
        void updateScore(int homePoints, int awayPoints) {
            homeScore += homePoints;
            awayScore += awayPoints;
            System.out.println("✓ 更新比分：" + homeScore + ":" + awayScore);
            
            // 检查是否达到20分
            checkQuarterEnd();
        }
        
        void checkQuarterEnd() {
            if (homeScore >= 20 || awayScore >= 20) {
                System.out.println("=== 第" + currentQuarter + "节结束（达到20分）===");
                
                // 更新累计总分
                lastTotalHomeScore += homeScore;
                lastTotalAwayScore += awayScore;
                
                // 更新显示总分
                totalHomeScore = lastTotalHomeScore;
                totalAwayScore = lastTotalAwayScore;
                
                System.out.println("✓ 更新累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
                System.out.println("✓ 更新显示总分：" + totalHomeScore + ":" + totalAwayScore);
                
                // 模拟用户选择进入下一节
                nextQuarterWithoutAddingScore();
            }
        }
        
        void nextQuarter() {
            System.out.println("\n=== 手动进入下一节 ===");
            
            // 保存当前节次比分到历史记录
            quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
            
            // 更新累计总分
            lastTotalHomeScore += homeScore;
            lastTotalAwayScore += awayScore;
            
            // 进入下一节
            currentQuarter++;
            homeScore = 0;
            awayScore = 0;
            
            // 更新显示总分
            updateTotalScoreDisplay();
            
            System.out.println("✓ 进入第" + currentQuarter + "节");
            System.out.println("✓ 累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
            System.out.println("✓ 显示总分：" + totalHomeScore + ":" + totalAwayScore);
        }
        
        void nextQuarterWithoutAddingScore() {
            System.out.println("\n=== 自动进入下一节（不累加比分）===");
            
            // 保存当前节次比分到历史记录
            quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
            
            // 注意：不累加总分，因为已经在checkQuarterEnd中累加过了
            
            // 进入下一节
            currentQuarter++;
            homeScore = 0;
            awayScore = 0;
            
            // 更新显示总分
            updateTotalScoreDisplay();
            
            System.out.println("✓ 进入第" + currentQuarter + "节");
            System.out.println("✓ 累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
            System.out.println("✓ 显示总分：" + totalHomeScore + ":" + totalAwayScore);
        }
        
        void updateTotalScoreDisplay() {
            // 计算当前累计总分（之前节次的总分 + 当前节次比分）
            totalHomeScore = lastTotalHomeScore + homeScore;
            totalAwayScore = lastTotalAwayScore + awayScore;
        }
        
        void showCurrentState() {
            System.out.println("\n当前状态：");
            System.out.println("  当前节次：" + currentQuarter);
            System.out.println("  当前比分：" + homeScore + ":" + awayScore);
            System.out.println("  累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
            System.out.println("  显示总分：" + totalHomeScore + ":" + totalAwayScore);
            System.out.println("  历史记录：" + quarterHistory);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== 测试手机侧总分计算错误的第二次修复 ===\n");
        
        // 测试场景1：第1节达到20分，自动进入第2节
        System.out.println("测试场景1：第1节达到20分，自动进入第2节");
        MobileMatchState match1 = new MobileMatchState();
        
        // 第1节：20-8
        System.out.println("\n第1节：20-8");
        match1.updateScore(20, 8);
        match1.showCurrentState();
        
        // 验证修复效果
        System.out.println("\n" + "=".repeat(50));
        System.out.println("验证修复效果：");
        
        int correctTotalHome1 = 20; // 第1节20
        int correctTotalAway1 = 8;  // 第1节8
        
        System.out.println("\n正确总分计算：");
        System.out.println("  第1节：20-8");
        System.out.println("  总分：" + correctTotalHome1 + "-" + correctTotalAway1);
        
        System.out.println("\n实际显示总分：" + match1.totalHomeScore + "-" + match1.totalAwayScore);
        
        if (match1.totalHomeScore == correctTotalHome1 && match1.totalAwayScore == correctTotalAway1) {
            System.out.println("✓ 修复成功！总分计算正确");
        } else {
            System.out.println("✗ 修复失败！总分计算仍有问题");
        }
        
        // 测试场景2：手动进入下一节
        System.out.println("\n\n测试场景2：手动进入下一节");
        MobileMatchState match2 = new MobileMatchState();
        
        // 第1节：15-10（未达到20分）
        System.out.println("\n第1节：15-10（未达到20分）");
        match2.updateScore(15, 10);
        match2.showCurrentState();
        
        // 手动进入第2节
        match2.nextQuarter();
        
        // 第2节：5-2
        System.out.println("\n第2节：5-2");
        match2.updateScore(5, 2);
        match2.showCurrentState();
        
        // 验证修复效果
        System.out.println("\n" + "=".repeat(50));
        System.out.println("验证修复效果：");
        
        int correctTotalHome2 = 15 + 5; // 第1节15 + 第2节5
        int correctTotalAway2 = 10 + 2; // 第1节10 + 第2节2
        
        System.out.println("\n正确总分计算：");
        System.out.println("  第1节：15-10");
        System.out.println("  第2节：5-2");
        System.out.println("  总分：" + correctTotalHome2 + "-" + correctTotalAway2);
        
        System.out.println("\n实际显示总分：" + match2.totalHomeScore + "-" + match2.totalAwayScore);
        
        if (match2.totalHomeScore == correctTotalHome2 && match2.totalAwayScore == correctTotalAway2) {
            System.out.println("✓ 修复成功！总分计算正确");
        } else {
            System.out.println("✗ 修复失败！总分计算仍有问题");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
} 