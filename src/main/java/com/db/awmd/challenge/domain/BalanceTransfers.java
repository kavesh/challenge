package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BalanceTransfers {

	@NotNull(message = "Sender account can't be Empty")
	@NotEmpty(message = "Sender account can't be Empty")
	private final String accountId;

	@NotNull(message = "Benefecary account can't be Empty")
	@NotEmpty(message = "Benefecary account can't be Empty")
	private final String toAccountId;

	@NotNull
	@Min(value = 1, message = "Amount should be greater than zero.")
	private BigDecimal transferAmount;

	public BalanceTransfers(String accountId, String toAccountId) {
		this.accountId = accountId;
		this.toAccountId = toAccountId;
		this.transferAmount = BigDecimal.ZERO;
	}

	@JsonCreator
	public BalanceTransfers(@JsonProperty("accountId") String accountId,
			@JsonProperty("transferAmount") BigDecimal transferAmount,
			@JsonProperty("toAccountId") String toAccountId) {
		this.accountId = accountId;
		this.transferAmount = transferAmount;
		this.toAccountId = toAccountId;
	}

}
