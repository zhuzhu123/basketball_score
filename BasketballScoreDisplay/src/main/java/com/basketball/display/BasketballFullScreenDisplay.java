package com.basketball.display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 全屏篮球比分显示系统
 * 集成数据库管理和比赛记录功能
 */
public class BasketballFullScreenDisplay extends JFrame implements KeyListener {
    
    // 显示组件
    private JPanel scorePanel;
    private JPanel countdownPanel;
    private JLabel homeTeamLabel;
    private JLabel awayTeamLabel;
    private JLabel homeScoreLabel;
    private JLabel awayScoreLabel;
    private JLabel vsLabel;
    private JLabel countdownLabel;
    
    // 数据
    private String homeTeam = "龙都F4";
    private String awayTeam = "暴风队";
    private int homeScore = 0;
    private int awayScore = 0;
    private int countdownTime = 15; // 倒计时时间
    private int savedCountdownTime = 15; // 保存的倒计时时间
    
    // 状态
    private boolean isCountdownMode = false;
    private boolean isCountdownRunning = false;
    private Timer countdownTimer;
    private ScheduledExecutorService executorService;
    
    // 数据库管理器
    private DatabaseManager databaseManager;
    private int currentMatchId = -1;
    private String currentMatchName = "";
    private String currentMatchNote = "";
    
    // 蓝牙管理器
    private BluetoothManager bluetoothManager;
    
    // 比赛数据
    private int totalHomeScore = 0;
    private int totalAwayScore = 0;
    private int currentQuarter = 1; // 当前节次
    private List<DatabaseManager.QuarterScore> quarterScores = new ArrayList<>();
    
    // 比赛信息面板的标签引用
    private JLabel matchNameLabel;
    private JLabel matchNoteLabel;
    private JLabel quarter1Label;
    private JLabel quarter2Label;
    private JLabel quarter3Label;
    private JLabel quarter4Label;
    private JLabel quarter5Label;
    private JLabel quarter6Label;
    private JLabel quarter7Label;
    private JLabel totalScoreLabel;
    private JLabel currentQuarterLabel; // 当前节次显示标签
    
    // 颜色主题
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0);
    private static final Color HOME_COLOR = new Color(255, 69, 0);  // 橙红色
    private static final Color AWAY_COLOR = new Color(30, 144, 255); // 蓝色
    private static final Color TEXT_COLOR = new Color(255, 255, 255); // 白色
    private static final Color COUNTDOWN_COLOR = new Color(255, 255, 0); // 黄色
    private static final Color COUNTDOWN_WARNING_COLOR = new Color(255, 0, 0); // 红色
    
    public BasketballFullScreenDisplay() {
        initializeUI();
        initializeTTS();
        initializeDatabase();
        initializeBluetooth();
        
        // 设置ESC键退出全屏
        addKeyListener(this);
        setFocusable(true);
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        setTitle("篮球计分系统 - 全屏显示");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // 设置全屏
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        }
        
        // 创建主面板
        setLayout(new CardLayout());
        
        // 创建比分面板
        scorePanel = createScorePanel();
        add(scorePanel, "score");
        
        // 创建倒计时面板
        createCountdownPanel();
        add(countdownPanel, "countdown");
        
        // 默认显示比分面板
        showScorePanel();
        
        // 设置窗口可见
        setVisible(true);
    }
    
    /**
     * 创建比分显示面板
     */
    private JPanel createScorePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        // 比赛信息面板（顶部）
        JPanel matchInfoPanel = createMatchInfoPanel();
        panel.add(matchInfoPanel, BorderLayout.NORTH);
        
        // 当前节次比分显示（中央）
        JPanel currentScorePanel = new JPanel();
        currentScorePanel.setLayout(new BorderLayout());
        currentScorePanel.setBackground(BACKGROUND_COLOR);
        currentScorePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // 龙都F4比分
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        homePanel.setBackground(BACKGROUND_COLOR);
        
        homeTeamLabel = new JLabel("龙都F4", SwingConstants.CENTER);
        homeTeamLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        homeTeamLabel.setForeground(HOME_COLOR);
        
        homeScoreLabel = new JLabel("0", SwingConstants.CENTER);
        homeScoreLabel.setFont(new Font("Arial", Font.BOLD, 200));
        homeScoreLabel.setForeground(HOME_COLOR);
        
        homePanel.add(homeTeamLabel, BorderLayout.NORTH);
        homePanel.add(homeScoreLabel, BorderLayout.CENTER);
        
        // 比分分隔符
        JLabel separatorLabel = new JLabel(":", SwingConstants.CENTER);
        separatorLabel.setFont(new Font("Arial", Font.BOLD, 200));
        separatorLabel.setForeground(TEXT_COLOR);
        
        // 暴风队比分
        JPanel awayPanel = new JPanel();
        awayPanel.setLayout(new BorderLayout());
        awayPanel.setBackground(BACKGROUND_COLOR);
        
        awayTeamLabel = new JLabel("暴风队", SwingConstants.CENTER);
        awayTeamLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        awayTeamLabel.setForeground(AWAY_COLOR);
        
        awayScoreLabel = new JLabel("0", SwingConstants.CENTER);
        awayScoreLabel.setFont(new Font("Arial", Font.BOLD, 200));
        awayScoreLabel.setForeground(AWAY_COLOR);
        
        awayPanel.add(awayTeamLabel, BorderLayout.NORTH);
        awayPanel.add(awayScoreLabel, BorderLayout.CENTER);
        
        // 添加比分面板到中央
        JPanel scoreDisplayPanel = new JPanel();
        scoreDisplayPanel.setLayout(new GridLayout(1, 3, 50, 0));
        scoreDisplayPanel.setBackground(BACKGROUND_COLOR);
        scoreDisplayPanel.add(homePanel);
        scoreDisplayPanel.add(separatorLabel);
        scoreDisplayPanel.add(awayPanel);
        
        currentScorePanel.add(scoreDisplayPanel, BorderLayout.CENTER);
        
        // 当前节次显示
        currentQuarterLabel = new JLabel("第1节", SwingConstants.CENTER);
        currentQuarterLabel.setFont(new Font("微软雅黑", Font.BOLD, 36));
        currentQuarterLabel.setForeground(TEXT_COLOR);
        currentQuarterLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        currentScorePanel.add(currentQuarterLabel, BorderLayout.SOUTH);
        
        panel.add(currentScorePanel, BorderLayout.CENTER);
        
        // 控制按钮面板（底部）
