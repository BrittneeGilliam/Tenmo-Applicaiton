package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AccountServices;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;

public class App {

	private static final String API_BASE_URL = "http://localhost:8080/";

	private static final String MENU_OPTION_EXIT = "Exit";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN,
			MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
			MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS,
			MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };

	private static AuthenticatedUser currentUser;
	private static ConsoleService console;
	private static AuthenticationService authenticationService;
	private static RestTemplate restTemplate = new RestTemplate();
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	public static void mainMenu() {
		while (true) {
			String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				System.out.println("Your current balance is: $" + AccountServices.viewCurrentBalance());

			} else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				AccountServices.login();
			} else {
				exitProgram();
			}
		}
	}

	private static Transfer[] viewTransferHistory() {
		HttpEntity entity = getEntity();
		Transfer[] transferArray = restTemplate
				.exchange(API_BASE_URL + "users/" + currentUser.getUser().getId() + "/transfers", HttpMethod.GET,
						getEntity(), Transfer[].class).getBody();

		viewAndSelectTransfers(entity, transferArray);
		return transferArray;
	}

	private static void viewAndSelectTransfers(HttpEntity entity, Transfer[] transferArray) {
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
			mainMenu();
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
				
	private static void getTransferTypeStatusDetails(Transfer[] transferArray, String[] transferString, String[] transferType,
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

	private static void viewPendingRequests() {
	

	}

	private static void sendBucks() {

		String amtToSend = null;

		System.out.println("How much money would you like to send?");

		try {
			amtToSend = scanner.nextLine();
			if (!amtToSend.equals("0")) {
				
				BigDecimal BDtoSend = new BigDecimal(amtToSend);
				BigDecimal balance = AccountServices.viewCurrentBalance();
				if (balance.compareTo(BDtoSend) < 0) {
					System.out.println("Insufficient funds.");
					mainMenu();
				}
			} else {
				mainMenu();
			}
		} catch (NumberFormatException e) {
			System.out.println("invalid input.");
			sendBucks();
		}

		System.out.println("Who would you like to send it to?");
		User recipient = viewAndSelectUsers();

		Transfer transfer = new Transfer(2, 2, currentUser.getUser().getId(), recipient.getId(),
				Double.valueOf(amtToSend));

		String token = currentUser.getToken();
		HttpHeaders header = new HttpHeaders();
		header.setBearerAuth(token);

		HttpEntity<Transfer> entity2 = new HttpEntity<>(transfer, header);

		try {
			Transfer transfer2 = restTemplate
					.exchange(API_BASE_URL + "new-transfer", HttpMethod.POST, entity2, Transfer.class).getBody();
			approveTransfer(transfer2);

		} catch (Exception e) {

			System.out.println("Cannot process transfer. Please try again.");
			mainMenu();
		}
		System.out.println("Transfer complete.");
	}

	private static User viewAndSelectUsers() {
		HttpEntity entity = getEntity();
		User[] userArray = restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET, entity, User[].class)
				.getBody();
		String[] userNames = new String[userArray.length];
		for (int i = 0; i < userArray.length; i++) {
			String name = userArray[i].getUsername();
			userNames[i] = name;
		}
		String chosen = (String) console.getChoiceFromOptions(userNames);
		if (chosen.equals("0")) {
			mainMenu();
		}
		User recipient = restTemplate
				.exchange(API_BASE_URL + "user-names/?name=" + chosen, HttpMethod.GET, entity, User.class).getBody();
		return recipient;
	}

	private static void requestBucks() {
		

	}

	private static void approveTransfer(Transfer transfer) {
		if (new BigDecimal(transfer.getAmount()).compareTo(AccountServices.viewCurrentBalance()) == 1) {
			System.out.println("You don't have enough money.");
			mainMenu();
		}

		String token = currentUser.getToken();
		HttpHeaders header = new HttpHeaders();
		header.setBearerAuth(token);
		HttpEntity<Transfer> entityComplete = new HttpEntity<>(transfer, header);
		Transfer transferComplete = restTemplate
				.exchange(API_BASE_URL + "new-transfer/approved", HttpMethod.PUT, entityComplete, Transfer.class)
				.getBody();
	}

	private void rejectTransfer(Transfer transfer) {
		
	}


	private void registerAndLogin() {
		while (!isAuthenticated()) {
			String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				AccountServices.login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				AccountServices.register();
			} else {
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}


	private static void exitProgram() {
		System.exit(0);
	}

	
	private static HttpEntity getEntity() {
		String token = currentUser.getToken();

		HttpHeaders header = new HttpHeaders();
		header.setBearerAuth(token);

		HttpEntity entity = new HttpEntity<>(header);
		return entity;
	}

}

