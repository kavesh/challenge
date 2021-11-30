package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.BalanceTransfers;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.BalanceTransferService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BalanceTransferServiceTest {
	
	@Autowired
	private AccountsService accountsService;
	
	@Autowired
	private BalanceTransferService balanceTransferService;

	  @Test
	  public void validateBalanceTransfer() throws Exception {
	    Account account = new Account("Id-123980");
	    account.setBalance(new BigDecimal(1000));
	    this.accountsService.createAccount(account);
	    
	    BalanceTransfers balanceTransfer = new BalanceTransfers("Id-123980", new BigDecimal(100) ,"Id-1234");
	    this.balanceTransferService.transferAmount(balanceTransfer);

	    assertThat(this.accountsService.getAccount("Id-123980").getBalance()).isEqualTo(new BigDecimal(900));
	  }
	  
		  @Test
		  public void validateBalanceTransfer_SameAsBalance() throws Exception {
		    Account account = new Account("Id-123981");
		    account.setBalance(new BigDecimal(1000));
		    this.accountsService.createAccount(account);
		    
		    BalanceTransfers balanceTransfer = new BalanceTransfers("Id-123981", new BigDecimal(1000) ,"Id-1234");
		    this.balanceTransferService.transferAmount(balanceTransfer);
		    
		    assertThat(this.accountsService.getAccount("Id-123981").getBalance()).isEqualTo(new BigDecimal(0));
		  }
		  
}
