package com.techelevator.tenmo.dao;

import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Transfer;
@Component
public interface AccountDAO {
	
	public double viewBalance(double id);

	public void transferFunds(Transfer transfer);

	
}
