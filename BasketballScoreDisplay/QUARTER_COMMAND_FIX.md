# PC侧处理QUARTER命令时正确恢复历史比分的修复

## 问题描述

当手机侧跳转到上一节时，比如从第2节跳转到第1节，手机侧发送的命令是`QUARTER:1`，而不是`SELECT_PREVIOUS_QUARTER`命令。但是PC侧在处理`QUARTER`命令时，总是重置比分为0-0，导致历史比分丢失。

## 问题分析

### 1. 问题现象
- 第1节历史比分：20-5
- 第2节历史比分：10-9
- 从第2节跳转到第1节时，手机侧发送`QUARTER:1`命令
- PC侧处理命令后，当前节比分显示为0-0（错误）
- 应该显示为20-5（正确）

### 2. 根本原因
在PC侧的`handleQuarterChange`方法中，处理`QUARTER`命令时：

1. 保存当前节次比分到数据库
2. 更新`currentQuarter`变量
3. **总是重置比分为0-0**（这是问题所在）

```java
// 修复前的代码（有问题）
private void handleQuarterChange(String value) {
    // ... 保存当前节次比分 ...
    
    // 更新当前节次
    currentQuarter = newQuarter;
    
    // ❌ 问题：总是重置比分为0-0
    homeScore = 0;
    awayScore = 0;
    
    // 更新UI显示
    if (homeScoreLabel != null) {
        homeScoreLabel.setText("0");
    }
    if (awayScoreLabel != null) {
        awayScoreLabel.setText("0");
    }
}
```

### 3. 命令流程分析
```
手机侧跳转到上一节：
1. 手机侧调用 previousQuarter()
2. 手机侧发送 QUARTER:1 命令
3. PC侧接收并处理 QUARTER:1 命令
4. PC侧调用 handleQuarterChange("1")
5. PC侧总是重置比分为0-0（问题）
```

## 修复方案

### 1. 修复内容
修改`handleQuarterChange`方法，在处理`QUARTER`命令时：

1. **保存当前节次比分到数据库**
2. **更新当前节次变量**
3. **从数据库获取目标节次的历史比分**
4. **如果有历史记录，恢复历史比分；如果没有，重置为0-0**

### 2. 修复后的代码
```java
// 修复后的代码（正确）
private void handleQuarterChange(String value) {
    // ... 保存当前节次比分 ...
    
    // 更新当前节次
    currentQuarter = newQuarter;
    
    // ✅ 修复：从数据库获取该节次的历史比分
    if (databaseManager != null && currentMatchId > 0) {
        try {
            DatabaseManager.QuarterScore quarterScore = databaseManager.getQuarterScore(currentMatchId, currentQuarter);
            if (quarterScore != null) {
                // 恢复历史比分
                homeScore = quarterScore.getHomeScore();
                awayScore = quarterScore.getAwayScore();
                System.out.println("恢复第" + currentQuarter + "节历史比分: " + homeScore + ":" + awayScore);
            } else {
                // 如果没有历史记录，重置为0
                homeScore = 0;
                awayScore = 0;
                System.out.println("第" + currentQuarter + "节无历史记录，重置为0:0");
            }
        } catch (Exception e) {
            System.err.println("获取历史比分时出错: " + e.getMessage());
            // 出错时重置为0
            homeScore = 0;
            awayScore = 0;
        }
    } else {
        // 数据库不可用时重置为0
        homeScore = 0;
        awayScore = 0;
    }
    
    // 更新UI显示
    if (homeScoreLabel != null) {
        homeScoreLabel.setText(String.valueOf(homeScore));
    }
    if (awayScoreLabel != null) {
        awayScoreLabel.setText(String.valueOf(awayScore));
    }
}
```

## 修复效果

### 1. 修复前的问题
- 处理`QUARTER`命令时，总是重置比分为0-0
- 导致跳转到上一节时，历史比分丢失
- 无法正确恢复历史比分

### 2. 修复后的效果
- 处理`QUARTER`命令时，先保存当前节次比分
- 从数据库获取目标节次的历史比分
- 如果有历史记录，恢复历史比分
- 如果没有历史记录，重置为0-0
- 确保数据一致性和用户体验

### 3. 验证结果
修复后，从第2节跳转到第1节时：
- 手机侧发送：`QUARTER:1`
- PC侧处理：保存第2节比分，恢复第1节比分
- 当前节次显示：第1节
- 当前比分显示：20-5（而不是0-0）
- 节次比分区域：第1节高亮显示20-5

## 测试验证

### 1. 测试文件
- `TestQuarterCommandFix.java`：功能测试
- `test_quarter_command_fix.bat`：测试运行脚本

### 2. 测试场景
1. 设置多节次比分数据
2. 从第2节跳转到第1节（QUARTER:1）
3. 从第1节跳转到第2节（QUARTER:2）
4. 跳转到新节次（QUARTER:3）
5. 验证历史比分恢复

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
  - `handleQuarterChange()`方法

### 2. 测试文件
- `TestQuarterCommandFix.java`：功能测试
- `test_quarter_command_fix.bat`：测试运行脚本

### 3. 文档文件
- `QUARTER_COMMAND_FIX.md`：修复说明文档

## 总结

这个修复解决了PC侧处理`QUARTER`命令时比分显示错误的问题。关键是在处理节次变更命令时，不仅要保存当前节次比分，还要从数据库恢复目标节次的历史比分。

修复的核心要点：
1. **保存当前节次比分**：确保当前节次的数据不丢失
2. **恢复历史比分**：从数据库获取目标节次的历史数据
3. **智能处理**：有历史记录时恢复，无历史记录时重置
4. **错误处理**：数据库异常时安全降级

这个修复确保了PC侧能够正确处理手机侧发送的`QUARTER`命令，无论是跳转到上一节还是下一节，都能正确显示比分数据。 