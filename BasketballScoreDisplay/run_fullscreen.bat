@echo off
echo ================================================
echo   全屏篮球比分显示系统 - 启动脚本
echo ================================================
echo.

echo 正在编译Java程序...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！请检查错误信息。
    pause
    exit /b 1
)

echo 编译成功！
echo.
echo 启动全屏显示系统...
echo.
echo 提示：
echo - 程序将以全屏模式启动
echo - 按 ESC 键退出程序
echo - 确保蓝牙已启用
echo.
pause

echo 正在启动...
java -cp target/classes com.basketball.display.BasketballFullScreenDisplay

echo.
echo 程序已退出。
pause 