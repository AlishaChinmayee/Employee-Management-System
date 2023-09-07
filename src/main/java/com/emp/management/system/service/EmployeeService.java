package com.emp.management.system.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.emp.management.system.exception.InsufficientBalanceException;
import com.emp.management.system.model.Employee;
import com.emp.management.system.model.PhoneNumber;
import com.emp.management.system.model.VoterID;
import com.emp.management.system.repository.EmployeeRepository;
import com.emp.management.system.repository.PhoneNumberRepository;
import com.emp.management.system.repository.VoterIDRepository;
import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.DateRangeRequest;
import com.emp.management.system.request.DepositRequest;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.request.PhoneNumberDTO;
import com.emp.management.system.request.VoterIDDTO;
import com.emp.management.system.request.WithdrawRequest;
import com.emp.management.system.response.AccountHistoryResponse;
import com.emp.management.system.response.AccountResponse;
import com.emp.management.system.utils.LoggingUtil;



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
	    private final String bankAccountHistoryUrl = "http://localhost:8989/BMS/transactions";


	    public void createEmployeeFromDTO(EmployeeDTO employeeDTO) {
	        LoggingUtil.logInfo("Creating employee from DTO");

	        Employee existingEmployeeById = employeeRepository.findByEmployeeId(employeeDTO.getEmployeeId());
	        Employee existingEmployeeByEmail = employeeRepository.findByEmailId(employeeDTO.getEmailId());

	        if (existingEmployeeById != null && existingEmployeeByEmail != null) {
	            String errorMessage = "Employee with both employeeId and emailId already exists";
	            LoggingUtil.logInfo(errorMessage);
	            throw new IllegalArgumentException(errorMessage);
	        } else if (existingEmployeeById != null) {
	            String errorMessage = "Employee with employeeId " + employeeDTO.getEmployeeId() + " already exists";
	            LoggingUtil.logInfo(errorMessage);
	            throw new IllegalArgumentException(errorMessage);
	        } else if (existingEmployeeByEmail != null) {
	            String errorMessage = "Employee with emailId " + employeeDTO.getEmailId() + " already exists";
	            LoggingUtil.logInfo(errorMessage);
	            throw new IllegalArgumentException(errorMessage);
	        }

	        // Proceed with employee creation logic
	        Employee employee = convertToEntity(employeeDTO);

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

	    public void createEmployee(Employee employee) {
	        try {
	            employeeRepository.save(employee);
	        } catch (Throwable t) {
	            throw new IllegalArgumentException("Failed to create employee: " + t.toString());
	        }
	    }

    public EmployeeDTO getEmployeeByIdWithAccount(Integer employeeId) {
        Optional<Employee> employee = employeeRepository.findByIdWithDetails(employeeId);
        if (employee.isPresent()) {
            EmployeeDTO employeeDTO = convertToDTO(employee.get());
            
            // Set account details
            employeeDTO.setAccountNumber(employee.get().getAccountNumber());
            employeeDTO.setAccountType(employee.get().getAccountType());
//            employeeDTO.setBalance(employee.get().getBalance());

            return employeeDTO;
        } else {
            // Handle the case when employee is not found
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }
    }
    
    
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManagerId(Integer managerId) {
        LoggingUtil.logInfo("Fetching employees by manager ID");
        List<Employee> employees = employeeRepository.findByManagerId(managerId);
        
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        for (Employee employee : employees) {
            employeeDTOs.add(convertToDTO(employee));
        }
        
        if (!employeeDTOs.isEmpty()) {
            return ResponseEntity.ok(employeeDTOs);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    public ResponseEntity<String> updateEmployeeDetails(Integer employeeId, EmployeeUpdateRequestDTO employeeUpdateRequestDTO) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);

        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found with id: " + employeeId);
        }

        Employee existingEmployee = optionalEmployee.get();

        // Update the fields that are present in the request body
        if (employeeUpdateRequestDTO.getName() != null) {
            existingEmployee.setName(employeeUpdateRequestDTO.getName());
        }

        if (employeeUpdateRequestDTO.getDob() != null) {
            existingEmployee.setDob(employeeUpdateRequestDTO.getDob());
        }

        if (employeeUpdateRequestDTO.getManagerId() != null) {
            existingEmployee.setManagerId(employeeUpdateRequestDTO.getManagerId());
        }

        if (employeeUpdateRequestDTO.getSalary() != null) {
            existingEmployee.setSalary(employeeUpdateRequestDTO.getSalary());
        }

        if (employeeUpdateRequestDTO.getEmailId() != null) {
            existingEmployee.setEmailId(employeeUpdateRequestDTO.getEmailId());
        }

        if (employeeUpdateRequestDTO.getPhoneNumbers() != null && !employeeUpdateRequestDTO.getPhoneNumbers().isEmpty()) {
            List<PhoneNumber> updatedPhoneNumbers = updateOrCreatePhoneNumbers(existingEmployee, employeeUpdateRequestDTO.getPhoneNumbers());
            existingEmployee.setPhoneNumbers(updatedPhoneNumbers);
        }

        // Update or create voter ID
        if (employeeUpdateRequestDTO.getVoterID() != null) {
            if (existingEmployee.getVoterID() != null) {
                updateVoterID(existingEmployee.getVoterID(), employeeUpdateRequestDTO.getVoterID());
            } else {
                VoterID newVoterID = convertVoterIDToEntity(employeeUpdateRequestDTO.getVoterID());
                existingEmployee.setVoterID(newVoterID);
            }
        }

        // Update timestamps
        existingEmployee.setUpdatedDateTime(LocalDateTime.now());

        // Save the updated employee
        employeeRepository.save(existingEmployee);

        return ResponseEntity.ok("Employee details updated successfully");
    }

    private List<PhoneNumber> updateOrCreatePhoneNumbers(Employee employee, List<PhoneNumberDTO> phoneNumberDTOs) {
        List<PhoneNumber> updatedPhoneNumbers = new ArrayList<>();
        for (PhoneNumberDTO phoneNumberDTO : phoneNumberDTOs) {
            if (phoneNumberDTO.getPhoneId() != null) {
                Optional<PhoneNumber> optionalPhoneNumber = phoneNumberRepository.findById(phoneNumberDTO.getPhoneId());
                optionalPhoneNumber.ifPresent(phoneNumber -> {
                    phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                    phoneNumber.setProvider(phoneNumberDTO.getProvider());
                    phoneNumber.setType(phoneNumberDTO.getType());
                    updatedPhoneNumbers.add(phoneNumber);
                });
            } else {
                PhoneNumber newPhoneNumber = new PhoneNumber();
                newPhoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                newPhoneNumber.setProvider(phoneNumberDTO.getProvider());
                newPhoneNumber.setType(phoneNumberDTO.getType());
                newPhoneNumber.setEmployee(employee);
                updatedPhoneNumbers.add(newPhoneNumber);
            }
        }
        return updatedPhoneNumbers;
    }

    private void updateVoterID(VoterID existingVoterID, VoterIDDTO voterIDDTO) {
        existingVoterID.setVoterNumber(voterIDDTO.getVoterNumber());
        existingVoterID.setCity(voterIDDTO.getCity());
    }

    
    
    public ResponseEntity<String> deleteEmployee(Integer id) {
        LoggingUtil.logInfo("Deleting employee");
        try {
            if (employeeRepository.existsById(id)) {
                employeeRepository.deleteById(id);
                return ResponseEntity.ok("Employee deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + id + " doesn't exist");
            }
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

        // Check if the employee exists
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            LoggingUtil.logError("Employee not found with ID: {}", employeeId);
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        Employee employee = optionalEmployee.get();
        String accountNumber = employee.getAccountNumber();

        // Check if an account for the employee exists
        if (accountNumber == null || accountNumber.isEmpty()) {
            LoggingUtil.logError("No account found for Employee ID: {}", employeeId);
            throw new RuntimeException("No account found for Employee ID: " + employeeId);
        }

        // Validate the deposit amount
        if (amount <= 0) {
            LoggingUtil.logError("Invalid deposit amount: {}", amount);
            throw new IllegalArgumentException("Invalid deposit amount: " + amount);
        }

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

                // Update employee balance and save
                Double currentBalance = employee.getBalance();
                Double updatedBalance = currentBalance + amount;
                employee.setBalance(updatedBalance);
                employeeRepository.save(employee);

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
    	  
    	 // Check if the employee exists
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            LoggingUtil.logError("Employee not found with ID: {}", employeeId);
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        Employee employee = optionalEmployee.get();

        // Check if an account for the employee exists
        if (employee.getAccountNumber() == null || employee.getAccountNumber().isEmpty()) {
            LoggingUtil.logError("No account found for Employee ID: {}", employeeId);
            throw new RuntimeException("No account found for Employee ID: " + employeeId);
        }
    	
    	 // Perform the withdraw operation
        if (amount < 0) {
            throw new IllegalArgumentException("Withdraw amount cannot be negative.");
        }

        Double Balance = getCurrentBalance(employeeId);
        if (amount > Balance) {
            throw new InsufficientBalanceException("Insufficient balance for Employee ID " + employeeId);
        }

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

             // Update employee balance and save
                Double CurrentBalance = employee.getBalance();
                Double updatedBalance = CurrentBalance - amount;
                employee.setBalance(updatedBalance);
                employeeRepository.save(employee);

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

    public Double getCurrentBalance(Integer employeeId) {
        // Check if the employee exists in the Employee table
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

       Employee employee = optionalEmployee.get();
        return employee.getBalance();
    }
   

//-----------------------------------------------------------------------------------------------------------------

    public List<AccountHistoryResponse> getTransactionDetails(Integer employeeId, DateRangeRequest dateRangeRequest) {
        LoggingUtil.logInfo("Fetching transaction details for employee ID: {}", employeeId);

        LocalDateTime startDate = dateRangeRequest.getStartDateTime();
        LocalDateTime endDate = dateRangeRequest.getEndDateTime();

        // Validate date range
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            LoggingUtil.logError("Invalid date range provided for employee ID: {}", employeeId);
            throw new IllegalArgumentException("Invalid date range provided. Make sure to provide both startDate and endDate.");
        }
        // Check if the employee exists
        Optional<Employee> optionalEmployee = employeeRepository.findByIdWithDetails(employeeId);
        if (optionalEmployee.isEmpty()) {
            LoggingUtil.logError("Employee not found with ID: {}", employeeId);
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        } 
        Employee employee = optionalEmployee.get();

        // Fetch the account number from the employee
        String accountNumber = employee.getAccountNumber();

        // Check if the account number exists for the employee
        if (accountNumber == null || accountNumber.isEmpty()) {
            LoggingUtil.logError("No account number found for employee ID: {}", employeeId);
            throw new IllegalArgumentException("No account number found for employee ID: " + employeeId);
        }

        // Hit the Transaction Details API using RestTemplate
        try {
            LoggingUtil.logInfo("Hitting account-history API for employee ID: {}", employeeId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
         
            HttpEntity<DateRangeRequest> requestEntity = new HttpEntity<>(dateRangeRequest, headers);

            // Make a POST request to retrieve account history
            ResponseEntity<AccountHistoryResponse[]> responseEntity = restTemplate.exchange(
                bankAccountHistoryUrl + "/" + accountNumber,
                HttpMethod.POST,  // Corrected HttpMethod to POST
                requestEntity,
                AccountHistoryResponse[].class
            );


            if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                LoggingUtil.logInfo("Retrieved Account History for employee with ID: {}", employeeId);
                return Arrays.asList(responseEntity.getBody());
            } else {
                LoggingUtil.logError("Failed to retrieve account history for Employee ID: {}", employeeId);
                throw new IllegalArgumentException("Failed to retrieve account history for Employee ID: " + employeeId);
            }
        } catch (RestClientException e) {
            LoggingUtil.logError("Exception occurred while hitting account-history using RestTemplate for employee ID: {}", employeeId);
            throw new IllegalArgumentException("Exception occurred while hitting account-history using RestTemplate", e);
        }
    }

// --------------------------------------------------------------------------------------------------

	// Helper methods for conversion
    public Employee convertToEntity(EmployeeDTO employeeDTO) {
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
//        employeeDTO.setBalance(employee.getBalance());

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

  
