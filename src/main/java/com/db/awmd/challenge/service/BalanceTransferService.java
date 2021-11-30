package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.BalanceTransfers;
import com.db.awmd.challenge.exception.BuisnessException;
import com.db.awmd.challenge.utility.Constants;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BalanceTransferService {
	
	@Getter
	  private final AccountsService accountsService;
	
	@Getter
	private final NotificationService emailNotificationService;

	  @Autowired
	  public BalanceTransferService(AccountsService accountsService, NotificationService emailNotificationService) {
	    this.accountsService = accountsService;
	    this.emailNotificationService = emailNotificationService;
	  }

	public ResponseEntity<Object> transferAmount(BalanceTransfers balanceTransfer) {
		log.info("Inside Balance transfer service transferAmount()");
		
		String transactionId = String.valueOf(LocalDateTime.now().getNano());
		
		//check if account number is not same
		if(checkSameAccount(balanceTransfer)) {
			log.error(Constants.SAME_ACCOUNT_TRANSFER);
			return new ResponseEntity<>(Constants.SAME_ACCOUNT_TRANSFER, HttpStatus.BAD_REQUEST);
		}
		
		
		//Initiate balance transfer
		Account benefeciaryAccount = accountsService.getAccount(balanceTransfer.getToAccountId());
		if(Optional.ofNullable(benefeciaryAccount).isEmpty()) {
			log.info("Iniate 3rd party bank transfer");
			try {
				intraBankTransfer(balanceTransfer, transactionId);
			} catch (BuisnessException be) {
				return new ResponseEntity<>(be.getMessage(), HttpStatus.BAD_REQUEST);
			} catch (Exception e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			return new ResponseEntity<>(Constants.THIRD_PARTY_TRANSFER.concat(transactionId), HttpStatus.CREATED);
			
		} else {
			log.info("Iniate same bank transfer");
			try {
				interBankTransfer(balanceTransfer, transactionId);
			} catch (BuisnessException be) {
				return new ResponseEntity<>(be.getMessage(), HttpStatus.BAD_REQUEST);
			} catch (Exception e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			return new ResponseEntity<>(Constants.SAME_BANK_TRANSFER.concat(transactionId), HttpStatus.CREATED);
		}
		
	}

	private void interBankTransfer(BalanceTransfers balanceTransfer, String transactionId) throws BuisnessException {
		synchronized (balanceTransfer) {
			//check if beneficiary or sender is present
			Account senderAccount = accountsService.getAccount(balanceTransfer.getAccountId());
			if(Optional.ofNullable(senderAccount).isEmpty()) {
				log.error(Constants.SENDER_ACCOUNT_NOT_PRESENT);
				throw new BuisnessException(Constants.SENDER_ACCOUNT_NOT_PRESENT);
			}
			
			//check balance if it goes in negative
			BigDecimal calculatedAmount = calculateAmount(senderAccount.getBalance(),balanceTransfer.getTransferAmount(), Constants.DEBIT);
			if(validateAmount(calculatedAmount)) {
				log.error(Constants.INSUFFICENT_BALANCE);
				notifyUser(senderAccount, Constants.INSUFFICENT_BALANCE);
				throw new BuisnessException(Constants.INSUFFICENT_BALANCE);
			}
				
			senderAccount.setBalance(calculatedAmount);
			accountsService.updateBalance(senderAccount);
			
			notifyUser(senderAccount, balanceTransfer.getTransferAmount() +" Has been debited from you account");
			
			Account benefeciaryAccount = accountsService.getAccount(balanceTransfer.getToAccountId());
			benefeciaryAccount.setBalance(calculateAmount(benefeciaryAccount.getBalance(),balanceTransfer.getTransferAmount(), Constants.CREDIT));
			accountsService.updateBalance(benefeciaryAccount);
			
			notifyUser(benefeciaryAccount, balanceTransfer.getTransferAmount() +" Has been credited in your account");
			notifyUser(senderAccount, balanceTransfer.getTransferAmount() + "Has been Sucessfully Transfered to beneficary account "+balanceTransfer.getToAccountId());
		}		
	}

	private void intraBankTransfer(BalanceTransfers balanceTransfer, String transactionId) throws BuisnessException {
		synchronized (balanceTransfer) {
			//check if beneficiary or sender is present
			Account senderAccount = accountsService.getAccount(balanceTransfer.getAccountId());
			if(Optional.ofNullable(senderAccount).isEmpty()) {
				log.error(Constants.SENDER_ACCOUNT_NOT_PRESENT);
				throw new BuisnessException(Constants.SENDER_ACCOUNT_NOT_PRESENT);
			}
			
			//check balance if it goes in negative
			BigDecimal calculatedAmount = calculateAmount(senderAccount.getBalance(),balanceTransfer.getTransferAmount(), Constants.DEBIT);
			if(validateAmount(calculatedAmount)) {
				log.error(Constants.INSUFFICENT_BALANCE);
				notifyUser(senderAccount, Constants.INSUFFICENT_BALANCE);
				throw new BuisnessException(Constants.INSUFFICENT_BALANCE);
			}
			
			senderAccount.setBalance(calculatedAmount);
			accountsService.updateBalance(senderAccount);
			notifyUser(senderAccount, balanceTransfer.getTransferAmount() +" Has been debited from you account, and transfer has been iniated to "+balanceTransfer.getToAccountId());
		}
	}

	private boolean validateAmount(BigDecimal calculatedAmount) {
		return calculatedAmount.compareTo(BigDecimal.ZERO) >= 0 ? false : true;
	}

	private BigDecimal calculateAmount(BigDecimal balance, BigDecimal transferAmount, String opreationType) throws BuisnessException {
		switch (opreationType) {
		case "Debit":
			return balance.subtract(transferAmount);
		case "Credit":
			return balance.add(transferAmount);
		default:
			throw new BuisnessException("Invalid Opreation Type Defined");
		}

	}

	private boolean checkSameAccount(BalanceTransfers balanceTransfer) {
		return balanceTransfer.getAccountId().equalsIgnoreCase(balanceTransfer.getToAccountId());
	}
	
	@Async("notificationService")
	private void notifyUser(Account account, String messageType) {
		emailNotificationService.notifyAboutTransfer(account, messageType);
	}
	
}
