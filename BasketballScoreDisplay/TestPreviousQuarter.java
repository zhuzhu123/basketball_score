import java.util.ArrayList;
import java.util.List;

/**
 * 测试选择上一节和修改上一节比分功能
 */
public class TestPreviousQuarter {
    
    public static void main(String[] args) {
        System.out.println("测试选择上一节和修改上一节比分功能...");
        
        // 测试1: 选择上一节
        testSelectPreviousQuarter();
        
        // 测试2: 修改上一节比分
        testModifyPreviousQuarter();
        
        // 测试3: 命令格式验证
        testCommandFormat();
        
        // 测试4: 数据流程模拟
        testDataFlow();
        
        System.out.println("测试完成！");
    }
    
    /**
     * 测试选择上一节
     */
    private static void testSelectPreviousQuarter() {
        System.out.println("\n=== 测试选择上一节 ===");
        
        // 模拟比赛数据
        int matchId = 1;
        int currentQuarter = 3;
        int targetQuarter = 2;
        
        System.out.println("当前比赛ID: " + matchId);
        System.out.println("当前节次: " + currentQuarter);
        System.out.println("目标节次: " + targetQuarter);
        
        // 验证节次有效性
        if (targetQuarter >= 1 && targetQuarter < currentQuarter) {
            System.out.println("节次有效，可以选择");
            
            // 模拟发送选择上一节命令
            String command = "SELECT_PREVIOUS_QUARTER:" + matchId + "|" + targetQuarter;
            System.out.println("发送命令: " + command);
            
            // 模拟PC端响应
            String response = "PREVIOUS_QUARTER:" + targetQuarter + "|15|12";
            System.out.println("收到响应: " + response);
            
            // 解析响应
            String[] parts = response.split(":");
            if (parts.length >= 2) {
                String[] data = parts[1].split("\\|");
                if (data.length >= 3) {
                    int quarter = Integer.parseInt(data[0]);
                    int homeScore = Integer.parseInt(data[1]);
                    int awayScore = Integer.parseInt(data[2]);
                    
                    System.out.println("解析结果:");
                    System.out.println("  节次: " + quarter);
                    System.out.println("  主队得分: " + homeScore);
                    System.out.println("  客队得分: " + awayScore);
                }
            }
            
        } else {
            System.out.println("节次无效，无法选择");
        }
    }
    
    /**
     * 测试修改上一节比分
     */
    private static void testModifyPreviousQuarter() {
        System.out.println("\n=== 测试修改上一节比分 ===");
        
        // 模拟比赛数据
        int matchId = 1;
        int quarterNumber = 2;
        int oldHomeScore = 15;
        int oldAwayScore = 12;
        int newHomeScore = 18;
        int newAwayScore = 14;
        
        System.out.println("比赛ID: " + matchId);
        System.out.println("节次: " + quarterNumber);
        System.out.println("原比分: " + oldHomeScore + ":" + oldAwayScore);
        System.out.println("新比分: " + newHomeScore + ":" + newAwayScore);
        
        // 模拟发送修改上一节比分命令
        String command = "UPDATE_PREVIOUS_QUARTER:" + matchId + "|" + quarterNumber + "|" + newHomeScore + "|" + newAwayScore;
        System.out.println("发送命令: " + command);
        
        // 模拟PC端响应
        String response = "PREVIOUS_QUARTER_UPDATED";
        System.out.println("收到响应: " + response);
        
        if ("PREVIOUS_QUARTER_UPDATED".equals(response)) {
            System.out.println("修改成功！");
            
            // 模拟收到更新后的总分
            String totalScoreResponse = "TOTAL_SCORE_UPDATED:45|38";
            System.out.println("收到总分更新: " + totalScoreResponse);
            
            // 解析总分
            String[] parts = totalScoreResponse.split(":");
            if (parts.length >= 2) {
                String[] data = parts[1].split("\\|");
                if (data.length >= 2) {
                    int totalHomeScore = Integer.parseInt(data[0]);
                    int totalAwayScore = Integer.parseInt(data[1]);
                    
                    System.out.println("更新后的总分:");
                    System.out.println("  主队总分: " + totalHomeScore);
                    System.out.println("  客队总分: " + totalAwayScore);
                }
            }
        } else {
            System.out.println("修改失败！");
        }
    }
    
