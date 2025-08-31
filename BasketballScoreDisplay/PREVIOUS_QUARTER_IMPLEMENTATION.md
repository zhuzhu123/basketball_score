# 选择上一节和修改上一节比分功能实现说明

## 功能概述

在PC侧和手机侧实现了选择上一节并带出该节比分，同时支持修改该节比分的完整功能。用户可以：
1. 选择任意已完成的节次
2. 查看该节次的比分
3. 修改该节次的比分
4. 自动同步更新总分

## 实现内容

### 1. PC侧数据库管理器增强

#### 1.1 新增方法
```java
// 获取指定节次的比分
public QuarterScore getQuarterScore(int matchId, int quarterNumber)

// 更新指定节次的比分
public boolean updateQuarterScore(int matchId, int quarterNumber, int homeScore, int awayScore)

// 根据ID获取比赛信息
public MatchInfo getMatchById(int matchId)

// 自动更新比赛总分
private void updateMatchTotalScore(int matchId)
```

#### 1.2 功能特点
- 支持按比赛ID和节次号查询比分
- 支持更新指定节次的比分
- 自动重新计算并更新比赛总分
- 完整的错误处理和异常管理

### 2. PC侧蓝牙管理器增强

#### 2.1 新增命令处理
```java
// 处理选择比赛命令
private void handleSelectMatch(String value)

// 处理获取上一节比分命令
private void handleGetPreviousQuarter(String value)

// 处理修改上一节比分命令
private void handleUpdatePreviousQuarter(String value)
```

#### 2.2 命令格式
- **选择上一节**: `SELECT_PREVIOUS_QUARTER:matchId|quarterNumber`
- **修改上一节比分**: `UPDATE_PREVIOUS_QUARTER:matchId|quarterNumber|homeScore|awayScore`

#### 2.3 响应格式
- **选择上一节成功**: `PREVIOUS_QUARTER:quarterNumber|homeScore|awayScore`
- **修改上一节成功**: `PREVIOUS_QUARTER_UPDATED`
- **总分更新**: `TOTAL_SCORE_UPDATED:totalHomeScore|totalAwayScore`

### 3. PC侧主显示类增强

#### 3.1 新增处理方法
```java
// 处理选择上一节命令
private void handleSelectPreviousQuarter(String value)

// 处理修改上一节比分命令
private void handleUpdatePreviousQuarter(String value)
```

#### 3.2 功能特点
- 自动更新比分显示
- 自动更新节次显示
- 语音播报操作结果
- 实时同步数据库状态

### 4. 手机侧蓝牙管理器增强

#### 4.1 新增命令类
```java
// 选择比赛命令
public static class SelectMatch extends ScoreCommand

// 选择上一节命令
public static class SelectPreviousQuarter extends ScoreCommand

// 修改上一节比分命令
public static class UpdatePreviousQuarter extends ScoreCommand
```

#### 4.2 命令转换
- `SELECT_PREVIOUS_QUARTER` → `SELECT_PREVIOUS_QUARTER:matchId|quarterNumber`
- `UPDATE_PREVIOUS_QUARTER` → `UPDATE_PREVIOUS_QUARTER:matchId|quarterNumber|homeScore|awayScore`

### 5. 手机侧MainActivity增强

#### 5.1 新增方法
```java
// 选择上一节并带出比分
private void selectPreviousQuarter(int quarterNumber)

// 修改上一节比分
private void modifyPreviousQuarter(int quarterNumber)
```

#### 5.2 功能特点
- 节次有效性验证
- 用户友好的对话框界面
- 实时蓝牙命令发送
- 完整的错误处理

## 使用流程

### 1. 选择上一节
1. 用户选择要查看的节次
2. 手机端发送`SELECT_PREVIOUS_QUARTER`命令
3. PC端查询数据库获取该节次比分
4. PC端返回比分信息
5. 手机端显示该节次比分
6. PC端同步显示该节次比分

### 2. 修改上一节比分
1. 用户选择要修改的节次
2. 手机端弹出修改比分对话框
3. 用户输入新的比分
4. 手机端发送`UPDATE_PREVIOUS_QUARTER`命令
5. PC端更新数据库中的比分
6. PC端重新计算比赛总分
7. PC端返回更新结果和新的总分
8. 手机端和PC端同步显示更新后的比分

## 数据同步机制

### 1. 实时同步
- 所有比分修改都实时同步到数据库
- PC端和手机端显示保持一致
- 总分自动重新计算

### 2. 状态管理
- 维护当前比赛ID
- 跟踪节次状态
- 管理比分缓存

### 3. 错误处理
- 网络连接检查
- 数据库操作验证
- 用户友好的错误提示

## 技术特点

### 1. 数据库设计
- 节次比分表支持按比赛ID和节次号查询
- 自动外键约束确保数据完整性
- 支持比分更新和总分自动计算

### 2. 蓝牙通信
- 标准化的命令格式
- 完整的响应机制
- 错误处理和重试机制

### 3. 用户体验
- 直观的操作界面
- 实时的状态反馈
- 语音播报支持

## 测试验证

### 测试文件
- `TestPreviousQuarter.java`: 测试选择上一节和修改上一节比分功能
- `test_previous_quarter.bat`: 运行测试的批处理脚本

### 测试内容
1. **选择上一节**: 验证节次选择、比分获取、数据解析
2. **修改上一节比分**: 验证比分修改、数据库更新、总分计算
3. **命令格式验证**: 验证命令格式的正确性和完整性
4. **数据流程模拟**: 模拟完整的操作流程和数据同步

### 运行测试
```bash
test_previous_quarter.bat
```

## 注意事项

### 1. 数据一致性
- 确保PC端和手机端数据同步
- 避免并发修改导致的数据冲突
- 定期验证数据库完整性

### 2. 性能考虑
- 大量节次数据时的查询性能
- 蓝牙通信的延迟处理
- 数据库连接的资源管理

### 3. 用户体验
- 操作反馈的及时性
- 错误信息的清晰性
- 界面操作的便捷性

## 扩展功能建议

### 1. 批量操作
- 支持批量修改多个节次比分
- 支持比分历史记录查看
- 支持比分统计和分析

### 2. 高级功能
- 支持比分回滚和撤销
- 支持比分版本管理
- 支持比分导出和备份

### 3. 移动端增强
- 支持手势操作
- 支持离线模式
- 支持多设备同步

## 总结

选择上一节和修改上一节比分功能的实现完善了篮球计分系统的数据管理能力，提供了完整的比分查看和修改功能。通过合理的数据库设计、蓝牙通信机制和用户界面设计，确保了功能的可靠性和易用性。该功能为比赛管理提供了更大的灵活性，支持比分纠错和历史回顾，提升了系统的实用价值。 