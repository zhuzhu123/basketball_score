@echo off
echo 启动篮球比分显示系统...
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java运行环境
    echo 请先安装Java 8或更高版本
    pause
    exit /b 1
)

REM 检查Maven是否安装
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 警告: 未找到Maven，尝试直接运行编译后的类文件...
    goto run_java
)

echo 使用Maven编译和运行...
echo.

REM 清理并编译
echo 正在编译项目...
mvn clean compile
if errorlevel 1 (
    echo 编译失败，尝试直接运行...
    goto run_java
)

REM 运行
echo 正在启动应用...
mvn exec:java -Dexec.mainClass="com.basketball.display.BasketballScoreDisplay"
goto end

:run_java
echo 尝试直接运行Java程序...
if exist "target\classes\com\basketball\display\BasketballScoreDisplay.class" (
    java -cp "target\classes" com.basketball.display.BasketballScoreDisplay
) else (
    echo 错误: 未找到编译后的类文件
    echo 请先编译项目或使用Maven
    pause
    exit /b 1
)

:end
echo.
echo 程序已退出
pause 