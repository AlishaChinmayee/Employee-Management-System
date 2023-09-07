package com.emp.management.system.test.controller;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.emp.management.system.controller.EmployeeController;
import com.emp.management.system.exception.EmployeeNotFoundException;
import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DateRangeRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.request.WithdrawRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestAccountController {
	
	@Autowired
    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }
    

//------------------------------------------------CREATE ACCOUNT API----------------------------------------------------------    
    
    @Test
    public void testCreateAccount_Success() {
        // Mock the employeeService's behavior when creating an account successfully
        when(employeeService.createAccount(anyInt(), anyString())).thenReturn("Account created successfully");

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(1);
        createAccountRequest.setAccountType("Savings");

        ResponseEntity<String> response = employeeController.createAccount(createAccountRequest);

        assertEquals("Account created successfully", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testCreateAccount_Failure_DuplicateKeyException() {
        // Mock the employeeService's behavior to throw DuplicateKeyException
        when(employeeService.createAccount(anyInt(), anyString()))
            .thenThrow(DuplicateKeyException.class);

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(1);
        createAccountRequest.setAccountType("Savings");

        // Use assertThrows to verify that DuplicateKeyException is thrown
        assertThrows(DuplicateKeyException.class, () -> {
            ResponseEntity<String> response = employeeController.createAccount(createAccountRequest);
        });
    }

    @Test
    public void testCreateAccount_EmployeeNotFound() {
        // Mock the behavior of employeeService to throw EmployeeNotFoundException
        when(employeeService.createAccount(anyInt(), anyString()))
            .thenThrow(new EmployeeNotFoundException("Employee not found with ID: 1"));

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(1);
        createAccountRequest.setAccountType("Savings");

        // Use assertThrows to verify the exception
        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            ResponseEntity<String> response = employeeController.createAccount(createAccountRequest);
        });

        // Verify the exception message
        assertEquals("Employee not found with ID: 1", exception.getMessage());
    }
//----------------------------------------------------------------------------------------------------------------------------
//-----------------------------------------------DEPOSIT AMOUNT API-----------------------------------------------------------
    
    @Test
    public void testDepositAmountSuccess() {
        // Arrange
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setEmployeeId(1);
        depositRequest.setAmount(100.0);

        when(employeeService.depositAmount(1, 100.0)).thenReturn("Deposit successful");

        // Act
        ResponseEntity<String> response = employeeController.depositAmount(depositRequest);

        // Assert
        verify(employeeService, times(1)).depositAmount(1, 100.0);
        verifyNoMoreInteractions(employeeService);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deposit successful", response.getBody());
    }
    
    @Test
    public void testDepositAmountValidationError() {
        // Arrange
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setEmployeeId(1);
        depositRequest.setAmount(0.0);

        // Act
        ResponseEntity<String> response = employeeController.depositAmount(depositRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Deposit amount cannot be zero"));
    }

    
    
    @Test
    public void testDepositAmountException() {
        // Arrange
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setEmployeeId(1);
        depositRequest.setAmount(100.0);

        when(employeeService.depositAmount(1, 100.0))
            .thenThrow(new RuntimeException("Deposit failed"));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeController.depositAmount(depositRequest);
        });

        assertEquals("Deposit failed", exception.getMessage());
        verify(employeeService, times(1)).depositAmount(1, 100.0);
        verifyNoMoreInteractions(employeeService);
    }

