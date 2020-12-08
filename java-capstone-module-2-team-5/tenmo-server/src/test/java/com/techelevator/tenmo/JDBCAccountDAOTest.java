//package com.techelevator.tenmo;
//
//import com.techelevator.tenmo.dao.AccountDAO;
//import com.techelevator.tenmo.dao.JDBCAccountDAO;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.support.rowset.SqlRowSet;
//
//import static org.junit.Assert.assertEquals;
//
//public class JDBCAccountDAOTest {
//
//	private AccountDAO dao;
//	private JdbcTemplate jdbcTemplate;
//
////This is a rough draft of my first ATTEMPT for View Balance. A few things need addressed/tinkered with. BSG. 
////I did add the dependency in the server pom.xml file, per Walt's instructions in Slack. 
//	
//@Before 
//	  	//public void setup() {
//        //dao = new JDBCAccountDAO(dataSource);
//		//jdbcTemplate = new JdbcTemplate(dataSource);
//	
//@Test
//	    public void viewBalance_Should_ReturnCurrentBalance() {
//	    	int expectedAccountId = 0;
//	    	SqlRowSet nextAccountId = jdbcTemplate.queryForRowSet("SELECT balance FROM accounts WHERE account_id = ?");
//			if (nextAccountId.next()) {
//				expectedAccountId = nextAccountId.getInt(1) + 1; 
//				//+1 because we are grabbing the next id before the method runs, 
//				//so this expected is 1 less than actual. The +1 reconciles the account_id
//			}
//	    	
//	    	double correctBalance = dao.viewBalance(1); 
//	        		
//
//	        //assertEquals(balance, expectedAccountId);
//	    }
//	
//	}
