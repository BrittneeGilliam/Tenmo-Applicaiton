package com.techelevator.tenmo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@PreAuthorize("isAuthenticated()")
@RestController
public class Controller {

	private AccountDAO accountDAO;
	private UserDAO userDAO;
	private TransferDAO transferDAO;
	
	public Controller (AccountDAO accountDAO, TransferDAO transferDAO, UserDAO userDAO) {
		this.accountDAO = accountDAO;
		this.transferDAO = transferDAO;
		this.userDAO = userDAO;
	}
	
		//returns list of all users
		@RequestMapping(path="users", method = RequestMethod.GET)
		public List<User> list() {
			return userDAO.findAll();
		}
		
		//returns user based on id
		@RequestMapping(path = "user-ids/", method = RequestMethod.GET)
		public User findUser(@RequestParam int id) {
			User user = userDAO.findUserById(id);
			return user;
		}
		
		//returns user based on username
		@RequestMapping(path = "user-names/", method = RequestMethod.GET)
		public User findUser(@RequestParam String name) {
			User user = userDAO.findByUsername(name);
			return user;
		}
		
		//returns balance of logged in user
		@RequestMapping(path="users/{id}/balance", method = RequestMethod.GET)
		public double viewBalance(@PathVariable int id) {
			return accountDAO.viewBalance(id);
		}

		//creates a send transfer that is pending approval
		@ResponseStatus(HttpStatus.CREATED)
		@RequestMapping(path="new-transfer", method = RequestMethod.POST)
		public Transfer newTransfer(@RequestBody Transfer transfer) {
			return transfer = transferDAO.createTransfer(transfer.getTypeId(), transfer.getStatusId(), 
					transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
		}
		
		//Changes given transfer's type to Approved and moves money between two accounts
		@ResponseStatus(HttpStatus.ACCEPTED)
		@RequestMapping(path = "new-transfer/approved", method = RequestMethod.PUT)
		public void transferMoney (@RequestBody Transfer transfer) {
			accountDAO.transferFunds(transfer);
		}
		
		//returns a Transfer by given id
		@RequestMapping(path = "transfers/{id}", method = RequestMethod.GET)
		public Transfer viewTransferById(@PathVariable("id") int transferId) {
			return transferDAO.viewTransfer(transferId);
		}
		
		//returns a list of transfers interacting with a given user id
		@RequestMapping(path = "users/{id}/transfers", method = RequestMethod.GET)
		public List<Transfer> getTransferHistory(@PathVariable int id){
			return transferDAO.transferHistory(id);
		}

	}