//----------------------------------------------------------------------------------------------------------------------------    
//-----------------------------------------------WITHDRAW MONEY API----------------------------------------------------------
    
    @Test
    public void testWithdrawMoneySuccessful() {
        // Create a valid WithdrawRequest
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setEmployeeId(123); // Replace with the actual employeeId
        withdrawRequest.setWithdrawalAmount(50.0); // Replace with the actual withdrawal amount

        // Mock employeeService methods with specific parameters
        when(employeeService.getCurrentBalance(123)).thenReturn(1000.0); // Mocking the balance for the given employeeId
        when(employeeService.withdrawAmount(123, 50.0)).thenReturn("Withdrawal successful"); // Mocking the withdrawal response

        // Call the controller method
        ResponseEntity<String> response = employeeController.withdrawMoney(withdrawRequest);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Withdrawal successful", response.getBody());
        
        // Verify that the employeeService methods were called with the expected parameters
        verify(employeeService).getCurrentBalance(123); // Verify that getCurrentBalance was called with employeeId 123
        verify(employeeService).withdrawAmount(123, 50.0); // Verify that withdrawAmount was called with employeeId 123 and withdrawal amount 50.0
    }
    
    @Test
    public void testWithdrawMoneyWithValidationError() throws Exception {
        WithdrawRequest invalidRequest = new WithdrawRequest();
        invalidRequest.setEmployeeId(null); // Set an invalid employeeId (null) to trigger validation error
        invalidRequest.setWithdrawalAmount(0.0); // Set withdrawalAmount as 0 to trigger validation error

        mockMvc.perform(MockMvcRequestBuilders.post("/EMS/BMS/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Both fields cannot be null")); // Corrected error message
    }


    @Test
    public void testWithdrawMoneyWithInsufficientBalance() throws Exception {
        // Create a sample valid WithdrawRequest
        WithdrawRequest validRequest = new WithdrawRequest();
        validRequest.setEmployeeId(123); // Set the employee ID
        validRequest.setWithdrawalAmount(50.0); // Set the withdrawal amount (assuming it's valid)

        // Mock the employeeService to return insufficient balance
        when(employeeService.getCurrentBalance(123)).thenReturn(100.0); // Assuming 100 is less than the withdrawal amount

        mockMvc.perform(MockMvcRequestBuilders.post("/EMS/BMS/withdraw") // Use the correct URL path
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expect HTTP 200
                .andExpect(MockMvcResultMatchers.content().string("")); // Expect an empty response content
    }


    // Utility method to convert an object to JSON
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
//-------------------------------------------------------------------------------------------------------------------------------------------------
//------------------------------------------------------GET TRANSACTION HISTORY API----------------------------------------------------------------
    
    @Test
    public void testGetTransactionDetails_Success() {
        // Arrange
        DateRangeRequest dateRangeRequest = new DateRangeRequest();
        dateRangeRequest.setStartDate("2023-01-01"); // Use the date string format
        dateRangeRequest.setEndDate("2023-01-31");   // Use the date string format
        int employeeId = 1;

        // Create AccountHistoryResponse instances
        AccountHistoryResponse response1 = new AccountHistoryResponse(1, LocalDateTime.of(2023, 1, 15, 12, 0), "Deposit", 100.0);
        AccountHistoryResponse response2 = new AccountHistoryResponse(2, LocalDateTime.of(2023, 1, 20, 14, 30), "Withdrawal", 50.0);

        List<AccountHistoryResponse> accountHistory = Arrays.asList(response1, response2);

        when(employeeService.getTransactionDetails(employeeId, dateRangeRequest)).thenReturn(accountHistory);

        // Act
        ResponseEntity<?> response = employeeController.getTransactionDetails(employeeId, dateRangeRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<AccountHistoryResponse> responseList = (List<AccountHistoryResponse>) response.getBody();
        assertEquals(2, responseList.size());
    }

    @Test
    public void testGetTransactionDetails_InvalidDateRange() {
        // Arrange
        DateRangeRequest dateRangeRequest = new DateRangeRequest(); // Empty date range
        int employeeId = 1;

        // Act
        ResponseEntity<?> response = employeeController.getTransactionDetails(employeeId, dateRangeRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof String);
        assertEquals("Start date is missing. Please provide a valid startDate.", response.getBody());
    }

    @Test
    public void testGetTransactionDetails_EndDateBeforeStartDate() {
        // Arrange
        DateRangeRequest dateRangeRequest = new DateRangeRequest();
        dateRangeRequest.setStartDate("2023-02-01"); // End date is before start date
        dateRangeRequest.setEndDate("2023-01-31");
        int employeeId = 1;

        // Act
        ResponseEntity<?> response = employeeController.getTransactionDetails(employeeId, dateRangeRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof String);
        assertEquals("End date should be greater than or equal to the start date.", response.getBody());
    }


    @Test
    public void testGetTransactionDetails_IllegalArgumentException() {
        // Arrange
        DateRangeRequest dateRangeRequest = new DateRangeRequest();
        dateRangeRequest.setStartDate("2023-01-01"); // Set start date as a string
        dateRangeRequest.setEndDate("2023-01-31");   // Set end date as a string
        int employeeId = 1;

        when(employeeService.getTransactionDetails(employeeId, dateRangeRequest))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // Act
        ResponseEntity<?> response = employeeController.getTransactionDetails(employeeId, dateRangeRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof String);
        assertEquals("Invalid argument", response.getBody());
    }

//    @Test
//    public void testGetTransactionDetails_EmployeeNotFoundException() {
//        // Arrange
//        EmployeeService employeeService = mock(EmployeeService.class);
//        EmployeeController employeeController = new EmployeeController(employeeService);
//
//        DateRangeRequest dateRangeRequest = new DateRangeRequest();
//        dateRangeRequest.setStartDate("2023-01-01"); // Set the start date as a string
//        dateRangeRequest.setEndDate("2023-01-31");   // Set the end date as a string
//        int employeeId = 1;
//
//        // Mock the behavior to throw EmployeeNotFoundException
//        when(employeeService.getTransactionDetails(employeeId, dateRangeRequest))
//                .thenThrow(new EmployeeNotFoundException("Employee not found"));
//
//        // Act
//        ResponseEntity<?> response = employeeController.getTransactionDetails(employeeId, dateRangeRequest);
//
//        // Assert
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertTrue(response.getBody() instanceof String);
//        assertEquals("Employee not found", response.getBody());
//    }

//----------------------------------------------------------------------------------------------------------------------------------------------    


}




