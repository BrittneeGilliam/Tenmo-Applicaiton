package com.techelevator.tenmo.model;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

public class Transfer {
	
	private int id;

	private int typeId;
	private int statusId;
	@NotBlank(message = "No one is listed as the transfer sender")
	private int accountFrom;
	@NotBlank(message = "No one is listed as the transfer recipient")
	private int accountTo;
	@DecimalMin(value = "0.01", message = "Each transfer has a minimum amount of $0.01")
	private double amount;

	public Transfer() {

	}

	public Transfer(int id, int type, int status, int from, int to, double amount) {
		this.id = id;
		this.typeId = type;
		this.statusId = status;
		this.accountFrom = from;
		this.accountTo = to;
		this.amount = amount;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public int getAccountFrom() {
		return accountFrom;
	}

	public void setAccountFrom(int accountFrom) {
		this.accountFrom = accountFrom;
	}

	public int getAccountTo() {
		return accountTo;
	}

	public void setAccountTo(int accountTo) {
		this.accountTo = accountTo;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	
}
