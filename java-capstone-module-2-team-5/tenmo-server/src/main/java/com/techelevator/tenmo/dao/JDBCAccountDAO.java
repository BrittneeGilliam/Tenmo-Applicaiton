package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Transfer;

@Component
public class JDBCAccountDAO implements AccountDAO{

	
	private JdbcTemplate jdbcTemplate;
	
	public JDBCAccountDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public double viewBalance(double id) {
		
		double balance = 0;
		
		String sqlBalanceSelect = "SELECT balance FROM accounts WHERE account_id = ?";
		SqlRowSet result = jdbcTemplate.queryForRowSet(sqlBalanceSelect, id);
		while(result.next()) {
			balance = result.getDouble("balance");
		}
		return balance;
	}

	@Override
	public void transferFunds(Transfer transfer) {
		BigDecimal fromBalance = new BigDecimal(0);
		BigDecimal toBalance = new BigDecimal(0);
		BigDecimal transferAmount = new BigDecimal(transfer.getAmount());

		String sqlTransferSelectFrom = "SELECT * FROM accounts WHERE account_id = ?";
		SqlRowSet result = jdbcTemplate.queryForRowSet(sqlTransferSelectFrom, transfer.getAccountFrom());
		while (result.next()) {
			fromBalance = (new BigDecimal(result.getDouble("balance")).setScale(2, RoundingMode.HALF_UP));
		}

		String sqlTransferSelectTo = "SELECT * FROM accounts WHERE account_id = ?";
		SqlRowSet result2 = jdbcTemplate.queryForRowSet(sqlTransferSelectTo, transfer.getAccountTo());
		while (result2.next()) {
			toBalance = (new BigDecimal(result2.getDouble("balance")).setScale(2, RoundingMode.HALF_UP));
		}

		fromBalance = fromBalance.subtract(transferAmount);
		toBalance = toBalance.add(transferAmount);

		//converting SQL int value to BigDecimal for correct money math

		double fromBalanceD = fromBalance.doubleValue();
		double toBalanceD = toBalance.doubleValue();

		//Converting BigDecimal back to double for SQL.

		String sqlTransferFrom = "UPDATE accounts SET balance = ?  WHERE account_id = ?";
		jdbcTemplate.update(sqlTransferFrom, fromBalanceD, transfer.getAccountFrom());

		String sqlTransferTo = "UPDATE accounts SET balance = ? WHERE account_id = ?";
		jdbcTemplate.update(sqlTransferTo, toBalanceD, transfer.getAccountTo());

		String sqlUpdateStatus = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?";
		jdbcTemplate.update(sqlUpdateStatus, transfer.getId());
	}

}
