package com.basketball.display;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * 蓝牙管理器 - 完全基于 BluetoothServer.java 的代码结构
 */
public class BluetoothManager {
    private static final String PC_BLUETOOTH_NAME = "BasketballScore";
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    
    // 蓝牙连接相关
    private LocalDevice localDevice;
    private StreamConnectionNotifier notifier;
    private StreamConnection connection;
    private InputStream bluetoothInput;
    private OutputStream bluetoothOutput;
    private boolean isConnected = false;
    
    // 添加线程控制
    private ExecutorService serverExecutor;
    private volatile boolean shouldStop = false;
    
    // 数据库管理器
    private DatabaseManager databaseManager;
    
    // 当前比赛ID
    private int currentMatchId = -1;
    
    // 回调接口
    private BluetoothCallback callback;
    
    // 连接类型
    private ConnectionType connectionType = ConnectionType.BLUETOOTH;
    
    public enum ConnectionType {
        BLUETOOTH, NETWORK, NONE
    }
    
    public interface BluetoothCallback {
        void onConnectionStatusChanged(boolean isConnected, ConnectionType type);
        void onError(String message);
        void onDeviceFound(String deviceInfo);
        void onDataReceived(String data);
        void onMatchCreated(int matchId, String matchName, String matchNote);
        void onQuarterSaved(int quarter, int homeScore, int awayScore);
        void onMatchSaved(String matchName, int totalHomeScore, int totalAwayScore);
    }
    
