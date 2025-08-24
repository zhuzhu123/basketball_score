@echo off
echo ========================================
echo 测试修复后的节次变更和总比分显示逻辑
echo ========================================
echo.

REM 编译测试文件
echo [1/3] 编译测试文件...
javac test_quarter_fix.java
if errorlevel 1 (
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ 编译成功

REM 运行测试
echo [2/3] 运行测试...
echo.
java TestQuarterFix
echo.

REM 清理
echo [3/3] 清理测试文件...
del TestQuarterFix.class
echo ✅ 清理完成

echo.
echo ========================================
echo 测试完成
echo ========================================
pause 