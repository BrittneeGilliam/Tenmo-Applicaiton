package com.techelevator.tenmo.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Transfer;

@Component
public interface TransferDAO {

	public List<Transfer> transferHistory(int accountId);
	
	public Transfer viewTransfer(int transferId);

	public Transfer createTransfer(int type, int status, int from, int to, double amount);
	

}