    public BluetoothManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.serverExecutor = Executors.newSingleThreadExecutor();
    }
    
    public void setCallback(BluetoothCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 初始化蓝牙（按照 BluetoothServer.java 的步骤1）
     */
    public boolean initializeBluetooth() {
        try {
            // 1. 获取本地蓝牙设备（完全复制 BluetoothServer.java 的代码）
            localDevice = LocalDevice.getLocalDevice();
            System.out.println("PC Bluetooth Address: " + localDevice.getBluetoothAddress());
            System.out.println("PC Bluetooth Name: " + localDevice.getFriendlyName());
            
            System.out.println("蓝牙初始化完成");
            return true;
        } catch (Exception e) {
            System.err.println("蓝牙初始化失败: " + e.getMessage());
            if (callback != null) {
                callback.onError("蓝牙初始化失败: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * 启动蓝牙服务器（按照 BluetoothServer.java 的步骤2-9）
     */
    public void startBluetoothServer() {
        if (localDevice == null) {
            if (callback != null) {
                callback.onError("蓝牙设备未初始化");
            }
            return;
        }
        
        // 重置停止标志
        shouldStop = false;
        
        // 在独立线程中启动服务器，避免阻塞主线程
        serverExecutor.submit(() -> {
        try {
            // 2. 创建 UUID（唯一标识服务）（完全复制 BluetoothServer.java 的代码）
            UUID uuid = new UUID("1101", true); // 标准串口服务 UUID
            
            // 3. 创建服务 URL（完全复制 BluetoothServer.java 的代码）
            String url = "btspp://localhost:" + uuid + ";name=BluetoothServer";
            // 4. 启动服务端 Socket（完全复制 BluetoothServer.java 的代码）
            notifier = (StreamConnectionNotifier) Connector.open(url);
            System.out.println("Server started. Waiting for client...");
            
            if (callback != null) {
                callback.onConnectionStatusChanged(false, ConnectionType.BLUETOOTH);
            }
            
                // 修复死循环：添加退出条件
                while (!shouldStop) {
                try {
                    // 5. 等待客户端连接（完全复制 BluetoothServer.java 的代码）
                    connection = notifier.acceptAndOpen();
                    System.out.println("Client connected!");
                    
                    // 6. 获取输入输出流（完全复制 BluetoothServer.java 的代码）
                    bluetoothInput = connection.openInputStream();
                    bluetoothOutput = connection.openOutputStream();
                    
                    isConnected = true;
                    if (callback != null) {
                        callback.onConnectionStatusChanged(true, ConnectionType.BLUETOOTH);
                    }
                    
                    // 7. 持续接收数据（扩展 BluetoothServer.java 的功能）
                    handleBluetoothData();
                    
                } catch (IOException e) {
                    if (isConnected) {
                        System.err.println("蓝牙连接错误: " + e.getMessage());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("蓝牙服务器启动失败: " + e.getMessage());
            if (callback != null) {
                callback.onError("蓝牙服务器启动失败: " + e.getMessage());
            }
        }
        });
    }
    
    /**
     * 处理蓝牙数据（基于 BluetoothServer.java 的接收逻辑）
     */
    private void handleBluetoothData() {
        try {
            // 使用与 BluetoothServer.java 完全相同的接收逻辑
            byte[] buffer = new byte[1024];
            while (isConnected && connection != null) {
                int bytesRead = bluetoothInput.read(buffer);
                if (bytesRead > 0) {
                    String received = new String(buffer, 0, bytesRead).trim();
                    System.out.println("Received: " + received);
                    
                    // 在UI线程中处理数据
                    SwingUtilities.invokeLater(() -> processCommand(received));
                    
                    // 发送响应（基于 BluetoothServer.java 的响应逻辑）
                    bluetoothOutput.write("ACK from BasketballScore".getBytes());
                }
            }
        } catch (IOException e) {
            System.err.println("蓝牙数据处理错误: " + e.getMessage());
        } finally {
            closeBluetoothConnection();
        }
    }
    
    /**
     * 处理命令
     */
    private void processCommand(String command) {
        // 只传递给回调处理，避免重复处理
        if (callback != null) {
            callback.onDataReceived(command);
        }
    }
    
    /**
     * 处理新建比赛命令
     */
    private void handleNewMatch(String value) throws Exception {
        String[] parts = value.split("\\|");
        if (parts.length >= 2) {
            String matchName = parts[0];
            String matchNote = parts[1];
            
            int matchId = databaseManager.createNewMatch(matchName, matchNote);
            currentMatchId = matchId; // 设置当前比赛ID
            System.out.println("新建比赛成功: " + matchName + ", ID: " + matchId);
            
            if (callback != null) {
                callback.onMatchCreated(matchId, matchName, matchNote);
            }
            
            sendResponse("MATCH_CREATED:" + matchId);
        }
    }
    
    /**
     * 处理保存节次命令
     */
    private void handleSaveQuarter(String value) throws Exception {
        String[] parts = value.split("\\|");
        if (parts.length >= 3) {
            int quarter = Integer.parseInt(parts[0]);
            int homeScore = Integer.parseInt(parts[1]);
            int awayScore = Integer.parseInt(parts[2]);
            
            // 假设使用最新的比赛ID，实际应用中需要维护当前比赛ID
            int matchId = getCurrentMatchId();
            if (matchId > 0) {
                boolean success = databaseManager.saveQuarterScore(matchId, quarter, homeScore, awayScore);
                System.out.println("保存第" + quarter + "节比分: " + homeScore + "-" + awayScore);
                
                if (callback != null) {
                    callback.onQuarterSaved(quarter, homeScore, awayScore);
                }
                
                sendResponse(success ? "QUARTER_SAVED" : "QUARTER_SAVE_FAILED");
            }
        }
    }
    
    /**
     * 处理保存比赛命令
     */
    private void handleSaveMatch(String value) throws Exception {
        String[] parts = value.split("\\|");
        if (parts.length >= 3) {
            String matchName = parts[0];
            int totalHomeScore = Integer.parseInt(parts[1]);
            int totalAwayScore = Integer.parseInt(parts[2]);
            
            int matchId = getCurrentMatchId();
            if (matchId > 0) {
                boolean success = databaseManager.updateMatchTotalScore(matchId, totalHomeScore, totalAwayScore);
                System.out.println("保存比赛总分: " + matchName + " " + totalHomeScore + "-" + totalAwayScore);
                
                if (callback != null) {
                    callback.onMatchSaved(matchName, totalHomeScore, totalAwayScore);
                }
                
                sendResponse(success ? "MATCH_SAVED" : "MATCH_SAVE_FAILED");
            }
        }
    }
    
    /**
     * 处理同步所有分数命令
     */
    private void handleSyncAllScores(String value) {
        try {
            System.out.println("收到同步所有分数命令");
            
            // 通知回调处理同步
            if (callback != null) {
                callback.onDataReceived("SYNC_ALL_SCORES");
            }
            
            sendResponse("SYNC_ALL_SCORES_RECEIVED");
        } catch (Exception e) {
            System.err.println("处理同步所有分数命令失败: " + e.getMessage());
            if (callback != null) {
                callback.onError("处理同步所有分数命令失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取当前比赛ID
     */
    private int getCurrentMatchId() {
        return currentMatchId;
    }
    
    /**
     * 发送响应
     */
    private void sendResponse(String response) {
        if (isConnected && bluetoothOutput != null) {
            try {
                bluetoothOutput.write(response.getBytes());
                System.out.println("发送响应: " + response);
            } catch (IOException e) {
                System.err.println("发送响应失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 公共发送方法，供外部调用
     */
    public void sendDataToMobile(String data) {
        if (isConnected && bluetoothOutput != null) {
            try {
                bluetoothOutput.write(data.getBytes());
                System.out.println("向手机端发送数据: " + data);
            } catch (IOException e) {
                System.err.println("向手机端发送数据失败: " + e.getMessage());
            }
        } else {
            System.out.println("蓝牙未连接，无法发送数据");
        }
    }
    
    /**
     * 开始扫描（模拟蓝牙扫描）
     */
    public void startScan() {
        // 模拟扫描过程
        System.out.println("开始扫描蓝牙设备...");
        try {
            Thread.sleep(1000); // 模拟扫描时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("发现模拟蓝牙设备: 篮球计分系统");
        if (callback != null) {
            callback.onDeviceFound("模拟蓝牙设备: 篮球计分系统");
        }
        startBluetoothServer();
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        shouldStop = true;
        closeBluetoothConnection();
    }
    
    /**
     * 关闭蓝牙连接（按照 BluetoothServer.java 的步骤9）
     */
    private void closeBluetoothConnection() {
        try {
            // 9. 关闭连接（完全复制 BluetoothServer.java 的代码）
            if (bluetoothInput != null) bluetoothInput.close();
            if (bluetoothOutput != null) bluetoothOutput.close();
            if (connection != null) connection.close();
            if (notifier != null) notifier.close();
        } catch (IOException e) {
            System.err.println("关闭蓝牙连接错误: " + e.getMessage());
        } finally {
            isConnected = false;
            if (callback != null) {
                callback.onConnectionStatusChanged(false, ConnectionType.BLUETOOTH);
            }
            System.out.println("蓝牙连接已关闭");
        }
    }
    
    /**
     * 获取连接状态
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * 获取连接类型
     */
    public ConnectionType getConnectionType() {
        return isConnected ? ConnectionType.BLUETOOTH : ConnectionType.NONE;
    }
    
    /**
     * 检查蓝牙是否可用
     */
    public boolean isBluetoothAvailable() {
        return localDevice != null;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        // 停止服务器线程
        shouldStop = true;
        if (serverExecutor != null && !serverExecutor.isShutdown()) {
            serverExecutor.shutdown();
            try {
                if (!serverExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    serverExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                serverExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("蓝牙管理器清理完成");
    }
} 