# 蓝牙连接问题解决指南

## 🔍 **问题分析**

你在真机上调试时遇到的错误：
```
IOException: "read failed, socket might closed or timeout, read ret: -1"
```

这个错误通常发生在 `bluetoothSocket.connect()` 执行时，表示蓝牙连接失败。

## 🛠️ **已实施的修复**

### 1. **连接重试机制**
- 添加了最多3次重试
- 每次重试之间有递增延迟（1秒、2秒、3秒）
- 自动清理失败的连接资源

### 2. **连接状态验证**
- 连接后验证 `bluetoothSocket.isConnected()`
- 确保Socket真正建立连接
- 防止虚假连接状态

### 3. **更好的错误处理**
- 详细的日志记录
- 区分不同类型的连接失败
- 用户友好的错误提示

## 📱 **真机调试建议**

### 权限检查
确保在AndroidManifest.xml中有以下权限：
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
```

### 运行时权限
确保在MainActivity中请求了必要的权限：
```java
// 检查并请求蓝牙权限
checkBluetoothPermissions();
```

### 设备状态检查
在连接前检查：
```java
// 检查蓝牙是否可用
if (!bluetoothManager.isBluetoothAvailable()) {
    showToast("此设备不支持蓝牙");
    return;
}

// 检查蓝牙是否已启用
if (!bluetoothManager.isBluetoothEnabled()) {
    // 请求启用蓝牙
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    enableBluetoothLauncher.launch(enableBtIntent);
    return;
}
```

## 🔧 **调试步骤**

### 1. **检查日志输出**
查看Logcat中的详细错误信息：
```
adb logcat | grep BluetoothManager
```

### 2. **验证设备配对状态**
确保目标蓝牙设备已经配对：
```java
List<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
for (BluetoothDevice device : pairedDevices) {
    Log.d("Bluetooth", "已配对设备: " + device.getName() + " - " + device.getAddress());
}
```

### 3. **检查UUID匹配**
确保Android和接收端使用相同的UUID：
```java
private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
```

### 4. **网络环境检查**
- 确保设备在同一网络环境
- 检查防火墙设置
- 验证端口是否被占用

## 🚀 **测试建议**

### 1. **使用已知工作的设备**
- 先用一个确定工作的蓝牙设备测试
- 验证连接流程是否正常

### 2. **简化连接流程**
- 暂时移除复杂的权限检查
- 专注于基本的连接逻辑

### 3. **逐步添加功能**
- 先测试基本连接
- 再添加数据传输
- 最后完善错误处理

## 📋 **常见解决方案**

### 方案1: 重启蓝牙
```java
// 关闭蓝牙
if (bluetoothAdapter.isEnabled()) {
    bluetoothAdapter.disable();
    Thread.sleep(1000);
}

// 重新启用蓝牙
bluetoothAdapter.enable();
```

### 方案2: 使用不同的连接方法
```java
// 尝试使用反射创建Socket
Method method = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
```

### 方案3: 增加连接超时
```java
// 设置连接超时
bluetoothSocket.connect();
```

## 🔍 **进一步调试**

如果问题仍然存在，请提供：

1. **完整的错误堆栈**
2. **设备信息**（Android版本、设备型号）
3. **目标蓝牙设备信息**
4. **Logcat完整输出**

## 📚 **相关资源**

- [Android蓝牙开发官方文档](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [蓝牙权限说明](https://developer.android.com/guide/topics/permissions/overview)
- [Socket连接最佳实践](https://developer.android.com/training/connectivity)

---

**注意**: 蓝牙连接问题通常与设备状态、权限、网络环境等因素相关。通过系统性的调试和测试，大多数问题都能得到解决。 