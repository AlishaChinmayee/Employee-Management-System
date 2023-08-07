package com.emp.management.system.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emp.management.system.request.AccountHistoryRequest;
import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.request.WithdrawRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.service.EmployeeService;
import com.emp.management.system.utils.LoggingUtil;

import jakarta.transaction.Transaction;


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
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManagerId(@PathVariable Integer managerId) {
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
    
    @PostMapping("/create-account/{employeeId}")
    public ResponseEntity<String> createAccount(@PathVariable Integer employeeId, @RequestBody CreateAccountRequest createAccountRequest) {
        String accountType = createAccountRequest.getAccountType(); // Extract accountType from the request body
        String result = employeeService.createAccount(employeeId, accountType);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/deposit/{employeeId}")
    public ResponseEntity<String> depositAmount(@PathVariable Integer employeeId, @RequestBody DepositRequest depositRequest) {
        Double amount = depositRequest.getAmount(); // Extract amount from the request body

        String result = employeeService.depositAmount(employeeId, amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/withdraw/{employeeId}")
    public ResponseEntity<String> withdrawAmount(@PathVariable Integer employeeId, @RequestBody WithdrawRequest withdrawRequest) {
        Double amount = withdrawRequest.getWithdrawalAmount(); // Extract withdrawal amount from the request body

        String response = employeeService.withdrawAmount(employeeId, amount);
        return ResponseEntity.ok(response);
    }
    
//    @PostMapping("/accountHistory/{employeeId}")
//    public ResponseEntity<List<String>> getAccountHistory(
//            @PathVariable Integer employeeId,
//            @RequestBody AccountHistoryRequest request
//    ) {
//        List<AccountHistoryResponse> historyResponses = employeeService.getTransactionDetails(
//                employeeId,
//                request.getStartDate(),
//                request.getEndDate()
//        );
//
//        List<String> accountHistory = new ArrayList<>();
//
//        for (AccountHistoryResponse response : historyResponses) {
//            String historyEntry = "Transaction Type: " + response.getTransactionType() +
//                    ", Account Number: " + response.getCustomerAccountNumber() +
//                    ", Date of Transaction: " + response.getDateOfTransaction();
//            accountHistory.add(historyEntry);
//        }
//
//        return ResponseEntity.ok(accountHistory);
//    }

    @PostMapping("/accountHistory/{employeeId}")
    public ResponseEntity<List<AccountHistoryResponse>> getAccountHistory(
            @PathVariable Integer employeeId,
            @RequestBody AccountHistoryRequest request
    ) {
        List<AccountHistoryResponse> historyResponses = employeeService.getTransactionDetails(
                employeeId,
                request.getStartDate(),
                request.getEndDate()
        );

        return ResponseEntity.ok(historyResponses);
    }

 

}

    	

    
    
    

