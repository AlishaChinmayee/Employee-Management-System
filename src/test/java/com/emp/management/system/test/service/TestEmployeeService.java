package com.emp.management.system.test.service;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.emp.management.system.model.Employee;
import com.emp.management.system.repository.EmployeeRepository;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;  // Import ObjectMapper

@ExtendWith(MockitoExtension.class)
public class TestEmployeeService {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;  // Declaring the ObjectMapper

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();  // Initializing the ObjectMapper
    }

//---------------------------------------Create Employee API-------------------------------------------------------------------    
    
    @Test
    public void testCreateEmployeeFromDTO_Success() throws Exception {
        // Mock repository behavior
        when(employeeRepository.findByEmployeeId(any())).thenReturn(null);
        when(employeeRepository.findByEmailId(any())).thenReturn(null);

        // Create EmployeeDTO with dummy JSON data
        String jsonEmployeeDTO = "{\"employeeId\": 1, \"name\": \"Alisha\", \"emailId\": \"ally@example.com\"}";
        EmployeeDTO employeeDTO = objectMapper.readValue(jsonEmployeeDTO, EmployeeDTO.class);

        // Call the method
        employeeService.createEmployeeFromDTO(employeeDTO);

        // Verify repository method is called
        verify(employeeRepository).save(any());
    }


    // Method to create an existing employee for testing purposes
    private Employee existingEmployee() {
        Employee employee = new Employee();
        // Set necessary properties to make it look like an existing employee
        return employee;
    }

    @Test
    public void testCreateEmployeeFromDTO_DuplicateBoth() throws Exception {
        // Mock repository behavior
        when(employeeRepository.findByEmployeeId(any())).thenReturn(existingEmployee());
        when(employeeRepository.findByEmailId(any())).thenReturn(existingEmployee());

        // Create EmployeeDTO with dummy JSON data
        String jsonEmployeeDTO = "{\"employeeId\": 1, \"name\": \"Alisha\", \"emailId\": \"ally@example.com\"}";
        EmployeeDTO employeeDTO = objectMapper.readValue(jsonEmployeeDTO, EmployeeDTO.class);

        // Call the method and assert exception
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployeeFromDTO(employeeDTO));
    }

    @Test
    public void testCreateEmployeeFromDTO_DuplicateEmployeeId() throws Exception {
        // Mock repository behavior
        when(employeeRepository.findByEmployeeId(any())).thenReturn(existingEmployee());
        when(employeeRepository.findByEmailId(any())).thenReturn(null);

        // Create EmployeeDTO with dummy JSON data
        String jsonEmployeeDTO = "{\"employeeId\": 1, \"name\": \"Alisha\", \"emailId\": \"new@example.com\"}";
        EmployeeDTO employeeDTO = objectMapper.readValue(jsonEmployeeDTO, EmployeeDTO.class);

        // Call the method and assert exception
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployeeFromDTO(employeeDTO));
    }

    @Test
    public void testCreateEmployeeFromDTO_DuplicateEmailId() throws Exception {
        // Mock repository behavior
        when(employeeRepository.findByEmployeeId(any())).thenReturn(null);
        when(employeeRepository.findByEmailId(any())).thenReturn(existingEmployee());

        // Create EmployeeDTO with dummy JSON data
        String jsonEmployeeDTO = "{\"employeeId\": 2, \"name\": \"Bryan\", \"emailId\": \"ally@example.com\"}";
        EmployeeDTO employeeDTO = objectMapper.readValue(jsonEmployeeDTO, EmployeeDTO.class);

        // Call the method and assert exception
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployeeFromDTO(employeeDTO));
    }

    @Test
    public void testCreateEmployeeFromDTO_NoDuplicates_Sucess() throws Exception {
        // Mock repository behavior
        when(employeeRepository.findByEmployeeId(any())).thenReturn(null);
        when(employeeRepository.findByEmailId(any())).thenReturn(null);

        // Create EmployeeDTO with dummy JSON data
        String jsonEmployeeDTO = "{\"employeeId\": 3, \"name\": \"Charlie\", \"emailId\": \"charlie@example.com\"}";
        EmployeeDTO employeeDTO = objectMapper.readValue(jsonEmployeeDTO, EmployeeDTO.class);

        // Call the method, should not throw any exception
        assertDoesNotThrow(() -> employeeService.createEmployeeFromDTO(employeeDTO));
    }

