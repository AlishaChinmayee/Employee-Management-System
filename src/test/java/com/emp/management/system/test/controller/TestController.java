package com.emp.management.system.test.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.emp.management.system.controller.EmployeeController;
import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.validation.ValidationException;


@ExtendWith(MockitoExtension.class)
//@WebMvcTest(EmployeeController.class)
public class TestController {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
        objectMapper = new ObjectMapper();
    }
//---------------------------------------------------------CREATE EMPLOYEE API-----------------------------------------------------------
	    @Test
	    void testCreateEmployeeSuccess() {
	        EmployeeDTO employeeDTO = new EmployeeDTO();
	        doNothing().when(employeeService).createEmployeeFromDTO(ArgumentMatchers.any());

	        ResponseEntity<String> response = employeeController.createEmployee(employeeDTO);

	        verify(employeeService).createEmployeeFromDTO(employeeDTO);

	        assertEquals(HttpStatus.OK, response.getStatusCode());
	        assertEquals("Employee created successfully", response.getBody());
	    }

	    @Test
	    void testCreateEmployeeDuplicateKeyException() {
	        EmployeeDTO employeeDTO = new EmployeeDTO();
	        String errorMessage = "Employee creation failed: Duplicate key";

	        // Mock the service to throw a DuplicateKeyException
	        doThrow(new DuplicateKeyException("Duplicate key")).when(employeeService).createEmployeeFromDTO(ArgumentMatchers.any());

	        ResponseEntity<String> response = employeeController.createEmployee(employeeDTO);

	        verify(employeeService).createEmployeeFromDTO(employeeDTO);

	        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	        assertTrue(response.getBody().contains(errorMessage));
	    }

	    @Test
	    void testCreateEmployeeInternalServerError() {
	        EmployeeDTO employeeDTO = new EmployeeDTO();
	        String errorMessage = "Failed to create employee: Internal error";

	        // Mock the service to throw a RuntimeException
	        doThrow(new RuntimeException("Internal error")).when(employeeService).createEmployeeFromDTO(ArgumentMatchers.any());

	        ResponseEntity<String> response = employeeController.createEmployee(employeeDTO);

	        verify(employeeService).createEmployeeFromDTO(employeeDTO);

	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	        assertTrue(response.getBody().contains(errorMessage));
	    }

//---------------------------------------------------------------------------------------------------------------------------------------    
//-------------------------------------------GET EMPLOYEES BY EMPLOYEEID WITH ACCOUNT----------------------------------------------------------------
	    @Test
	    void testGetEmployeeByIdWithAccount_Success() {
	        EmployeeDTO employeeDTO = new EmployeeDTO();
	        employeeDTO.setEmployeeId(1);

	        when(employeeService.getEmployeeByIdWithAccount(1)).thenReturn(employeeDTO);

	        ResponseEntity<?> responseEntity = employeeController.getEmployeeByIdWithAccount(1);

	        verify(employeeService, times(1)).getEmployeeByIdWithAccount(1);
	        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	        assertEquals(employeeDTO, responseEntity.getBody());
	    }

	    @Test
	    void testGetEmployeeByIdWithAccount_NotFound() {
	        when(employeeService.getEmployeeByIdWithAccount(1)).thenThrow(new IllegalArgumentException("Employee not found"));

	        ResponseEntity<?> responseEntity = employeeController.getEmployeeByIdWithAccount(1);

	        verify(employeeService, times(1)).getEmployeeByIdWithAccount(1);
	        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	        assertEquals("Employee not found", responseEntity.getBody());
	    }

	    @Test
	    void testGetEmployeeByIdWithAccount_InternalServerError() {
	        when(employeeService.getEmployeeByIdWithAccount(1)).thenThrow(new RuntimeException("Internal Server Error"));

	        ResponseEntity<?> responseEntity = employeeController.getEmployeeByIdWithAccount(1);

	        verify(employeeService, times(1)).getEmployeeByIdWithAccount(1);
	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	        assertEquals("An error occurred while processing the request.", responseEntity.getBody());
	    }
//--------------------------------------------------------------------------------------------------------------------------------------	    
//------------------------------------------------GET EMPLOYEES BY MANAGERS ID API------------------------------------------------------
	  
