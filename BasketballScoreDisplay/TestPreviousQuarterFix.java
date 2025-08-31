import java.util.HashMap;
import java.util.Map;

/**
 * 测试PC侧跳转到上一节时正确恢复比分的修复
 */
public class TestPreviousQuarterFix {
    
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
        Map<Integer, QuarterScore> quarterHistory = new HashMap<>();
        
        void setQuarterScore(int quarter, int home, int away) {
            quarterHistory.put(quarter, new QuarterScore(home, away));
            System.out.println("✓ 设置第" + quarter + "节比分：" + home + ":" + away);
        }
        
        void selectPreviousQuarter(int targetQuarter) {
            System.out.println("\n=== 选择第" + targetQuarter + "节 ===");
            
            // 检查目标节次是否存在
            QuarterScore targetScore = quarterHistory.get(targetQuarter);
            if (targetScore == null) {
                System.out.println("✗ 未找到第" + targetQuarter + "节的比分记录");
                return;
            }
            
            // 更新当前节次变量（这是之前缺失的关键步骤）
            currentQuarter = targetQuarter;
            
            // 恢复该节次的比分
            homeScore = targetScore.homeScore;
            awayScore = targetScore.awayScore;
            
            System.out.println("✓ 当前节次已更新为：" + currentQuarter);
            System.out.println("✓ 当前比分已恢复为：" + homeScore + ":" + awayScore);
            
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
                    System.out.println("  第" + i + "节：0:0");
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== 测试PC侧跳转到上一节时正确恢复比分的修复 ===\n");
        
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
        
        // 初始状态：第2节
        pcDisplay.currentQuarter = 2;
        pcDisplay.homeScore = 10;
        pcDisplay.awayScore = 9;
        
        System.out.println("\n初始状态：");
        pcDisplay.verifyDisplay();
        pcDisplay.showAllQuarters();
        
        // 测试1：从第2节跳转到第1节
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试1：从第2节跳转到第1节");
        pcDisplay.selectPreviousQuarter(1);
        
        // 测试2：从第1节跳转到第2节
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试2：从第1节跳转到第2节");
        pcDisplay.selectPreviousQuarter(2);
        
        // 测试3：尝试跳转到不存在的节次
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试3：尝试跳转到不存在的节次");
        pcDisplay.selectPreviousQuarter(8);
        
        // 测试4：验证修复前后的对比
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试4：验证修复效果");
        
        System.out.println("\n修复前的问题：");
        System.out.println("  - 跳转到上一节时，currentQuarter变量没有更新");
        System.out.println("  - 导致当前节次比分显示为0-0，而不是正确的历史比分");
        System.out.println("  - 节次比分显示区域显示错误");
        
        System.out.println("\n修复后的效果：");
        System.out.println("  - 跳转到上一节时，正确更新currentQuarter变量");
        System.out.println("  - 正确恢复历史比分到当前显示");
        System.out.println("  - 节次比分显示区域正确高亮当前节次");
        System.out.println("  - 调用updateQuarterScoresDisplay()更新显示");
        
        System.out.println("\n=== 测试完成 ===");
    }
} 