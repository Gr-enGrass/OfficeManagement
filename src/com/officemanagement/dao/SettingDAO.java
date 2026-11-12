package com.officemanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.officemanagement.db.DBConnection;

public class SettingDAO {

    // DB에서 현재 시간당 요금 가져오기
    public int getPricePerHour() {
        String sql = "SELECT setting_value FROM office_settings WHERE setting_key = 'PRICE_PER_HOUR'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Integer.parseInt(rs.getString("setting_value"));
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
        
        return 5000; // 오류 발생 시 기본값 5000원 반환
    }

    // DB에 새로운 시간당 요금 저장하기
    public boolean updatePricePerHour(int newPrice) {
        String sql = "UPDATE office_settings SET setting_value = ? WHERE setting_key = 'PRICE_PER_HOUR'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, String.valueOf(newPrice));
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
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
}