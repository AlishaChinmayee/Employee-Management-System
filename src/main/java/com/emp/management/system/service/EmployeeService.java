package com.emp.management.system.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emp.management.system.model.Employee;
import com.emp.management.system.model.PhoneNumber;
import com.emp.management.system.model.VoterID;
import com.emp.management.system.repository.EmployeeRepository;
import com.emp.management.system.repository.PhoneNumberRepository;
import com.emp.management.system.repository.VoterIDRepository;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.request.PhoneNumberDTO;
import com.emp.management.system.request.VoterIDDTO;
import com.emp.management.system.utils.LoggingUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
	@Autowired
    private  EmployeeRepository employeeRepository;
	
	@Autowired
    private  PhoneNumberRepository phoneNumberRepository;
	
	@Autowired
    private  VoterIDRepository voterIDRepository;

  

    @Transactional
    public ResponseEntity<?> createEmployee(EmployeeDTO employeeDTO) {
        LoggingUtil.logInfo("Creating employee");
        // Convert DTO to Entity
        Employee employee = convertToEntity(employeeDTO);
        // Save the Employee
        try {
            employeeRepository.save(employee);
            return ResponseEntity.ok("Employee created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee creation failed: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getEmployeeById(Integer id) {
        LoggingUtil.logInfo("Fetching employee by ID");
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            EmployeeDTO employeeDTO = convertToDTO(employee.get());
            return ResponseEntity.ok(employeeDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
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
        List<PhoneNumber> updatedPhoneNumbers = new ArrayList<>();
        if (employeeUpdateRequestDTO.getPhoneNumbers() != null) {
            for (PhoneNumberDTO phoneNumberDTO : employeeUpdateRequestDTO.getPhoneNumbers()) {
                PhoneNumber phoneNumber;
                if (phoneNumberDTO.getPhoneId() != null) {
                    // Update existing phone number
                    Optional<PhoneNumber> optionalPhoneNumber = phoneNumberRepository.findById(phoneNumberDTO.getPhoneId());
                    if (optionalPhoneNumber.isPresent()) {
                        phoneNumber = optionalPhoneNumber.get();
                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
                        phoneNumber.setType(phoneNumberDTO.getType());
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Phone number not found with id: " + phoneNumberDTO.getPhoneId());
                    }
                } else {
                    // Create new phone number
                    if (phoneNumberDTO.getPhoneNumber() != null) {  // Check if phoneNumber is not null
                        phoneNumber = new PhoneNumber();
                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
                        phoneNumber.setType(phoneNumberDTO.getType());
                        phoneNumber.setEmployee(existingEmployee);
                        updatedPhoneNumbers.add(phoneNumber);
                    }
                }
            }
        }
        existingEmployee.getPhoneNumbers().clear();
        existingEmployee.getPhoneNumbers().addAll(updatedPhoneNumbers);


        // Update the voter ID
        if (employeeUpdateRequestDTO.getVoterID() != null) {
            VoterID voterID = existingEmployee.getVoterID();
            if (voterID != null) {
                voterID.setVoterNumber(employeeUpdateRequestDTO.getVoterID().getVoterNumber());
                voterID.setCity(employeeUpdateRequestDTO.getVoterID().getCity());
            } else {
                voterID = new VoterID();
                voterID.setVoterNumber(employeeUpdateRequestDTO.getVoterID().getVoterNumber());
                voterID.setCity(employeeUpdateRequestDTO.getVoterID().getCity());
                existingEmployee.setVoterID(voterID);
            }
        }

        existingEmployee.setUpdatedDateTime(LocalDateTime.now()); // Set the updatedDateTime to the current timestamp

        // Save the updated employee
        try {
            employeeRepository.save(existingEmployee);
            return ResponseEntity.ok("Employee details updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update employee details: " + e.getMessage());
        }
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

    // Helper methods for conversion
    private Employee convertToEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setDob(employeeDTO.getDob());
        employee.setManagerId(employeeDTO.getManagerId());
        employee.setSalary(employeeDTO.getSalary());
        employee.setEmailId(employeeDTO.getEmailId());
        return employee;
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

        // Convert phone numbers
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

        // Convert voter ID
        if (employee.getVoterID() != null) {
            VoterIDDTO voterIDDTO = new VoterIDDTO();
            voterIDDTO.setVoterId(employee.getVoterID().getVoterId());
            voterIDDTO.setEmployeeId(employee.getVoterID().getEmployeeId());
            voterIDDTO.setVoterNumber(employee.getVoterID().getVoterNumber());
            voterIDDTO.setCity(employee.getVoterID().getCity());
            employeeDTO.setVoterID(voterIDDTO);
        }

        return employeeDTO;
    }
    
 //-----------------------------------------------------------------------   
    
    @Scheduled(cron = "1 * * * * *")
//  (cron = "0 0 0 5 * *")
  public void sendTaxableSalaryMessage() {
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
      LocalDateTime now = LocalDateTime.now();
      // Get all employees without voterId
      List<Employee> employeesWithoutVoterId = employeeRepository.findByVoterIdIsNull();

      // Print the employee ID and employee name
      for (Employee employee : employeesWithoutVoterId) {
          System.out.println("Employee ID: " + employee.getEmployeeId() + ", Employee Name: " + employee.getName());
      }
  }

}

  
