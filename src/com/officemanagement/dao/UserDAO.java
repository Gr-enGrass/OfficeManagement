package com.officemanagement.dao;

import com.officemanagement.db.DBConnection;
import com.officemanagement.model.UserDTO;

import java.sql.*;

public class UserDAO {

    // 회원가입 메서드
    public boolean registerUser(UserDTO user) {
        String sql = "INSERT INTO users (username, password, phone, role) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getRole());
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
            
        } finally {
            try {
                if (pstmt != null) 
                	pstmt.close();
                if (conn != null) 
                	conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 로그인 메서드
    public UserDTO login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserDTO user = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) 
            {
                user = new UserDTO(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("role"),
                    rs.getString("created_at")
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) 
                	rs.close();
                if (pstmt != null) 
                	pstmt.close();
                if (conn != null) 
                	conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return user;
    }
    
    //3. 아이디 중복체크 메서드
    public boolean checkUsernameExist(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isExist = false;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // 개수가 1개 이상이면 이미 존재하는 아이디
                if (rs.getInt(1) > 0) {
                    isExist = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) 
                	rs.close();
                if (pstmt != null) 
                	pstmt.close();
                if (conn != null) 
                	conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return isExist;
    }
}