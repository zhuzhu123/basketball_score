# 篮球计分应用图标解决方案

## 问题说明
你的Android项目缺少应用图标文件：
- `ic_launcher.png` - 方形应用图标
- `ic_launcher_round.png` - 圆形应用图标

## 解决方案

### 方案1：使用HTML图标生成器（推荐）
1. 在浏览器中打开 `icon_generator.html` 文件
2. 点击"生成所有尺寸图标"按钮
3. 点击"下载所有图标"按钮下载ZIP文件
4. 解压后将图标文件放入对应的mipmap目录

### 方案2：手动创建图标
如果你有图像编辑软件，可以创建以下尺寸的图标：
- `mipmap-mdpi/`: 48x48 像素
- `mipmap-hdpi/`: 72x72 像素  
- `mipmap-xhdpi/`: 96x96 像素
- `mipmap-xxhdpi/`: 144x144 像素
- `mipmap-xxxhdpi/`: 192x192 像素

### 方案3：使用在线工具
可以使用以下在线工具生成Android图标：
- Android Asset Studio
- App Icon Generator
- Icon Kitchen

## 图标要求
- 格式：PNG
- 背景：透明或与应用主题一致
- 风格：与你的篮球计分应用界面保持一致（深色主题，橙色元素）

## 文件结构
```
app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-hdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xxhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
└── mipmap-xxxhdpi/
    ├── ic_launcher.png
    └── ic_launcher_round.png
```

## 注意事项
- 图标文件名必须完全匹配
- 不同密度的图标尺寸要准确
- 图标应该在不同尺寸下都清晰可见
- 建议使用矢量图形设计，然后导出为不同尺寸

## 当前状态
✅ mipmap目录已创建
❌ 图标文件缺失
⚠️ 需要生成或添加图标文件 