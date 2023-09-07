package com.emp.management.system.response;

import java.time.LocalDateTime;

public class AccountHistoryResponse {
	  
	    private String transactionId;
	    private String transactionType;
	    private String customerAccountNumber;
	    private Integer employeeId;
	    private Double balance;
	    private Double transactionAmount;
	    private LocalDateTime dateOfTransaction;
	    
		public AccountHistoryResponse() {
		
			
		}
		

	    public AccountHistoryResponse(Integer employeeId, LocalDateTime dateOfTransaction, String transactionType, Double transactionAmount) {
	        this.employeeId = employeeId;
	        this.dateOfTransaction = dateOfTransaction;
	        this.transactionType = transactionType;
	        this.transactionAmount = transactionAmount;
	    }

		public String getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}

		public String getTransactionType() {
			return transactionType;
		}

		public void setTransactionType(String transactionType) {
			this.transactionType = transactionType;
		}

		public String getCustomerAccountNumber() {
			return customerAccountNumber;
		}

		public void setCustomerAccountNumber(String customerAccountNumber) {
			this.customerAccountNumber = customerAccountNumber;
		}

		public Integer getEmployeeId() {
			return employeeId;
		}

		public void setEmployeeId(Integer employeeId) {
			this.employeeId = employeeId;
		}

		public Double getBalance() {
			return balance;
		}

		public void setBalance(Double balance) {
			this.balance = balance;
		}

		public Double getTransactionAmount() {
			return transactionAmount;
		}

		public void setTransactionAmount(Double transactionAmount) {
			this.transactionAmount = transactionAmount;
		}

		public LocalDateTime getDateOfTransaction() {
			return dateOfTransaction;
		}

		public void setDateOfTransaction(LocalDateTime dateOfTransaction) {
			this.dateOfTransaction = dateOfTransaction;
		}
	    
	    
	
	    
}
