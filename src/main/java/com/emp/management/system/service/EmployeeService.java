package com.emp.management.system.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.emp.management.system.model.Employee;
import com.emp.management.system.model.PhoneNumber;
import com.emp.management.system.model.VoterID;
import com.emp.management.system.repository.EmployeeRepository;
import com.emp.management.system.repository.PhoneNumberRepository;
import com.emp.management.system.repository.VoterIDRepository;
import com.emp.management.system.request.AccountHistoryRequest;
import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.request.PhoneNumberDTO;
import com.emp.management.system.request.TransactionRequest;
import com.emp.management.system.request.VoterIDDTO;
import com.emp.management.system.request.WithdrawRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.response.AccountResponse;
import com.emp.management.system.utils.LoggingUtil;

import jakarta.transaction.Transaction;



@Service
public class EmployeeService {
	@Autowired
    private  EmployeeRepository employeeRepository;
	
	@Autowired
    private  PhoneNumberRepository phoneNumberRepository;
	
	@Autowired
	private VoterIDRepository voterIDRepository;
	
	@Autowired
	private RestTemplate restTemplate;
	

	    private final String bankBaseUrl = "http://localhost:8989/BMS";
	    private final String bankCreateAccountUrl = bankBaseUrl + "/create-account";
	    private final String bankDepositUrl = bankBaseUrl + "/deposit";
	    private final String bankWithdrawalUrl = bankBaseUrl + "/withdraw";
	    private final String bankAccountHistoryUrl = "http://localhost:8989/BMS/account-history";



	
	public void createEmployeeFromDTO(EmployeeDTO employeeDTO) {
		LoggingUtil.logInfo("Creating employee from DTO");
        Employee employee = convertToEntity(employeeDTO);
        // Initialize account balance to zero
        employee.setBalance(0.0);


        if (employeeDTO.getPhoneNumbers() != null && !employeeDTO.getPhoneNumbers().isEmpty()) {
            List<PhoneNumber> phoneNumbers = convertPhoneNumbersToEntities(employeeDTO.getPhoneNumbers());
            for (PhoneNumber phoneNumber : phoneNumbers) {
                phoneNumber.setEmployee(employee);
            }
            employee.setPhoneNumbers(phoneNumbers);
        }

        if (employeeDTO.getVoterID() != null) {
            VoterID voterID = convertVoterIDToEntity(employeeDTO.getVoterID());
            employee.setVoterID(voterID);
        }

        createEmployee(employee);
       LoggingUtil.logInfo("Employee created successfully");
    }

    private void createEmployee(Employee employee) {
        try {
            employeeRepository.save(employee);
        } catch (Throwable t) {
            throw new IllegalArgumentException("Failed to create employee: " + t.toString());
        }
    }

//	private boolean isValidEmail(String email) {
//	    // Add your email validation logic here, for example, using regex or other checks.
//	    // For simplicity, let's assume any non-empty string is considered a valid email.
//	    return email != null && !email.isEmpty();
//	}

