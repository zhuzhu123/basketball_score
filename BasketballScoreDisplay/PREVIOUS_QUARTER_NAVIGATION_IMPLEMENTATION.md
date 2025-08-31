# 手机侧跳转到上一节功能实现

## 功能概述

为手机侧Android应用添加了跳转到上一节的功能，用户可以在比赛进行中随时返回到之前的节次查看和修改比分。

## 新增功能

### 1. UI界面更新

#### 1.1 新增按钮
- 在节次显示区域添加了"上一节"按钮
- 按钮位置：节次显示区域的左侧，与"下一节"按钮对称
- 按钮样式：使用蓝色背景，与"下一节"按钮形成视觉对比

#### 1.2 历史记录查看
- 添加了"查看历史"按钮，可以查看所有节次的比分记录
- 按钮位置：比赛管理区域
- 功能：显示所有已完成的节次比分

### 2. 数据结构

#### 2.1 节次比分历史记录
```java
// 节次比分历史记录
private Map<Integer, QuarterScore> quarterHistory = new HashMap<>();

// 节次比分数据类
private static class QuarterScore {
    int homeScore;
    int awayScore;
    
    QuarterScore(int homeScore, int awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }
}
```

#### 2.2 数据管理
- 每次进入下一节时，自动保存当前节次比分到历史记录
- 每次返回上一节时，从历史记录中恢复比分
- 新建比赛时清空所有历史记录

### 3. 核心功能实现

#### 3.1 跳转到上一节 (`previousQuarter()`)
```java
private void previousQuarter() {
    // 验证比赛状态
    if (!isMatchStarted) {
        showToast("请先新建比赛");
        return;
    }
    
    if (currentQuarter <= 1) {
        showToast("已经是第1节，无法返回上一节");
        return;
    }
    
    // 保存当前节次比分到历史记录
    quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
    
    // 从总分中减去当前节次比分
    lastTotalHomeScore -= homeScore;
    lastTotalAwayScore -= awayScore;
    
    // 返回上一节
    currentQuarter--;
    
    // 从历史记录中获取上一节的比分
    QuarterScore previousScore = quarterHistory.get(currentQuarter);
    if (previousScore != null) {
        homeScore = previousScore.homeScore;
        awayScore = previousScore.awayScore;
        showToast("返回第" + currentQuarter + "节，比分：" + homeScore + ":" + awayScore);
    } else {
        // 如果没有历史记录，尝试从PC端获取
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SelectPreviousQuarter(currentMatchId, currentQuarter));
            showToast("正在从PC获取第" + currentQuarter + "节比分...");
        } else {
            homeScore = 0;
            awayScore = 0;
            showToast("返回第" + currentQuarter + "节（无历史记录）");
        }
    }
    
    // 更新UI显示
    updateMatchInfo();
    updateScoreDisplay();
    updateQuarterDisplay();
    
    // 发送节次更新命令到PC
    if (bluetoothManager != null) {
        bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Quarter(currentQuarter));
    }
}
```

#### 3.2 历史记录查看 (`showQuarterHistory()`)
```java
private void showQuarterHistory() {
    if (!isMatchStarted) {
        showToast("请先新建比赛");
        return;
    }
    
    if (quarterHistory.isEmpty()) {
        showToast("暂无历史记录");
        return;
    }
    
    // 构建历史记录信息
    StringBuilder historyText = new StringBuilder();
    historyText.append("节次历史记录：\n\n");
    
    // 按节次顺序显示
    for (int i = 1; i <= currentQuarter; i++) {
        QuarterScore score = quarterHistory.get(i);
        if (score != null) {
            historyText.append("第").append(i).append("节：")
                      .append(score.homeScore).append(" : ").append(score.awayScore).append("\n");
        }
    }
    
    // 显示当前节次
    historyText.append("\n当前第").append(currentQuarter).append("节：")
              .append(homeScore).append(" : ").append(awayScore);
    
    new AlertDialog.Builder(this)
        .setTitle("节次历史记录")
        .setMessage(historyText.toString())
        .setPositiveButton("确定", null)
        .show();
}
```

### 4. 数据同步

#### 4.1 与PC端同步
- 跳转到上一节时，自动发送节次更新命令到PC端
- 如果本地没有历史记录，会尝试从PC端获取比分数据
- PC端返回的比分数据会自动更新到本地历史记录

#### 4.2 历史记录管理
- 每次计分时自动保存到当前节次
- 进入下一节时保存当前节次比分到历史记录
- 返回上一节时从历史记录恢复比分
- 新建比赛时清空所有历史记录

### 5. 使用场景

#### 5.1 比分修正
- 发现当前节次比分有误时，可以返回上一节进行修正
- 修正后可以继续进入下一节

#### 5.2 历史查看
- 随时查看所有节次的比分记录
- 了解比赛的进展过程

#### 5.3 比赛回顾
- 在比赛结束后回顾各节次的比分
- 分析比赛的关键节点

### 6. 测试验证

#### 6.1 测试文件
- `TestPreviousQuarterNavigation.java`：功能测试
- `test_previous_quarter_navigation.bat`：测试运行脚本

#### 6.2 测试场景
1. 新建比赛
2. 多节次计分
3. 跳转到上一节
4. 历史记录查看
5. 边界条件测试（第1节、第10节等）

### 7. 注意事项

#### 7.1 数据一致性
- 确保本地历史记录与PC端数据一致
- 网络断开时使用本地历史记录
- 重新连接时同步PC端数据

#### 7.2 用户体验
- 提供清晰的操作反馈
- 显示当前节次和比分状态
- 防止误操作（如在第1节时点击上一节）

#### 7.3 性能考虑
- 历史记录使用内存存储，适合中小型比赛
- 大型比赛可考虑持久化存储
- 定期清理过期的历史记录

## 总结

跳转到上一节功能的实现为手机侧应用增加了重要的灵活性，用户可以：
1. 随时返回之前的节次查看和修正比分
2. 查看完整的比赛历史记录
3. 在比赛过程中进行比分回顾和分析

该功能与现有的下一节功能形成完整的节次导航体系，提升了用户体验和比赛管理的便利性。 