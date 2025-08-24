package com.basketball.scoreremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.InputStreamReader;

public class BluetoothManager {
    
    private static final String TAG = "BluetoothManager";
    // 标准串口服务UUID
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    // 连接配置
    private static final int CONNECTION_TIMEOUT = 10000; // 10秒连接超时
    private static final int MAX_RETRY_ATTEMPTS = 3; // 最大重试次数
    
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private boolean isConnected = false;
    private BluetoothCallback callback;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    // 网络连接相关（备用连接方式）
    private Socket networkSocket;
    private PrintWriter networkWriter;
    private BufferedReader networkReader;
    private Thread networkThread;
    private boolean isNetworkConnected = false;
    
    // PC端蓝牙配置
    private static final String PC_BLUETOOTH_NAME = "BasketballScore";
    
    // PC端网络配置（备用）
    private static final String PC_HOST = "192.168.1.100"; // 需要修改为实际PC IP
    private static final int PC_PORT = 8888;
    
    // 回调接口
    public interface BluetoothCallback {
        void onConnectionStatusChanged(boolean isConnected);
        void onError(String message);
        void onDeviceFound(BluetoothDevice device);
    }
    
    public BluetoothManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void setCallback(BluetoothCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 检查蓝牙是否可用
     */
    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }
    
    /**
     * 检查蓝牙是否已启用
     */
    @SuppressLint("MissingPermission")
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    
    /**
     * 检查蓝牙权限
     */
    public boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * 获取已配对的设备列表
     */
    @SuppressLint("MissingPermission")
    public List<BluetoothDevice> getPairedDevices() {
        if (!hasBluetoothPermissions() || !isBluetoothEnabled()) {
            return new ArrayList<>();
        }
        
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        return new ArrayList<>(bondedDevices);
    }
    
