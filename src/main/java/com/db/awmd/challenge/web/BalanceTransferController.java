package com.db.awmd.challenge.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.BalanceTransfers;
import com.db.awmd.challenge.service.BalanceTransferService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
public class BalanceTransferController {
	
	private final BalanceTransferService balanceTransferService;

	  @Autowired
	  public BalanceTransferController(BalanceTransferService balanceTransferService) {
	    this.balanceTransferService = balanceTransferService;
	  }


	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<Object> balanceTransfers(@RequestBody @Valid BalanceTransfers balanceTransfer) {
	    log.info("Iniate a Balance Transfer {}", balanceTransfer);

	    return this.balanceTransferService.transferAmount(balanceTransfer);
	  }
}
