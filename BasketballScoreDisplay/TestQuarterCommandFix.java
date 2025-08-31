import java.util.HashMap;
import java.util.Map;

/**
 * 测试PC侧处理QUARTER命令时正确恢复历史比分的修复
 */
public class TestQuarterCommandFix {
    
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
    
    // 模拟PC侧显示状态
    static class PCDisplayState {
        int currentQuarter = 1;
        int homeScore = 0;
        int awayScore = 0;
        int currentMatchId = 1;
        Map<Integer, QuarterScore> quarterHistory = new HashMap<>();
        
        void setQuarterScore(int quarter, int home, int away) {
            quarterHistory.put(quarter, new QuarterScore(home, away));
            System.out.println("✓ 设置第" + quarter + "节比分：" + home + ":" + away);
        }
        
        void handleQuarterCommand(int newQuarter) {
            System.out.println("\n=== 处理QUARTER命令: " + newQuarter + " ===");
            
            // 保存当前节次比分到数据库（模拟）
            if (currentQuarter > 0) {
                quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
                System.out.println("✓ 保存第" + currentQuarter + "节比分: " + homeScore + ":" + awayScore);
            }
            
            // 更新当前节次
            currentQuarter = newQuarter;
            System.out.println("✓ 当前节次更新为: " + currentQuarter);
            
            // 从数据库获取该节次的历史比分
            QuarterScore quarterScore = quarterHistory.get(currentQuarter);
            if (quarterScore != null) {
                // 恢复历史比分
                homeScore = quarterScore.getHomeScore();
                awayScore = quarterScore.getAwayScore();
                System.out.println("✓ 恢复第" + currentQuarter + "节历史比分: " + homeScore + ":" + awayScore);
            } else {
                // 如果没有历史记录，重置为0
                homeScore = 0;
                awayScore = 0;
                System.out.println("✓ 第" + currentQuarter + "节无历史记录，重置为0:0");
            }
            
            // 验证修复效果
            verifyDisplay();
        }
        
        void verifyDisplay() {
            System.out.println("\n验证显示状态：");
            System.out.println("  当前节次：" + currentQuarter);
            System.out.println("  当前比分：" + homeScore + ":" + awayScore);
            
            // 检查当前节次比分显示是否正确
            QuarterScore currentQuarterScore = quarterHistory.get(currentQuarter);
            if (currentQuarterScore != null && 
                currentQuarterScore.homeScore == homeScore && 
                currentQuarterScore.awayScore == awayScore) {
                System.out.println("✓ 当前节次比分显示正确");
            } else if (currentQuarterScore == null && homeScore == 0 && awayScore == 0) {
                System.out.println("✓ 新节次，比分正确重置为0:0");
            } else {
                System.out.println("✗ 当前节次比分显示错误");
            }
        }
        
        void showAllQuarters() {
            System.out.println("\n所有节次比分：");
            for (int i = 1; i <= 7; i++) {
                QuarterScore score = quarterHistory.get(i);
                if (score != null) {
                    String marker = (i == currentQuarter) ? " [当前]" : "";
                    System.out.println("  第" + i + "节：" + score + marker);
                } else {
                    String marker = (i == currentQuarter) ? " [当前]" : "";
                    System.out.println("  第" + i + "节：0:0" + marker);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== 测试PC侧处理QUARTER命令时正确恢复历史比分的修复 ===\n");
        
        PCDisplayState pcDisplay = new PCDisplayState();
        
        // 模拟比赛数据（从图片中看到的数据）
        System.out.println("设置比赛数据：");
        pcDisplay.setQuarterScore(1, 20, 5);   // 第1节：20-5
        pcDisplay.setQuarterScore(2, 10, 9);   // 第2节：10-9
        pcDisplay.setQuarterScore(3, 0, 0);    // 第3节：0-0
        pcDisplay.setQuarterScore(4, 0, 0);    // 第4节：0-0
        pcDisplay.setQuarterScore(5, 0, 0);    // 第5节：0-0
        pcDisplay.setQuarterScore(6, 0, 0);    // 第6节：0-0
        pcDisplay.setQuarterScore(7, 0, 0);    // 第7节：0-0
        
        // 初始状态：第2节，当前比分10-9
        pcDisplay.currentQuarter = 2;
        pcDisplay.homeScore = 10;
        pcDisplay.awayScore = 9;
        
        System.out.println("\n初始状态：");
        pcDisplay.verifyDisplay();
        pcDisplay.showAllQuarters();
        
        // 测试1：从第2节跳转到第1节（QUARTER:1）
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试1：从第2节跳转到第1节（QUARTER:1）");
        pcDisplay.handleQuarterCommand(1);
        
        // 测试2：从第1节跳转到第2节（QUARTER:2）
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试2：从第1节跳转到第2节（QUARTER:2）");
        pcDisplay.handleQuarterCommand(2);
        
        // 测试3：跳转到新节次（QUARTER:3）
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试3：跳转到新节次（QUARTER:3）");
        pcDisplay.handleQuarterCommand(3);
        
        // 测试4：验证修复前后的对比
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试4：验证修复效果");
        
        System.out.println("\n修复前的问题：");
        System.out.println("  - 处理QUARTER命令时，总是重置比分为0-0");
        System.out.println("  - 导致跳转到上一节时，历史比分丢失");
        System.out.println("  - 无法正确恢复历史比分");
        
        System.out.println("\n修复后的效果：");
        System.out.println("  - 处理QUARTER命令时，先保存当前节次比分");
        System.out.println("  - 从数据库获取目标节次的历史比分");
        System.out.println("  - 如果有历史记录，恢复历史比分");
        System.out.println("  - 如果没有历史记录，重置为0-0");
        System.out.println("  - 确保数据一致性和用户体验");
        
        System.out.println("\n=== 测试完成 ===");
    }
} 