//        JPanel controlPanel = createControlPanel();
//        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建队伍面板
     */
    private JPanel createTeamPanel(String teamName, String score, Color color, boolean isHome) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // 队名
        JLabel nameLabel = new JLabel(teamName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 80));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 分数
        JLabel scoreLabel = new JLabel(score, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 300));
        scoreLabel.setForeground(color);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalGlue());
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalGlue());
        
        // 保存引用以便更新
        if (isHome) {
            homeTeamLabel = nameLabel;
            homeScoreLabel = scoreLabel;
        } else {
            awayTeamLabel = nameLabel;
            awayScoreLabel = scoreLabel;
        }
        
        return panel;
    }
    
    /**
     * 创建控制按钮面板
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 倒计时控制按钮
        JButton startCountdownButton = createStyledButton("开始倒计时", new Color(0, 150, 0));
        startCountdownButton.addActionListener(e -> startCountdown());
        
        JButton pauseCountdownButton = createStyledButton("暂停倒计时", new Color(255, 140, 0));
        pauseCountdownButton.addActionListener(e -> pauseCountdown());
        
        JButton continueCountdownButton = createStyledButton("继续倒计时", new Color(0, 100, 255));
        continueCountdownButton.addActionListener(e -> continueCountdown());
        
        JButton resetCountdownButton = createStyledButton("重置倒计时", new Color(150, 0, 150));
        resetCountdownButton.addActionListener(e -> resetCountdown());
        
        // 得分控制按钮
        JButton homeScoreButton = createStyledButton("龙都F4+1", HOME_COLOR);
        homeScoreButton.addActionListener(e -> updateHomeScore(1));
        
        JButton awayScoreButton = createStyledButton("暴风队+1", AWAY_COLOR);
        awayScoreButton.addActionListener(e -> updateAwayScore(1));
        
        JButton resetScoreButton = createStyledButton("重置比分", new Color(128, 128, 128));
        resetScoreButton.addActionListener(e -> resetScore());
        
        // 添加按钮到面板
        panel.add(startCountdownButton);
        panel.add(pauseCountdownButton);
        panel.add(continueCountdownButton);
        panel.add(resetCountdownButton);
        panel.add(Box.createHorizontalStrut(40)); // 分隔符
        panel.add(homeScoreButton);
        panel.add(awayScoreButton);
        panel.add(resetScoreButton);
        
        return panel;
    }
    
    /**
     * 创建样式化的按钮
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * 创建倒计时面板
     */
    private void createCountdownPanel() {
        countdownPanel = new JPanel();
        countdownPanel.setLayout(new BorderLayout());
        countdownPanel.setBackground(BACKGROUND_COLOR);
        
        // 倒计时显示
        countdownLabel = new JLabel(String.valueOf(countdownTime), SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 400));
        countdownLabel.setForeground(COUNTDOWN_COLOR);
        
        countdownPanel.add(countdownLabel, BorderLayout.CENTER);
        
        // 初始化倒计时定时器
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCountdown();
            }
        });
    }
    
    /**
     * 初始化语音合成
     */
    private void initializeTTS() {
        try {
            // 这里使用简单的系统铃声代替复杂的TTS
            // 在实际部署时可以使用 Windows SAPI 或其他 TTS 引擎
            System.out.println("语音播报系统已初始化");
        } catch (Exception e) {
            System.err.println("语音播报初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化数据库
     */
    private void initializeDatabase() {
        try {
            databaseManager = new DatabaseManager();
            databaseManager.initialize();
            System.out.println("数据库管理器已初始化");
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化蓝牙管理器
     */
    private void initializeBluetooth() {
        try {
            bluetoothManager = new BluetoothManager(databaseManager);
            bluetoothManager.setCallback(new BluetoothManager.BluetoothCallback() {
                @Override
                public void onConnectionStatusChanged(boolean isConnected, BluetoothManager.ConnectionType type) {
                    System.out.println("蓝牙连接状态: " + (isConnected ? "已连接" : "已断开"));
                }
                
                @Override
                public void onError(String message) {
                    System.err.println("蓝牙错误: " + message);
                }
                
                @Override
                public void onDeviceFound(String deviceInfo) {
                    System.out.println("发现设备: " + deviceInfo);
                }
                
                @Override
                public void onDataReceived(String data) {
                    System.out.println("接收数据: " + data);
                    String[] commands = data.split(";");
                    for (String cmd : commands) {
                        processBluetoothCommand(cmd);
                    }
                }
                
                @Override
                public void onMatchCreated(int matchId, String matchName, String matchNote) {
                    currentMatchId = matchId;
                    currentMatchName = matchName;
                    currentMatchNote = matchNote;
                    updateMatchInfoDisplay();
                    System.out.println("比赛创建: " + matchName);
                }
                
                @Override
                public void onQuarterSaved(int quarter, int homeScore, int awayScore) {
                    System.out.println("节次保存: 第" + quarter + "节 " + homeScore + "-" + awayScore);
                    updateQuarterScoresDisplay();
                }
                
                @Override
                public void onMatchSaved(String matchName, int totalHomeScore, int totalAwayScore) {
                    System.out.println("比赛保存: " + matchName + " " + totalHomeScore + "-" + totalAwayScore);
                }
            });
            
            // 初始化蓝牙
            if (bluetoothManager.initializeBluetooth()) {
                bluetoothManager.startScan();
            }
            
            System.out.println("蓝牙管理器已初始化");
        } catch (Exception e) {
            System.err.println("蓝牙管理器初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 更新龙都F4得分
     */
    private void updateHomeScore(int points) {
        homeScore += points;
        if (homeScore < 0) homeScore = 0;
        
        // 更新UI显示 - 中间区域显示当前节次比分
        if (homeScoreLabel != null) {
            homeScoreLabel.setText(String.valueOf(homeScore));
        }
        

        // 检查游戏是否结束
        checkGameEnd();
        
        System.out.println("龙都F4当前节次得分更新: " + homeScore);
    }
    
    /**
     * 更新暴风队得分
     */
    private void updateAwayScore(int points) {
        awayScore += points;
        if (awayScore < 0) awayScore = 0;
        
        // 更新UI显示 - 中间区域显示当前节次比分
        if (awayScoreLabel != null) {
            awayScoreLabel.setText(String.valueOf(awayScore));
        }
        

        // 检查游戏是否结束
        checkGameEnd();
        
        System.out.println("暴风队当前节次得分更新: " + awayScore);
    }
    
    /**
     * 设置龙都F4名称
     */
    private void setHomeTeam(String name) {
        homeTeam = name;
        homeTeamLabel.setText(name);
        System.out.println("龙都F4名称: " + name);
    }
    
    /**
     * 设置暴风队名称
     */
    private void setAwayTeam(String name) {
        awayTeam = name;
        awayTeamLabel.setText(name);
        System.out.println("暴风队名称: " + name);
    }
    
    /**
     * 开始倒计时
     */
    private void startCountdown() {
        countdownTime = 15; // 每次从15秒开始
        savedCountdownTime = countdownTime;
        isCountdownMode = true;
        isCountdownRunning = true;
        showCountdownPanel();
        countdownTimer.start();
        System.out.println("开始倒计时: " + countdownTime + "秒");
        
        // 播报开始信息
        WindowsTTS.speakAsync("开始倒计时，15秒");
    }
    
    /**
     * 继续倒计时
     */
    private void continueCountdown() {
        if (savedCountdownTime > 0) {
            countdownTime = savedCountdownTime;
            isCountdownMode = true;
            isCountdownRunning = true;
            showCountdownPanel();
            countdownTimer.start();
            System.out.println("继续倒计时: " + countdownTime + "秒");
            
            // 播报继续信息
            WindowsTTS.speakAsync("继续倒计时，" + countdownTime + "秒");
        }
    }
    
    /**
     * 停止倒计时
     */
    private void stopCountdown() {
        if (countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        isCountdownRunning = false;
        savedCountdownTime = countdownTime; // 保存当前时间
        System.out.println("停止倒计时，保存时间: " + savedCountdownTime + "秒");
    }
    
    /**
     * 暂停倒计时
     */
    private void pauseCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
            isCountdownRunning = false;
            savedCountdownTime = countdownTime; // 保存当前时间
            System.out.println("暂停倒计时，保存时间: " + savedCountdownTime + "秒");
            
            // 播报暂停信息
            WindowsTTS.speakAsync("倒计时已暂停");
        }
    }
    
    /**
     * 重置比分
     */
    private void resetScore() {
        // 重置当前节次比分（中间区域显示）
        homeScore = 0;
        awayScore = 0;
        homeScoreLabel.setText("0");
        awayScoreLabel.setText("0");
        
        System.out.println("当前节次比分已重置");
        
        // 播报重置信息
        WindowsTTS.speakAsync("当前节次比分已重置");
    }
    
    /**
     * 重置倒计时
     */
    private void resetCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTime = 15;
        savedCountdownTime = 15;
        isCountdownRunning = false;
        countdownLabel.setText(String.valueOf(countdownTime));
        countdownLabel.setForeground(COUNTDOWN_COLOR);
        System.out.println("重置倒计时");
        
        // 播报重置信息
        WindowsTTS.speakAsync("倒计时已重置");
    }
    
    /**
     * 更新倒计时
     */
    private void updateCountdown() {
        if (countdownTime > 0) {
            countdownTime--;
            countdownLabel.setText(String.valueOf(countdownTime));
            
            // 时间小于等于6秒时变红色并播报
            if (countdownTime <= 6) {
                countdownLabel.setForeground(COUNTDOWN_WARNING_COLOR);
                playCountdownSound(countdownTime);
            }
            
            // 更新保存的时间
            savedCountdownTime = countdownTime;
            
        } else {
            // 时间到0
            countdownTimer.stop();
            isCountdownRunning = false;
            playCountdownSound(0); // 播报"进攻结束"
            
            // 2秒后返回比分页面
            Timer delayTimer = new Timer(2000, e -> {
                showScorePanel();
                ((Timer)e.getSource()).stop();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
    }
    
    /**
     * 播放倒计时音效和语音
     */
    private void playCountdownSound(int time) {
        if (time > 0) {
            System.out.println("播报: " + time);
            // 使用 WindowsTTS 播报数字
            WindowsTTS.speakAsync(String.valueOf(time));
        } else {
            System.out.println("播报: 进攻结束");
            // 使用 WindowsTTS 播报"进攻结束"
            WindowsTTS.speakAsync("进攻结束");
        }
    }
    
    /**
     * 显示比分面板
     */
    private void showScorePanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "score");
        isCountdownMode = false;
        System.out.println("切换到比分页面");
    }
    
    /**
     * 显示倒计时面板
     */
    private void showCountdownPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "countdown");
        isCountdownMode = true;
        System.out.println("切换到倒计时页面");
    }
    
    /**
     * 创建比赛信息面板
     */
    private JPanel createMatchInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 比赛基本信息
        JPanel basicInfoPanel = new JPanel();
        basicInfoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        basicInfoPanel.setBackground(BACKGROUND_COLOR);
        
        matchNameLabel = new JLabel("比赛名称：未设置");
        matchNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        matchNameLabel.setForeground(TEXT_COLOR);
        
        matchNoteLabel = new JLabel("备注：未设置");
        matchNoteLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        matchNoteLabel.setForeground(TEXT_COLOR);
        
        basicInfoPanel.add(matchNameLabel);
        basicInfoPanel.add(matchNoteLabel);
        
        // 每节比分显示
        JPanel quarterScoresPanel = new JPanel();
        quarterScoresPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        quarterScoresPanel.setBackground(BACKGROUND_COLOR);
        
        quarter1Label = new JLabel("第1节: 0-0");
        quarter2Label = new JLabel("第2节: 0-0");
        quarter3Label = new JLabel("第3节: 0-0");
        quarter4Label = new JLabel("第4节: 0-0");
        quarter5Label = new JLabel("第5节: 0-0");
        quarter6Label = new JLabel("第6节: 0-0");
        quarter7Label = new JLabel("第7节: 0-0");
        
        // 设置标签样式
        Font quarterFont = new Font("微软雅黑", Font.BOLD, 16);
        Color quarterColor = new Color(200, 200, 200);
        
        quarter1Label.setFont(quarterFont);
        quarter1Label.setForeground(quarterColor);
        quarter2Label.setFont(quarterFont);
        quarter2Label.setForeground(quarterColor);
        quarter3Label.setFont(quarterFont);
        quarter3Label.setForeground(quarterColor);
        quarter4Label.setFont(quarterFont);
        quarter4Label.setForeground(quarterColor);
        quarter5Label.setFont(quarterFont);
        quarter5Label.setForeground(quarterColor);
        quarter6Label.setFont(quarterFont);
        quarter6Label.setForeground(quarterColor);
        quarter7Label.setFont(quarterFont);
        quarter7Label.setForeground(quarterColor);
        
        quarterScoresPanel.add(quarter1Label);
        quarterScoresPanel.add(quarter2Label);
        quarterScoresPanel.add(quarter3Label);
        quarterScoresPanel.add(quarter4Label);
        quarterScoresPanel.add(quarter5Label);
        quarterScoresPanel.add(quarter6Label);
        quarterScoresPanel.add(quarter7Label);
        
        // 总比分显示
        JPanel totalScorePanel = new JPanel();
        totalScorePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        totalScorePanel.setBackground(BACKGROUND_COLOR);
        
        totalScoreLabel = new JLabel("总比分：0 - 0");
        totalScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        totalScoreLabel.setForeground(new Color(255, 215, 0)); // 金色
        
        totalScorePanel.add(totalScoreLabel);
        
        // 将面板添加到主面板
        panel.add(basicInfoPanel, BorderLayout.NORTH);
        panel.add(quarterScoresPanel, BorderLayout.CENTER);
        panel.add(totalScorePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 键盘事件处理（ESC退出全屏）
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    /**
     * 清理资源
     */
    private void cleanup() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        if (bluetoothManager != null) {
            bluetoothManager.cleanup();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            BasketballFullScreenDisplay display = new BasketballFullScreenDisplay();
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(display::cleanup));
        });
    }

    /**
     * 处理蓝牙命令
     */
    private void processBluetoothCommand(String command) {
        try {
            System.out.println("收到原始命令: " + command);
            
            // 处理连续的命令字符串，如 "HOME_SCORE:1 SAVE_QUARTER:1|13|19"
            // 使用空格分割命令，然后分别处理
            String[] commands = command.split(" ");
            for (String singleCommand : commands) {
                if (!singleCommand.trim().isEmpty()) {
                    // 按冒号分割命令和值
                    String[] parts = singleCommand.split(":", 2);
                    if (parts.length >= 2) {
                        processSingleCommand(parts[0].trim(), parts[1].trim());
                    } else if (parts.length == 1) {
                        processSingleCommand(parts[0].trim(), "");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("命令解析错误: " + command + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理单个命令
     */
    private void processSingleCommand(String cmdName, String value) {
        System.out.println("解析命令: " + cmdName + " = " + value);
        
        try {
                    switch (cmdName) {
                            case "NEW_MATCH":
                                handleNewMatch(value);
                                break;
                            case "SAVE_QUARTER":
                                handleSaveQuarter(value);
                                break;
                            case "SAVE_MATCH":
                                handleSaveMatch(value);
                                break;
                            case "QUARTER":
                                handleQuarterChange(value);
                                break;
                            case "HOME_SCORE":
                                int homePoints = Integer.parseInt(value);
                                updateHomeScore(homePoints);
                                showScorePanel();
                                break;
                            case "AWAY_SCORE":
                                int awayPoints = Integer.parseInt(value);
                                updateAwayScore(awayPoints);
                                showScorePanel();
                                break;
                            case "HOME_TEAM":
                                setHomeTeam(value);
                                showScorePanel();
                                break;
                            case "AWAY_TEAM":
                                setAwayTeam(value);
                                showScorePanel();
                                break;
                            case "START_COUNTDOWN":
                                startCountdown();
                                break;
                            case "CONTINUE_COUNTDOWN":
                                continueCountdown();
                                break;
                            case "STOP_COUNTDOWN":
                                stopCountdown();
                                showScorePanel();
                                break;
                            case "RESET_COUNTDOWN":
                                resetCountdown();
                                break;
                            case "RESET_SCORE":
                                resetScore();
                                showScorePanel();
                                break;
                            case "GET_ALL_MATCHES":
                                handleGetAllMatches(); // 不需要参数
                                break;
                            case "SELECT_MATCH":
                                handleSelectMatch(value);
                                break;
                            case "SYNC_ALL_SCORES":
                                handleSyncAllScores(); // 不需要参数
                                break;
                            case "SELECT_PREVIOUS_QUARTER":
                                handleSelectPreviousQuarter(value);
                                break;
                            case "UPDATE_PREVIOUS_QUARTER":
                                handleUpdatePreviousQuarter(value);
                                break;
                            default:
                                System.out.println("未知命令: " + cmdName);
                        }
        } catch (NumberFormatException e) {
            System.err.println("数值解析错误 - 命令: " + cmdName + ", 值: " + value + ", 错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("处理命令错误 - 命令: " + cmdName + ", 值: " + cmdName + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理节次变更命令
     */
    private void handleQuarterChange(String value) {
        try {
            int newQuarter = Integer.parseInt(value);
            if (newQuarter >= 1 && newQuarter <= 7) {
                // 保存当前节次的比分到数据库
                if (databaseManager != null && currentMatchId > 0) {
                    try {
                        boolean success = databaseManager.saveQuarterScore(currentMatchId, currentQuarter, homeScore, awayScore);
                        if (success) {
                            System.out.println("第" + currentQuarter + "节比分保存成功: " + homeScore + ":" + awayScore);
                            
                            // 更新节次比分显示
                            updateQuarterScoresDisplay();
                            
                            // 播报保存成功信息
                            WindowsTTS.speakAsync("第" + currentQuarter + "节比分已保存");
                        } else {
                            System.err.println("第" + currentQuarter + "节比分保存失败");
                            WindowsTTS.speakAsync("第" + currentQuarter + "节比分保存失败");
                        }
                    } catch (Exception e) {
                        System.err.println("保存节次比分时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("无法保存节次比分：数据库管理器未初始化或当前比赛ID无效");
                }
                
                // 更新当前节次
                currentQuarter = newQuarter;
                
                // 从数据库获取该节次的历史比分
                if (databaseManager != null && currentMatchId > 0) {
                    try {
                        DatabaseManager.QuarterScore quarterScore = databaseManager.getQuarterScore(currentMatchId, currentQuarter);
                        if (quarterScore != null) {
                            // 恢复历史比分
                            homeScore = quarterScore.getHomeScore();
                            awayScore = quarterScore.getAwayScore();
                            System.out.println("恢复第" + currentQuarter + "节历史比分: " + homeScore + ":" + awayScore);
                        } else {
                            // 如果没有历史记录，重置为0
                            homeScore = 0;
                            awayScore = 0;
                            System.out.println("第" + currentQuarter + "节无历史记录，重置为0:0");
                        }
                    } catch (Exception e) {
                        System.err.println("获取历史比分时出错: " + e.getMessage());
                        // 出错时重置为0
                        homeScore = 0;
                        awayScore = 0;
                    }
                } else {
                    // 数据库不可用时重置为0
                    homeScore = 0;
                    awayScore = 0;
                }
                
                // 更新UI显示 - 中间区域显示当前节次比分
                if (homeScoreLabel != null) {
                    homeScoreLabel.setText(String.valueOf(homeScore));
                }
                if (awayScoreLabel != null) {
                    awayScoreLabel.setText(String.valueOf(awayScore));
                }
                
                // 更新当前节次标签显示
                if (currentQuarterLabel != null) {
                    currentQuarterLabel.setText("第" + currentQuarter + "节");
                }
                
                // 更新节次显示
                updateQuarterScoresDisplay();
                

                // 确保当前节次标签高亮显示
                updateQuarterLabels();
                
                // 播报节次变更
                WindowsTTS.speakAsync("进入第" + currentQuarter + "节");
                
                System.out.println("节次已变更到第" + currentQuarter + "节，比分已重置");
                
            } else {
                System.err.println("无效的节次值: " + value);
            }
        } catch (NumberFormatException e) {
            System.err.println("节次值格式错误: " + value);
        } catch (Exception e) {
            System.err.println("处理节次变更失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理保存节次命令
     */
    private void handleSaveQuarter(String value) {
        if (databaseManager != null) {
            try {
                String[] parts = value.split("\\|");
                if (parts.length >= 3) {
                    int quarter = Integer.parseInt(parts[0]);
                    int homeScore = Integer.parseInt(parts[1]);
                    int awayScore = Integer.parseInt(parts[2]);
                    
                    databaseManager.saveQuarterScore(currentMatchId, quarter, homeScore, awayScore);
                    System.out.println("处理保存节次命令: 第" + quarter + "节 " + homeScore + "-" + awayScore);
                }
            } catch (Exception e) {
                System.err.println("处理保存节次命令失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 处理新建比赛命令
     */
    private void handleNewMatch(String value) {
        try {
            String[] parts = value.split("\\|");
            if (parts.length >= 2) {
                String matchName = parts[0];
                String matchNote = parts[1];
                
                // 创建新比赛
                if (databaseManager != null) {
                    currentMatchId = databaseManager.createNewMatch(matchName, matchNote);
                    currentMatchName = matchName;
                    currentMatchNote = matchNote;
                    
                    // 重置比赛数据
                    currentQuarter = 1;
                    homeScore = 0;
                    awayScore = 0;
                    totalHomeScore = 0;
                    totalAwayScore = 0;
                    
                    // 更新UI显示
                    updateMatchInfoDisplay();
                    updateQuarterScoresDisplay();

                    // 重置比分显示
                    if (homeScoreLabel != null) {
                        homeScoreLabel.setText("0");
                    }
                    if (awayScoreLabel != null) {
                        awayScoreLabel.setText("0");
                    }
                    if (currentQuarterLabel != null) {
                        currentQuarterLabel.setText("第1节");
                    }
                    
                    System.out.println("新建比赛: " + matchName + ", 备注: " + matchNote);
                    WindowsTTS.speakAsync("新建比赛：" + matchName);
                }
            }
        } catch (Exception e) {
            System.err.println("处理新建比赛命令失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理选择比赛命令
     */
    private void handleSelectMatch(String value) {
        try {
            int matchId = Integer.parseInt(value);
            System.out.println("收到选择比赛命令，比赛ID: " + matchId);
            
            // 设置当前比赛ID
            currentMatchId = matchId;
            
            // 从数据库获取比赛信息
            if (databaseManager != null) {
                DatabaseManager.MatchInfo matchInfo = databaseManager.getMatchInfo(matchId);
                if (matchInfo != null) {
                    currentMatchName = matchInfo.getMatchName();
                    currentMatchNote = matchInfo.getMatchNote();
                    
                    // 获取该比赛的所有节次比分
                    List<DatabaseManager.QuarterScore> quarterScores = databaseManager.getQuarterScores(matchId);
                    
                    // 更新UI显示
                    updateMatchInfoDisplay();
                    updateQuarterScoresDisplay();
                    
                    // 播报选择比赛信息
                    WindowsTTS.speakAsync("已选择比赛：" + currentMatchName);
                    
                    System.out.println("已选择比赛: " + currentMatchName);
                }
            }
            
        } catch (NumberFormatException e) {
            System.err.println("比赛ID格式错误: " + value);
        } catch (Exception e) {
            System.err.println("处理选择比赛命令失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理获取所有比赛命令
     */
    private void handleGetAllMatches() {
        if (databaseManager != null) {
            try {
                System.out.println("收到获取所有比赛命令");
                
                // 从数据库获取所有比赛
                List<DatabaseManager.MatchInfo> matches = databaseManager.getAllMatches();
                
                // 发送比赛列表到手机端
                if (matches != null && !matches.isEmpty()) {
                    for (DatabaseManager.MatchInfo match : matches) {
                        String matchData = "MATCH_INFO:" + match.getId() + "|" + 
                                         match.getMatchName() + "|" + 
                                         match.getMatchNote() + "|" + 
                                         match.getTotalHomeScore() + "|" + 
                                         match.getTotalAwayScore() + "|" + 
                                         match.getCreatedAt();
                        bluetoothManager.sendDataToMobile(matchData);
                    }
                    
                    // 发送结束标记
                    bluetoothManager.sendDataToMobile("MATCH_LIST_END");
                    System.out.println("已发送" + matches.size() + "场比赛信息到手机端");
                } else {
                    bluetoothManager.sendDataToMobile("NO_MATCHES_FOUND");
                    System.out.println("没有找到已存在的比赛");
                }
                
            } catch (Exception e) {
                System.err.println("处理获取所有比赛命令失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("数据库管理器未初始化");
        }
    }
    
    /**
     * 处理保存比赛命令
     */
    private void handleSaveMatch(String value) {
        if (databaseManager != null) {
            try {
                String[] parts = value.split("\\|");
                if (parts.length >= 3) {
                    String matchName = parts[0];
                    int totalHomeScore = Integer.parseInt(parts[1]);
                    int totalAwayScore = Integer.parseInt(parts[2]);
                    updateTotalScoreDisplay(totalHomeScore, totalAwayScore);
                    // 保存比赛总分到数据库
                    if (databaseManager != null && currentMatchId > 0) {
                        try {
                            // 计算总比分
                            int calculatedTotalHomeScore = 0;
                            int calculatedTotalAwayScore = 0;
                            
                            // 从数据库获取所有节次比分并计算总和
                            List<DatabaseManager.QuarterScore> quarterScores = databaseManager.getQuarterScores(currentMatchId);
                            for (DatabaseManager.QuarterScore quarterScore : quarterScores) {
                                calculatedTotalHomeScore += quarterScore.getHomeScore();
                                calculatedTotalAwayScore += quarterScore.getAwayScore();
                            }
                            
                            // 更新比赛信息显示
                            updateMatchInfoDisplay();
                            
                            System.out.println("比赛总分计算完成: " + calculatedTotalHomeScore + ":" + calculatedTotalAwayScore);
                            WindowsTTS.speakAsync("比赛总分计算完成，龙都F4：" + calculatedTotalHomeScore + "分，暴风队：" + calculatedTotalAwayScore + "分");
                            
                        } catch (Exception e) {
                            System.err.println("计算比赛总分时出错: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("无法计算比赛总分：数据库管理器未初始化或当前比赛ID无效");
                    }
                    System.out.println("处理保存比赛命令: " + totalHomeScore + "-" + totalAwayScore);
                }
            } catch (Exception e) {
                System.err.println("处理保存比赛命令失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 处理同步所有分数命令
     */
    private void handleSyncAllScores() {
        if (databaseManager != null && currentMatchId > 0) {
            try {
                System.out.println("开始同步所有分数，当前比赛ID: " + currentMatchId);
                
                // 从数据库读取当前比赛的所有节次比分
                List<DatabaseManager.QuarterScore> quarterScores = databaseManager.getQuarterScores(currentMatchId);
                
                if (quarterScores != null && !quarterScores.isEmpty()) {
                    // 计算总分
                    int totalHomeScore = 0;
                    int totalAwayScore = 0;
                    
                    for (DatabaseManager.QuarterScore qs : quarterScores) {
                        totalHomeScore += qs.getHomeScore();
                        totalAwayScore += qs.getAwayScore();
                        System.out.println("第" + qs.getQuarterNumber() + "节: " + qs.getHomeScore() + "-" + qs.getAwayScore());
                    }
                    
                    // 注意：这里不应该更新当前节次比分，因为中间区域应该显示当前节次比分
                    // 累计总分应该在顶部面板显示，由updateTotalScoreDisplay()处理
                    
                    // 中间区域保持显示当前节次比分，不显示累计总分
                    System.out.println("同步完成，累计总分: " + totalHomeScore + "-" + totalAwayScore + 
                                     "，当前节次比分: " + this.homeScore + "-" + this.awayScore);
                    
                    // 更新节次比分显示
                    updateQuarterScoresDisplay();
                    
                    // 播报同步信息
                    WindowsTTS.speakAsync("分数已同步，当前比分 " + totalHomeScore + " 比 " + totalAwayScore);
                    System.out.println("同步完成，总分: " + totalHomeScore + "-" + totalAwayScore);
                    
                    // 向手机端发送同步后的数据，确保数据一致
                    sendSyncDataToMobile(totalHomeScore, totalAwayScore);
                    
                    // 显示比分面板
                    showScorePanel();
                } else {
                    System.out.println("没有找到节次比分数据");
                    WindowsTTS.speakAsync("没有找到比分数据");
                }
                
            } catch (Exception e) {
                System.err.println("处理同步所有分数命令失败: " + e.getMessage());
                e.printStackTrace();
                WindowsTTS.speakAsync("同步分数失败");
            }
        } else {
            System.err.println("数据库管理器未初始化或当前比赛ID无效");
            WindowsTTS.speakAsync("无法同步，请先创建比赛");
        }
    }
    
    /**
     * 向手机端发送同步后的数据，确保数据一致
     */
    private void sendSyncDataToMobile(int totalHomeScore, int totalAwayScore) {
        if (bluetoothManager != null && bluetoothManager.isConnected()) {
            try {
                // 发送当前总分到手机端
                String syncData = "SYNC_RESULT:" + totalHomeScore + "|" + totalAwayScore;
                bluetoothManager.sendDataToMobile(syncData);
                System.out.println("向手机端发送同步结果: " + syncData);
                
                // 发送所有节次比分详情
                if (quarterScores != null && !quarterScores.isEmpty()) {
                    for (DatabaseManager.QuarterScore qs : quarterScores) {
                        String quarterData = "QUARTER_DATA:" + qs.getQuarterNumber() + "|" + 
                                           qs.getHomeScore() + "|" + qs.getAwayScore();
                        bluetoothManager.sendDataToMobile(quarterData);
                        System.out.println("向手机端发送节次数据: " + quarterData);
                    }
                }
                
                // 发送同步完成确认
                bluetoothManager.sendDataToMobile("SYNC_COMPLETE");
                System.out.println("向手机端发送同步完成确认");
                
            } catch (Exception e) {
                System.err.println("向手机端发送同步数据失败: " + e.getMessage());
            }
        } else {
            System.out.println("蓝牙未连接，无法向手机端发送同步数据");
        }
    }
    
    /**
     * 处理选择上一节命令
     */
    private void handleSelectPreviousQuarter(String value) {
        try {
            String[] parts = value.split("\\|");
            if (parts.length >= 2) {
                int matchId = Integer.parseInt(parts[0]);
                int quarterNumber = Integer.parseInt(parts[1]);
                
                System.out.println("收到选择上一节命令，比赛ID: " + matchId + "，节次: " + quarterNumber);
                
                // 从数据库获取该节次比分
                if (databaseManager != null) {
                    DatabaseManager.QuarterScore quarterScore = databaseManager.getQuarterScore(matchId, quarterNumber);
                    if (quarterScore != null) {
                        // 更新当前节次变量
                        currentQuarter = quarterNumber;
                        
                        // 显示该节次比分
                        homeScore = quarterScore.getHomeScore();
                        awayScore = quarterScore.getAwayScore();
                        
                        // 更新比分显示
                        if (homeScoreLabel != null) {
                            homeScoreLabel.setText(String.valueOf(homeScore));
                        }
                        if (awayScoreLabel != null) {
                            awayScoreLabel.setText(String.valueOf(awayScore));
                        }
                        if (currentQuarterLabel != null) {
                            currentQuarterLabel.setText("第" + quarterNumber + "节");
                        }
                        
                        // 更新节次比分显示
                        updateQuarterScoresDisplay();
                        
                        // 播报选择上一节信息
                        WindowsTTS.speakAsync("已选择第" + quarterNumber + "节，比分：" + homeScore + "比" + awayScore);
                        
                        System.out.println("已选择第" + quarterNumber + "节，比分：" + homeScore + ":" + awayScore);
                    } else {
                        System.err.println("未找到第" + quarterNumber + "节的比分记录");
                        WindowsTTS.speakAsync("未找到第" + quarterNumber + "节的比分记录");
                    }
                }
            }
            
        } catch (NumberFormatException e) {
            System.err.println("节次信息格式错误: " + value);
        } catch (Exception e) {
            System.err.println("处理选择上一节命令失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理修改上一节比分命令
     */
    private void handleUpdatePreviousQuarter(String value) {
        try {
            String[] parts = value.split("\\|");
            if (parts.length >= 4) {
                int matchId = Integer.parseInt(parts[0]);
                int quarterNumber = Integer.parseInt(parts[1]);
                int newHomeScore = Integer.parseInt(parts[2]);
                int newAwayScore = Integer.parseInt(parts[3]);
                
                System.out.println("收到修改上一节比分命令，比赛ID: " + matchId + "，节次: " + quarterNumber + 
                                 "，新比分：" + newHomeScore + ":" + newAwayScore);
                
                // 更新数据库中的比分
                if (databaseManager != null) {
                    boolean success = databaseManager.updateQuarterScore(matchId, quarterNumber, newHomeScore, newAwayScore);
                    if (success) {
                        // 更新当前显示
                        homeScore = newHomeScore;
                        awayScore = newAwayScore;
                        
                        // 更新比分显示
                        if (homeScoreLabel != null) {
                            homeScoreLabel.setText(String.valueOf(homeScore));
                        }
                        if (awayScoreLabel != null) {
                            awayScoreLabel.setText(String.valueOf(awayScore));
                        }
                        
                        // 更新节次比分显示
                        updateQuarterScoresDisplay();
                        
                        // 播报修改成功信息
                        WindowsTTS.speakAsync("第" + quarterNumber + "节比分已修改为：" + newHomeScore + "比" + newAwayScore);
                        
                        System.out.println("第" + quarterNumber + "节比分修改成功：" + newHomeScore + ":" + newAwayScore);
                    } else {
                        System.err.println("第" + quarterNumber + "节比分修改失败");
                        WindowsTTS.speakAsync("第" + quarterNumber + "节比分修改失败");
                    }
                }
            }
            
        } catch (NumberFormatException e) {
            System.err.println("比分信息格式错误: " + value);
        } catch (Exception e) {
            System.err.println("处理修改上一节比分命令失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 更新比赛信息显示
     */
    private void updateMatchInfoDisplay() {
        if (matchNameLabel != null) {
            matchNameLabel.setText("比赛名称：" + (currentMatchName.isEmpty() ? "未设置" : currentMatchName));
        }
        if (matchNoteLabel != null) {
            matchNoteLabel.setText("备注：" + (currentMatchNote.isEmpty() ? "未设置" : currentMatchNote));
        }
        System.out.println("更新比赛信息显示: " + currentMatchName);
    }
    
    /**
     * 更新节次比分显示
     */
    private void updateQuarterScoresDisplay() {
        try {
            if (currentMatchId > 0) {
                quarterScores = databaseManager.getQuarterScores(currentMatchId);
                System.out.println("更新节次比分显示，共" + quarterScores.size() + "节");
                
                // 更新每节比分标签
                updateQuarterLabels();
                
                // 更新当前节次比分显示（中间区域）
                updateCurrentQuarterScoreDisplay();
            }
        } catch (Exception e) {
            System.err.println("更新节次比分显示失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新每节比分标签
     */
    private void updateQuarterLabels() {
        // 重置所有标签
        if (quarter1Label != null) quarter1Label.setText("第1节: 0-0");
        if (quarter2Label != null) quarter2Label.setText("第2节: 0-0");
        if (quarter3Label != null) quarter3Label.setText("第3节: 0-0");
        if (quarter4Label != null) quarter4Label.setText("第4节: 0-0");
        if (quarter5Label != null) quarter5Label.setText("第5节: 0-0");
        if (quarter6Label != null) quarter6Label.setText("第6节: 0-0");
        if (quarter7Label != null) quarter7Label.setText("第7节: 0-0");
        
        // 根据数据库数据更新标签
        for (DatabaseManager.QuarterScore quarterScore : quarterScores) {
            int quarter = quarterScore.getQuarterNumber();
            int homeScore = quarterScore.getHomeScore();
            int awayScore = quarterScore.getAwayScore();
            
            JLabel targetLabel = null;
            switch (quarter) {
                case 1: targetLabel = quarter1Label; break;
                case 2: targetLabel = quarter2Label; break;
                case 3: targetLabel = quarter3Label; break;
                case 4: targetLabel = quarter4Label; break;
                case 5: targetLabel = quarter5Label; break;
                case 6: targetLabel = quarter6Label; break;
                case 7: targetLabel = quarter7Label; break;
            }
            
            if (targetLabel != null) {
                targetLabel.setText("第" + quarter + "节: " + homeScore + "-" + awayScore);
                // 高亮当前节次
                if (quarter == currentQuarter) {
                    targetLabel.setForeground(new Color(255, 215, 0)); // 金色
                    System.out.println("高亮第" + quarter + "节为当前节次");
                } else {
                    targetLabel.setForeground(new Color(200, 200, 200)); // 灰色
                }
            }
        }
    }
    
    /**
     * 更新当前节次比分显示（中间区域）
     */
    private void updateCurrentQuarterScoreDisplay() {
        try {
            if (currentMatchId > 0 && currentQuarter > 0) {
                // 查找当前节次的比分
                int currentQuarterHomeScore = 0;
                int currentQuarterAwayScore = 0;
                
                for (DatabaseManager.QuarterScore quarterScore : quarterScores) {
                    if (quarterScore.getQuarterNumber() == currentQuarter) {
                        currentQuarterHomeScore = quarterScore.getHomeScore();
                        currentQuarterAwayScore = quarterScore.getAwayScore();
                        break;
                    }
                }
                
                // 更新中间区域的比分显示
                if (quarter1Label != null && quarter2Label != null && quarter3Label != null && quarter4Label != null &&
                    quarter5Label != null && quarter6Label != null && quarter7Label != null) {
                    // 高亮当前节次标签
                    JLabel currentLabel = null;
                    switch (currentQuarter) {
                        case 1: currentLabel = quarter1Label; break;
                        case 2: currentLabel = quarter2Label; break;
                        case 3: currentLabel = quarter3Label; break;
                        case 4: currentLabel = quarter4Label; break;
                        case 5: currentLabel = quarter5Label; break;
                        case 6: currentLabel = quarter6Label; break;
                        case 7: currentLabel = quarter7Label; break;
                    }
                    if (currentLabel != null) {
                        currentLabel.setForeground(new Color(255, 215, 0)); // 金色高亮
                    }
                }
                
                System.out.println("更新当前节次比分显示: 第" + currentQuarter + "节 " + currentQuarterHomeScore + "-" + currentQuarterAwayScore);
            }
        } catch (Exception e) {
            System.err.println("更新当前节次比分显示失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新总比分显示
     */
    private void updateTotalScoreDisplay(int totalHomeScoreP, int totalAwayScoreP) {
        try {
            if (currentMatchId > 0) {
                // 优先使用当前内存中的比分，确保数据一致性
                if (totalHomeScoreP > 0 || totalAwayScoreP > 0) {
                    totalHomeScore = totalHomeScoreP;
                    totalAwayScore = totalAwayScoreP;
                    
                    if (totalScoreLabel != null) {
                        totalScoreLabel.setText("总比分：" + totalHomeScore + " - " + totalAwayScore);
                    }
                    
                    System.out.println("使用当前比分更新总比分显示: " + totalHomeScore + "-" + totalAwayScore);
                } else {
                    // 如果当前比分为0，则从数据库读取
                    DatabaseManager.MatchInfo matchInfo = databaseManager.getMatchInfo(currentMatchId);
                    if (matchInfo != null) {
                        totalHomeScore = matchInfo.getTotalHomeScore();
                        totalAwayScore = matchInfo.getTotalAwayScore();
                        
                        if (totalScoreLabel != null) {
                            totalScoreLabel.setText("总比分：" + totalHomeScore + " - " + totalAwayScore);
                        }
                        
                        System.out.println("从数据库更新总比分显示: " + totalHomeScore + "-" + totalAwayScore);
                    }
                }
                
                // 检查是否达到100分结束条件
                checkGameEnd();
            }
        } catch (Exception e) {
            System.err.println("更新总比分显示失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查比赛是否结束（总分达到100分）
     */
    private void checkGameEnd() {
        if (totalHomeScore >= 100 || totalAwayScore >= 100) {
            String winner = totalHomeScore >= 100 ? "龙都F4" : "暴风队";
            String message = winner + "获胜！比赛结束！";
            
            // 语音播报
            WindowsTTS.speakAsync(message);
            
            // 显示消息
            JOptionPane.showMessageDialog(this, message, "比赛结束", JOptionPane.INFORMATION_MESSAGE);
            
            System.out.println("比赛结束：" + message);
        }
    }
} 