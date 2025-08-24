# 篮球比分显示系统

这是一个Java Swing桌面应用程序，用于显示篮球比赛的实时比分和计时信息。系统通过TCP网络连接接收来自Android应用的比分数据。

## 功能特性

- 🏀 实时比分显示（龙都F4 vs 暴风队）
- ⏱️ 比赛计时器（12分钟/节）
- 📊 节次管理（第1-4节）
- 🔄 比分重置功能
- 📱 支持Android应用远程控制
- 🎨 现代化用户界面

## 系统要求

- Java 11 或更高版本
- Windows 10/11 操作系统
- 网络连接（用于接收Android应用数据）

## 安装和运行

### 方法1：使用Maven构建

```bash
# 克隆项目
git clone <repository-url>
cd BasketballScoreDisplay

# 编译项目
mvn clean compile

# 运行程序
mvn exec:java -Dexec.mainClass="com.basketball.display.BasketballScoreDisplay"
```

### 方法2：直接运行JAR文件

```bash
# 打包项目
mvn clean package

# 运行生成的JAR文件
java -jar target/basketball-score-display-1.0.0.jar
```

### 方法3：使用批处理文件（Windows）

双击 `run.bat` 文件即可运行程序。

## 使用说明

### 启动程序

1. 运行程序后，系统会自动在端口8888上监听连接
2. 状态栏会显示 "等待设备连接... (端口: 8888)"
3. 确保Android应用连接到同一网络

### 连接Android应用

1. 在Android应用中输入运行Java程序的电脑IP地址
2. 端口号设置为：8888
3. 点击连接按钮

### 控制命令

Android应用可以发送以下命令：

| 命令 | 格式 | 说明 |
|------|------|------|
| 龙都F4得分 | `HOME_SCORE:2` | 龙都F4增加2分 |
| 暴风队得分 | `AWAY_SCORE:3` | 暴风队增加3分 |
| 重置比分 | `RESET_SCORE` | 重置所有比分和计时器 |
| 设置节次 | `QUARTER:2` | 切换到第2节 |
| 开始计时 | `TIMER_START` | 开始计时器 |
| 暂停计时 | `TIMER_PAUSE` | 暂停计时器 |
| 设置时间 | `TIMER:600` | 设置计时器为10分钟 |
| 设置龙都F4名 | `HOME_TEAM:湖人队` | 设置龙都F4名称 |
| 设置暴风队名 | `AWAY_TEAM:勇士队` | 设置暴风队名称 |
| 超时处理 | `TIMEOUT:2` | 切换到第2节并重置计时器 |

### 手动控制

程序界面底部提供以下手动控制按钮：

- **重置比分**：重置所有比分和计时器
- **开始计时**：开始比赛计时
- **暂停计时**：暂停比赛计时
- **下一节**：切换到下一节比赛

## 网络配置

### 端口设置

默认端口：8888
如需修改端口，请编辑 `BasketballScoreDisplay.java` 文件中的 `PORT` 常量。

### 防火墙设置

确保Windows防火墙允许Java程序通过端口8888进行网络通信。

### 网络连接

- 程序使用TCP Socket监听连接
- 支持局域网内的设备连接
- 如需外网访问，请配置端口转发

## 故障排除

### 连接问题

1. **无法连接**：检查防火墙设置和端口是否被占用
2. **连接断开**：检查网络连接和Android应用状态
3. **数据不更新**：确认Android应用发送的命令格式正确

### 编译问题

1. **Java版本错误**：确保使用Java 11或更高版本
2. **依赖缺失**：运行 `mvn clean install` 重新安装依赖

### 运行问题

1. **端口被占用**：修改端口号或关闭占用端口的程序
2. **权限不足**：以管理员身份运行程序

## 开发说明

### 项目结构

```
BasketballScoreDisplay/
├── src/main/java/com/basketball/display/
│   └── BasketballScoreDisplay.java    # 主程序
├── pom.xml                            # Maven配置
├── run.bat                            # Windows运行脚本
└── README.md                          # 说明文档
```

### 技术栈

- **Java 11**：核心编程语言
- **Java Swing**：图形用户界面
- **TCP Socket**：网络通信
- **Maven**：项目构建工具

### 扩展功能

可以添加以下功能来增强系统：

- 数据库记录比赛历史
- 网络直播功能
- 多语言支持
- 自定义主题
- 数据导出功能

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交Issue到项目仓库
- 发送邮件到开发者邮箱

---

**注意**：本系统设计用于篮球比赛现场使用，请确保网络环境稳定，以获得最佳使用体验。 