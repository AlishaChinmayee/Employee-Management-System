package com.emp.management.system.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DateRangeRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.request.WithdrawRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.response.AccountResponse;

@Service
public class AccountService {

    private final RestTemplate restTemplate;
    private final String bankCreateAccountUrl;
    private final String bankDepositUrl;
    private final String bankWithdrawalUrl;
    private final String bankAccountHistoryUrl;

    @Autowired
    public AccountService(RestTemplate restTemplate,
    		@Value("${bank.createAccountUrl}") String bankCreateAccountUrl,
    		@Value("${bank.depositUrl}") String bankDepositUrl,
            @Value("${bank.withdrawalUrl}") String bankWithdrawalUrl,
            @Value("${bank.accountHistoryUrl}") String bankAccountHistoryUrl) {
        this.restTemplate = restTemplate;
        this.bankCreateAccountUrl = bankCreateAccountUrl;
        this.bankDepositUrl = bankDepositUrl;
        this.bankWithdrawalUrl = bankWithdrawalUrl;
        this.bankAccountHistoryUrl = bankAccountHistoryUrl;
    }


    public String createAccount(CreateAccountRequest createAccountRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateAccountRequest> request = new HttpEntity<>(createAccountRequest, headers);
        ResponseEntity<AccountResponse> responseEntity = restTemplate.postForEntity(bankCreateAccountUrl, request, AccountResponse.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AccountResponse response = responseEntity.getBody();
            if (response != null) {
                if (response.getStatus().equals("00")) {
                    return "Account created successfully. Account Number: " + response.getAccountNumber() + ". " + response.getMessage();
                } else {
                    return "Account creation failed. " + response.getMessage();
                }
            }
        } else {
            return "Failed to create an account.";
        }

        return "Failed to create an account.";
    }
    
    public String depositAmount(Integer employeeId, Double amount) {
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setEmployeeId(employeeId);
        depositRequest.setAmount(amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DepositRequest> request = new HttpEntity<>(depositRequest, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(bankDepositUrl, request, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                return "Failed to deposit amount.";
            }
        } catch (Exception e) {
            return "Exception occurred while depositing amount using RestTemplate: " + e.getMessage();
        }
    }

    public String withdrawAmount(Integer employeeId, Double amount) {
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setEmployeeId(employeeId);
        withdrawRequest.setWithdrawalAmount(amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<WithdrawRequest> request = new HttpEntity<>(withdrawRequest, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(bankWithdrawalUrl, request, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                return "Failed to withdraw amount.";
            }
        } catch (Exception e) {
            return "Exception occurred while withdrawing amount using RestTemplate: " + e.getMessage();
        }
    }

    public List<AccountHistoryResponse> getTransactionDetails(Integer employeeId, DateRangeRequest dateRangeRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DateRangeRequest> requestEntity = new HttpEntity<>(dateRangeRequest, headers);

        try {
            ResponseEntity<AccountHistoryResponse[]> responseEntity = restTemplate.exchange(
                    bankAccountHistoryUrl + "/" + employeeId,
                    HttpMethod.POST,
                    requestEntity,
                    AccountHistoryResponse[].class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return Arrays.asList(responseEntity.getBody());
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
