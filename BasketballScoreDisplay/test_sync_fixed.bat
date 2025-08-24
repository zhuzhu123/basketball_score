@echo off
echo ========================================
echo 测试修复后的SYNC_ALL_SCORES命令处理逻辑
echo ========================================
echo.

REM 编译测试文件
echo [1/3] 编译测试文件...
javac test_sync_fixed.java
if errorlevel 1 (
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ 编译成功

REM 运行测试
echo [2/3] 运行测试...
echo.
java TestSyncFixed
echo.

REM 清理
echo [3/3] 清理测试文件...
del TestSyncFixed.class
echo ✅ 清理完成

echo.
echo ========================================
echo 测试完成
echo ========================================
pause 