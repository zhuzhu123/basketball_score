# SET_SCORE_COMMANDS 实现说明

## 问题描述

在原有的实现中，当手机侧更新当节比分时，PC侧会将新比分加到之前的比分上，而不是按照手机侧的比分来设置。这是因为：

1. **手机侧发送的命令**：`HOME_SCORE:newScore` 和 `AWAY_SCORE:newScore`
2. **PC侧处理逻辑**：使用累加逻辑 `homeScore += points` 和 `awayScore += points`
3. **结果**：分数重复累加，显示错误

## 解决方案

### 1. 新增命令类型

在手机侧 `BluetoothManager.java` 中添加了两个新的命令类型：

```java
public static class SetHomeScore extends ScoreCommand {
    private final int score;
    
    public SetHomeScore(int score) {
        this.score = score;
    }
    
    public int getScore() {
        return score;
    }
}

public static class SetAwayScore extends ScoreCommand {
    private final int score;
    
    public SetAwayScore(int score) {
        this.score = score;
    }
    
    public int getScore() {
        return score;
    }
}
```

### 2. 命令字符串格式

新的命令格式：
- `SET_HOME_SCORE:score` - 直接设置主队得分
- `SET_AWAY_SCORE:score` - 直接设置客队得分

### 3. 手机侧使用方式

在 `MainActivity.java` 中，将原来的累加命令改为直接设置命令：

```java
// 原来的累加命令（已废弃）
// bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.HomeScore(newHomeScore));
// bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.AwayScore(newAwayScore));

// 新的直接设置命令
bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SetHomeScore(newHomeScore));
bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SetAwayScore(newAwayScore));
```

### 4. PC侧处理逻辑

在 `BasketballFullScreenDisplay.java` 中添加了新的命令处理：

```java
case "SET_HOME_SCORE":
    int setHomeScore = Integer.parseInt(value);
    setHomeScore(setHomeScore);
    showScorePanel();
    break;
case "SET_AWAY_SCORE":
    int setAwayScore = Integer.parseInt(value);
    setAwayScore(setAwayScore);
    showScorePanel();
    break;
```

### 5. 新增方法

添加了两个新方法来直接设置比分（不累加）：

```java
/**
 * 直接设置龙都F4得分（不累加）
 */
private void setHomeScore(int score) {
    homeScore = score;  // 直接设置，不累加
    if (homeScore < 0) homeScore = 0;
    
    // 更新UI显示
    if (homeScoreLabel != null) {
        homeScoreLabel.setText(String.valueOf(homeScore));
    }
    
    // 保存到数据库
    if (databaseManager != null && currentMatchId > 0) {
        try {
            databaseManager.saveQuarterScore(currentMatchId, currentQuarter, homeScore, awayScore);
            System.out.println("龙都F4得分直接设置: " + score + ", 当前比分: " + homeScore + ":" + awayScore);
        } catch (Exception e) {
            System.err.println("保存龙都F4得分失败: " + e.getMessage());
        }
    }
    
    // 检查游戏是否结束
    checkGameEnd();
    
    System.out.println("龙都F4当前节次得分直接设置: " + homeScore);
}

/**
 * 直接设置暴风队得分（不累加）
 */
private void setAwayScore(int score) {
    awayScore = score;  // 直接设置，不累加
    if (awayScore < 0) awayScore = 0;
    
    // 更新UI显示
    if (awayScoreLabel != null) {
        awayScoreLabel.setText(String.valueOf(awayScore));
    }
    
    // 保存到数据库
    if (databaseManager != null && currentMatchId > 0) {
        try {
            databaseManager.saveQuarterScore(currentMatchId, currentQuarter, homeScore, awayScore);
            System.out.println("暴风队得分直接设置: " + score + ", 当前比分: " + homeScore + ":" + awayScore);
        } catch (Exception e) {
            System.err.println("保存暴风队得分失败: " + e.getMessage());
        }
    }
    
    // 检查游戏是否结束
    checkGameEnd();
    
    System.out.println("暴风队当前节次得分直接设置: " + awayScore);
}
```

## 命令对比

| 命令类型 | 手机侧发送 | PC侧处理 | 结果 |
|---------|-----------|----------|------|
| `HOME_SCORE` | `HOME_SCORE:15` | `homeScore += 15` | 累加 |
| `AWAY_SCORE` | `AWAY_SCORE:12` | `awayScore += 12` | 累加 |
| `SET_HOME_SCORE` | `SET_HOME_SCORE:15` | `homeScore = 15` | 直接设置 |
| `SET_AWAY_SCORE` | `SET_AWAY_SCORE:12` | `awayScore = 12` | 直接设置 |

## 使用场景

### 1. 累加命令（原有）
- 适用于：实时计分，每次加分
- 命令：`HOME_SCORE`、`AWAY_SCORE`
- 效果：`当前分数 += 新分数`

### 2. 直接设置命令（新增）
- 适用于：同步比分，修正比分，设置最终比分
- 命令：`SET_HOME_SCORE`、`SET_AWAY_SCORE`
- 效果：`当前分数 = 新分数`

## 测试验证

创建了测试文件 `TestSetScoreCommands.java` 来验证：

1. **直接设置比分**：发送 `SET_HOME_SCORE:15` 和 `SET_AWAY_SCORE:12`，PC端应显示 15:12
2. **不会累加**：先设置 15:12，再设置 20:18，应显示 20:18（不是 35:30）

运行测试：
```bash
cd BasketballScoreDisplay
test_set_score_commands.bat
```

## 注意事项

1. **向后兼容**：原有的 `HOME_SCORE` 和 `AWAY_SCORE` 命令仍然保留，不会影响现有功能
2. **数据库更新**：新的设置命令会立即更新数据库中的比分
3. **UI同步**：设置比分后会立即更新PC端的显示界面
4. **游戏结束检查**：设置比分后会触发游戏结束检查逻辑

## 总结

通过新增 `SET_HOME_SCORE` 和 `SET_AWAY_SCORE` 命令，解决了手机侧更新比分时PC侧累加分数的问题。现在手机侧可以：

1. 使用累加命令进行实时计分
2. 使用设置命令同步最终比分

这样既保持了原有功能的完整性，又解决了比分同步的准确性问题。 