//------------------------------------------------------------------------------------------------------------------------------    
    
//----------------------------------------Get By EmployeeId API-----------------------------------------------------------------
    
    @Test
    public void testGetEmployeeByIdWithAccount() {
        Integer employeeId = 1;
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setName("Alisha");
        employee.setAccountNumber("123456789");
        employee.setAccountType("Savings");
        // employee.setBalance(1000.0);  // Assuming you want to test balance as well

        // Mock the behavior of employeeRepository.findByIdWithDetails
        when(employeeRepository.findByIdWithDetails(employeeId)).thenReturn(Optional.of(employee));

        EmployeeDTO employeeDTO = employeeService.getEmployeeByIdWithAccount(employeeId);

        // Assert the result
        Assertions.assertEquals(employee.getEmployeeId(), employeeDTO.getEmployeeId());
        Assertions.assertEquals(employee.getName(), employeeDTO.getName());
        Assertions.assertEquals(employee.getAccountNumber(), employeeDTO.getAccountNumber());
        Assertions.assertEquals(employee.getAccountType(), employeeDTO.getAccountType());
        // Assertions.assertEquals(employee.getBalance(), employeeDTO.getBalance()); // Test balance as well

        // Verify that the repository method was called
        verify(employeeRepository, times(1)).findByIdWithDetails(employeeId);
    }

    @Test
    public void testGetEmployeeByIdWithAccountNotFound() {
        Integer employeeId = 1;

        // Mock the behavior of employeeRepository.findByIdWithDetails to return an empty Optional
        when(employeeRepository.findByIdWithDetails(employeeId)).thenReturn(Optional.empty());

        // Assert that the method throws the correct exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeByIdWithAccount(employeeId);
        });
    }
//------------------------------------------------------------------------------------------------------------------------------    
   