    public EmployeeDTO getEmployeeByIdWithAccount(Integer employeeId) {
        Optional<Employee> employee = employeeRepository.findByIdWithAccountDetails(employeeId);
        if (employee.isPresent()) {
            EmployeeDTO employeeDTO = convertToDTO(employee.get());
            
            // Set account details
            employeeDTO.setAccountNumber(employee.get().getAccountNumber());
            employeeDTO.setAccountType(employee.get().getAccountType());
            employeeDTO.setBalance(employee.get().getBalance());

            return employeeDTO;
        } else {
            // Handle the case when employee is not found
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }
    }


    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManagerId(Integer managerId) {
       LoggingUtil.logInfo("Fetching employees by manager ID");
        List<Employee> employees = employeeRepository.findByManagerId(managerId);
        if (!employees.isEmpty()) {
            List<EmployeeDTO> employeeDTOs = employees.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(employeeDTOs);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @Transactional
    public ResponseEntity<String> updateEmployeeDetails(Integer employeeId, EmployeeUpdateRequestDTO employeeUpdateRequestDTO) {
       LoggingUtil.logInfo("Updating employee details");
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found with id: " + employeeId);
        }
        Employee existingEmployee = optionalEmployee.get();

        // Update the employee details
        existingEmployee.setName(employeeUpdateRequestDTO.getName());
        existingEmployee.setDob(employeeUpdateRequestDTO.getDob());
        existingEmployee.setManagerId(employeeUpdateRequestDTO.getManagerId());
        existingEmployee.setSalary(employeeUpdateRequestDTO.getSalary());
        existingEmployee.setEmailId(employeeUpdateRequestDTO.getEmailId());

        
        // Update or create the phone numbers
        if (employeeUpdateRequestDTO.getPhoneNumbers() != null) {
            for (PhoneNumberDTO phoneNumberDTO : employeeUpdateRequestDTO.getPhoneNumbers()) {
                PhoneNumber phoneNumber;
                if (phoneNumberDTO.getPhoneId() != null) {
                    // Update existing phone number if found
                    Optional<PhoneNumber> optionalPhoneNumber = phoneNumberRepository.findById(phoneNumberDTO.getPhoneId());
                    if (optionalPhoneNumber.isPresent()) {
                        phoneNumber = optionalPhoneNumber.get();
                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
                        phoneNumber.setType(phoneNumberDTO.getType());
                    } else {
                        // If phoneId is not found, create a new PhoneNumber entity
                        phoneNumber = new PhoneNumber();
                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
                        phoneNumber.setType(phoneNumberDTO.getType());
                        phoneNumber.setEmployee(existingEmployee);
                    }
                } else {
                    // If phoneId is not provided, create a new PhoneNumber entity
                    phoneNumber = new PhoneNumber();
                    phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                    phoneNumber.setProvider(phoneNumberDTO.getProvider());
                    phoneNumber.setType(phoneNumberDTO.getType());
                    phoneNumber.setEmployee(existingEmployee);
                }
                phoneNumberRepository.save(phoneNumber);
            }
        }

        // Update or create the voter ID
        if (employeeUpdateRequestDTO.getVoterID() != null) {
            VoterIDDTO voterIDDTO = employeeUpdateRequestDTO.getVoterID();
            if (voterIDDTO.getVoterId() != null) {
                // Update existing voter ID if found
                Optional<VoterID> optionalVoterID = voterIDRepository.findById(voterIDDTO.getVoterId());
                if (optionalVoterID.isPresent()) {
                    VoterID voterID = optionalVoterID.get();
                    voterID.setVoterNumber(voterIDDTO.getVoterNumber());
                    voterID.setCity(voterIDDTO.getCity());
                    existingEmployee.setVoterID(voterID);
                } else {
                    // If voterId is not found, create a new VoterID entity
                    VoterID newVoterID = convertVoterIDToEntity(voterIDDTO);
                    existingEmployee.setVoterID(newVoterID);
                }
            } else {
                // If voterId is not provided, create a new VoterID entity
                VoterID newVoterID = convertVoterIDToEntity(voterIDDTO);
                existingEmployee.setVoterID(newVoterID);
            }
        }

        existingEmployee.setCreatedDateTime(existingEmployee.getCreatedDateTime()); // Preserve the existing createdDateTime
        existingEmployee.setUpdatedDateTime(LocalDateTime.now()); // Set the updatedDateTime to the current timestamp
        employeeRepository.save(existingEmployee);

        return ResponseEntity.ok("Employee details updated successfully");
    }

    public ResponseEntity<String> deleteEmployee(Integer id) {
       LoggingUtil.logInfo("Deleting employee");
        try {
            employeeRepository.deleteById(id);
            return ResponseEntity.ok("Employee deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete employee: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getAllEmployees() {
       LoggingUtil.logInfo("Fetching all employees");
        List<Employee> employees = employeeRepository.findAll();
        if (!employees.isEmpty()) {
            return ResponseEntity.ok(employees);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No employees found");
        }
    }
    
// --------------------------------------------------------------------------------------------------------------------------------
    public String createAccount(Integer employeeId, String accountType) {
    	LoggingUtil.logInfo("Creating account for employee with ID: {} and account type: {}", employeeId, accountType);
        // Check if the employee exists in the Employee table
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            return "Employee not found with ID: " + employeeId;
        }

        Employee employee = optionalEmployee.get();

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmployeeId(employeeId);
        createAccountRequest.setEmployeeName(employee.getName());
        createAccountRequest.setAccountType(accountType);
        

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CreateAccountRequest> request = new HttpEntity<>(createAccountRequest, headers);
            ResponseEntity<AccountResponse> responseEntity = restTemplate.postForEntity(bankCreateAccountUrl, request, AccountResponse.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                AccountResponse response = responseEntity.getBody();
                if (response != null) {
                    if (response.getStatus().equals("00")) {
                        // Update the employee's account details and save it to the database
                        employee.setAccountNumber(response.getAccountNumber());
                        employee.setAccountType(accountType);
                        employeeRepository.save(employee);
                        return "Account created successfully. Account Number: " + response.getAccountNumber() + ". " + response.getMessage();                
                        } else {
                        return "Account creation failed. " + response.getMessage();
                    }
                }
            } else {
                return "Failed to create an account for the employee with ID: " + employeeId;
            }
        } catch (Exception e) {
            return "Exception occurred while hitting create account using RestTemplate: " + e.getMessage();
        }

        return "Failed to create an account for the employee with ID: " + employeeId;
    }
    
//------------------------------------------------------------------------------------------------------------------------
    public String depositAmount(Integer employeeId, Double amount) {
    	 LoggingUtil.logInfo("Depositing amount for Employee ID: {}, Amount: {}", employeeId, amount);
    	// Prepare DepositRequest
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setEmployeeId(employeeId);
        depositRequest.setAmount(amount);

        // Set Content-Type header for JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with DepositRequest and headers
        HttpEntity<DepositRequest> request = new HttpEntity<>(depositRequest, headers);

        try {
            // Send the HTTP POST request to BMS to deposit amount
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(bankDepositUrl, request, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
            	 LoggingUtil.logInfo("Amount deposited successfully for Employee ID: {}, Amount: {}", employeeId, amount);
            	return responseEntity.getBody();
            } else {
            	 LoggingUtil.logError("Failed to deposit amount for Employee ID: {}, Amount: {}", employeeId, amount);
                return "Failed to deposit amount for Employee ID: " + employeeId;
            }
        } catch (Exception e) {
        	 LoggingUtil.logError("Exception occurred while hitting deposit amount using RestTemplate: {}", e.getMessage());
            return "Exception occurred while hitting deposit amount using RestTemplate: " + e.getMessage();
        }
    }
    
    
    //--------------------------------------------------------------------------------------------------------------

    public String withdrawAmount(Integer employeeId, Double amount) {
    	LoggingUtil.logInfo("Withdrawing amount for Employee ID: {}, Amount: {}", employeeId, amount);
    	// Prepare WithdrawRequest
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setEmployeeId(employeeId);
        withdrawRequest.setWithdrawalAmount(amount);

        // Set Content-Type header for JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with WithdrawRequest and headers
        HttpEntity<WithdrawRequest> request = new HttpEntity<>(withdrawRequest, headers);

        try {
            // Send the HTTP POST request to BMS to withdraw amount
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(bankWithdrawalUrl, request, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
            	LoggingUtil.logInfo("Amount withdrawn successfully for Employee ID: {}, Amount: {}", employeeId, amount);
                return responseEntity.getBody();
            } else {
            	 LoggingUtil.logError("Failed to withdraw amount for Employee ID: {}, Amount: {}", employeeId, amount);
                return "Failed to withdraw amount for Employee ID: " + employeeId;
            }
        } catch (Exception e) {
        	 LoggingUtil.logError("Exception occurred while hitting withdraw amount using RestTemplate: {}", e.getMessage());
            return "Exception occurred while hitting withdraw amount using RestTemplate: " + e.getMessage();
        }
    }


// -------------------------------------------------------------------------------------------------------------------

    public double getAccountBalance(Integer employeeId) {
        // Check if the employee exists in the Employee table
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        Employee employee = optionalEmployee.get();
        return employee.getBalance();
    }

//-----------------------------------------------------------------------------------------------------------------
    
//    public List<AccountHistoryResponse> getTransactionDetails(Integer employeeId, Date startDate, Date endDate) {
//        LoggingUtil.logInfo("Checking Account History for employee with ID: {}", employeeId);
//
//        // Check if the employee exists in the Employee table
//        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
//        if (optionalEmployee.isEmpty()) {
//            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
//        }
//
//        AccountHistoryRequest accountHistoryRequest = new AccountHistoryRequest();
//        accountHistoryRequest.setEmployeeId(employeeId);
//        accountHistoryRequest.setStartDate(startDate);
//        accountHistoryRequest.setEndDate(endDate);
//
//        // Set Content-Type header for JSON
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // Create HttpEntity
//        HttpEntity<AccountHistoryRequest> requestEntity = new HttpEntity<>(accountHistoryRequest, headers);
//
//        try {
//            // Send the HTTP POST request to BMS to get account history
//            ResponseEntity<AccountHistoryResponse[]> responseEntity = restTemplate.exchange(
//                bankAccountHistoryUrl,
//                HttpMethod.POST,
//                requestEntity,
//                AccountHistoryResponse[].class
//            );
//
//            if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            	LoggingUtil.logInfo("Checking Account History for employee with ID: {}", employeeId);
//                return Arrays.asList(responseEntity.getBody());
//            } else {
//            	LoggingUtil.logError("Failed to retrieve account history for Employee ID: {}", employeeId);
//                throw new IllegalArgumentException("Failed to retrieve account history for Employee ID: " + employeeId);
//            }
//        } catch (RestClientException e) {
//        	  LoggingUtil.logError("Exception occurred while hitting account-history using RestTemplate: {}", e.getMessage());
//            throw new IllegalArgumentException("Exception occurred while hitting account-history using RestTemplate", e);
//        }
//    }
//
    public List<AccountHistoryResponse> getTransactionDetails(Integer employeeId, Date startDate, Date endDate) {
        LoggingUtil.logInfo("Checking Account History for employee with ID: {}", employeeId);

        // Check if the employee exists in the Employee table
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }

        AccountHistoryRequest accountHistoryRequest = new AccountHistoryRequest();
        accountHistoryRequest.setEmployeeId(employeeId);
        accountHistoryRequest.setStartDate(startDate);
        accountHistoryRequest.setEndDate(endDate);

        // Set Content-Type header for JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity
        HttpEntity<AccountHistoryRequest> requestEntity = new HttpEntity<>(accountHistoryRequest, headers);

        try {
            // Send the HTTP POST request to BMS to get account history
            ResponseEntity<AccountHistoryResponse[]> responseEntity = restTemplate.exchange(
                bankAccountHistoryUrl,
                HttpMethod.POST,
                requestEntity,
                AccountHistoryResponse[].class
            );

            if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                LoggingUtil.logInfo("Checking Account History for employee with ID: {}", employeeId);
                return Arrays.asList(responseEntity.getBody());
            } else {
                LoggingUtil.logError("Failed to retrieve account history for Employee ID: {}", employeeId);
                throw new IllegalArgumentException("Failed to retrieve account history for Employee ID: " + employeeId);
            }
        } catch (RestClientException e) {
            LoggingUtil.logError("Exception occurred while hitting account-history using RestTemplate: {}", e.getMessage());
            throw new IllegalArgumentException("Exception occurred while hitting account-history using RestTemplate", e);
        }
    }

    
    
