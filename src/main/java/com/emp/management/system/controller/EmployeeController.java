package com.emp.management.system.controller;


import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DateRangeRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.request.WithdrawRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.service.EmployeeService;
import com.emp.management.system.utils.LoggingUtil;


@Validated
@RestController
@RequestMapping("/employees")
public class EmployeeController {
	
	@Autowired
    private  EmployeeService employeeService;
	
		
	
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createEmployee(@RequestBody @Valid EmployeeDTO employeeDTO) {
        try {
            employeeService.createEmployeeFromDTO(employeeDTO);
            return ResponseEntity.ok("Employee created successfully");
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee creation failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create employee: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeByIdWithAccount(@PathVariable Integer id) {
        try {
            LoggingUtil.logInfo("Request received to get employee by ID");
            EmployeeDTO employeeDTO = employeeService.getEmployeeByIdWithAccount(id);
            return ResponseEntity.ok(employeeDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/managers/{managerId}")
    public ResponseEntity<?> getEmployeesByManagerId(@PathVariable Integer managerId) {
        if (managerId < 101 || managerId > 105) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Manager ID should be between 101 and 105");
        }
        
        LoggingUtil.logInfo("Request received to get employees by manager ID");
        return employeeService.getEmployeesByManagerId(managerId);
    }



    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateEmployeeDetails(@PathVariable Integer id,
                                                        @RequestBody @Valid EmployeeUpdateRequestDTO employeeUpdateRequestDTO) {
        LoggingUtil.logInfo("Request received to update employee details");
        ResponseEntity<String> response;
        try {
            response = employeeService.updateEmployeeDetails(id, employeeUpdateRequestDTO);
        } catch (ValidationException e) {
            response = ResponseEntity.badRequest().body(e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Integer id) {
        LoggingUtil.logInfo("Request received to delete employee");
        return employeeService.deleteEmployee(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        LoggingUtil.logInfo("Request received to get all employees");
        return employeeService.getAllEmployees();
    }
    
    @PostMapping("/create-account")
    public ResponseEntity<String> createAccount(@RequestBody CreateAccountRequest createAccountRequest) {
        String accountType = createAccountRequest.getAccountType(); 
        String result = employeeService.createAccount(createAccountRequest.getEmployeeId(), accountType);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/deposit")
    public ResponseEntity<String> depositAmount(@RequestBody DepositRequest depositRequest) {
        String validationMessage = depositRequest.validate();

        if (validationMessage != null) {
            return ResponseEntity.badRequest().body(validationMessage);
        }

        Integer employeeId = depositRequest.getEmployeeId();
        Double depositAmount = depositRequest.getAmount();

        if (depositAmount == 0) {
            // Get current balance logic (replace this with your actual logic)
            Double currentBalance = employeeService.getCurrentBalance(employeeId);

            String responseMessage = "Deposit amount cannot be zero. Current balance: " + currentBalance;
            return ResponseEntity.badRequest().body(responseMessage);
        }

        String response = employeeService.depositAmount(employeeId, depositAmount);
        return ResponseEntity.ok(response);
    }


    
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawMoney(@RequestBody WithdrawRequest withdrawRequest) {
        String validationError = withdrawRequest.validate();
        
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        Integer employeeId = withdrawRequest.getEmployeeId();
        Double withdrawalAmount = withdrawRequest.getWithdrawalAmount();
        
        Double currentBalance = employeeService.getCurrentBalance(employeeId);

        if (withdrawalAmount == 0) {
            return ResponseEntity.ok("WithdrawalAmount is 0. Current balance: " + currentBalance);
        }
        
        if (withdrawalAmount > currentBalance) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Insufficient Balance for the withdrawal. Current balance: " + currentBalance);
        }

        String response = employeeService.withdrawAmount(employeeId, withdrawalAmount);
        return ResponseEntity.ok(response);
    }

    
    
    @PostMapping("/transactions/{employeeId}")
    public ResponseEntity<?> getTransactionDetails(
            @PathVariable Integer employeeId,
            @RequestBody DateRangeRequest dateRangeRequest) {
        
        try {
            LoggingUtil.logInfo("Received request for employee ID: {}", employeeId);

            LocalDateTime startDate = dateRangeRequest.getStartDateTime();
            LocalDateTime endDate = dateRangeRequest.getEndDateTime();

            // Check if the start date and end date are valid
            if (startDate == null || endDate == null) {
                LoggingUtil.logError("Invalid date range provided for employee ID: {}", employeeId);
                return ResponseEntity.badRequest().body("Invalid date range provided. Make sure to provide both startDate and endDate.");
            }

            if (startDate.isAfter(endDate)) {
                LoggingUtil.logError("Invalid date range provided for employee ID: {}", employeeId);
                return ResponseEntity.badRequest().body("End date should be greater than or equal to the start date.");
            }

            List<AccountHistoryResponse> accountHistory = employeeService.getTransactionDetails(employeeId, dateRangeRequest);
            LoggingUtil.logInfo("Account history retrieved successfully for employee ID: {}", employeeId);
            return ResponseEntity.ok(accountHistory);
        } catch (IllegalArgumentException e) {
            LoggingUtil.logError("Error processing request for employee ID: {}", employeeId);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
    
    
    