    /**
     * 根据设备名称查找已配对的设备
     */
    @SuppressLint("MissingPermission")
    public BluetoothDevice findDeviceByName(String deviceName) {
        if (!hasBluetoothPermissions()) {
            return null;
        }
        
        List<BluetoothDevice> pairedDevices = getPairedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName() != null && 
                device.getName().toLowerCase().contains(deviceName.toLowerCase())) {
                return device;
            }
        }
        return null;
    }
    
    /**
     * 连接到指定的蓝牙设备（带重试机制）
     */
    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device) {
        if (!hasBluetoothPermissions()) {
            notifyError("缺少蓝牙权限");
            return;
        }
        
        executorService.execute(() -> {
            int retryCount = 0;
            boolean connected = false;
            
            while (retryCount < MAX_RETRY_ATTEMPTS && !connected) {
                try {
                    // 如果已经连接，先断开
                    disconnect();
                    
                    Log.d(TAG, "尝试连接到设备: " + device.getName() + " (第" + (retryCount + 1) + "次)");
                    
                    // 创建蓝牙Socket
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    
                    // 取消发现过程以提高连接速度
                    if (bluetoothAdapter != null) {
                        bluetoothAdapter.cancelDiscovery();
                    }

                    // 设置连接超时
                    bluetoothSocket.connect();

                    // 验证连接是否成功
                    if (bluetoothSocket.isConnected()) {
                        // 获取输出流
                        outputStream = bluetoothSocket.getOutputStream();

                        isConnected = true;
                        connected = true;

                        mainHandler.post(() -> {
                            if (callback != null) {
                                callback.onConnectionStatusChanged(true);
                            }
                        });

                        Log.d(TAG, "成功连接到设备: " + device.getName());
                        break;
                    } else {
                        throw new IOException("Socket连接后状态异常");
                    }
                    
                } catch (IOException e) {
                    retryCount++;
                    Log.e(TAG, "连接失败 (第" + retryCount + "次): " + e.getMessage());
                    
                    // 清理资源
                    disconnect();
                    
                    if (retryCount >= MAX_RETRY_ATTEMPTS) {
                        // 所有重试都失败了
                        String errorMsg = "连接失败，已重试" + MAX_RETRY_ATTEMPTS + "次: " + e.getMessage();
                        Log.e(TAG, errorMsg);
                        notifyError(errorMsg);
                    } else {
                        // 等待一段时间后重试
                        try {
                            Thread.sleep(1000 * retryCount); // 递增延迟
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        });
    }
    
    /**
     * 连接到指定名称的设备
     */
    public void connectToDeviceByName(String deviceName) {
        BluetoothDevice device = findDeviceByName(deviceName);
        if (device != null) {
            connectToDevice(device);
        } else {
            notifyError("未找到名为 '" + deviceName + "' 的设备");
        }
    }
    
    /**
     * 发送数据到连接的设备
     */
    public boolean sendData(String data) {
        if (!isConnected || outputStream == null) {
            notifyError("设备未连接");
            return false;
        }
        
        try {
            byte[] bytes = data.getBytes("UTF-8");
            outputStream.write(bytes);
            outputStream.flush();
            Log.d(TAG, "发送数据: " + data);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "发送数据失败", e);
            notifyError("发送数据失败: " + e.getMessage());
            disconnect();
            return false;
        }
    }
    
    /**
     * 发送计分指令
     */
    public void sendScoreCommand(ScoreCommand command) {
        String commandString = "";
        
        if (command instanceof ScoreCommand.HomeScore) {
            commandString = "HOME_SCORE:" + ((ScoreCommand.HomeScore) command).getPoints();
        } else if (command instanceof ScoreCommand.AwayScore) {
            commandString = "AWAY_SCORE:" + ((ScoreCommand.AwayScore) command).getPoints();
        } else if (command instanceof ScoreCommand.ResetScore) {
            commandString = "RESET_SCORE";
        } else if (command instanceof ScoreCommand.Timeout) {
            commandString = "TIMEOUT:" + ((ScoreCommand.Timeout) command).getTeam();
        } else if (command instanceof ScoreCommand.Quarter) {
            commandString = "QUARTER:" + ((ScoreCommand.Quarter) command).getQuarter();
        } else if (command instanceof ScoreCommand.Timer) {
            commandString = "TIMER:" + ((ScoreCommand.Timer) command).getAction();
        } else if (command instanceof ScoreCommand.NewMatch) {
            ScoreCommand.NewMatch newMatch = (ScoreCommand.NewMatch) command;
            commandString = "NEW_MATCH:" + newMatch.getMatchName() + "|" + newMatch.getMatchNote();
        } else if (command instanceof ScoreCommand.SaveQuarter) {
            ScoreCommand.SaveQuarter saveQuarter = (ScoreCommand.SaveQuarter) command;
            commandString = "SAVE_QUARTER:" + saveQuarter.getQuarter() + "|" + saveQuarter.getHomeScore() + "|" + saveQuarter.getAwayScore() + " ";
        } else if (command instanceof ScoreCommand.SaveMatch) {
            ScoreCommand.SaveMatch saveMatch = (ScoreCommand.SaveMatch) command;
            commandString = "SAVE_MATCH:" + saveMatch.getMatchName() + "|" + saveMatch.getTotalHomeScore() + "|" + saveMatch.getTotalAwayScore();
        } else if (command instanceof ScoreCommand.SyncAllScores) {
            commandString = "SYNC_ALL_SCORES";
        }
        
        Log.d(TAG, "准备发送命令: " + commandString);
        Log.d(TAG, "蓝牙连接状态: " + isConnected + ", 网络连接状态: " + isNetworkConnected);
        
        // 优先使用蓝牙连接发送命令
        if (isConnected && outputStream != null) {
            Log.d(TAG, "使用蓝牙连接发送命令");
            sendBluetoothData(commandString);
        } else if (isNetworkConnected && networkWriter != null) {
            // 备用：使用网络连接
            Log.d(TAG, "使用网络连接发送命令");
            sendNetworkData(commandString);
        } else {
            Log.w(TAG, "未连接，无法发送数据: " + commandString);
            Log.w(TAG, "蓝牙连接: " + isConnected + ", 输出流: " + (outputStream != null));
            Log.w(TAG, "网络连接: " + isNetworkConnected + ", 写入器: " + (networkWriter != null));
            if (callback != null) {
                callback.onError("未连接到PC端");
            }
        }
    }
    
    /**
     * 通过网络发送数据
     */
    private void sendNetworkData(String data) {
        if (isNetworkConnected && networkWriter != null) {
            executorService.execute(() -> {
                try {
                    networkWriter.println(data);
                    Log.d(TAG, "发送到PC: " + data);
                } catch (Exception e) {
                    Log.e(TAG, "发送数据失败", e);
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError("发送数据失败: " + e.getMessage());
                        }
                    });
                }
            });
        } else {
            Log.w(TAG, "网络未连接，无法发送数据: " + data);
            if (callback != null) {
                callback.onError("网络未连接");
            }
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        // 断开蓝牙连接
        disconnectBluetooth();
        
        // 断开网络连接
        disconnectNetwork();
    }
    
    /**
     * 断开蓝牙连接
     */
    private void disconnectBluetooth() {
        try {
            isConnected = false;
            
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            
            Log.d(TAG, "蓝牙连接已断开");
        } catch (IOException e) {
            Log.e(TAG, "断开蓝牙连接时出错", e);
        } finally {
            // 只有在没有网络连接时才重置isConnected
            if (!isNetworkConnected) {
                isConnected = false;
            }
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onConnectionStatusChanged(false);
                }
            });
        }
    }
    
    /**
     * 断开网络连接
     */
    private void disconnectNetwork() {
        try {
            isNetworkConnected = false;
            // 如果网络是唯一连接，也要重置isConnected
            if (!isConnected || outputStream == null) {
                isConnected = false;
            }
            
            if (networkWriter != null) {
                networkWriter.close();
                networkWriter = null;
            }
            if (networkReader != null) {
                networkReader.close();
                networkReader = null;
            }
            if (networkSocket != null) {
                networkSocket.close();
                networkSocket = null;
            }
            if (networkThread != null) {
                networkThread.interrupt();
                networkThread = null;
            }
            
            Log.d(TAG, "网络连接已断开");
        } catch (IOException e) {
            Log.e(TAG, "断开网络连接时出错", e);
        }
    }
    
    /**
     * 获取连接状态
     */
    public boolean isConnected() {
        boolean bluetoothConnected = isConnected && outputStream != null;
        boolean networkConnected = isNetworkConnected && networkWriter != null;
        
        Log.d(TAG, "连接状态检查 - 蓝牙: " + bluetoothConnected + ", 网络: " + networkConnected);
        return bluetoothConnected || networkConnected;
    }
    
    /**
     * 获取连接类型
     */
    public String getConnectionType() {
        if (isConnected && outputStream != null) {
            return "蓝牙连接";
        } else if (isNetworkConnected && networkWriter != null) {
            return "网络连接";
        } else {
            return "未连接";
        }
    }
    
    private void notifyError(String message) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onError(message);
            }
        });
    }
    
    /**
     * 连接到PC端（网络连接）
     */
    public void connectToPCNetwork(String pcHost) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "正在连接到PC: " + pcHost + ":" + PC_PORT);
                
                networkSocket = new Socket(pcHost, PC_PORT);
                networkWriter = new PrintWriter(networkSocket.getOutputStream(), true);
                networkReader = new BufferedReader(new InputStreamReader(networkSocket.getInputStream()));
                
                isNetworkConnected = true;
                isConnected = true; // 设置连接状态
                
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onConnectionStatusChanged(true);
                    }
                });
                
                Log.d(TAG, "PC网络连接成功");
                
                // 启动接收线程
                startNetworkReceiver();
                
            } catch (IOException e) {
                Log.e(TAG, "PC网络连接失败", e);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("PC网络连接失败: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * 启动网络接收线程
     */
    private void startNetworkReceiver() {
        networkThread = new Thread(() -> {
            try {
                String response;
                while (isNetworkConnected && (response = networkReader.readLine()) != null) {
                    Log.d(TAG, "收到PC响应: " + response);
                    
                    final String finalResponse = response;
                    mainHandler.post(() -> {
                        // 处理PC响应
                        handlePCResponse(finalResponse);
                    });
                }
            } catch (IOException e) {
                if (isNetworkConnected) {
                    Log.e(TAG, "网络接收错误", e);
                }
            }
        });
        networkThread.start();
    }
    
    /**
     * 处理PC响应
     */
    private void handlePCResponse(String response) {
        if (response.startsWith("MATCH_CREATED:")) {
            String matchId = response.substring("MATCH_CREATED:".length());
            Log.d(TAG, "比赛创建成功，ID: " + matchId);
        } else if (response.equals("QUARTER_SAVED")) {
            Log.d(TAG, "节次保存成功");
        } else if (response.equals("MATCH_SAVED")) {
            Log.d(TAG, "比赛保存成功");
        } else if (response.equals("ACK")) {
            Log.d(TAG, "PC确认收到数据");
        }
    }
    
    /**
     * 连接到PC端蓝牙服务器
     */
    @SuppressLint("MissingPermission")
    public void connectToPCBluetooth(BluetoothDevice pcDevice) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "正在连接到PC蓝牙设备: " + pcDevice.getName());
                
                // 创建蓝牙Socket连接
                bluetoothSocket = pcDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                bluetoothSocket.connect();
                
                // 获取输出流
                outputStream = bluetoothSocket.getOutputStream();
                
                isConnected = true;
                
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onConnectionStatusChanged(true);
                    }
                });
                
                Log.d(TAG, "PC蓝牙连接成功");
                
                // 启动接收线程
                startBluetoothReceiver();
                
            } catch (IOException e) {
                Log.e(TAG, "PC蓝牙连接失败", e);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("PC蓝牙连接失败: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * 启动蓝牙接收线程
     */
    private void startBluetoothReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                
                while (isConnected) {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        String response = new String(buffer, 0, bytesRead).trim();
                        Log.d(TAG, "收到PC蓝牙响应: " + response);
                        
                        final String finalResponse = response;
                        mainHandler.post(() -> {
                            // 处理PC响应
                            handlePCResponse(finalResponse);
                        });
                    }
                }
            } catch (IOException e) {
                if (isConnected) {
                    Log.e(TAG, "蓝牙接收错误", e);
                }
            }
        });
        receiverThread.start();
    }
    
    /**
     * 通过蓝牙发送数据
     */
    private void sendBluetoothData(String data) {
        if (isConnected && outputStream != null) {
            executorService.execute(() -> {
                try {
                    outputStream.write(data.getBytes());
                    Log.d(TAG, "蓝牙发送到PC: " + data);
                } catch (IOException e) {
                    Log.e(TAG, "蓝牙发送数据失败", e);
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError("蓝牙发送数据失败: " + e.getMessage());
                        }
                    });
                }
            });
        }
    }
    
    /**
     * 计分指令抽象基类
     */
    public static abstract class ScoreCommand {
        
        public static class HomeScore extends ScoreCommand {
            private final int points;
            
            public HomeScore(int points) {
                this.points = points;
            }
            
            public int getPoints() {
                return points;
            }
        }
        
        public static class AwayScore extends ScoreCommand {
            private final int points;
            
            public AwayScore(int points) {
                this.points = points;
            }
            
            public int getPoints() {
                return points;
            }
        }
        
        public static class ResetScore extends ScoreCommand {
        }
        
        public static class Timeout extends ScoreCommand {
            private final String team;
            
            public Timeout(String team) {
                this.team = team;
            }
            
            public String getTeam() {
                return team;
            }
        }
        
        public static class Quarter extends ScoreCommand {
            private final int quarter;
            
            public Quarter(int quarter) {
                this.quarter = quarter;
            }
            
            public int getQuarter() {
                return quarter;
            }
        }
        
        public static class Timer extends ScoreCommand {
            private final String action;
            
            public Timer(String action) {
                this.action = action;
            }
            
            public String getAction() {
                return action;
            }
        }
        
        public static class NewMatch extends ScoreCommand {
            private final String matchName;
            private final String matchNote;
            
            public NewMatch(String matchName, String matchNote) {
                this.matchName = matchName;
                this.matchNote = matchNote;
            }
            
            public String getMatchName() {
                return matchName;
            }
            
            public String getMatchNote() {
                return matchNote;
            }
        }
        
        public static class SaveQuarter extends ScoreCommand {
            private final int quarter;
            private final int homeScore;
            private final int awayScore;
            
            public SaveQuarter(int quarter, int homeScore, int awayScore) {
                this.quarter = quarter;
                this.homeScore = homeScore;
                this.awayScore = awayScore;
            }
            
            public int getQuarter() {
                return quarter;
            }
            
            public int getHomeScore() {
                return homeScore;
            }
            
            public int getAwayScore() {
                return awayScore;
            }
        }
        
        public static class SaveMatch extends ScoreCommand {
            private final String matchName;
            private final int totalHomeScore;
            private final int totalAwayScore;
            
            public SaveMatch(String matchName, int totalHomeScore, int totalAwayScore) {
                this.matchName = matchName;
                this.totalHomeScore = totalHomeScore;
                this.totalAwayScore = totalAwayScore;
            }
            
            public String getMatchName() {
                return matchName;
            }
            
            public int getTotalHomeScore() {
                return totalHomeScore;
            }
            
            public int getTotalAwayScore() {
                return totalAwayScore;
            }
        }
        
        public static class SyncAllScores extends ScoreCommand {
            // 同步所有比分命令，不需要额外参数
        }
    }
} 