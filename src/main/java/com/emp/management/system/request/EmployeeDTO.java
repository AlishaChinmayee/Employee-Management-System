package com.emp.management.system.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.emp.management.system.response.AccountHistoryResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@JsonInclude(Include.NON_NULL)
public class EmployeeDTO {
	
	    private Integer employeeId;
	    private String name;
	    private LocalDate dob;
	    private Integer managerId;
	    private BigDecimal salary;
	    private String emailId;
	    @JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during deserialization
	    private List<PhoneNumberDTO> phoneNumbers;
	    @JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during deserialization
	    private VoterIDDTO voterID;
	    private LocalDateTime createdDateTime;
	    private LocalDateTime updatedDateTime;
	    private String accountNumber;
	    private String accountType;
//	    private Double balance;
	    
	    
		public Integer getEmployeeId() {
			return employeeId;
		}
		public void setEmployeeId(Integer employeeId) {
			this.employeeId = employeeId;
		}
		public String getAccountType() {
			return accountType;
		}
		public void setAccountType(String accountType) {
			this.accountType = accountType;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public LocalDate getDob() {
			return dob;
		}
		public void setDob(LocalDate dob) {
			this.dob = dob;
		}
		public Integer getManagerId() {
			return managerId;
		}
		public void setManagerId(Integer managerId) {
			this.managerId = managerId;
		}
		public BigDecimal getSalary() {
			return salary;
		}
		public void setSalary(BigDecimal salary) {
			this.salary = salary;
		}
		public String getEmailId() {
			return emailId;
		}
		public void setEmailId(String emailId) {
			this.emailId = emailId;
		}
		public List<PhoneNumberDTO> getPhoneNumbers() {
			return phoneNumbers;
		}
		public void setPhoneNumbers(List<PhoneNumberDTO> phoneNumbers) {
			this.phoneNumbers = phoneNumbers;
		}
		public VoterIDDTO getVoterID() {
			return voterID;
		}
		public void setVoterID(VoterIDDTO voterID) {
			this.voterID = voterID;
		}
		public EmployeeDTO() {
			super();
		}
		public LocalDateTime getCreatedDateTime() {
			return createdDateTime;
		}
		public void setCreatedDateTime(LocalDateTime createdDateTime) {
			this.createdDateTime = createdDateTime;
		}
		public LocalDateTime getUpdatedDateTime() {
			return updatedDateTime;
		}
		public void setUpdatedDateTime(LocalDateTime updatedDateTime) {
			this.updatedDateTime = updatedDateTime;
		}

		 public void validate() throws ValidationException {
		        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		        Validator validator = factory.getValidator();
		        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(this);

		        if (!violations.isEmpty()) {
		            StringBuilder errorBuilder = new StringBuilder();
		            for (ConstraintViolation<EmployeeDTO> violation : violations) {
		                String fieldName = violation.getPropertyPath().toString();
		                String errorMessage = violation.getMessage();
		                errorBuilder.append(fieldName).append(": ").append(errorMessage).append("; ");
		            }

		            throw new ValidationException(errorBuilder.toString());
		        }
		    }
		 
		public String getAccountNumber() {
			return accountNumber;
		}
		public void setAccountNumber(String accountNumber) {
			this.accountNumber = accountNumber;
		}
		
		
		
}



