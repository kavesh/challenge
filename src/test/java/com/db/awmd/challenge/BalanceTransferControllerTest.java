package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class BalanceTransferControllerTest {
	
	private MockMvc mockMvc;
	
	@Autowired
	private AccountsService accountsService;
	
	 @Autowired
	  private WebApplicationContext webApplicationContext;

	  @Before
	  public void prepareMockMvc() {
	    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
	  }
	  
	  //Success 3rd Party Bank
	  @Test
	  public void createBalanceTransfer() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
			      .content("{\"accountId\":\"Id-12\",\"balance\":1000}")).andExpect(status().isCreated());  
		  
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12\",\"transferAmount\":10,\"toAccountId\":\"Id-12345678\"}")).andExpect(status().isCreated());

	    Account account = accountsService.getAccount("Id-12");
	    assertThat(account.getBalance()).isEqualByComparingTo("990");
	  }
	  
	  //Success within same bank
	  @Test
	  public void createBalanceTransferSameBank() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
			      .content("{\"accountId\":\"Id-1234\",\"balance\":1000}")).andExpect(status().isCreated());  
		
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
			      .content("{\"accountId\":\"Id-12345\",\"balance\":10}")).andExpect(status().isCreated());
		  
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-1234\",\"transferAmount\":10,\"toAccountId\":\"Id-12345\"}")).andExpect(status().isCreated());

	    Account account = accountsService.getAccount("Id-1234");
	    assertThat(account.getBalance()).isEqualByComparingTo("990");
	    
	    Account beneAccount = accountsService.getAccount("Id-12345");
	    assertThat(beneAccount.getBalance()).isEqualByComparingTo("20");
	  }
	  
	  //Negative amount in request
	  @Test
	  public void createBalanceTransferWithNegative() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12\",\"transferAmount\":-10,\"toAccountId\":\"Id-1234\"}")).andExpect(status().isBadRequest());
	  }
	  
	  //Zero amount in request
	  @Test
	  public void createBalanceTransferWithZero() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12\",\"transferAmount\":0,\"toAccountId\":\"Id-1234\"}")).andExpect(status().isBadRequest());
	  }
	  
	  //No amount in request
	  @Test
	  public void createBalanceTransferWithNoAmount() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12\",\"toAccountId\":\"Id-1234\"}")).andExpect(status().isBadRequest());
	  }
	  
	  //No sender account
	  @Test
	  public void createBalanceTransferWithNoSenderAccount() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"transferAmount\":0,\"toAccountId\":\"Id-1234\"}")).andExpect(status().isBadRequest());
	  }
	  
	  //No Beneficiary Account
	  @Test
	  public void createBalanceTransferWithNoBeneficiary() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12\",\"transferAmount\":0}")).andExpect(status().isBadRequest());
	  }
	  
	  //No Beneficiary Account
	  @Test
	  public void createBalanceTransferWithNoRequest() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON))
	      .andExpect(status().isBadRequest());
	  }
	  
	  //Invalid http Method
	  @Test
	  public void createBalanceTransferWithInvalidHttpMethod() throws Exception {
	    this.mockMvc.perform(get("/v1/transfers").contentType(MediaType.APPLICATION_JSON))
	      .andExpect(status().isMethodNotAllowed());
	  }
	  
	  //Invalid Url
	  @Test
	  public void createBalanceTransferWithInvalidUri() throws Exception {
	    this.mockMvc.perform(post("/v1/transfersData").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12\",\"transferAmount\":0,\"toAccountId\":\"Id-1234\"}"))
	      .andExpect(status().isNotFound());
	  }
	  
	  //Sender Account Not available
	  @Test
	  public void createBalanceTransferWithNoAccountRegisterd() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-123567\",\"transferAmount\":0,\"toAccountId\":\"Id-1234\"}"))
	      .andExpect(status().isBadRequest());
	  }
	  
	  //Insufficient Balance
	  @Test
	  public void createBalanceTransferWithInsufficentBalance() throws Exception {
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-1234\",\"transferAmount\":1000,\"toAccountId\":\"Id-1234\"}"))
	      .andExpect(status().isBadRequest());
	  }
	  
	  //Max balance account
	  @Test
	  public void createBalanceTransferWithMaxBalance() throws Exception {
		  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
			      .content("{\"accountId\":\"Id-12345699\",\"balance\":10}")).andExpect(status().isCreated());  
		  
	    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"Id-12345699\",\"transferAmount\":10,\"toAccountId\":\"Id-12345678\"}")).andExpect(status().isCreated());
	    
	    Account account = accountsService.getAccount("Id-12345699");
	    assertThat(account.getBalance()).isEqualByComparingTo("0");
	  }
}
