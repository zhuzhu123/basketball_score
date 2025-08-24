import java.io.*;
import java.net.*;

/**
 * 连接测试程序 - 验证PC端和手机端的通信
 */
public class TestConnection {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    public static void main(String[] args) {
        System.out.println("=== 篮球计分系统连接测试 ===");
        
        // 测试1：网络连接
        testNetworkConnection();
        
        // 测试2：命令发送
        testCommandSending();
        
        System.out.println("=== 测试完成 ===");
    }
    
    /**
     * 测试网络连接
     */
    private static void testNetworkConnection() {
        System.out.println("\n1. 测试网络连接...");
        
        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("✓ 网络连接成功");
            
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 发送测试命令
            String testCommand = "TEST:Hello from TestClient";
            writer.println(testCommand);
            System.out.println("✓ 发送测试命令: " + testCommand);
            
            // 接收响应
            String response = reader.readLine();
            System.out.println("✓ 收到响应: " + response);
            
            socket.close();
            System.out.println("✓ 连接已关闭");
            
        } catch (Exception e) {
            System.err.println("✗ 网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试命令发送
     */
    private static void testCommandSending() {
        System.out.println("\n2. 测试命令发送...");
        
        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 测试命令列表
            String[] testCommands = {
                "NEW_MATCH:测试比赛|这是一场测试比赛",
                "HOME_SCORE:2",
                "AWAY_SCORE:3",
                "SAVE_QUARTER:1|2|3",
                "START_COUNTDOWN",
                "SAVE_MATCH:测试比赛|2|3"
            };
            
            for (String command : testCommands) {
                System.out.println("发送命令: " + command);
                writer.println(command);
                
                String response = reader.readLine();
                System.out.println("收到响应: " + response);
                
                // 等待一下
                Thread.sleep(500);
            }
            
            socket.close();
            System.out.println("✓ 所有命令测试完成");
            
        } catch (Exception e) {
            System.err.println("✗ 命令发送测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 