// --------------------------------------------------------------------------------------------------

	// Helper methods for conversion
    private Employee convertToEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeDTO.getEmployeeId());
        employee.setName(employeeDTO.getName());
        employee.setDob(employeeDTO.getDob());
        employee.setManagerId(employeeDTO.getManagerId());
        employee.setSalary(employeeDTO.getSalary());
        employee.setEmailId(employeeDTO.getEmailId());
        return employee;
    }

    private List<PhoneNumber> convertPhoneNumbersToEntities(List<PhoneNumberDTO> phoneNumberDTOs) {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        for (PhoneNumberDTO phoneNumberDTO : phoneNumberDTOs) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setPhoneId(phoneNumberDTO.getPhoneId());
            phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
            phoneNumber.setProvider(phoneNumberDTO.getProvider());
            phoneNumber.setType(phoneNumberDTO.getType());
            phoneNumbers.add(phoneNumber);
        }
        return phoneNumbers;
    }

    private VoterID convertVoterIDToEntity(VoterIDDTO voterIDDTO) {
        VoterID voterID = new VoterID();
        voterID.setVoterId(voterIDDTO.getVoterId());
        voterID.setEmployeeId(voterIDDTO.getEmployeeId());
        voterID.setVoterNumber(voterIDDTO.getVoterNumber());
        voterID.setCity(voterIDDTO.getCity());
        return voterID;
    }

 

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId(employee.getEmployeeId());
        employeeDTO.setName(employee.getName());
        employeeDTO.setDob(employee.getDob());
        employeeDTO.setManagerId(employee.getManagerId());
        employeeDTO.setSalary(employee.getSalary());
        employeeDTO.setEmailId(employee.getEmailId());
        employeeDTO.setCreatedDateTime(employee.getCreatedDateTime());
        employeeDTO.setUpdatedDateTime(employee.getUpdatedDateTime());
        System.out.println(employee.getCreatedDateTime());
        System.out.println(employee.getUpdatedDateTime());
        
         
       
        List<PhoneNumberDTO> phoneNumberDTOs = new ArrayList<>();
        for (PhoneNumber phoneNumber : employee.getPhoneNumbers()) {
            PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO();
            phoneNumberDTO.setPhoneId(phoneNumber.getPhoneId());
            phoneNumberDTO.setPhoneNumber(phoneNumber.getPhoneNumber());
            phoneNumberDTO.setProvider(phoneNumber.getProvider());
            phoneNumberDTO.setType(phoneNumber.getType());
            phoneNumberDTOs.add(phoneNumberDTO);
        }
        employeeDTO.setPhoneNumbers(phoneNumberDTOs);

        if (employee.getVoterID() != null) {
            VoterID voterID = employee.getVoterID();
            VoterIDDTO voterIDDTO = new VoterIDDTO();
            voterIDDTO.setVoterId(voterID.getVoterId());
            voterIDDTO.setEmployeeId(voterID.getEmployeeId());
            voterIDDTO.setVoterNumber(voterID.getVoterNumber());
            voterIDDTO.setCity(voterID.getCity());
            employeeDTO.setVoterID(voterIDDTO);
        }
        
        
     // Populate the fields from the Account object
        employeeDTO.setAccountNumber(employee.getAccountNumber());
        employeeDTO.setAccountType(employee.getAccountType());
        employeeDTO.setBalance(employee.getBalance());

        return employeeDTO;
    }
    
 //-----------------------------------------------------------------------   
    
    @Scheduled(cron = "1 * * * * *")
