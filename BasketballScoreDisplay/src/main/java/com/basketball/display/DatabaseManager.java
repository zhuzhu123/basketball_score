package com.basketball.display;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理器 - 负责MySQL数据库的连接和操作
 */
public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sd_score?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";
    
    private Connection connection;
    
    /**
     * 初始化数据库连接
     */
    public boolean initialize() {
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // 创建数据库和表（如果不存在）
            createDatabaseAndTables();
            
            System.out.println("数据库连接成功");
            return true;
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动加载失败: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 创建数据库和表
     */
    private void createDatabaseAndTables() throws SQLException {
        // 创建数据库（如果不存在）
        try (Connection rootConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8", 
                DB_USER, DB_PASSWORD)) {
            
            Statement stmt = rootConnection.createStatement();
            stmt.execute("CREATE DATABASE IF NOT EXISTS ba_score CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("数据库 ba_score 创建成功或已存在");
        }
        
        // 创建比赛表
        String createMatchTable = """
            CREATE TABLE IF NOT EXISTS matches (
                id INT AUTO_INCREMENT PRIMARY KEY,
                match_name VARCHAR(255) NOT NULL,
                match_note TEXT,
                total_home_score INT DEFAULT 0,
                total_away_score INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        // 创建节次比分表
        String createQuarterScoresTable = """
            CREATE TABLE IF NOT EXISTS quarter_scores (
                id INT AUTO_INCREMENT PRIMARY KEY,
                match_id INT NOT NULL,
                quarter_number INT NOT NULL,
                home_score INT DEFAULT 0,
                away_score INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
                UNIQUE KEY unique_quarter (match_id, quarter_number)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMatchTable);
            stmt.execute(createQuarterScoresTable);
            System.out.println("数据表创建成功");
        }
    }
    
    /**
     * 新建比赛
     */
    public int createNewMatch(String matchName, String matchNote) throws SQLException {
        String sql = "INSERT INTO matches (match_name, match_note) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, matchName);
            pstmt.setString(2, matchNote);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int matchId = rs.getInt(1);
                        System.out.println("新建比赛成功，ID: " + matchId);
                        return matchId;
                    }
                }
            }
        }
        
        throw new SQLException("新建比赛失败");
    }
    
    /**
     * 保存节次比分（更新或新增）
     */
    public boolean saveQuarterScore(int matchId, int quarterNumber, int homeScore, int awayScore) throws SQLException {
        // 先检查是否已存在该节次的记录
        String checkSql = "SELECT id FROM quarter_scores WHERE match_id = ? AND quarter_number = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, matchId);
            checkStmt.setInt(2, quarterNumber);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // 已存在，执行更新
                    String updateSql = "UPDATE quarter_scores SET home_score = ?, away_score = ?, updated_at = NOW() WHERE match_id = ? AND quarter_number = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, homeScore);
                        updateStmt.setInt(2, awayScore);
                        updateStmt.setInt(3, matchId);
                        updateStmt.setInt(4, quarterNumber);
                        
                        int result = updateStmt.executeUpdate();
                        System.out.println("更新第" + quarterNumber + "节比分: " + homeScore + "-" + awayScore);
                        return result > 0;
                    }
                } else {
                    // 不存在，执行插入
                    String insertSql = "INSERT INTO quarter_scores (match_id, quarter_number, home_score, away_score, created_at) VALUES (?, ?, ?, ?, NOW())";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, matchId);
                        insertStmt.setInt(2, quarterNumber);
                        insertStmt.setInt(3, homeScore);
                        insertStmt.setInt(4, awayScore);
                        
                        int result = insertStmt.executeUpdate();
                        System.out.println("新增第" + quarterNumber + "节比分: " + homeScore + "-" + awayScore);
                        return result > 0;
                    }
                }
            }
        }
        // 如果发生异常，返回false
    }
    
    /**
     * 获取指定节次的比分
     */
    public QuarterScore getQuarterScore(int matchId, int quarterNumber) throws SQLException {
        String sql = "SELECT * FROM quarter_scores WHERE match_id = ? AND quarter_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            pstmt.setInt(2, quarterNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new QuarterScore(
                        rs.getInt("id"),
                        rs.getInt("match_id"),
                        rs.getInt("quarter_number"),
                        rs.getInt("home_score"),
                        rs.getInt("away_score"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * 根据ID获取比赛信息
     */
    public MatchInfo getMatchById(int matchId) throws SQLException {
        String sql = "SELECT * FROM matches WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new MatchInfo(
                        rs.getInt("id"),
                        rs.getString("match_name"),
                        rs.getString("match_note"),
                        rs.getInt("total_home_score"),
                        rs.getInt("total_away_score"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * 更新指定节次的比分
     */
    public boolean updateQuarterScore(int matchId, int quarterNumber, int homeScore, int awayScore) throws SQLException {
        String sql = "UPDATE quarter_scores SET home_score = ?, away_score = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE match_id = ? AND quarter_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, homeScore);
            pstmt.setInt(2, awayScore);
            pstmt.setInt(3, matchId);
            pstmt.setInt(4, quarterNumber);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 更新比赛总分
                updateMatchTotalScore(matchId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 更新比赛总分
     */
    private void updateMatchTotalScore(int matchId) throws SQLException {
        String sql = "UPDATE matches SET total_home_score = (SELECT SUM(home_score) FROM quarter_scores WHERE match_id = ?), " +
                    "total_away_score = (SELECT SUM(away_score) FROM quarter_scores WHERE match_id = ?), " +
                    "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            pstmt.setInt(2, matchId);
            pstmt.setInt(3, matchId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 获取比赛信息
     */
    public MatchInfo getMatchInfo(int matchId) throws SQLException {
        String sql = "SELECT * FROM matches WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new MatchInfo(
                        rs.getInt("id"),
                        rs.getString("match_name"),
                        rs.getString("match_note"),
                        rs.getInt("total_home_score"),
                        rs.getInt("total_away_score"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * 获取比赛的所有节次比分
     */
    public List<QuarterScore> getQuarterScores(int matchId) throws SQLException {
        String sql = "SELECT * FROM quarter_scores WHERE match_id = ? ORDER BY quarter_number";
        List<QuarterScore> quarterScores = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    quarterScores.add(new QuarterScore(
                        rs.getInt("id"),
                        rs.getInt("match_id"),
                        rs.getInt("quarter_number"),
                        rs.getInt("home_score"),
                        rs.getInt("away_score"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        
        return quarterScores;
    }
    
    /**
     * 获取所有比赛列表
     */
    public List<MatchInfo> getAllMatches() throws SQLException {
        String sql = "SELECT * FROM matches ORDER BY created_at DESC";
        List<MatchInfo> matches = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                matches.add(new MatchInfo(
                    rs.getInt("id"),
                    rs.getString("match_name"),
                    rs.getString("match_note"),
                    rs.getInt("total_home_score"),
                    rs.getInt("total_away_score"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        
        return matches;
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 比赛信息数据类
     */
    public static class MatchInfo {
        private final int id;
        private final String matchName;
        private final String matchNote;
        private final int totalHomeScore;
        private final int totalAwayScore;
        private final Timestamp createdAt;
        
        public MatchInfo(int id, String matchName, String matchNote, int totalHomeScore, int totalAwayScore, Timestamp createdAt) {
            this.id = id;
            this.matchName = matchName;
            this.matchNote = matchNote;
            this.totalHomeScore = totalHomeScore;
            this.totalAwayScore = totalAwayScore;
            this.createdAt = createdAt;
        }
        
        // Getters
        public int getId() { return id; }
        public String getMatchName() { return matchName; }
        public String getMatchNote() { return matchNote; }
        public int getTotalHomeScore() { return totalHomeScore; }
        public int getTotalAwayScore() { return totalAwayScore; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
    
    /**
     * 节次比分数据类
     */
    public static class QuarterScore {
        private final int id;
        private final int matchId;
        private final int quarterNumber;
        private final int homeScore;
        private final int awayScore;
        private final Timestamp createdAt;
        
        public QuarterScore(int id, int matchId, int quarterNumber, int homeScore, int awayScore, Timestamp createdAt) {
            this.id = id;
            this.matchId = matchId;
            this.quarterNumber = quarterNumber;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
            this.createdAt = createdAt;
        }
        
        // Getters
        public int getId() { return id; }
        public int getMatchId() { return matchId; }
        public int getQuarterNumber() { return quarterNumber; }
        public int getHomeScore() { return homeScore; }
        public int getAwayScore() { return awayScore; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
} 