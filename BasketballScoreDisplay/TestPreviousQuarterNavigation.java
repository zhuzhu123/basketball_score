import java.util.HashMap;
import java.util.Map;

/**
 * 测试手机侧跳转到上一节功能
 */
public class TestPreviousQuarterNavigation {
    
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
    
    // 模拟比赛状态
    static class MatchState {
        int currentQuarter = 1;
        int homeScore = 0;
        int awayScore = 0;
        int totalHomeScore = 0;
        int totalAwayScore = 0;
        int lastTotalHomeScore = 0;
        int lastTotalAwayScore = 0;
        boolean isMatchStarted = false;
        Map<Integer, QuarterScore> quarterHistory = new HashMap<>();
        
        void createNewMatch() {
            currentQuarter = 1;
            homeScore = 0;
            awayScore = 0;
            totalHomeScore = 0;
            totalAwayScore = 0;
            lastTotalHomeScore = 0;
            lastTotalAwayScore = 0;
            isMatchStarted = true;
            quarterHistory.clear();
            System.out.println("✓ 新建比赛成功");
        }
        
        void updateScore(int homePoints, int awayPoints) {
            if (!isMatchStarted) {
                System.out.println("✗ 请先新建比赛");
                return;
            }
            
            homeScore += homePoints;
            awayScore += awayPoints;
            System.out.println("✓ 更新比分：" + homeScore + ":" + awayScore);
        }
        
        void nextQuarter() {
            if (!isMatchStarted) {
                System.out.println("✗ 请先新建比赛");
                return;
            }
            
            if (currentQuarter >= 10) {
                System.out.println("✗ 已达到最大节次");
                return;
            }
            
            // 保存当前节次比分到历史记录
            quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
            
            // 更新总分
            lastTotalHomeScore += homeScore;
            lastTotalAwayScore += awayScore;
            
            // 进入下一节
            currentQuarter++;
            homeScore = 0;
            awayScore = 0;
            
            System.out.println("✓ 进入第" + currentQuarter + "节");
        }
        
        void previousQuarter() {
            if (!isMatchStarted) {
                System.out.println("✗ 请先新建比赛");
                return;
            }
            
            if (currentQuarter <= 1) {
                System.out.println("✗ 已经是第1节，无法返回上一节");
                return;
            }
            
            // 保存当前节次比分到历史记录
            quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
            
            // 从总分中减去当前节次比分
            lastTotalHomeScore -= homeScore;
            lastTotalAwayScore -= awayScore;
            
            // 返回上一节
            currentQuarter--;
            
            // 从历史记录中获取上一节的比分
            QuarterScore previousScore = quarterHistory.get(currentQuarter);
            if (previousScore != null) {
                homeScore = previousScore.homeScore;
                awayScore = previousScore.awayScore;
                System.out.println("✓ 返回第" + currentQuarter + "节，比分：" + homeScore + ":" + awayScore);
            } else {
                homeScore = 0;
                awayScore = 0;
                System.out.println("✓ 返回第" + currentQuarter + "节（无历史记录）");
            }
        }
        
        void showCurrentState() {
            System.out.println("\n当前状态：");
            System.out.println("  当前节次：" + currentQuarter);
            System.out.println("  当前比分：" + homeScore + ":" + awayScore);
            System.out.println("  累计总分：" + (lastTotalHomeScore + homeScore) + ":" + (lastTotalAwayScore + awayScore));
            System.out.println("  历史记录：" + quarterHistory);
        }
        
        void showHistory() {
            if (quarterHistory.isEmpty()) {
                System.out.println("暂无历史记录");
                return;
            }
            
            System.out.println("\n节次历史记录：");
            for (int i = 1; i <= currentQuarter; i++) {
                QuarterScore score = quarterHistory.get(i);
                if (score != null) {
                    System.out.println("  第" + i + "节：" + score);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== 测试手机侧跳转到上一节功能 ===\n");
        
        MatchState match = new MatchState();
        
        // 测试1：新建比赛
        System.out.println("测试1：新建比赛");
        match.createNewMatch();
        match.showCurrentState();
        
        // 测试2：第1节计分
        System.out.println("\n测试2：第1节计分");
        match.updateScore(5, 3);
        match.updateScore(2, 1);
        match.showCurrentState();
        
        // 测试3：进入第2节
        System.out.println("\n测试3：进入第2节");
        match.nextQuarter();
        match.showCurrentState();
        
        // 测试4：第2节计分
        System.out.println("\n测试4：第2节计分");
        match.updateScore(3, 2);
        match.updateScore(1, 4);
        match.showCurrentState();
        
        // 测试5：进入第3节
        System.out.println("\n测试5：进入第3节");
        match.nextQuarter();
        match.showCurrentState();
        
        // 测试6：第3节计分
        System.out.println("\n测试6：第3节计分");
        match.updateScore(2, 2);
        match.showCurrentState();
        
        // 测试7：显示历史记录
        System.out.println("\n测试7：显示历史记录");
        match.showHistory();
        
        // 测试8：返回第2节
        System.out.println("\n测试8：返回第2节");
        match.previousQuarter();
        match.showCurrentState();
        
        // 测试9：返回第1节
        System.out.println("\n测试9：返回第1节");
        match.previousQuarter();
        match.showCurrentState();
        
        // 测试10：尝试返回第0节（应该失败）
        System.out.println("\n测试10：尝试返回第0节");
        match.previousQuarter();
        match.showCurrentState();
        
        // 测试11：再次显示历史记录
        System.out.println("\n测试11：再次显示历史记录");
        match.showHistory();
        
        // 测试12：从第1节进入第2节，验证历史记录
        System.out.println("\n测试12：从第1节进入第2节");
        match.nextQuarter();
        match.showCurrentState();
        match.showHistory();
        
        System.out.println("\n=== 测试完成 ===");
    }
} 