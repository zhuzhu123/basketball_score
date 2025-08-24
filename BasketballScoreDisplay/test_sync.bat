@echo off
echo ========================================
echo 测试SYNC_ALL_SCORES命令处理逻辑
echo ========================================
echo.

REM 编译测试文件
echo [1/3] 编译测试文件...
javac test_sync_scores.java
if errorlevel 1 (
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ 编译成功

REM 运行测试
echo [2/3] 运行测试...
echo.
java TestSyncScores
echo.

REM 清理
echo [3/3] 清理测试文件...
del TestSyncScores.class
echo ✅ 清理完成

echo.
echo ========================================
echo 测试完成
echo ========================================
pause 