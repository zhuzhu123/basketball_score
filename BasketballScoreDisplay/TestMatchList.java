import java.util.ArrayList;
import java.util.List;

/**
 * 测试比赛列表功能
 */
public class TestMatchList {
    
    public static void main(String[] args) {
        System.out.println("测试比赛列表功能...");
        
        // 模拟比赛数据
        List<MatchInfo> matches = new ArrayList<>();
        matches.add(new MatchInfo(1, "2024年1月15日14时", "友谊赛", 85, 92, "2024-01-15 14:00"));
        matches.add(new MatchInfo(2, "2024年1月16日16时", "联赛", 78, 75, "2024-01-16 16:00"));
        matches.add(new MatchInfo(3, "2024年1月17日19时", "杯赛", 88, 85, "2024-01-17 19:00"));
        
        System.out.println("找到 " + matches.size() + " 场比赛:");
        for (MatchInfo match : matches) {
            System.out.println("ID: " + match.getMatchId() + 
                             ", 名称: " + match.getMatchName() + 
                             ", 比分: " + match.getHomeScore() + ":" + match.getAwayScore() + 
                             ", 时间: " + match.getCreatedAt());
        }
        
        // 测试选择比赛
        MatchInfo selectedMatch = matches.get(1);
        System.out.println("\n选择比赛: " + selectedMatch.getMatchName());
        System.out.println("当前比分: " + selectedMatch.getHomeScore() + ":" + selectedMatch.getAwayScore());
        
        // 模拟更新比分
        int newHomeScore = 80;
        int newAwayScore = 78;
        System.out.println("更新比分: " + newHomeScore + ":" + newAwayScore);
        
        System.out.println("测试完成！");
    }
    
    /**
     * 比赛信息数据类
     */
    static class MatchInfo {
        private int matchId;
        private String matchName;
        private String matchNote;
        private int homeScore;
        private int awayScore;
        private String createdAt;
        
        public MatchInfo(int matchId, String matchName, String matchNote, int homeScore, int awayScore, String createdAt) {
            this.matchId = matchId;
            this.matchName = matchName;
            this.matchNote = matchNote;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
            this.createdAt = createdAt;
        }
        
        // Getters
        public int getMatchId() { return matchId; }
        public String getMatchName() { return matchName; }
        public String getMatchNote() { return matchNote; }
        public int getHomeScore() { return homeScore; }
        public int getAwayScore() { return awayScore; }
        public String getCreatedAt() { return createdAt; }
    }
} 