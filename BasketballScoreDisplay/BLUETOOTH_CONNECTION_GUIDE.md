# 蓝牙连接指南

## 🔗 连接架构

系统使用**真正的蓝牙连接**，PC端完全基于`BluetoothServer.java`的代码结构实现。

### PC端（BasketballScoreDisplay）
- **蓝牙服务器**：使用JSR-82 API（javax.bluetooth）
- **服务名称**：BasketballScore
- **UUID**：1101（标准串口服务）
- **连接方式**：SPP（Serial Port Profile）

### 手机端（Android）
- **蓝牙客户端**：使用Android标准蓝牙API
- **连接方式**：RFCOMM Socket
- **备用方案**：网络Socket连接

## 💻 PC端配置

### 1. 蓝牙服务器启动流程
PC端完全按照`BluetoothServer.java`的步骤：

```java
// 1. 获取本地蓝牙设备
LocalDevice localDevice = LocalDevice.getLocalDevice();

// 2. 创建 UUID（唯一标识服务）
UUID uuid = new UUID("1101", true);

// 3. 创建服务 URL
String url = "btspp://localhost:" + uuid + ";name=BasketballScore";

// 4. 启动服务端 Socket
StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(url);

// 5. 等待客户端连接
StreamConnection connection = notifier.acceptAndOpen();

// 6. 获取输入输出流
InputStream input = connection.openInputStream();
OutputStream output = connection.openOutputStream();

// 7. 接收数据
byte[] buffer = new byte[1024];
int bytesRead = input.read(buffer);
String received = new String(buffer, 0, bytesRead);

// 8. 发送响应
output.write("ACK from BasketballScore".getBytes());

// 9. 关闭连接
connection.close();
notifier.close();
```

### 2. 启动PC端程序
1. 确保PC蓝牙已启用
2. 运行`BasketballFullScreenDisplay`
3. 系统自动启动蓝牙服务器
4. 等待手机端连接

### 3. 蓝牙设备信息
- **设备名称**：BasketballScore
- **服务类型**：SPP（串口服务）
- **UUID**：00001101-0000-1000-8000-00805F9B34FB

## 📱 手机端配置

### 1. 连接方式选择
手机端提供两种连接方式：

#### 方式一：蓝牙连接（推荐）
- 扫描已配对的蓝牙设备
- 选择PC蓝牙设备
- 建立RFCOMM连接

#### 方式二：网络连接（备用）
- 输入PC的IP地址
- 建立TCP Socket连接
- 作为蓝牙连接的备用方案

### 2. 蓝牙连接步骤
1. 点击"连接PC"按钮
2. 选择"蓝牙连接"
3. 从已配对设备列表中选择PC
4. 等待连接成功

### 3. 网络连接步骤
1. 点击"连接PC"按钮
2. 选择"网络连接"
3. 输入PC的IP地址
4. 点击连接

## 🔧 技术实现

### PC端蓝牙管理器
```java
public class BluetoothManager {
    // 蓝牙相关变量（完全按照 BluetoothServer.java）
    private LocalDevice localDevice;
    private StreamConnectionNotifier bluetoothNotifier;
    private StreamConnection bluetoothConnection;
    private InputStream bluetoothInput;
    private OutputStream bluetoothOutput;
    
    // 初始化蓝牙（按照 BluetoothServer.java 的步骤1）
    public boolean initializeBluetooth() {
        localDevice = LocalDevice.getLocalDevice();
        System.out.println("PC Bluetooth Address: " + localDevice.getBluetoothAddress());
        System.out.println("PC Bluetooth Name: " + localDevice.getFriendlyName());
        return true;
    }
    
    // 启动蓝牙服务器（按照 BluetoothServer.java 的步骤2-9）
    public void startBluetoothServer() {
        UUID uuid = new UUID("1101", true);
        String url = "btspp://localhost:" + uuid + ";name=BasketballScore";
        bluetoothNotifier = (StreamConnectionNotifier) Connector.open(url);
        // ... 其他步骤
    }
}
```

