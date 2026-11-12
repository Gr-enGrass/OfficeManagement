package com.officemanagement.model;

public class ReservationDTO {
    private int reservationId;
    private String reservationDate;
    private int startHour;
    private int endHour;
    private String status;
    private int amount;
    private String paymentMethod;
    private String username;
    
	public ReservationDTO(int reservationId, String reservationDate, int startHour, int endHour, String status,
			int amount, String paymentMethod) {
		super();
		this.reservationId = reservationId;
		this.reservationDate = reservationDate;
		this.startHour = startHour;
		this.endHour = endHour;
		this.status = status;
		this.amount = amount;
		this.paymentMethod = paymentMethod;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getReservationId() {
		return reservationId;
	}
	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}
	public String getReservationDate() {
		return reservationDate;
	}
	public void setReservationDate(String reservationDate) {
		this.reservationDate = reservationDate;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getEndHour() {
		return endHour;
	}
	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}