//	    @Test
//	    void testValidManagerId() throws Exception {
//	        EmployeeDTO mockedEmployee = new EmployeeDTO();
//	        mockedEmployee.setName("John Doe");
//	        mockedEmployee.setAccountType("Developer");
//
//	        when(employeeService.getEmployeesByManagerId(anyInt()))
//	            .thenReturn(new ResponseEntity<>(Collections.singletonList(mockedEmployee), HttpStatus.OK));
//
//	        mockMvc.perform(MockMvcRequestBuilders.get("/managers/102")) // Make sure the URL matches the endpoint
//	            .andExpect(status().isOk())
//	            .andExpect((ResultMatcher) content().json(objectMapper.writeValueAsString(Collections.singletonList(mockedEmployee))));
//	    }
//
//	    @Test
//	    void testInvalidManagerId() throws Exception {
//	        mockMvc.perform(MockMvcRequestBuilders.get("/managers/100"))
//	            .andExpect(status().isBadRequest())
//	            .andExpect((ResultMatcher) content().string("Manager ID should be between 101 and 105"));
//	    }
//
//	    @Test
//	    void testExceptionHandling() throws Exception {
//	        when(employeeService.getEmployeesByManagerId(anyInt()))
//	            .thenThrow(new RuntimeException("Mocked exception"));
//
//	        mockMvc.perform(MockMvcRequestBuilders.get("/managers/103"))
//	            .andExpect(status().isInternalServerError())
//	            .andExpect((ResultMatcher) content().string("An error occurred while processing the request"));
//	    }
//--------------------------------------------------------------------------------------------------------------------------------------	    
//--------------------------------------------------UPDATE EMPLOYEE DETAILS API---------------------------------------------------------
	    
	    @Test
	    public void testUpdateEmployeeDetails() {
	        // Prepare test data
	        Integer employeeId = 1;
	        EmployeeUpdateRequestDTO requestDTO = new EmployeeUpdateRequestDTO();
	        requestDTO.setEmployeeId(employeeId);
	        // ... set other fields as needed

	        // Mock employeeService behavior
	        when(employeeService.updateEmployeeDetails(employeeId, requestDTO))
	            .thenReturn(ResponseEntity.ok("Employee details updated successfully"));

	        // Call the controller method
	        ResponseEntity<String> response = employeeController.updateEmployeeDetails(employeeId, requestDTO);

	        // Verify the response
	        verify(employeeService, times(1)).updateEmployeeDetails(employeeId, requestDTO);
	        assertEquals(HttpStatus.OK, response.getStatusCode());
	        assertEquals("Employee details updated successfully", response.getBody());
	    }

	    @Test
	    public void testUpdateEmployeeDetails_ValidationException() {
	        // Mocking the behavior of employeeService.updateEmployeeDetails() to throw ValidationException
	        when(employeeService.updateEmployeeDetails(anyInt(), any(EmployeeUpdateRequestDTO.class)))
	            .thenThrow(new ValidationException("Validation failed"));

	        EmployeeUpdateRequestDTO requestDTO = new EmployeeUpdateRequestDTO();
	        // Set properties for requestDTO

	        ValidationException exception = assertThrows(ValidationException.class,
	            () -> employeeController.updateEmployeeDetails(1, requestDTO));

	        // Verify the interaction with the service and check the thrown exception
	        verify(employeeService).updateEmployeeDetails(eq(1), eq(requestDTO));
	        assertEquals("Validation failed", exception.getMessage());
	    }

//--------------------------------------------------------------------------------------------------------------------------------------	    
//	  //------------------------------------------------------------DELETE EMPLOYEE API-------------------------------------------------------
//	 @Test
	    public void testDeleteEmployee_Success() throws Exception {
	        int employeeId = 1;

	        // Mock the behavior of the employeeService.deleteEmployee method
	        when(employeeService.deleteEmployee(employeeId)).thenReturn(new ResponseEntity<>("Employee deleted successfully", HttpStatus.OK));

	        // Perform a mock HTTP DELETE request to the specified URL
	        MockHttpServletResponse response = mockMvc.perform(
	                MockMvcRequestBuilders.delete("/delete/{id}", employeeId)
	                        .contentType(MediaType.APPLICATION_JSON))
	                .andExpect(MockMvcResultMatchers.status().isOk())
	                .andReturn().getResponse();

	        // Verify the response
	        assertEquals(HttpStatus.OK.value(), response.getStatus());
	        assertEquals("Employee deleted successfully", response.getContentAsString());
	    }
	

	    @Test
	    public void testDeleteEmployee_NotFound() {
	        int employeeId = 2;
	        
	        when(employeeService.deleteEmployee(employeeId)).thenReturn(new ResponseEntity<>("Employee not found", HttpStatus.NOT_FOUND));

	        ResponseEntity<String> response = employeeController.deleteEmployee(employeeId);

	        verify(employeeService, times(1)).deleteEmployee(employeeId);
	        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	        assertEquals("Employee not found", response.getBody());
	    }

	    @Test
	    public void testDeleteEmployee_InternalServerError() {
	        int employeeId = 3;
	        
	        when(employeeService.deleteEmployee(employeeId)).thenReturn(new ResponseEntity<>("Error deleting employee", HttpStatus.INTERNAL_SERVER_ERROR));

	        ResponseEntity<String> response = employeeController.deleteEmployee(employeeId);

	        verify(employeeService, times(1)).deleteEmployee(employeeId);
	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	        assertEquals("Error deleting employee", response.getBody());
	    }
//--------------------------------------------------------------------------------------------------------------------------------------	    
}