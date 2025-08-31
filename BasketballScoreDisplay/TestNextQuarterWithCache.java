import java.util.HashMap;
import java.util.Map;

/**
 * 测试nextQuarter方法的缓存逻辑
 * 参考previousQuarter，先查询下一节的比分是否已经缓存过
 */
public class TestNextQuarterWithCache {
    
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
        }
        
        void nextQuarter() {
            System.out.println("\n=== 进入下一节 ===");
            
            // 保存当前节次比分到历史记录
            quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
            
            // 更新累计总分
            lastTotalHomeScore += homeScore;
            lastTotalAwayScore += awayScore;
            
            // 更新显示总分
            totalHomeScore = lastTotalHomeScore;
            totalAwayScore = lastTotalAwayScore;
            
            // 进入下一节
            currentQuarter++;
            
            // 从历史记录中获取下一节的比分
            QuarterScore nextScore = quarterHistory.get(currentQuarter);
            if (nextScore != null) {
                // 使用历史记录中的比分
                homeScore = nextScore.homeScore;
                awayScore = nextScore.awayScore;
                
                System.out.println("✓ 从缓存中获取第" + currentQuarter + "节比分：" + homeScore + ":" + awayScore);
            } else {
                // 如果历史记录中没有，重置为0（新节次）
                homeScore = 0;
                awayScore = 0;
                
                System.out.println("✓ 第" + currentQuarter + "节为新节次，重置为0:0");
            }
            
            System.out.println("✓ 累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
            System.out.println("✓ 显示总分：" + totalHomeScore + ":" + totalAwayScore);
        }
        
        void previousQuarter() {
            System.out.println("\n=== 返回上一节 ===");
            
            // 保存当前节次比分到历史记录
            quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
            
            // 返回上一节
            currentQuarter--;
            
            // 从历史记录中获取上一节的比分
            QuarterScore previousScore = quarterHistory.get(currentQuarter);
            if (previousScore != null) {
                // 使用历史记录中的比分
                homeScore = previousScore.homeScore;
                awayScore = previousScore.awayScore;
                
                System.out.println("✓ 从缓存中获取第" + currentQuarter + "节比分：" + homeScore + ":" + awayScore);
            } else {
                // 如果历史记录中没有，重置为0
                homeScore = 0;
                awayScore = 0;
                
                System.out.println("✓ 第" + currentQuarter + "节无历史记录，重置为0:0");
            }
            
            System.out.println("✓ 累计总分：" + lastTotalHomeScore + ":" + lastTotalAwayScore);
            System.out.println("✓ 显示总分：" + totalHomeScore + ":" + totalAwayScore);
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
        System.out.println("=== 测试nextQuarter方法的缓存逻辑 ===\n");
        
        MobileMatchState match = new MobileMatchState();
        
        // 测试场景：模拟用户在节次间来回切换
        System.out.println("测试场景：模拟用户在节次间来回切换");
        
        // 第1节：10-5
        System.out.println("\n第1节：10-5");
        match.updateScore(10, 5);
        match.showCurrentState();
        
        // 进入第2节
        match.nextQuarter();
        match.showCurrentState();
        
        // 第2节：8-3
        System.out.println("\n第2节：8-3");
        match.updateScore(8, 3);
        match.showCurrentState();
        
        // 进入第3节
        match.nextQuarter();
        match.showCurrentState();
        
        // 第3节：5-2
        System.out.println("\n第3节：5-2");
        match.updateScore(5, 2);
        match.showCurrentState();
        
        // 返回第2节
        match.previousQuarter();
        match.showCurrentState();
        
        // 再次进入第3节（应该从缓存中获取）
        match.nextQuarter();
        match.showCurrentState();
        
        // 验证修复效果
        System.out.println("\n" + "=".repeat(50));
        System.out.println("验证修复效果：");
        
        System.out.println("\n修复前的逻辑：");
        System.out.println("  - nextQuarter()总是重置为0:0");
        System.out.println("  - 无法恢复之前保存的节次比分");
        
        System.out.println("\n修复后的逻辑：");
        System.out.println("  - nextQuarter()先检查缓存");
        System.out.println("  - 如果缓存中有数据，从缓存中获取");
        System.out.println("  - 如果缓存中没有数据，重置为0:0");
        System.out.println("  - 与previousQuarter()逻辑保持一致");
        
        // 验证缓存功能
        System.out.println("\n缓存功能验证：");
        System.out.println("  第3节历史记录：" + match.quarterHistory.get(3));
        System.out.println("  当前第3节比分：" + match.homeScore + ":" + match.awayScore);
        
        if (match.quarterHistory.get(3) != null && 
            match.homeScore == match.quarterHistory.get(3).homeScore &&
            match.awayScore == match.quarterHistory.get(3).awayScore) {
            System.out.println("✓ 缓存功能正常！成功从缓存中恢复比分");
        } else {
            System.out.println("✗ 缓存功能异常！未能从缓存中恢复比分");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
} 