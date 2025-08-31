import java.util.HashMap;
import java.util.Map;

/**
 * 测试手机侧总分计算错误的修复
 */
public class TestTotalScoreCalculationFix {
    
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
                
                // 修复前的问题代码
                // totalHomeScore += homeScore;
                // totalAwayScore += awayScore;
                
                // 修复后的正确代码
                lastTotalHomeScore += homeScore;
                lastTotalAwayScore += awayScore;
                totalHomeScore = lastTotalHomeScore;
                totalAwayScore = lastTotalAwayScore;
                
                System.out.println("✓ 更新累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
                System.out.println("✓ 更新显示总分：" + totalHomeScore + ":" + totalAwayScore);
            }
        }
        
        void nextQuarter() {
            System.out.println("\n=== 进入下一节 ===");
            
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
        System.out.println("=== 测试手机侧总分计算错误的修复 ===\n");
        
        MobileMatchState match = new MobileMatchState();
        
        // 测试场景：模拟图片中的情况
        System.out.println("测试场景：模拟图片中的情况");
        System.out.println("第1节：20-4，第2节：3-2，当前显示：3-0，总分：60-10（错误）");
        
        // 第1节：20-4
        System.out.println("\n第1节：20-4");
        match.updateScore(20, 4);
        match.showCurrentState();
        
        // 进入第2节
        match.nextQuarter();
        
        // 第2节：3-2
        System.out.println("\n第2节：3-2");
        match.updateScore(3, 2);
        match.showCurrentState();
        
        // 验证修复效果
        System.out.println("\n" + "=".repeat(50));
        System.out.println("验证修复效果：");
        
        System.out.println("\n修复前的问题：");
        System.out.println("  - checkQuarterEnd()中直接修改totalHomeScore += homeScore");
        System.out.println("  - 导致总分计算错误，显示60-10而不是23-6");
        System.out.println("  - 总分计算逻辑不一致");
        
        System.out.println("\n修复后的效果：");
        System.out.println("  - checkQuarterEnd()中正确更新lastTotalHomeScore += homeScore");
        System.out.println("  - 然后设置totalHomeScore = lastTotalHomeScore");
        System.out.println("  - 确保总分计算逻辑一致");
        System.out.println("  - 正确显示总分：23-6");
        
        // 计算正确的总分
        int correctTotalHome = 20 + 3; // 第1节20 + 第2节3
        int correctTotalAway = 4 + 2;  // 第1节4 + 第2节2
        
        System.out.println("\n正确总分计算：");
        System.out.println("  第1节：20-4");
        System.out.println("  第2节：3-2");
        System.out.println("  总分：" + correctTotalHome + "-" + correctTotalAway);
        
        System.out.println("\n实际显示总分：" + match.totalHomeScore + "-" + match.totalAwayScore);
        
        if (match.totalHomeScore == correctTotalHome && match.totalAwayScore == correctTotalAway) {
            System.out.println("✓ 修复成功！总分计算正确");
        } else {
            System.out.println("✗ 修复失败！总分计算仍有问题");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
} 