### 手机端蓝牙管理器
```java
public class BluetoothManager {
    // 连接到PC端蓝牙服务器
    public void connectToPCBluetooth(BluetoothDevice pcDevice) {
        bluetoothSocket = pcDevice.createRfcommSocketToServiceRecord(SPP_UUID);
        bluetoothSocket.connect();
        outputStream = bluetoothSocket.getOutputStream();
    }
    
    // 发送命令
    public void sendScoreCommand(ScoreCommand command) {
        // 优先使用蓝牙连接
        if (isConnected && outputStream != null) {
            sendBluetoothData(commandString);
        } else if (isNetworkConnected && networkWriter != null) {
            // 备用：使用网络连接
            sendNetworkData(commandString);
        }
    }
}
```

## 📋 命令协议

### 手机端 → PC端命令
| 命令 | 格式 | 说明 | PC响应 |
|------|------|------|--------|
| 新建比赛 | `NEW_MATCH:名称|备注` | 创建新比赛 | `MATCH_CREATED:ID` |
| 龙都F4得分 | `HOME_SCORE:分数` | 龙都F4加分 | `ACK` |
| 暴风队得分 | `AWAY_SCORE:分数` | 暴风队加分 | `ACK` |
| 保存节次 | `SAVE_QUARTER:节次|龙都F4分|暴风队分` | 保存节次比分 | `QUARTER_SAVED` |
| 保存比赛 | `SAVE_MATCH:名称|龙都F4总分|暴风队总分` | 保存比赛总分 | `MATCH_SAVED` |
| 开始倒计时 | `START_COUNTDOWN` | 开始15秒倒计时 | `ACK` |
| 停止倒计时 | `STOP_COUNTDOWN` | 停止倒计时 | `ACK` |

### 数据传输格式
- **编码**：UTF-8
- **分隔符**：冒号（:）和竖线（|）
- **缓冲区**：1024字节
- **响应**：字节数组

## 🚀 使用步骤

### 1. PC端准备
1. 确保PC蓝牙已启用
2. 启动MySQL数据库服务
3. 运行`BasketballFullScreenDisplay`
4. 等待蓝牙服务器启动

### 2. 手机端准备
1. 确保手机蓝牙已启用
2. 与PC进行蓝牙配对
3. 启动Android应用

### 3. 建立连接
1. 在手机端点击"连接PC"
2. 选择"蓝牙连接"
3. 选择PC蓝牙设备
4. 等待连接成功

### 4. 开始比赛
1. 点击"新建比赛"，输入比赛信息
2. 使用加分按钮记录得分
3. PC端实时显示比分和语音播报
4. 系统自动保存比赛数据

## 🔍 故障排除

### 1. 蓝牙连接失败
**问题**：手机无法连接到PC蓝牙
**解决方案**：
- 检查PC和手机蓝牙是否已启用
- 确认PC和手机已配对
- 检查PC端蓝牙服务器是否启动
- 查看PC端控制台日志

### 2. 权限问题
**问题**：Android蓝牙权限不足
**解决方案**：
- 确保已授予蓝牙权限
- 检查Android版本兼容性
- 重启应用重新申请权限

### 3. 数据传输失败
**问题**：命令发送后无响应
**解决方案**：
- 检查蓝牙连接状态
- 确认命令格式正确
- 查看PC端接收日志
- 尝试重新连接

### 4. 备用连接
**问题**：蓝牙连接不稳定
**解决方案**：
- 使用网络连接作为备用
- 确保PC和手机在同一网络
- 输入正确的PC IP地址

## 📊 性能特点

### 蓝牙连接优势
- **低延迟**：直接蓝牙通信，延迟低
- **稳定性**：专用蓝牙连接，干扰少
- **兼容性**：支持所有Android设备
- **安全性**：蓝牙配对验证

### 网络连接优势
- **备用方案**：蓝牙不可用时可用
- **配置简单**：只需IP地址
- **调试方便**：网络连接易于调试

## 🎯 最佳实践

1. **优先使用蓝牙连接**：更稳定，延迟更低
2. **网络连接作为备用**：确保连接可靠性
3. **定期检查连接状态**：及时发现连接问题
4. **保存重要数据**：避免数据丢失
5. **测试连接稳定性**：确保比赛顺利进行

---

**总结**：系统现在完全基于`BluetoothServer.java`的代码结构，使用真正的蓝牙连接，同时提供网络连接作为备用方案，确保连接的可靠性和稳定性。 