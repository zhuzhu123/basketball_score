# 篮球计分应用图标生成器
Write-Host "正在生成篮球计分应用图标..." -ForegroundColor Green

try {
    Add-Type -AssemblyName System.Drawing
    
    function Create-BasketballIcon {
        param([int]$Size, [string]$OutputPath)
        
        $bitmap = New-Object System.Drawing.Bitmap($Size, $Size)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        
        # 设置背景为透明
        $graphics.Clear([System.Drawing.Color]::Transparent)
        
        # 创建篮球橙色
        $basketballColor = [System.Drawing.Color]::FromArgb(255, 107, 53)
        $darkOrange = [System.Drawing.Color]::FromArgb(229, 90, 43)
        $white = [System.Drawing.Color]::White
        
        # 计算中心点和半径
        $center = $Size / 2
        $radius = [int]($Size * 0.45)
        
        # 绘制背景圆形
        $brush = New-Object System.Drawing.SolidBrush($basketballColor)
        $pen = New-Object System.Drawing.Pen($darkOrange, [Math]::Max(1, $Size/50))
        $graphics.FillEllipse($brush, $center - $radius, $center - $radius, $radius * 2, $radius * 2)
        $graphics.DrawEllipse($pen, $center - $radius, $center - $radius, $radius * 2, $radius * 2)
        
        # 绘制中心白色圆圈
        $innerRadius = [int]($radius * 0.4)
        $whiteBrush = New-Object System.Drawing.SolidBrush($white)
        $graphics.FillEllipse($whiteBrush, $center - $innerRadius, $center - $innerRadius, $innerRadius * 2, $innerRadius * 2)
        
        # 保存图片
        $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
        
        $graphics.Dispose()
        $bitmap.Dispose()
        $brush.Dispose()
        $pen.Dispose()
        $whiteBrush.Dispose()
    }
    
    # 创建不同尺寸的图标
    $densities = @{
        'mdpi' = 48
        'hdpi' = 72
        'xhdpi' = 96
        'xxhdpi' = 144
        'xxxhdpi' = 192
    }
    
    foreach ($density in $densities.Keys) {
        $size = $densities[$density]
        $iconDir = "app\src\main\res\mipmap-$density"
        
        Write-Host "生成 $density 图标 ($size x $size)..." -ForegroundColor Yellow
        
        # 创建图标
        $launcherPath = "$iconDir\ic_launcher.png"
        $roundPath = "$iconDir\ic_launcher_round.png"
        
        Create-BasketballIcon -Size $size -OutputPath $launcherPath
        Create-BasketballIcon -Size $size -OutputPath $roundPath
        
        Write-Host "✓ $density 图标已生成" -ForegroundColor Green
    }
    
    Write-Host "所有图标生成完成！" -ForegroundColor Green
    
} catch {
    Write-Host "生成图标时出错: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "请尝试使用HTML图标生成器作为替代方案" -ForegroundColor Yellow
}

Write-Host "按任意键继续..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 