//----------------------------------------Get By ManagersId API-----------------------------------------------------------------

    @Test
    public void testGetEmployeesByManagerId() {
        // Prepare mock data
        List<Employee> mockEmployees = new ArrayList<>();
        Employee emp = new Employee();
        emp.setEmployeeId(1);
        emp.setName("Alisha");
        emp.setManagerId(101);
        mockEmployees.add(emp);

        Employee emp2 = new Employee();
        emp2.setEmployeeId(2);
        emp2.setName("Pratik");
        emp2.setManagerId(101);
        mockEmployees.add(emp2);

        when(employeeRepository.findByManagerId(anyInt())).thenReturn(mockEmployees);

        ResponseEntity<List<EmployeeDTO>> response = employeeService.getEmployeesByManagerId(101);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testGetEmployeesByManagerIdNotFound() {
        when(employeeRepository.findByManagerId(anyInt())).thenReturn(new ArrayList<>());

        ResponseEntity<List<EmployeeDTO>> response = employeeService.getEmployeesByManagerId(101);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    public void testGetEmployeesByManagerIdException() {
        when(employeeRepository.findByManagerId(anyInt())).thenThrow(new RuntimeException("Database connection error"));

        // Exception testing using lambda expression
        Exception exception = assertThrows(RuntimeException.class, () -> {
            employeeService.getEmployeesByManagerId(101);
        });

        assertEquals("Database connection error", exception.getMessage());

        // Verify that the repository method was called
        verify(employeeRepository, times(1)).findByManagerId(101);
    }
//------------------------------------------------------------------------------------------------------------------------------    
//---------------------------------------------Update Employee Details API------------------------------------------------------
    
    @Test
    public void testUpdateEmployeeDetails() {
        // Set up test data
        Employee existingEmployee = new Employee();
        existingEmployee.setEmployeeId(1);
        existingEmployee.setName("Alisha");
        existingEmployee.setManagerId(101);
        existingEmployee.setEmailId("ally@gmail.com");
        // Set other properties accordingly

        EmployeeUpdateRequestDTO updateRequest = new EmployeeUpdateRequestDTO();
        updateRequest.setName("Updated Name");
        // Initialize other updateRequest properties

        // Mock repository methods
        when(employeeRepository.findById(existingEmployee.getEmployeeId())).thenReturn(Optional.of(existingEmployee));

        // Call the service method
        ResponseEntity<String> response = employeeService.updateEmployeeDetails(existingEmployee.getEmployeeId(), updateRequest);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Employee details updated successfully", response.getBody());

        // Verify that repository save method was called with the updated employee
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    public void testUpdateEmployeeDetailsNotFound() {
        Integer nonExistentEmployeeId = 100;

        // Mock repository methods
        when(employeeRepository.findById(nonExistentEmployeeId)).thenReturn(Optional.empty());

        // Call the service method
        ResponseEntity<String> response = employeeService.updateEmployeeDetails(nonExistentEmployeeId, new EmployeeUpdateRequestDTO());

        // Assertions
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Employee not found with id: " + nonExistentEmployeeId, response.getBody());
    }

    @Test
    public void testUpdateEmployeeDetailsWithException() {
        // Set up test data
        Employee existingEmployee = new Employee();
        existingEmployee.setEmployeeId(2);
        existingEmployee.setName("Jane Smith");
        // Initialize other properties

        EmployeeUpdateRequestDTO updateRequest = new EmployeeUpdateRequestDTO();
        updateRequest.setName("Updated Name");
        // Initialize other updateRequest properties

        // Mock repository methods
        when(employeeRepository.findById(existingEmployee.getEmployeeId())).thenReturn(Optional.of(existingEmployee));

        // Mock repository save method to throw an exception
        doThrow(new RuntimeException("Simulated exception")).when(employeeRepository).save(any());

        // Call the service method and catch exception
        try {
            ResponseEntity<String> response = employeeService.updateEmployeeDetails(existingEmployee.getEmployeeId(), updateRequest);
            fail("Expected exception was not thrown.");
        } catch (RuntimeException e) {
            assertEquals("Simulated exception", e.getMessage());
        }

        // Verify that repository save method was called
        verify(employeeRepository, times(1)).save(existingEmployee);
    }


//------------------------------------------------------------------------------------------------------------------------------    
    
//--------------------------------------------------DELETE EMPLOYEE API---------------------------------------------------------
    
    @Test
    public void testDeleteEmployeeSuccess() {
        int existingEmployeeId = 1;
        when(employeeRepository.existsById(existingEmployeeId)).thenReturn(true);

        ResponseEntity<String> response = employeeService.deleteEmployee(existingEmployeeId);

        verify(employeeRepository, times(1)).deleteById(existingEmployeeId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Employee deleted successfully", response.getBody());
    }

    @Test
    public void testDeleteEmployeeNotFound() {
        int nonExistingEmployeeId = 2;
        when(employeeRepository.existsById(nonExistingEmployeeId)).thenReturn(false);

        ResponseEntity<String> response = employeeService.deleteEmployee(nonExistingEmployeeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Employee with ID " + nonExistingEmployeeId + " doesn't exist", response.getBody());
    }

    @Test
    public void testDeleteEmployeeException() {
        int existingEmployeeId = 1;
        when(employeeRepository.existsById(existingEmployeeId)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(employeeRepository).deleteById(existingEmployeeId);

        ResponseEntity<String> response = employeeService.deleteEmployee(existingEmployeeId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to delete employee"));
    }
    
//------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------GET ALL EMPLOYEES API-------------------------------------------------------
 
    @Test
    public void testGetAllEmployeesSuccess() {
        List<Employee> mockEmployees = new ArrayList<>();

        Employee emp = new Employee();
        emp.setEmployeeId(1);
        emp.setName("Alisha");
        emp.setManagerId(101);
        mockEmployees.add(emp);

        Employee emp2 = new Employee();
        emp2.setEmployeeId(2);
        emp2.setName("Pratik");
        emp2.setManagerId(101);
        mockEmployees.add(emp2);

        when(employeeRepository.findAll()).thenReturn(mockEmployees);

        ResponseEntity<?> response = employeeService.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockEmployees, response.getBody());
    }

    @Test
    public void testGetAllEmployeesNotFound() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = employeeService.getAllEmployees();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No employees found", response.getBody());
    }

    @Test
    public void testGetAllEmployeesException() {
        when(employeeRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            employeeService.getAllEmployees();
        });
    }   
    
//-------------------------------------------------------------------------------------------------------------------------------------
    
}
