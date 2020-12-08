package com.techelevator.tenmo.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.model.Transfer;

@Component
public class JDBCTransferDAO implements TransferDAO{
	private JdbcTemplate jdbc;

	public JDBCTransferDAO (JdbcTemplate jdbc){
		this.jdbc = jdbc;
	}

	@Override
	public List<Transfer> transferHistory(int accountId) {
		List<Transfer> transferList = new ArrayList<Transfer>();
		String sql = "SELECT * FROM transfers WHERE account_to = ? OR account_from = ?";
		SqlRowSet result = jdbc.queryForRowSet(sql, accountId, accountId);
		while(result.next()) {
			int id = result.getInt("transfer_id");
			int typeId = result.getInt("transfer_type_id");
			int statusId = result.getInt("transfer_status_id");
			int accountFrom = result.getInt("account_from");
			int accountTo = result.getInt("account_to");
			double amount = result.getDouble("amount");
			Transfer transfer = new Transfer(id, typeId, statusId, accountFrom, accountTo, amount);
			transferList.add(transfer);
		}
		
		return transferList;
	}

	@Override
	public Transfer viewTransfer(int transferId) {
		Transfer transfer = null;
		String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
		SqlRowSet result = jdbc.queryForRowSet(sql, transferId);
		while(result.next()) {
			int id = result.getInt("transfer_id");
			int typeId = result.getInt("transfer_type_id");
			int statusId = result.getInt("transfer_status_id");
			int accountFrom = result.getInt("account_from");
			int accountTo = result.getInt("account_to");
			double amount = result.getDouble("amount");
			transfer = new Transfer(id, typeId, statusId, accountFrom, accountTo, amount);
		}
		return transfer;
	}

	@Override
	public Transfer createTransfer(int type, int status, int from, int to, double amount) {
		String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?)";
		jdbc.update(sql, type, status, from, to, amount);
		String sql2 = "SELECT transfer_id FROM transfers ORDER BY transfer_id DESC LIMIT 1";
		SqlRowSet result = jdbc.queryForRowSet(sql2);
		int id = 0;
		while(result.next()) {
			id = result.getInt("transfer_id");
		}
		Transfer transfer = new Transfer(id, type, status, from, to, amount);
		return transfer;
	}
	

	
}