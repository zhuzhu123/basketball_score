# PC侧跳转到上一节时正确恢复比分的修复

## 问题描述

从图片中可以看到，当从第2节切回到第1节时，当前节比分显示为0-0，但实际应该是20-5。这说明PC侧在跳转到上一节时没有正确恢复历史比分。

## 问题分析

### 1. 问题现象
- 第1节历史比分：20-5
- 第2节历史比分：10-9
- 从第2节跳转到第1节时，当前节比分显示为0-0（错误）
- 应该显示为20-5（正确）

### 2. 根本原因
在PC侧的`handleSelectPreviousQuarter`方法中，虽然从数据库获取了正确的比分数据，但是**没有更新`currentQuarter`变量**，导致：

1. 当前节次变量仍然是第2节
2. 节次比分显示区域仍然高亮第2节
3. 当前节比分显示为第2节的比分（0-0），而不是第1节的历史比分（20-5）

### 3. 代码问题位置
```java
// 修复前的代码（有问题）
private void handleSelectPreviousQuarter(String value) {
    // ... 解析参数 ...
    
    DatabaseManager.QuarterScore quarterScore = databaseManager.getQuarterScore(matchId, quarterNumber);
    if (quarterScore != null) {
        // 显示该节次比分
        homeScore = quarterScore.getHomeScore();
        awayScore = quarterScore.getAwayScore();
        
        // 更新比分显示
        if (homeScoreLabel != null) {
            homeScoreLabel.setText(String.valueOf(homeScore));
        }
        if (awayScoreLabel != null) {
            awayScoreLabel.setText(String.valueOf(awayScore));
        }
        if (currentQuarterLabel != null) {
            currentQuarterLabel.setText("第" + quarterNumber + "节");
        }
        
        // ❌ 问题：没有更新currentQuarter变量
        // ❌ 问题：没有调用updateQuarterScoresDisplay()
    }
}
```

## 修复方案

### 1. 修复内容
在`handleSelectPreviousQuarter`方法中添加两个关键步骤：

1. **更新当前节次变量**：`currentQuarter = quarterNumber;`
2. **更新节次比分显示**：`updateQuarterScoresDisplay();`

### 2. 修复后的代码
```java
// 修复后的代码（正确）
private void handleSelectPreviousQuarter(String value) {
    // ... 解析参数 ...
    
    DatabaseManager.QuarterScore quarterScore = databaseManager.getQuarterScore(matchId, quarterNumber);
    if (quarterScore != null) {
        // ✅ 修复：更新当前节次变量
        currentQuarter = quarterNumber;
        
        // 显示该节次比分
        homeScore = quarterScore.getHomeScore();
        awayScore = quarterScore.getAwayScore();
        
        // 更新比分显示
        if (homeScoreLabel != null) {
            homeScoreLabel.setText(String.valueOf(homeScore));
        }
        if (awayScoreLabel != null) {
            awayScoreLabel.setText(String.valueOf(awayScore));
        }
        if (currentQuarterLabel != null) {
            currentQuarterLabel.setText("第" + quarterNumber + "节");
        }
        
        // ✅ 修复：更新节次比分显示
        updateQuarterScoresDisplay();
        
        // ... 其他逻辑 ...
    }
}
```

## 修复效果

### 1. 修复前的问题
- 跳转到上一节时，`currentQuarter`变量没有更新
- 导致当前节次比分显示为0-0，而不是正确的历史比分
- 节次比分显示区域显示错误

### 2. 修复后的效果
- 跳转到上一节时，正确更新`currentQuarter`变量
- 正确恢复历史比分到当前显示
- 节次比分显示区域正确高亮当前节次
- 调用`updateQuarterScoresDisplay()`更新显示

### 3. 验证结果
修复后，从第2节跳转到第1节时：
- 当前节次显示：第1节
- 当前比分显示：20-5
- 节次比分区域：第1节高亮显示20-5
- 总分显示：30-14（保持不变）

## 测试验证

### 1. 测试文件
- `TestPreviousQuarterFix.java`：功能测试
- `test_previous_quarter_fix.bat`：测试运行脚本

### 2. 测试场景
1. 设置多节次比分数据
2. 从第2节跳转到第1节
3. 验证当前节次和比分显示
4. 验证节次比分区域高亮
5. 测试边界条件

### 3. 测试数据
```java
// 模拟比赛数据（从图片中看到的数据）
第1节：20-5
第2节：10-9
第3节：0-0
第4节：0-0
第5节：0-0
第6节：0-0
第7节：0-0
总分：30-14
```

## 相关文件

### 1. 修改的文件
- `src/main/java/com/basketball/display/BasketballFullScreenDisplay.java`
  - `handleSelectPreviousQuarter()`方法

### 2. 测试文件
- `TestPreviousQuarterFix.java`：功能测试
- `test_previous_quarter_fix.bat`：测试运行脚本

### 3. 文档文件
- `PC_PREVIOUS_QUARTER_FIX.md`：修复说明文档

## 总结

这个修复解决了PC侧跳转到上一节时比分显示错误的问题。关键是在处理选择上一节命令时，不仅要恢复比分数据，还要更新当前节次变量和刷新显示。

修复的核心要点：
1. **更新`currentQuarter`变量**：确保系统知道当前是第几节
2. **调用`updateQuarterScoresDisplay()`**：确保节次比分显示区域正确更新
3. **保持数据一致性**：确保显示的数据与数据库中的数据一致

这个修复确保了PC侧和手机侧的节次导航功能能够正确同步，提升了用户体验。 