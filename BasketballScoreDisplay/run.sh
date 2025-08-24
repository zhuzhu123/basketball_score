#!/bin/bash

echo "启动篮球比分显示系统..."
echo

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java运行环境"
    echo "请先安装Java 8或更高版本"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "警告: 未找到Maven，尝试直接运行编译后的类文件..."
    goto_run_java
fi

echo "使用Maven编译和运行..."
echo

# 清理并编译
echo "正在编译项目..."
if ! mvn clean compile; then
    echo "编译失败，尝试直接运行..."
    goto_run_java
fi

# 运行
echo "正在启动应用..."
mvn exec:java -Dexec.mainClass="com.basketball.display.BasketballScoreDisplay"
exit 0

goto_run_java:
echo "尝试直接运行Java程序..."
if [ -f "target/classes/com/basketball/display/BasketballScoreDisplay.class" ]; then
    java -cp "target/classes" com.basketball.display.BasketballScoreDisplay
else
    echo "错误: 未找到编译后的类文件"
    echo "请先编译项目或使用Maven"
    exit 1
fi

echo
echo "程序已退出" 