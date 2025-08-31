package com.basketball.scoreremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements BluetoothManager.BluetoothCallback, BluetoothManager.DataCallback {
    
    // UI组件
    private TextView tvBluetoothStatus;
    private Button btnConnect;
    private TextView tvHomeScore;
    private TextView tvAwayScore;
    private TextView tvQuarter;
    private Button btnHomePlus1, btnHomePlus2, btnHomePlus3, btnHomeMinus1;
    private Button btnAwayPlus1, btnAwayPlus2, btnAwayPlus3, btnAwayMinus1;
    private Button btnResetScore, btnTimeout, btnStartTimer, btnPauseTimer, btnContinueTimer, btnNextQuarter, btnPreviousQuarter;
    
    // 比赛管理UI组件
    private View layoutMatchInfo;
    private TextView tvMatchName, tvMatchNote, tvCurrentQuarter, tvQuarterScore, tvTotalScore;
    private Button btnNewMatch, btnSaveQuarter, btnSaveMatch, btnSyncAllScores;
    
    // 上一节比分管理
    private Button btnViewPreviousQuarter, btnEditPreviousQuarter;
    private int previousQuarterHomeScore = 0;
    private int previousQuarterAwayScore = 0;
    
    private BluetoothManager bluetoothManager;
    
    // 计分数据
    private int homeScore = 0;
    private int awayScore = 0;
    private int currentQuarter = 1;
    
    // 比赛管理数据
    private String matchName = "";
    private String matchNote = "";
    private int totalHomeScore = 0;
    private int lastTotalHomeScore = 0;
    private int totalAwayScore = 0;
    private int lastTotalAwayScore = 0;
    private boolean isMatchStarted = false;
    
    // 倒计时状态管理
    private boolean isTimerRunning = false;
    private boolean isTimerPaused = false;
    
    // 当前比赛ID
    private int currentMatchId = -1;
    
    // 节次比分历史记录
    private java.util.Map<Integer, QuarterScore> quarterHistory = new java.util.HashMap<>();
    
    // 节次比分数据类
    private static class QuarterScore {
        int homeScore;
        int awayScore;
        
        QuarterScore(int homeScore, int awayScore) {
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }
    }
    
    // 权限请求
    private final ActivityResultLauncher<String[]> bluetoothPermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), 
            (Map<String, Boolean> permissions) -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    initializeBluetooth();
                } else {
                    showToast("需要蓝牙权限才能使用此功能");
                }
            });
    
    // 启用蓝牙请求
    private final ActivityResultLauncher<Intent> enableBluetoothLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    initializeBluetooth();
                } else {
                    showToast("需要启用蓝牙才能使用此功能");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupBluetoothManager();
        setupUI();
        checkBluetoothPermissions();
    }
    
    private void initViews() {
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        btnConnect = findViewById(R.id.btnConnect);
        tvHomeScore = findViewById(R.id.tvHomeScore);
        tvAwayScore = findViewById(R.id.tvAwayScore);
        tvQuarter = findViewById(R.id.tvQuarter);
        
        btnHomePlus1 = findViewById(R.id.btnHomePlus1);
        btnHomePlus2 = findViewById(R.id.btnHomePlus2);
        btnHomePlus3 = findViewById(R.id.btnHomePlus3);
        btnHomeMinus1 = findViewById(R.id.btnHomeMinus1);
        
        btnAwayPlus1 = findViewById(R.id.btnAwayPlus1);
        btnAwayPlus2 = findViewById(R.id.btnAwayPlus2);
        btnAwayPlus3 = findViewById(R.id.btnAwayPlus3);
        btnAwayMinus1 = findViewById(R.id.btnAwayMinus1);
        
        btnResetScore = findViewById(R.id.btnResetScore);
        btnTimeout = findViewById(R.id.btnTimeout);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnPauseTimer = findViewById(R.id.btnPauseTimer);
        btnContinueTimer = findViewById(R.id.btnContinueTimer);
        btnNextQuarter = findViewById(R.id.btnNextQuarter);
        btnPreviousQuarter = findViewById(R.id.btnPreviousQuarter);
        
        // 比赛管理UI组件
        layoutMatchInfo = findViewById(R.id.layoutMatchInfo);
        tvMatchName = findViewById(R.id.tvMatchName);
        tvMatchNote = findViewById(R.id.tvMatchNote);
        tvCurrentQuarter = findViewById(R.id.tvCurrentQuarter);
        tvQuarterScore = findViewById(R.id.tvQuarterScore);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        btnNewMatch = findViewById(R.id.btnNewMatch);
        btnSaveQuarter = findViewById(R.id.btnSaveQuarter);
        btnSaveMatch = findViewById(R.id.btnSaveMatch);
        btnSyncAllScores = findViewById(R.id.btnSyncAllScores); // 新增同步按钮

        // 上一节比分管理
        btnViewPreviousQuarter = findViewById(R.id.btnViewPreviousQuarter);
        btnEditPreviousQuarter = findViewById(R.id.btnEditPreviousQuarter);
    }
    
    private void setupBluetoothManager() {
        bluetoothManager = new BluetoothManager(this);
        bluetoothManager.setCallback(this);
        bluetoothManager.setDataCallback(this);
    }
    
    private void setupUI() {
        updateScoreDisplay();
        updateQuarterDisplay();
        
        // 蓝牙连接按钮
        btnConnect.setOnClickListener(v -> showDeviceSelectionDialog());
        
        // 龙都F4计分按钮
        btnHomePlus1.setOnClickListener(v -> updateHomeScore(1));
        btnHomePlus2.setOnClickListener(v -> updateHomeScore(2));
        btnHomePlus3.setOnClickListener(v -> updateHomeScore(3));
        btnHomeMinus1.setOnClickListener(v -> updateHomeScore(-1));
        
        // 暴风队计分按钮
        btnAwayPlus1.setOnClickListener(v -> updateAwayScore(1));
        btnAwayPlus2.setOnClickListener(v -> updateAwayScore(2));
        btnAwayPlus3.setOnClickListener(v -> updateAwayScore(3));
        btnAwayMinus1.setOnClickListener(v -> updateAwayScore(-1));
        
        // 游戏控制按钮
        btnResetScore.setOnClickListener(v -> resetScore());
        btnTimeout.setOnClickListener(v -> callTimeout());
        btnStartTimer.setOnClickListener(v -> startTimer());
        btnPauseTimer.setOnClickListener(v -> pauseTimer());
        btnContinueTimer.setOnClickListener(v -> continueTimer());
        
        // 设置倒计时按钮初始状态
        btnStartTimer.setEnabled(true);
        btnPauseTimer.setEnabled(false);
        btnContinueTimer.setEnabled(false);
        
        btnNextQuarter.setOnClickListener(v -> nextQuarter());
        btnPreviousQuarter.setOnClickListener(v -> previousQuarter());
        
        // 比赛管理按钮
        btnNewMatch.setOnClickListener(v -> showNewMatchDialog());
        btnSaveQuarter.setOnClickListener(v -> saveCurrentQuarter());
        btnSaveMatch.setOnClickListener(v -> saveMatch());
        
        // 上一节比分管理按钮
        btnViewPreviousQuarter.setOnClickListener(v -> viewPreviousQuarter());
        btnEditPreviousQuarter.setOnClickListener(v -> editPreviousQuarter());
        
        // 添加查看历史记录按钮
        Button btnViewHistory = new Button(this);
        btnViewHistory.setText("查看历史");
        btnViewHistory.setOnClickListener(v -> showQuarterHistory());
        
        // 使用兼容的方式设置背景颜色
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            btnViewHistory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_purple)));
        } else {
            btnViewHistory.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        }
        
        btnViewHistory.setTextColor(getResources().getColor(android.R.color.white));
        btnViewHistory.setPadding(20, 10, 20, 10);
        btnViewHistory.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 将按钮添加到比赛管理区域
        android.widget.LinearLayout matchButtonLayout = findViewById(R.id.layoutMatchInfo);
        if (matchButtonLayout != null) {
            matchButtonLayout.addView(btnViewHistory);
        }
        
        // 添加连接PC按钮功能
        Button btnConnectPC = new Button(this);
        btnConnectPC.setText("连接PC");
        btnConnectPC.setOnClickListener(v -> showConnectPCDialog());

        // 同步按钮
        btnSyncAllScores.setOnClickListener(v -> syncAllScores());
    }
    
    private void checkBluetoothPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            // Android 11及以下
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            bluetoothPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            initializeBluetooth();
        }
    }
    
    @SuppressLint("MissingPermission")
    private void initializeBluetooth() {
        if (!bluetoothManager.isBluetoothAvailable()) {
            showToast("此设备不支持蓝牙");
            return;
        }
        
        if (!bluetoothManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBtIntent);
        }
    }
    
    @SuppressLint("MissingPermission")
    private void showDeviceSelectionDialog() {
        if (!bluetoothManager.hasBluetoothPermissions()) {
            showToast("缺少蓝牙权限");
            return;
        }
        
        List<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
        if (pairedDevices.isEmpty()) {
            showToast("没有已配对的蓝牙设备");
            return;
        }
        
        String[] deviceNames = new String[pairedDevices.size()];
        for (int i = 0; i < pairedDevices.size(); i++) {
            BluetoothDevice device = pairedDevices.get(i);
            String deviceName = "未知设备";
            
            // 安全地获取设备名称
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "未知设备";
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
                        == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "未知设备";
                    }
                }
            } catch (SecurityException e) {
                deviceName = "未知设备";
            }
            
            deviceNames[i] = deviceName + " (" + device.getAddress() + ")";
        }
        
        new AlertDialog.Builder(this)
            .setTitle("选择蓝牙设备")
            .setItems(deviceNames, (dialog, which) -> {
                BluetoothDevice selectedDevice = pairedDevices.get(which);
                bluetoothManager.connectToDevice(selectedDevice);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    // 计分功能
    private void updateHomeScore(int points) {
        homeScore += points;
        updateScoreDisplay();
        updateMatchInfo();
        
        // 发送得分命令到PC
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.HomeScore(points));
        }
        
        // 检查是否达到20分
        checkQuarterEnd();
        
        // 实时保存当前节次比分到数据库
        saveCurrentQuarterToDatabase();
    }
    
    private void updateAwayScore(int points) {
        awayScore += points;
        updateScoreDisplay();
        updateMatchInfo();
        
        // 发送得分命令到PC
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.AwayScore(points));
        }
        
        // 检查是否达到20分
        checkQuarterEnd();
        
        // 实时保存当前节次比分到数据库
        saveCurrentQuarterToDatabase();
    }
    
    private void resetScore() {
        // 重置当前节次比分
        homeScore = 0;
        awayScore = 0;
        
        // 更新显示
        updateScoreDisplay();
        updateMatchInfo();
        
        // 发送重置命令到PC
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.ResetScore());
        }
        
        showToast("比分已重置");
    }
    
    private void callTimeout() {
        // 显示选择暂停队伍的对话框
        new AlertDialog.Builder(this)
            .setTitle("选择暂停队伍")
            .setItems(new String[]{"龙都F4", "暴风队"}, (dialog, which) -> {
                String team = which == 0 ? "HOME" : "AWAY";
                if (bluetoothManager != null) {
                    bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Timeout(team));
                }
                showToast((which == 0 ? "龙都F4" : "暴风队") + "暂停");
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void startTimer() {
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Timer("START_COUNTDOWN"));
            isTimerRunning = true;
            isTimerPaused = false;
            
            // 更新按钮状态
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(true);
            btnContinueTimer.setEnabled(false);
        }
        showToast("计时开始");
    }
    
    private void pauseTimer() {
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Timer("STOP_COUNTDOWN"));
            isTimerRunning = false;
            isTimerPaused = true;
            
            // 更新按钮状态
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(false);
            btnContinueTimer.setEnabled(true);
        }
        showToast("计时暂停");
    }

    private void continueTimer() {
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Timer("CONTINUE_COUNTDOWN"));
            isTimerRunning = true;
            isTimerPaused = false;
            
            // 更新按钮状态
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(true);
            btnContinueTimer.setEnabled(false);
        }
        showToast("继续倒计时");
    }
    
    private void nextQuarter() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (currentQuarter >= 10) { // 最多10节
            showToast("已达到最大节次");
            return;
        }
        
        // 保存当前节次比分到历史记录
        quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
        
        // 更新累计总分（使用正确的逻辑）
        lastTotalHomeScore += homeScore;
        lastTotalAwayScore += awayScore;

        // 更新显示总分
        totalHomeScore = lastTotalHomeScore;
        totalAwayScore = lastTotalAwayScore;
        
        // 保存当前节次比分
        saveCurrentQuarter();
        
        // 进入下一节
        currentQuarter++;
        
        // 从历史记录中获取下一节的比分
        QuarterScore nextScore = quarterHistory.get(currentQuarter);
        if (nextScore != null) {
            // 使用历史记录中的比分
            homeScore = nextScore.homeScore;
            awayScore = nextScore.awayScore;

            // 从总分中减去当前节次比分
            lastTotalHomeScore -= homeScore;
            lastTotalAwayScore -= awayScore;

            // 更新显示
            updateMatchInfo();
            updateScoreDisplay();
            updateQuarterDisplay();
            
            showToast("进入第" + currentQuarter + "节，比分：" + homeScore + ":" + awayScore);
        } else {
            // 如果历史记录中没有，重置为0（新节次）
            homeScore = 0;
            awayScore = 0;
            
            // 更新显示
            updateMatchInfo();
            updateScoreDisplay();
            updateQuarterDisplay();
            
            showToast("进入第" + currentQuarter + "节（新节次）");
        }
        
        // 显示上一节比分管理按钮（从第2节开始）
        if (currentQuarter > 1) {
            btnViewPreviousQuarter.setVisibility(View.VISIBLE);
            btnEditPreviousQuarter.setVisibility(View.VISIBLE);
        }
        
        // 发送节次更新命令
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Quarter(currentQuarter));
        }
        
        // 检查是否达到100分结束条件
        checkGameEnd();
    }
    
    /**
     * 进入下一节但不累加当前节次比分（用于从checkQuarterEnd调用）
     */
    private void nextQuarterWithoutAddingScore() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (currentQuarter >= 10) { // 最多10节
            showToast("已达到最大节次");
            return;
        }
        
        // 保存当前节次比分到历史记录
        quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
        
        // 注意：不累加总分，因为已经在checkQuarterEnd中累加过了

        // 更新累计总分（使用正确的逻辑）
        lastTotalHomeScore += homeScore;
        lastTotalAwayScore += awayScore;

        // 更新显示总分
        totalHomeScore = lastTotalHomeScore;
        totalAwayScore = lastTotalAwayScore;

        // 保存当前节次比分
        saveCurrentQuarter();
        
        // 进入下一节
        currentQuarter++;
        
        // 从历史记录中获取下一节的比分
        QuarterScore nextScore = quarterHistory.get(currentQuarter);
        if (nextScore != null) {
            // 使用历史记录中的比分
            homeScore = nextScore.homeScore;
            awayScore = nextScore.awayScore;
            
            // 更新显示
            updateMatchInfo();
            updateScoreDisplay();
            updateQuarterDisplay();
            
            showToast("进入第" + currentQuarter + "节，比分：" + homeScore + ":" + awayScore);
        } else {
            // 如果历史记录中没有，重置为0（新节次）
            homeScore = 0;
            awayScore = 0;
            
            // 更新显示
            updateMatchInfo();
            updateScoreDisplay();
            updateQuarterDisplay();
            
            showToast("进入第" + currentQuarter + "节（新节次）");
        }
        
        // 显示上一节比分管理按钮（从第2节开始）
        if (currentQuarter > 1) {
            btnViewPreviousQuarter.setVisibility(View.VISIBLE);
            btnEditPreviousQuarter.setVisibility(View.VISIBLE);
        }
        
        // 发送节次更新命令
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Quarter(currentQuarter));
        }
        
        // 检查是否达到100分结束条件
        checkGameEnd();
    }
    
    private void previousQuarter() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (currentQuarter <= 1) {
            showToast("已经是第1节，无法返回上一节");
            return;
        }
        
        // 保存当前节次比分到历史记录
        quarterHistory.put(currentQuarter, new QuarterScore(homeScore, awayScore));
        

        // 返回上一节
        currentQuarter--;
        
        // 从历史记录中获取上一节的比分
        QuarterScore previousScore = quarterHistory.get(currentQuarter);
        if (previousScore != null) {
            // 使用历史记录中的比分
            homeScore = previousScore.homeScore;
            awayScore = previousScore.awayScore;
            // 从总分中减去当前节次比分
            lastTotalHomeScore = totalHomeScore - homeScore;
            lastTotalAwayScore = totalAwayScore - awayScore;
            // 更新显示
            updateMatchInfo();
            updateScoreDisplay();
            updateQuarterDisplay();
            
            showToast("返回第" + currentQuarter + "节，比分：" + homeScore + ":" + awayScore);
        } else {
            // 如果历史记录中没有，尝试从PC端获取
            if (bluetoothManager != null) {
                bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SelectPreviousQuarter(currentMatchId, currentQuarter));
                showToast("正在从PC获取第" + currentQuarter + "节比分...");
            } else {
                // 如果没有连接PC且没有历史记录，重置为0
                homeScore = 0;
                awayScore = 0;
                
                // 更新显示
                updateMatchInfo();
                updateScoreDisplay();
                updateQuarterDisplay();
                
                showToast("返回第" + currentQuarter + "节（无历史记录）");
            }
        }
        
        // 隐藏上一节比分管理按钮（如果是第1节）
        if (currentQuarter <= 1) {
            btnViewPreviousQuarter.setVisibility(View.GONE);
            btnEditPreviousQuarter.setVisibility(View.GONE);
        }
        
        // 发送节次更新命令到PC
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Quarter(currentQuarter));
        }
    }
    
    private void updateScoreDisplay() {
        tvHomeScore.setText(String.valueOf(homeScore));
        tvAwayScore.setText(String.valueOf(awayScore));
    }
    
    private void updateQuarterDisplay() {
        tvQuarter.setText(String.valueOf(currentQuarter));
    }
    
    // BluetoothCallback 实现
    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                tvBluetoothStatus.setText("已连接 (" + bluetoothManager.getConnectionType() + ")");
                tvBluetoothStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                btnConnect.setText("断开连接");
            } else {
                tvBluetoothStatus.setText("未连接");
                tvBluetoothStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnConnect.setText("连接PC");
            }
        });
    }
    
    @Override
    public void onError(String message) {
        runOnUiThread(() -> showToast(message));
    }
    
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        // 目前未使用设备发现功能
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示连接PC对话框
     */
    private void showConnectPCDialog() {
        // 显示连接方式选择对话框
        new AlertDialog.Builder(this)
            .setTitle("连接PC")
            .setItems(new String[]{"蓝牙连接", "网络连接"}, (dialog, which) -> {
                if (which == 0) {
                    // 蓝牙连接
                    showBluetoothDeviceSelectionDialog();
                } else {
                    // 网络连接
                    showNetworkConnectionDialog();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 显示蓝牙设备选择对话框
     */
    @SuppressLint("MissingPermission")
    private void showBluetoothDeviceSelectionDialog() {
        if (!bluetoothManager.hasBluetoothPermissions()) {
            showToast("缺少蓝牙权限");
            return;
        }
        
        List<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
        if (pairedDevices.isEmpty()) {
            showToast("没有已配对的蓝牙设备");
            return;
        }
        
        String[] deviceNames = new String[pairedDevices.size()];
        for (int i = 0; i < pairedDevices.size(); i++) {
            BluetoothDevice device = pairedDevices.get(i);
            String deviceName = "未知设备";
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "未知设备";
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
                        == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "未知设备";
                    }
                }
            } catch (SecurityException e) {
                deviceName = "未知设备";
            }
            
            deviceNames[i] = deviceName + " (" + device.getAddress() + ")";
        }
        
        new AlertDialog.Builder(this)
            .setTitle("选择PC蓝牙设备")
            .setItems(deviceNames, (dialog, which) -> {
                BluetoothDevice selectedDevice = pairedDevices.get(which);
                connectToPCBluetooth(selectedDevice);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 显示网络连接对话框
     */
    private void showNetworkConnectionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("网络连接");
        
        // 创建输入视图
        android.widget.EditText etPCHost = new android.widget.EditText(this);
        etPCHost.setHint("请输入PC的IP地址");
        etPCHost.setText("192.168.1.100"); // 默认IP
        
        builder.setView(etPCHost);
        
        builder.setPositiveButton("连接", (dialog, which) -> {
            String pcHost = etPCHost.getText().toString().trim();
            
            if (pcHost.isEmpty()) {
                showToast("请输入PC的IP地址");
                return;
            }
            
//            connectToPCNetwork(pcHost);
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 连接到PC蓝牙设备
     */
    private void connectToPCBluetooth(BluetoothDevice pcDevice) {
        if (bluetoothManager != null) {
            bluetoothManager.connectToPCBluetooth(pcDevice);
            showToast("正在连接PC蓝牙设备: " + pcDevice.getName());
        } else {
            showToast("蓝牙管理器未初始化");
        }
    }
    
    // 比赛管理方法
    private void showNewMatchDialog() {
        // 显示新建比赛对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("新建比赛");
        
        // 创建输入视图
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        // 比赛名称输入框
        android.widget.EditText etMatchName = new android.widget.EditText(this);
        etMatchName.setHint("比赛名称");
        etMatchName.setText(generateDefaultMatchName());
        layout.addView(etMatchName);
        
        // 备注输入框
        android.widget.EditText etMatchNote = new android.widget.EditText(this);
        etMatchNote.setHint("备注");
        layout.addView(etMatchNote);
        
        builder.setView(layout);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String name = etMatchName.getText().toString().trim();
            String note = etMatchNote.getText().toString().trim();
            
            if (name.isEmpty()) {
                name = generateDefaultMatchName();
            }
            
            createNewMatch(name, note);
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    private String generateDefaultMatchName() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日HH时", java.util.Locale.CHINA);
        return sdf.format(new java.util.Date());
    }
    
    private void createNewMatch(String name, String note) {
        matchName = name;
        matchNote = note;
        currentQuarter = 1;
        homeScore = 0;
        awayScore = 0;
        totalHomeScore = 0;
        totalAwayScore = 0;
        lastTotalHomeScore = 0;
        lastTotalAwayScore = 0;
        isMatchStarted = true;
        
        // 清空节次历史记录
        quarterHistory.clear();
        
        // 更新UI
        updateMatchInfo();
        updateScoreDisplay();
        updateQuarterDisplay();
        
        // 显示比赛信息
        layoutMatchInfo.setVisibility(View.VISIBLE);
        btnSaveQuarter.setVisibility(View.VISIBLE);
        btnSaveMatch.setVisibility(View.VISIBLE);
        btnSyncAllScores.setVisibility(View.VISIBLE);
        
        // 显示上一节比分管理按钮（从第2节开始）
        if (currentQuarter > 1) {
            btnViewPreviousQuarter.setVisibility(View.VISIBLE);
            btnEditPreviousQuarter.setVisibility(View.VISIBLE);
        }
        
        // 发送新建比赛命令到PC
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.NewMatch(name, note));
        }
        
        showToast("新建比赛：" + name);
    }
    
    private void saveCurrentQuarter() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        // 保存当前节次比分
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SaveQuarter(currentQuarter, homeScore, awayScore));
        }
        
        showToast("已保存第" + currentQuarter + "节比分");
    }
    
    private void saveMatch() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        // 保存比赛总分
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SaveMatch(matchName, totalHomeScore, totalAwayScore));
        }
        
        showToast("已保存比赛总分");
    }
    
    /**
     * 查看上一节比分
     */
    private void viewPreviousQuarter() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (currentQuarter <= 1) {
            showToast("没有上一节比分");
            return;
        }
        
        // 这里应该从数据库或缓存中获取上一节比分
        // 暂时使用模拟数据
        String message = "第" + (currentQuarter - 1) + "节比分：\n" +
                        "龙都F4：" + previousQuarterHomeScore + "分\n" +
                        "暴风队：" + previousQuarterAwayScore + "分";
        
        new AlertDialog.Builder(this)
            .setTitle("上一节比分")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show();
    }
    
    /**
     * 编辑上一节比分
     */
    private void editPreviousQuarter() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (currentQuarter <= 1) {
            showToast("没有上一节比分");
            return;
        }
        
        // 创建编辑对话框
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_quarter, null);
        EditText etHomeScore = dialogView.findViewById(R.id.etHomeScore);
        EditText etAwayScore = dialogView.findViewById(R.id.etAwayScore);
        
        // 设置当前值
        etHomeScore.setText(String.valueOf(previousQuarterHomeScore));
        etAwayScore.setText(String.valueOf(previousQuarterAwayScore));
        
        new AlertDialog.Builder(this)
            .setTitle("编辑第" + (currentQuarter - 1) + "节比分")
            .setView(dialogView)
            .setPositiveButton("保存", (dialog, which) -> {
                try {
                    int newHomeScore = Integer.parseInt(etHomeScore.getText().toString());
                    int newAwayScore = Integer.parseInt(etAwayScore.getText().toString());
                    
                    // 更新上一节比分
                    previousQuarterHomeScore = newHomeScore;
                    previousQuarterAwayScore = newAwayScore;
                    
                    // 发送更新命令到PC
                    if (bluetoothManager != null) {
                        bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SaveQuarter(currentQuarter - 1, newHomeScore, newAwayScore));
                    }
                    
                    showToast("已更新第" + (currentQuarter - 1) + "节比分");
                    
                } catch (NumberFormatException e) {
                    showToast("请输入有效的分数");
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void updateMatchInfo() {
        tvMatchName.setText("比赛名称：" + matchName);
        tvMatchNote.setText("备注：" + (matchNote.isEmpty() ? "无" : matchNote));
        tvCurrentQuarter.setText("当前节次：第" + currentQuarter + "节");
        tvQuarterScore.setText("本节比分：" + homeScore + " : " + awayScore);
        
        tvTotalScore.setText("总比分：" + totalHomeScore + " : " + totalAwayScore);
    }
    
    // 检查是否达到20分
    private void checkQuarterEnd() {
        if (homeScore >= 20 || awayScore >= 20) {
            // 达到20分，自动结束本节
            showToast("第" + currentQuarter + "节结束！");
            

            // 自动保存本节
            saveCurrentQuarter();
            
            // 询问是否进入下一节
            new AlertDialog.Builder(this)
                .setTitle("本节结束")
                .setMessage("是否进入下一节？")
                .setPositiveButton("是", (dialog, which) -> nextQuarterWithoutAddingScore())
                .setNegativeButton("否", null)
                .show();
        }
    }
    
    /**
     * 检查比赛是否结束（总分达到100分）
     */
    private void checkGameEnd() {
        if (totalHomeScore >= 100 || totalAwayScore >= 100) {
            // 保存当前节次比分
            saveCurrentQuarter();
            
            String winner = totalHomeScore >= 100 ? "龙都F4" : "暴风队";
            String message = winner + "获胜！比赛结束！\n" +
                           "最终比分：" + totalHomeScore + " - " + totalAwayScore;
            
            new AlertDialog.Builder(this)
                .setTitle("比赛结束")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 保存比赛
                    saveMatch();
                })
                .setCancelable(false)
                .show();
            
            showToast("比赛结束：" + winner + "获胜！");
        }
    }
    
    /**
     * 实时保存当前节次比分到数据库
     */
    private void saveCurrentQuarterToDatabase() {
        if (isMatchStarted && bluetoothManager != null) {
            // 发送保存节次命令到PC
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SaveQuarter(currentQuarter, homeScore, awayScore));
            
            // 更新总比分并保存
            updateTotalScoreAndSave();
        }
    }
    
    /**
     * 更新总比分并保存到数据库
     */
    private void updateTotalScoreAndSave() {
        // 计算当前累计总分（之前节次的总分 + 当前节次比分）
        totalHomeScore = lastTotalHomeScore + homeScore;
        totalAwayScore = lastTotalAwayScore + awayScore;
        
        // 更新显示
        updateMatchInfo();
        
        // 发送更新总比分命令到PC（显示当前累计总分）
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SaveMatch(matchName, totalHomeScore, totalAwayScore));
        }
    }

    /**
     * 同步所有比分到PC
     */
    private void syncAllScores() {
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SyncAllScores());
            showToast("正在同步所有比分...");
        } else {
            showToast("蓝牙管理器未初始化，无法同步比分");
        }
    }
    
    /**
     * 选择上一节并带出比分
     */
    private void selectPreviousQuarter(int quarterNumber) {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (quarterNumber < 1 || quarterNumber >= currentQuarter) {
            showToast("无效的节次");
            return;
        }
        
        // 发送选择上一节命令到PC
        if (bluetoothManager != null) {
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.SelectPreviousQuarter(currentMatchId, quarterNumber));
            showToast("正在获取第" + quarterNumber + "节比分...");
        } else {
            showToast("蓝牙管理器未初始化");
        }
    }
    
    /**
     * 处理PC端返回的上一节比分数据
     */
    private void handlePreviousQuarterData(int quarterNumber, int homeScore, int awayScore) {
        // 将上一节比分显示到当前小节
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        
        // 更新历史记录
        quarterHistory.put(quarterNumber, new QuarterScore(homeScore, awayScore));
        
        // 更新比分显示
        updateScoreDisplay();
        
        // 更新节次显示
        currentQuarter = quarterNumber;
        updateQuarterDisplay();
        
        // 更新比赛信息显示
        updateMatchInfo();
        
        // 显示成功提示
        showToast("已加载第" + quarterNumber + "节比分：" + homeScore + ":" + awayScore);
        
        // 发送同步命令到PC，让PC端也显示到当前小节
        if (bluetoothManager != null) {
            // 发送节次同步命令
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.Quarter(quarterNumber));
            // 发送比分同步命令
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.HomeScore(homeScore));
            bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.AwayScore(awayScore));
        }
    }
    
    // DataCallback 实现方法
    @Override
    public void onPreviousQuarterData(int quarterNumber, int homeScore, int awayScore) {
        handlePreviousQuarterData(quarterNumber, homeScore, awayScore);
    }
    
    @Override
    public void onTotalScoreUpdate(int totalHomeScore, int totalAwayScore) {
        handleTotalScoreUpdate(totalHomeScore, totalAwayScore);
    }
    
    /**
     * 处理PC端返回的总分更新数据
     */
    private void handleTotalScoreUpdate(int totalHomeScore, int totalAwayScore) {
        // 更新总分
        this.totalHomeScore = totalHomeScore;
        this.totalAwayScore = totalAwayScore;
        
        // 更新比赛信息显示
        updateMatchInfo();
        
        // 显示总分更新提示
        showToast("总分已更新：" + totalHomeScore + ":" + totalAwayScore);
    }
    
    /**
     * 显示节次历史记录
     */
    private void showQuarterHistory() {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (quarterHistory.isEmpty()) {
            showToast("暂无历史记录");
            return;
        }
        
        // 构建历史记录信息
        StringBuilder historyText = new StringBuilder();
        historyText.append("节次历史记录：\n\n");
        
        // 按节次顺序显示
        for (int i = 1; i <= currentQuarter; i++) {
            QuarterScore score = quarterHistory.get(i);
            if (score != null) {
                historyText.append("第").append(i).append("节：")
                          .append(score.homeScore).append(" : ").append(score.awayScore).append("\n");
            }
        }
        
        // 显示当前节次
        historyText.append("\n当前第").append(currentQuarter).append("节：")
                  .append(homeScore).append(" : ").append(awayScore);
        
        new AlertDialog.Builder(this)
            .setTitle("节次历史记录")
            .setMessage(historyText.toString())
            .setPositiveButton("确定", null)
            .show();
    }
    
    /**
     * 修改上一节比分
     */
    private void modifyPreviousQuarter(int quarterNumber) {
        if (!isMatchStarted) {
            showToast("请先新建比赛");
            return;
        }
        
        if (quarterNumber < 1 || quarterNumber >= currentQuarter) {
            showToast("无效的节次");
            return;
        }
        
        // 创建修改比分的对话框
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_quarter, null);
        EditText etHomeScore = dialogView.findViewById(R.id.etHomeScore);
        EditText etAwayScore = dialogView.findViewById(R.id.etAwayScore);
        
        // 设置当前值（可以从缓存或数据库获取）
        etHomeScore.setText("0");
        etAwayScore.setText("0");
        
        new AlertDialog.Builder(this)
            .setTitle("修改第" + quarterNumber + "节比分")
            .setView(dialogView)
            .setPositiveButton("保存", (dialog, which) -> {
                try {
                    int newHomeScore = Integer.parseInt(etHomeScore.getText().toString());
                    int newAwayScore = Integer.parseInt(etAwayScore.getText().toString());
                    
                    // 发送修改上一节比分命令到PC
                    if (bluetoothManager != null) {
                        bluetoothManager.sendScoreCommand(new BluetoothManager.ScoreCommand.UpdatePreviousQuarter(
                            currentMatchId, quarterNumber, newHomeScore, newAwayScore));
                        showToast("正在修改第" + quarterNumber + "节比分...");
                    } else {
                        showToast("蓝牙管理器未初始化");
                    }
                    
                } catch (NumberFormatException e) {
                    showToast("请输入有效的分数");
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothManager != null) {
            bluetoothManager.disconnect();
        }
    }
} 