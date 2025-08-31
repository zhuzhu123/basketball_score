# 继续倒计时功能实现说明

## 功能概述

在手机侧添加了"继续倒计时"按钮，实现完整的倒计时控制流程：开始计时 → 暂停计时 → 继续倒计时。

## 实现内容

### 1. 布局文件修改

在`activity_main.xml`中添加了继续倒计时按钮：

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center"
    android:layout_marginTop="8dp">

    <Button
        android:id="@+id/btnContinueTimer"
        style="@style/GameButtonStyle"
        android:layout_width="0dp"
        android:layout_height="60dp"
        android:layout_weight="1"
        android:text="继续倒计时"
        android:textSize="16sp"
        android:backgroundTint="@color/basketball_green" />

</LinearLayout>
```

### 2. 代码实现

#### 2.1 按钮声明和初始化
```java
// 在MainActivity中添加按钮声明
private Button btnContinueTimer;

// 在initViews()方法中初始化
btnContinueTimer = findViewById(R.id.btnContinueTimer);

// 在setupUI()方法中绑定事件监听器
btnContinueTimer.setOnClickListener(v -> continueTimer());
```

#### 2.2 倒计时状态管理
```java
// 添加状态管理变量
private boolean isTimerRunning = false;
private boolean isTimerPaused = false;
```

#### 2.3 继续倒计时方法
```java
private void continueTimer() {
    if (bluetoothManager != null) {
        bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Timer("CONTINUE_COUNTDOWN"));
        isTimerRunning = true;
        isTimerPaused = false;
        
        // 更新按钮状态
        btnStartTimer.setEnabled(false);
        btnPauseTimer.setEnabled(true);
        btnContinueTimer.setEnabled(false);
    }
    showToast("继续倒计时");
}
```

### 3. 按钮状态管理

#### 3.1 初始状态
- **开始计时按钮**: 启用
- **暂停计时按钮**: 禁用
- **继续倒计时按钮**: 禁用

#### 3.2 开始计时后
- **开始计时按钮**: 禁用
- **暂停计时按钮**: 启用
- **继续倒计时按钮**: 禁用

#### 3.3 暂停计时后
- **开始计时按钮**: 禁用
- **暂停计时按钮**: 禁用
- **继续倒计时按钮**: 启用

#### 3.4 继续倒计时后
- **开始计时按钮**: 禁用
- **暂停计时按钮**: 启用
- **继续倒计时按钮**: 禁用

### 4. 命令发送

#### 4.1 命令格式
- **开始计时**: `START_COUNTDOWN` → `START_TIMER`
- **暂停计时**: `STOP_COUNTDOWN` → `PAUSE_TIMER`
- **继续倒计时**: `CONTINUE_COUNTDOWN` → `CONTINUE_TIMER`

#### 4.2 蓝牙命令转换
在`BluetoothManager.java`中，命令会被转换为PC端可识别的格式：
```java
if ("CONTINUE_COUNTDOWN".equals(action)) {
    commandString = "CONTINUE_TIMER";
}
```

## 使用流程

### 1. 开始计时
1. 用户点击"开始计时"按钮
2. 发送`START_COUNTDOWN`命令到PC端
3. PC端开始倒计时
4. 按钮状态更新：开始[禁用] 暂停[启用] 继续[禁用]

### 2. 暂停计时
1. 用户点击"暂停计时"按钮
2. 发送`STOP_COUNTDOWN`命令到PC端
3. PC端暂停倒计时
4. 按钮状态更新：开始[禁用] 暂停[禁用] 继续[启用]

### 3. 继续倒计时
1. 用户点击"继续倒计时"按钮
2. 发送`CONTINUE_COUNTDOWN`命令到PC端
3. PC端继续倒计时
4. 按钮状态更新：开始[禁用] 暂停[启用] 继续[禁用]

## 测试验证

### 测试文件
- `TestContinueTimer.java`: 测试继续倒计时功能
- `test_continue_timer.bat`: 运行测试的批处理脚本

### 测试内容
1. **倒计时状态管理**: 验证状态变量正确更新
2. **按钮状态切换**: 验证按钮启用/禁用状态正确
3. **命令发送**: 验证命令格式和转换正确
4. **倒计时流程**: 模拟完整的倒计时控制流程

### 运行测试
```bash
test_continue_timer.bat
```

## 技术特点

### 1. 状态同步
- 手机侧和PC端的倒计时状态保持同步
- 按钮状态与倒计时状态一致

### 2. 用户体验
- 按钮状态清晰，用户知道当前可以执行的操作
- 防止误操作，避免重复点击

### 3. 错误处理
- 检查蓝牙连接状态
- 提供用户反馈（Toast消息）

### 4. 扩展性
- 可以轻松添加更多倒计时控制功能
- 状态管理机制支持复杂场景

## 注意事项

1. **蓝牙连接**: 确保手机侧与PC端已建立蓝牙连接
2. **命令顺序**: 必须按照正确的顺序使用按钮（开始→暂停→继续）
3. **状态同步**: 确保PC端正确处理CONTINUE_TIMER命令
4. **按钮状态**: 注意按钮的启用/禁用状态，避免用户困惑

## 后续优化建议

1. **视觉反馈**: 可以添加倒计时时间的显示
2. **声音提示**: 可以添加按钮点击的声音反馈
3. **手势支持**: 可以添加手势控制倒计时
4. **快捷键**: 可以添加键盘快捷键支持

## 总结

继续倒计时功能的实现完善了手机侧的倒计时控制能力，提供了完整的开始→暂停→继续流程，提升了用户体验。通过合理的状态管理和按钮控制，确保了操作的准确性和可靠性。 