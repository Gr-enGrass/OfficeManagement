package com.officemanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.officemanagement.db.DBConnection;
import com.officemanagement.model.ReservationDTO;

public class ReservationDAO {

    // 특정 날짜의 예약 현황 조회 (start_hour, end_hour 가져오기)
    public Map<Integer, Boolean> getReservedHoursByDate(String reservationDate) {
        Map<Integer, Boolean> reservedMap = new HashMap<>(); // 시간대별 예약 여부
        
        String sql = "SELECT start_hour, end_hour FROM reservations WHERE reservation_date = ? AND status = 'RESERVED'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, reservationDate);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int start = rs.getInt("start_hour");
                int end = rs.getInt("end_hour");
                for (int i = start; i < end; i++) {
                    reservedMap.put(i, true); // 예약된 시간은 true로 표시
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
        
        return reservedMap;
    }

    // 예약 및 결제 트랜잭션 처리 메서드
    public boolean insertReservationAndPayment(int userId, String date, int startHour, int endHour, int amount, String paymentMethod) {
        Connection conn = null;
        PreparedStatement resPstmt = null;
        PreparedStatement payPstmt = null;
        ResultSet keys = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 예약 INSERT
            String resSql = "INSERT INTO reservations (user_id, reservation_date, start_hour, end_hour, status) VALUES (?, ?, ?, ?, 'RESERVED')";
            resPstmt = conn.prepareStatement(resSql, new String[]{"reservation_id"});
            resPstmt.setInt(1, userId);
            resPstmt.setString(2, date);
            resPstmt.setInt(3, startHour);
            resPstmt.setInt(4, endHour);
            resPstmt.executeUpdate();
            
            int generatedId = -1;
            keys = resPstmt.getGeneratedKeys();
            if (keys.next()) {
                generatedId = keys.getInt(1);
            }
            
            if (generatedId == -1) {
                throw new SQLException("예약 ID 생성 실패");
            }
            
            // 결제 INSERT
            String paySql = "INSERT INTO payments (reservation_id, amount, payment_method) VALUES (?, ?, ?)";
            payPstmt = conn.prepareStatement(paySql);
            payPstmt.setInt(1, generatedId);
            payPstmt.setInt(2, amount);
            payPstmt.setString(3, paymentMethod);
            payPstmt.executeUpdate();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (keys != null) 
                	keys.close();
                if (resPstmt != null) 
                	resPstmt.close();
                if (payPstmt != null) 
                	payPstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 특정 유저의 예약 및 결제 내역 조회 메서드
    public java.util.List<ReservationDTO> getMyReservations(int userId) {
        java.util.List<ReservationDTO> list = new java.util.ArrayList<>();
        String sql = "SELECT r.reservation_id, r.reservation_date, r.start_hour, r.end_hour, r.status, p.amount, p.payment_method " +
                     "FROM reservations r " +
                     "JOIN payments p ON r.reservation_id = p.reservation_id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.reservation_date DESC, r.start_hour DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(new ReservationDTO(
                    rs.getInt("reservation_id"),
                    rs.getString("reservation_date"),
                    rs.getInt("start_hour"),
                    rs.getInt("end_hour"),
                    rs.getString("status"),
                    rs.getInt("amount"),
                    rs.getString("payment_method")
                ));
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
        
        return list;
    }
    
    // 예약 취소 메서드
    public boolean cancelReservation(int reservationId, int userId) {
        String sql = "UPDATE reservations SET status = 'CANCELED' WHERE reservation_id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reservationId);
            pstmt.setInt(2, userId);
            
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
    
    // 관리자용 전체 예약 조회 메서드
    public java.util.List<ReservationDTO> getAllReservations() {
        java.util.List<ReservationDTO> list = new java.util.ArrayList<>();
        String sql = "SELECT r.reservation_id, u.username, r.reservation_date, r.start_hour, r.end_hour, r.status, p.amount, p.payment_method " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "JOIN payments p ON r.reservation_id = p.reservation_id " +
                     "ORDER BY r.reservation_date DESC, r.start_hour DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ReservationDTO dto = new ReservationDTO(
                    rs.getInt("reservation_id"),
                    rs.getString("reservation_date"),
                    rs.getInt("start_hour"),
                    rs.getInt("end_hour"),
                    rs.getString("status"),
                    rs.getInt("amount"),
                    rs.getString("payment_method")
                );
                dto.setUsername(rs.getString("username")); // 예약자 아이디 세팅
                list.add(dto);
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
        
        return list;
    }
}