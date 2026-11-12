package com.officemanagement.db;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    static {
        Properties prop = new Properties();
        
        // 프로젝트 폴더에 있는 db.properties 파일 읽기
        try (FileInputStream input = new FileInputStream("db.properties")) {
            prop.load(input);
            driver = prop.getProperty("db.driver");
            url = prop.getProperty("db.url");
            username = prop.getProperty("db.username");
            password = prop.getProperty("db.password");
            
            Class.forName(driver);
            
        } catch (Exception e) {
            System.out.println("[오류] db.properties 파일을 찾거나 읽어오는 중 문제가 발생했습니다.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}