    /**
     * 测试命令格式验证
     */
    private static void testCommandFormat() {
        System.out.println("\n=== 测试命令格式验证 ===");
        
        // 测试选择上一节命令格式
        String selectCommand = "SELECT_PREVIOUS_QUARTER:1|2";
        System.out.println("选择上一节命令: " + selectCommand);
        
        if (selectCommand.startsWith("SELECT_PREVIOUS_QUARTER:")) {
            String[] parts = selectCommand.split(":");
            if (parts.length >= 2) {
                String[] data = parts[1].split("\\|");
                if (data.length >= 2) {
                    System.out.println("命令格式正确");
                    System.out.println("  比赛ID: " + data[0]);
                    System.out.println("  节次: " + data[1]);
                } else {
                    System.out.println("命令格式错误：数据部分不完整");
                }
            } else {
                System.out.println("命令格式错误：缺少冒号分隔符");
            }
        } else {
            System.out.println("命令格式错误：不是选择上一节命令");
        }
        
        // 测试修改上一节比分命令格式
        String updateCommand = "UPDATE_PREVIOUS_QUARTER:1|2|18|14";
        System.out.println("\n修改上一节比分命令: " + updateCommand);
        
        if (updateCommand.startsWith("UPDATE_PREVIOUS_QUARTER:")) {
            String[] parts = updateCommand.split(":");
            if (parts.length >= 2) {
                String[] data = parts[1].split("\\|");
                if (data.length >= 4) {
                    System.out.println("命令格式正确");
                    System.out.println("  比赛ID: " + data[0]);
                    System.out.println("  节次: " + data[1]);
                    System.out.println("  主队得分: " + data[2]);
                    System.out.println("  客队得分: " + data[3]);
                } else {
                    System.out.println("命令格式错误：数据部分不完整");
                }
            } else {
                System.out.println("命令格式错误：缺少冒号分隔符");
            }
        } else {
            System.out.println("命令格式错误：不是修改上一节比分命令");
        }
    }
    
    /**
     * 测试数据流程模拟
     */
    private static void testDataFlow() {
        System.out.println("\n=== 测试数据流程模拟 ===");
        
        // 模拟完整的上一节操作流程
        System.out.println("1. 用户选择第2节");
        System.out.println("   手机端发送: SELECT_PREVIOUS_QUARTER:1|2");
        System.out.println("   PC端接收并查询数据库");
        System.out.println("   PC端返回: PREVIOUS_QUARTER:2|15|12");
        System.out.println("   手机端显示第2节比分: 15:12");
        
        System.out.println("\n2. 用户修改第2节比分");
        System.out.println("   手机端发送: UPDATE_PREVIOUS_QUARTER:1|2|18|14");
        System.out.println("   PC端接收并更新数据库");
        System.out.println("   PC端返回: PREVIOUS_QUARTER_UPDATED");
        System.out.println("   PC端计算并返回新总分: TOTAL_SCORE_UPDATED:45|38");
        System.out.println("   手机端更新显示");
        
        System.out.println("\n3. 数据同步完成");
        System.out.println("   手机端和PC端比分保持一致");
        System.out.println("   第2节比分: 18:14");
        System.out.println("   比赛总分: 45:38");
        
        System.out.println("数据流程模拟完成");
    }
    
    /**
     * 模拟节次比分数据
     */
    static class QuarterScore {
        private int quarterNumber;
        private int homeScore;
        private int awayScore;
        
        public QuarterScore(int quarterNumber, int homeScore, int awayScore) {
            this.quarterNumber = quarterNumber;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }
        
        public int getQuarterNumber() { return quarterNumber; }
        public int getHomeScore() { return homeScore; }
        public int getAwayScore() { return awayScore; }
    }
} 