//  (cron = "0 0 0 5 * *")
  public void sendTaxableSalaryMessage() {
       LoggingUtil.logInfo("Sending taxable salary message");

      // Get all employees with a salary>1 lac
      List<Employee> taxableEmployees = employeeRepository.findBySalaryGreaterThan(new BigDecimal(100000));

      // Send a message to each employee
      for (Employee employee : taxableEmployees) {
          // Print the employee ID and employee name
//          System.out.println("Employee ID: " + employee.getEmployeeId()+"Employee Name: " + employee.getName());

          // Print the output in the desired format
          System.out.println(String.format("Employee %d - %s : Your salary is now in taxable range", employee.getEmployeeId(), employee.getName()));
      }

      scheduleMethodNow1();
  }

  public void scheduleMethodNow1() {
      displayEmployeesWithoutVoterId();
  }

  @Scheduled(cron = "0 1 * * * *")
  public void displayEmployeesWithoutVoterId() {
      LocalDateTime.now();
      // Get all employees without voterId
      List<Employee> employeesWithoutVoterId = employeeRepository.findByVoterIdIsNull();

      // Print the employee ID and employee name
      for (Employee employee : employeesWithoutVoterId) {
          System.out.println("Employee ID: " + employee.getEmployeeId() + ", Employee Name: " + employee.getName());
      }
  }

}

  
