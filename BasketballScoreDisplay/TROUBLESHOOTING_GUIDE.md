# 手机端编译和连接问题排查指南

## 🔍 编译错误排查

### 1. 导入错误
**问题**：`AlertDialog` 无法解析
**解决方案**：
```java
// 确保正确导入
import androidx.appcompat.app.AlertDialog;
// 而不是
import android.app.AlertDialog;
```

### 2. 权限问题
**问题**：蓝牙权限相关错误
**解决方案**：
确保在 `AndroidManifest.xml` 中添加权限：
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. 版本兼容性
**问题**：Android API 版本兼容性错误
**解决方案**：
在 `build.gradle` 中设置正确的 `minSdkVersion` 和 `targetSdkVersion`：
```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 21
        targetSdk 34
    }
}
```

## 🔗 连接问题排查

### 1. 连接状态检查
**问题**：显示已连接但发送请求失败
**解决方案**：
```java
// 在发送命令前检查连接状态
if (bluetoothManager.isConnected()) {
    Log.d(TAG, "连接类型: " + bluetoothManager.getConnectionType());
    bluetoothManager.sendScoreCommand(command);
} else {
    Log.w(TAG, "未连接，无法发送命令");
}
```

### 2. 网络连接问题
**问题**：网络连接建立但数据传输失败
**解决方案**：
1. 检查PC端IP地址是否正确
2. 确保PC和手机在同一网络
3. 检查防火墙设置
4. 验证端口8888是否被占用

### 3. 蓝牙连接问题
**问题**：蓝牙连接失败
**解决方案**：
1. 确保PC和手机蓝牙已启用
2. 检查蓝牙配对状态
3. 确认PC端蓝牙服务器已启动
4. 检查蓝牙权限是否已授予

## 🛠️ 调试步骤

### 步骤1：检查编译
```bash
# 在app目录下执行
./gradlew clean build
```

### 步骤2：检查连接状态
在手机端应用中：
1. 点击"连接PC"
2. 选择连接方式（蓝牙或网络）
3. 观察连接状态显示
4. 查看Logcat日志

### 步骤3：测试命令发送
1. 连接成功后，尝试发送简单命令
2. 观察PC端是否收到命令
3. 检查PC端响应

### 步骤4：使用测试工具
运行测试程序验证连接：
```bash
# 编译测试程序
javac TestConnection.java

# 运行测试
java TestConnection
```

## 📱 手机端调试代码

### 1. 添加详细日志
```java
public void sendScoreCommand(ScoreCommand command) {
    String commandString = buildCommandString(command);
    
    Log.d(TAG, "准备发送命令: " + commandString);
    Log.d(TAG, "蓝牙连接状态: " + isConnected + ", 网络连接状态: " + isNetworkConnected);
    
    if (isConnected && outputStream != null) {
        Log.d(TAG, "使用蓝牙连接发送命令");
        sendBluetoothData(commandString);
    } else if (isNetworkConnected && networkWriter != null) {
        Log.d(TAG, "使用网络连接发送命令");
        sendNetworkData(commandString);
    } else {
        Log.w(TAG, "未连接，无法发送数据");
        Log.w(TAG, "蓝牙连接: " + isConnected + ", 输出流: " + (outputStream != null));
        Log.w(TAG, "网络连接: " + isNetworkConnected + ", 写入器: " + (networkWriter != null));
    }
}
```

### 2. 连接状态监控
```java
public boolean isConnected() {
    boolean bluetoothConnected = isConnected && outputStream != null;
    boolean networkConnected = isNetworkConnected && networkWriter != null;
    
    Log.d(TAG, "连接状态检查 - 蓝牙: " + bluetoothConnected + ", 网络: " + networkConnected);
    return bluetoothConnected || networkConnected;
}
```

## 💻 PC端调试

### 1. 检查服务器状态
```java
// 在PC端添加日志
System.out.println("蓝牙服务器启动，等待连接...");
System.out.println("收到连接请求: " + clientAddress);
System.out.println("接收到数据: " + receivedData);
```

### 2. 测试网络服务器
```bash
# 使用telnet测试端口
telnet localhost 8888

# 或使用netstat检查端口
netstat -an | findstr 8888
```

## 🔧 常见问题解决

### 问题1：编译成功但运行时崩溃
**原因**：权限不足或资源访问错误
**解决**：
1. 检查运行时权限
2. 确保蓝牙已启用
3. 验证网络权限

### 问题2：连接成功但数据传输失败
**原因**：数据格式或编码问题
**解决**：
1. 检查数据编码（UTF-8）
2. 验证命令格式
3. 确认缓冲区大小

### 问题3：连接不稳定
**原因**：网络或蓝牙信号问题
**解决**：
1. 检查网络稳定性
2. 确保蓝牙设备距离合适
3. 减少干扰源

## 📋 检查清单

### 编译前检查
- [ ] 所有导入语句正确
- [ ] 权限已添加到AndroidManifest.xml
- [ ] build.gradle配置正确
- [ ] 依赖库版本兼容

### 连接前检查
- [ ] PC端程序已启动
- [ ] 网络连接正常
- [ ] 蓝牙已启用
- [ ] 权限已授予

### 运行时检查
- [ ] 连接状态显示正确
- [ ] 日志输出正常
- [ ] 命令发送成功
- [ ] PC端有响应

## 🚀 快速修复

如果遇到编译错误，可以尝试以下快速修复：

1. **清理并重新编译**：
```bash
./gradlew clean build
```

2. **检查导入语句**：
确保所有必要的类都已正确导入

3. **验证权限**：
确保AndroidManifest.xml包含所有必要权限

4. **重启应用**：
完全关闭应用并重新启动

5. **检查网络**：
确保PC和手机在同一网络，防火墙允许连接

---

**注意**：如果问题仍然存在，请查看详细的错误日志，这将帮助定位具体的问题所在。 