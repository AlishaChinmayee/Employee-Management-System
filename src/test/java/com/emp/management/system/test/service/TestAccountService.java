package com.emp.management.system.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DateRangeRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.response.AccountResponse;
import com.emp.management.system.service.AccountService;

class TestAccountService {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountService accountService;
    
    private static final String BANK_DEPOSIT_URL = "http://localhost:8989/BMS/deposit";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String bankCreateAccountUrl = "http://localhost:8989/BMS/create-account";
        String bankDepositUrl = "http://localhost:8989/BMS/deposit";
        String bankWithdrawalUrl = "http://localhost:8989/BMS/withdraw";
        String bankAccountHistoryUrl = "http://localhost:8989/BMS/account-history"; 
        
        accountService = new AccountService(
            restTemplate,
            bankCreateAccountUrl,
            bankDepositUrl,
            bankWithdrawalUrl,
            bankAccountHistoryUrl
        );
    }

//------------------------------------------CREATE ACCOUNT FOR EMPLOYEE API-------------------------------------------------------------
    @Test
    void testCreateAccount_Success() {
        // Mock the response from the bank
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setStatus("00");
        accountResponse.setAccountNumber("123456789");
        accountResponse.setMessage("Account created successfully.");

        ResponseEntity<AccountResponse> responseEntity = new ResponseEntity<>(accountResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AccountResponse.class)))
                .thenReturn(responseEntity);

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(1);
        createAccountRequest.setEmployeeName("Alisha");
        createAccountRequest.setAccountType("Savings");

        String result = accountService.createAccount(createAccountRequest);

        assertEquals("Account created successfully. Account Number: 123456789. Account created successfully.", result);
    }

    @Test
    void testCreateAccount_Failure() {
        // Mock the response from the bank
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setStatus("99");
        accountResponse.setMessage("Account creation failed.");

        ResponseEntity<AccountResponse> responseEntity = new ResponseEntity<>(accountResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AccountResponse.class)))
                .thenReturn(responseEntity);

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(2);
        createAccountRequest.setEmployeeName("Alisha");
        createAccountRequest.setAccountType("Checking");

        String result = accountService.createAccount(createAccountRequest);

        assertEquals("Account creation failed. Account creation failed.", result);
    }
    

    @Test
    void testCreateAccount_Exception() {
        // Mock an exception when calling the restTemplate
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AccountResponse.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(1);
        createAccountRequest.setEmployeeName("Alisha");
        createAccountRequest.setAccountType("Savings");

        // Ensure that RuntimeException is thrown when createAccount is called
        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountService.createAccount(createAccountRequest));
        assertEquals("Connection timeout", exception.getMessage());

        // Verify that restTemplate.postForEntity was called with the expected arguments
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(AccountResponse.class));
    }
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------DEPOST AMOUNT API-------------------------------------------------------------
    
    @Test
    public void testDepositAmount_Success() {
        Integer employeeId = 1;
        Double amount = 100.0;

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setEmployeeId(employeeId);
        depositRequest.setAmount(amount);

        ResponseEntity<String> successResponse = new ResponseEntity<>("Deposit successful", HttpStatus.OK);

        when(restTemplate.postForEntity(eq(BANK_DEPOSIT_URL), any(), eq(String.class)))
                .thenReturn(successResponse);

        String result = accountService.depositAmount(employeeId, amount);

        assertEquals("Deposit successful", result);
    }

    @Test
    public void testDepositAmount_Failure() {
        Integer employeeId = 2;
        Double amount = 50.0;

        ResponseEntity<String> failureResponse = new ResponseEntity<>("Failed to deposit amount.", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(eq(BANK_DEPOSIT_URL), any(), eq(String.class)))
                .thenReturn(failureResponse);

        String result = accountService.depositAmount(employeeId, amount);

        assertEquals("Failed to deposit amount.", result);
    }


    @Test
    public void testDepositAmount_Exception() {
        Integer employeeId = 3;
        Double amount = 200.0;

        when(restTemplate.postForEntity(eq(BANK_DEPOSIT_URL), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Something went wrong"));

        String result = accountService.depositAmount(employeeId, amount);

        assertTrue(result.contains("Exception occurred while depositing amount"));
    }
    
//--------------------------------------------------------------------------------------------------------------------------------------    
//-------------------------------------------------WITHDRAW AMOUNT API------------------------------------------------------------------
    
    @Test
    void withdrawAmount_Success() {
        Integer employeeId = 1;
        Double amount = 100.0;

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Withdrawal successful.", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(responseEntity);

        String result = accountService.withdrawAmount(employeeId, amount);

        assertEquals("Withdrawal successful.", result);
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void withdrawAmount_Failure() {
        Integer employeeId = 1;
        Double amount = 100.0;

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Withdrawal failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(responseEntity);

        String result = accountService.withdrawAmount(employeeId, amount);

        assertEquals("Failed to withdraw amount.", result);
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void withdrawAmount_Exception() {
        Integer employeeId = 1;
        Double amount = 100.0;

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenThrow(new RuntimeException("Mocked exception"));

        String result = accountService.withdrawAmount(employeeId, amount);

        assertTrue(result.startsWith("Exception occurred while withdrawing amount using RestTemplate:"));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }
    
//--------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------GET TRANSACTIONS API----------------------------------------------------------------
    @Test
    void testGetTransactionDetails_Success() {
        // Prepare test data
        Integer employeeId = 123;
        DateRangeRequest dateRangeRequest = new DateRangeRequest();

        // Prepare mock response
        AccountHistoryResponse[] mockResponse = { new AccountHistoryResponse(), new AccountHistoryResponse() };
        ResponseEntity<AccountHistoryResponse[]> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Mock restTemplate behavior
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(AccountHistoryResponse[].class)))
                .thenReturn(responseEntity);

        // Call the method being tested
        List<AccountHistoryResponse> transactionDetails = accountService.getTransactionDetails(employeeId, dateRangeRequest);

        // Assertions
        assertEquals(2, transactionDetails.size());
    }

    @Test
    void testGetTransactionDetails_Failure() {
        // Prepare test data
        Integer employeeId = 123;
        DateRangeRequest dateRangeRequest = new DateRangeRequest();

        // Prepare mock response
        ResponseEntity<AccountHistoryResponse[]> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // Mock restTemplate behavior
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(AccountHistoryResponse[].class)))
                .thenReturn(responseEntity);

        // Call the method being tested
        List<AccountHistoryResponse> transactionDetails = accountService.getTransactionDetails(employeeId, dateRangeRequest);

        // Assertions
        assertTrue(transactionDetails.isEmpty());
    }

    @Test
    void testGetTransactionDetails_Exception() {
        // Prepare test data
        Integer employeeId = 123;
        DateRangeRequest dateRangeRequest = new DateRangeRequest();

        // Mock restTemplate to throw an exception
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(AccountHistoryResponse[].class)))
                .thenThrow(new RuntimeException("Mocked exception"));

        // Call the method being tested
        List<AccountHistoryResponse> transactionDetails = accountService.getTransactionDetails(employeeId, dateRangeRequest);

        // Assertions
        assertTrue(transactionDetails.isEmpty());
    }
    
//--------------------------------------------------------------------------------------------------------------------------------------    
}
