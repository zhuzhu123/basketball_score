import java.io.*;
import java.net.*;

/**
 * 网络客户端测试程序 - 模拟手机端发送命令
 */
public class TestNetworkClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    public static void main(String[] args) {
        try {
            System.out.println("正在连接到PC端服务器...");
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            System.out.println("连接成功！开始发送测试命令...");
            
            // 测试新建比赛
            System.out.println("\n1. 测试新建比赛");
            writer.println("NEW_MATCH:2025年08月23日13时测试赛|这是一场测试比赛");
            String response = reader.readLine();
            System.out.println("PC响应: " + response);
            Thread.sleep(1000);
            
            // 测试龙都F4得分
            System.out.println("\n2. 测试龙都F4得分");
            writer.println("HOME_SCORE:2");
            response = reader.readLine();
            System.out.println("PC响应: " + response);
            Thread.sleep(1000);
            
            // 测试暴风队得分
            System.out.println("\n3. 测试暴风队得分");
            writer.println("AWAY_SCORE:3");
            response = reader.readLine();
            System.out.println("PC响应: " + response);
            Thread.sleep(1000);
            
            // 测试保存节次
            System.out.println("\n4. 测试保存节次");
            writer.println("SAVE_QUARTER:1|2|3");
            response = reader.readLine();
            System.out.println("PC响应: " + response);
            Thread.sleep(1000);
            
            // 测试倒计时
            System.out.println("\n5. 测试倒计时");
            writer.println("START_COUNTDOWN");
            response = reader.readLine();
            System.out.println("PC响应: " + response);
            Thread.sleep(2000);
            
            // 测试保存比赛
            System.out.println("\n6. 测试保存比赛");
            writer.println("SAVE_MATCH:2025年08月23日13时测试赛|2|3");
            response = reader.readLine();
            System.out.println("PC响应: " + response);
            
            System.out.println("\n测试完成！");
            
            socket.close();
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 