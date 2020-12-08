package com.techelevator.tenmo.services;

	import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.App;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.view.ConsoleService;


public class AccountServices {
	private static AuthenticatedUser currentUser;
	private static ConsoleService console;
	private static AuthenticationService authenticationService;
	private static RestTemplate restTemplate = new RestTemplate();
	
	public AccountServices(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	Scanner scanner = new Scanner(System.in);
	
	AccountServices AccountServices = new AccountServices(new AuthenticationService(API_BASE_URL));
	
	private static final String API_BASE_URL = "http://localhost:8080/";
	
	
	public static BigDecimal viewCurrentBalance() {
		HttpEntity entity = getEntity();
		Double balance = restTemplate.exchange(API_BASE_URL + "users/" + currentUser.getUser().getId() + "/balance",
				HttpMethod.GET, entity, Double.class).getBody();

		BigDecimal balanceMoney = new BigDecimal(balance).setScale(2, RoundingMode.HALF_UP);
		return balanceMoney;
	}

	private Transfer[] viewTransferHistory() {
		HttpEntity entity = getEntity();
		Transfer[] transferArray = restTemplate
				.exchange(API_BASE_URL + "users/" + currentUser.getUser().getId() + "/transfers", HttpMethod.GET,
						getEntity(), Transfer[].class).getBody();

		viewAndSelectTransfers(entity, transferArray);
		return transferArray;
	}
	
	private void viewAndSelectTransfers(HttpEntity entity, Transfer[] transferArray) {
		String[] transferString = new String[transferArray.length];

		String[] accountFromUser = new String[transferArray.length];
		String[] accountToUser = new String[transferArray.length];
		String[] transferType = new String[transferArray.length];
		String[] transferStatus = new String[transferArray.length];

		System.out.println("Transfers");
		System.out.println("ID\tFrom/To \t Amount");
		System.out.println("---------------------------------------------");
		System.out.println("                                             ");

		for (int i = 0; i < transferArray.length; i++) {

			if (transferArray[i].getAccountFrom() == currentUser.getUser().getId()) {

				HttpEntity userEnt = getEntity();
				User recipient = restTemplate
						.exchange(API_BASE_URL + "user-ids/?" + "id=" + transferArray[i].getAccountTo(), HttpMethod.GET,
								userEnt, User.class).getBody();
			
				String transfer = transferArray[i].toString(recipient);
				transferString[i] = transfer;

				accountFromUser[i] = currentUser.getUser().getUsername();
				accountToUser[i] = recipient.getUsername();
				
				
				getTransferTypeStatusDetails(transferArray, transferString, transferType, transferStatus, i, transfer);

			}

			if (transferArray[i].getAccountTo() == currentUser.getUser().getId()) {

				HttpEntity userEnt = getEntity();
				User sender = restTemplate
						.exchange(API_BASE_URL + "user-ids/?" + "id=" + transferArray[i].getAccountFrom(),
								HttpMethod.GET, userEnt, User.class)
						.getBody();

				String transfer = transferArray[i].toString(sender);
				transferString[i] = transfer;

				accountFromUser[i] = sender.getUsername();
				accountToUser[i] = currentUser.getUser().getUsername();

				getTransferTypeStatusDetails(transferArray, transferString, transferType, transferStatus, i, transfer);
			}
		}
		String choice = (String) console.getChoiceFromOptions(transferString);
		if (choice.equals("0")) {
			App.mainMenu();
		}
		
		String[] choiceArray = choice.split("\\t");
		int choiceId = Integer.valueOf(choiceArray[0]);
		Transfer details = restTemplate
				.exchange(API_BASE_URL + "transfers/" + choiceId, HttpMethod.GET, entity, Transfer.class).getBody();

		for (int i = 0; i < transferArray.length; i++) {
			if (transferArray[i].getId() == choiceId) {
				System.out.println("Transfer: " + transferArray[i].getId());
				System.out.println("---------------------------------------------");
				System.out.println("ID: " + transferArray[i].getId());
				System.out.println("From: " + accountFromUser[i]);
				System.out.println("To: " + accountToUser[i]);
				System.out.println("Type: " + transferType[i]);
				System.out.println("Status: " + transferStatus[i]);
				System.out.println("Amount: $" + new BigDecimal(transferArray[i].getAmount()).setScale(2, RoundingMode.HALF_UP));
			}
		}
	}
	
private void approveTransfer(Transfer transfer) {
		if (new BigDecimal(transfer.getAmount()).compareTo(viewCurrentBalance()) == 1) {
			System.out.println("You don't have enough money.");
			App.mainMenu();
		}

		String token = currentUser.getToken();
		HttpHeaders header = new HttpHeaders();
		header.setBearerAuth(token);
		HttpEntity<Transfer> entityComplete = new HttpEntity<>(transfer, header);
		Transfer transferComplete = restTemplate
				.exchange(API_BASE_URL + "new-transfer/approved", HttpMethod.PUT, entityComplete, Transfer.class)
				.getBody();
	}
	
private void getTransferTypeStatusDetails(Transfer[] transferArray, String[] transferString, String[] transferType,
			String[] transferStatus, int i, String transferDetails) {
		
		if (transferArray[i].getTypeId() == 1) {
			transferType[i] = "Request";
		}else {
			transferType[i] = "Send";
		}

		if (transferArray[i].getStatusId() == 1) {
			transferStatus[i] = "Pending";
		} else if (transferArray[i].getStatusId() == 2) {
			transferStatus[i] = "Approved";
		} else {
			transferStatus[i] = "Rejected";
		}

		transferString[i] = transferDetails;
		
	}
		
	private static HttpEntity getEntity() {
		String token = currentUser.getToken();

		HttpHeaders header = new HttpHeaders();
		header.setBearerAuth(token);

		HttpEntity entity = new HttpEntity<>(header);
		return entity;
	}



	public static void register() {
	System.out.println("Please register a new user account");
	boolean isRegistered = false;
	while (!isRegistered)
	{
		UserCredentials credentials = collectUserCredentials();
		try {
			authenticationService.register(credentials);
			isRegistered = true;
			System.out.println("Registration successful. You can now login.");
		} catch (AuthenticationServiceException e) {
			System.out.println("REGISTRATION ERROR: " + e.getMessage());
			System.out.println("Please attempt to register again.");
		}
	}
}
	
	public static void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null)
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: " + e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}

	private static UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username").trim();
		String password = console.getUserInput("Password").trim();
		return new UserCredentials(username, password);
	}
}