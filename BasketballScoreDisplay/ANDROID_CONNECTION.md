# Android应用连接说明

本文档说明如何将Android篮球计分应用连接到Java桌面显示程序。

## 连接方式

### 1. 网络连接（推荐）

Android应用通过TCP Socket连接到Java程序，这是最简单和稳定的连接方式。

#### 连接步骤

1. **启动Java程序**
   - 运行 `BasketballScoreDisplay.java`
   - 程序会在端口8888上监听连接
   - 状态栏显示 "等待设备连接... (端口: 8888)"

2. **获取电脑IP地址**
   - 在Windows命令提示符中输入：`ipconfig`
   - 找到局域网IP地址（通常是192.168.x.x格式）

3. **配置Android应用**
   - 在Android应用中输入电脑IP地址
   - 端口号设置为：8888
   - 点击连接按钮

#### 网络要求

- Android设备和电脑必须在同一局域网内
- 确保防火墙允许端口8888的通信
- 建议使用稳定的WiFi网络

### 2. 蓝牙连接（实验性）

如果需要真正的蓝牙连接，需要额外的配置和库支持。

## Android应用代码示例

### 连接代码

```java
public class BluetoothManager {
    private static final String SERVER_IP = "192.168.1.100"; // 替换为实际IP
    private static final int SERVER_PORT = 8888;
    
    public void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                // 发送命令示例
                out.println("HOME_TEAM:湖人队");
                out.println("AWAY_TEAM:勇士队");
                out.println("HOME_SCORE:2");
                
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

### 命令格式

Android应用发送的命令必须遵循以下格式：

| 功能 | 命令格式 | 示例 |
|------|----------|------|
| 龙都F4得分 | `HOME_SCORE:分数` | `HOME_SCORE:2` |
| 暴风队得分 | `AWAY_SCORE:分数` | `AWAY_SCORE:3` |
| 重置比分 | `RESET_SCORE` | `RESET_SCORE` |
| 设置节次 | `QUARTER:节次` | `QUARTER:2` |
| 开始计时 | `TIMER_START` | `TIMER_START` |
| 暂停计时 | `TIMER_PAUSE` | `TIMER_PAUSE` |
| 设置时间 | `TIMER:秒数` | `TIMER:600` |
| 设置龙都F4名 | `HOME_TEAM:队名` | `HOME_TEAM:湖人队` |
| 设置暴风队名 | `AWAY_TEAM:队名` | `AWAY_TEAM:勇士队` |
| 超时处理 | `TIMEOUT:节次` | `TIMEOUT:2` |

### 响应处理

Java程序会发送确认消息：

```
ACK:龙都F4得分已更新: 2
ACK:暴风队得分已更新: 5
ACK:计时器已设置为: 10:00
```

## 故障排除

### 常见问题

1. **连接失败**
   - 检查IP地址是否正确
   - 确认端口8888未被占用
   - 检查防火墙设置

2. **数据不更新**
   - 确认命令格式正确
   - 检查网络连接状态
   - 查看Java程序控制台输出

3. **连接断开**
   - 检查网络稳定性
   - 确认Java程序仍在运行
   - 重新连接

### 调试方法

1. **查看Java程序状态**
   - 状态栏显示连接信息
   - 控制台输出详细日志

2. **测试连接**
   - 使用 `TestClient.java` 测试连接
   - 运行 `test.bat` 进行自动化测试

3. **网络诊断**
   - 使用 `ping` 命令测试网络连通性
   - 使用 `telnet` 测试端口连通性

## 性能优化

### 连接优化

- 使用连接池管理多个连接
- 实现自动重连机制
- 添加连接超时处理

### 数据传输优化

- 批量发送命令减少网络开销
- 实现数据压缩
- 添加数据校验

### 用户体验优化

- 显示连接状态指示器
- 添加连接失败重试按钮
- 实现离线模式

## 安全考虑

### 网络安全

- 限制连接IP地址范围
- 实现简单的身份验证
- 加密敏感数据传输

### 数据验证

- 验证命令格式和参数
- 防止恶意数据注入
- 记录操作日志

## 扩展功能

### 多设备支持

- 支持多个Android设备同时连接
- 实现设备间数据同步
- 添加设备管理功能

### 数据持久化

- 保存比赛记录到数据库
- 实现数据备份和恢复
- 添加历史记录查询

### 实时通信

- 实现双向实时通信
- 添加推送通知功能
- 支持语音播报

---

**注意**：本连接方式使用TCP网络协议，确保网络环境稳定以获得最佳使用体验。 