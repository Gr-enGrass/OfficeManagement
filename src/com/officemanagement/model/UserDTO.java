package com.officemanagement.model;

public class UserDTO 
{
    private int userId;
    private String username;
    private String password;
    private String phone;
    private String role; // "USER" 또는 "ADMIN"
    private String createdAt;

    // 기본 생성자
    public UserDTO() {}

    // 회원가입용 생성자
    public UserDTO(String username, String password, String phone, String role) 
    {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    // 조회용 전체 생성자
    public UserDTO(int userId, String username, String password, String phone, String role, String createdAt) 
    {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}