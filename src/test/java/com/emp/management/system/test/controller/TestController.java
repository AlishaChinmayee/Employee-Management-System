package com.emp.management.system.test.controller;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.emp.management.system.controller.EmployeeController;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ValidationException;


@ExtendWith(MockitoExtension.class)
@WebMvcTest
public class TestController {
	
	  @Autowired
	  private MockMvc mockMvc;

	  @Autowired
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
	    @Test
	    void testGetEmployeesByManagerId_ValidId_ReturnsEmployeeList() throws Exception {
	        // Prepare test data
	        List<EmployeeDTO> employees = new ArrayList<>();
	        employees.add(new EmployeeDTO(/* employee details here */));
	        employees.add(new EmployeeDTO(/* another employee details here */));

	        // Mock the service method
	        ResponseEntity<List<EmployeeDTO>> responseEntity = ResponseEntity.ok(employees);
	        when(employeeService.getEmployeesByManagerId(101)).thenReturn(responseEntity);

	        // Setup MockMvc
	        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();

	        // Perform GET request and validate the response
	        mockMvc.perform(get("/EMS/managers/101"))
	                .andExpect(status().isOk())
	                .andExpect(jsonPath("$.size()").value(2)); // Assuming JSON structure has a list of employees
	    }

//	    @Test
//	    void testGetEmployeesByManagerId_InvalidId_ReturnsBadRequest() throws Exception {
//	        // Mock the service method with an invalid managerId
//	        ResponseEntity<List<EmployeeDTO>> responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST)
//	                .body(Collections.emptyList()); // You can use an empty list or mock actual EmployeeDTO objects
//
//	        // Mock the service method
//	        when(employeeService.getEmployeesByManagerId(106)).thenReturn(responseEntity);
//
//	        // Set up controller method to be invoked
//	        mockMvc.perform(get("/EMS/managers/106"))
//	                .andExpect(status().isBadRequest())
//	                .andExpect(content().string("Manager ID should be between 101 and 105"));
//
//	        // Verify that the mocked service method was called with the expected argument
//	        verify(employeeService).getEmployeesByManagerId(106); // This line checks if the expected method was called
//	    }
//
//	    @Test
//	    void testGetEmployeesByManagerId_InternalServerError_ReturnsInternalServerError() throws Exception {
//	        // Mock the service method to throw an exception
//	        when(employeeService.getEmployeesByManagerId(ArgumentMatchers.anyInt())).thenAnswer(new Answer<ResponseEntity<?>>() {
//	            @Override
//	            public ResponseEntity<?> answer(InvocationOnMock invocation) throws Throwable {
//	                throw new RuntimeException("Internal Server Error");
//	            }
//	        });
//
//	        // Setup MockMvc
//	        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
//
//	        // Perform GET request and validate the response
//	        mockMvc.perform(get("/EMS/managers/101"))
//	                .andExpect(status().isInternalServerError())
//	                .andExpect(content().string("An error occurred while processing the request."));
//	    }
//--------------------------------------------------------------------------------------------------------------------------------------	    
//--------------------------------------------------UPDATE EMPLOYEE DETAILS API---------------------------------------------------------
	    
	    @Test
	    public void testUpdateEmployeeDetails() throws Exception {
	        EmployeeUpdateRequestDTO requestDTO = new EmployeeUpdateRequestDTO();
	        // Set properties in the requestDTO

	        when(employeeService.updateEmployeeDetails(anyInt(), any(EmployeeUpdateRequestDTO.class)))
	            .thenReturn(ResponseEntity.ok("Employee details updated successfully"));

	        mockMvc.perform(MockMvcRequestBuilders.put("/update/1")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(requestDTO)))
	                .andExpect(MockMvcResultMatchers.status().isOk())
	                .andExpect(MockMvcResultMatchers.content().string("Employee details updated successfully"));
	    }

	    @Test
	    public void testUpdateEmployeeDetails_ValidationException() throws Exception {
	        EmployeeUpdateRequestDTO requestDTO = new EmployeeUpdateRequestDTO();
	        // Set properties in the requestDTO

	        when(employeeService.updateEmployeeDetails(anyInt(), any(EmployeeUpdateRequestDTO.class)))
	            .thenThrow(new ValidationException("Invalid data"));

	        mockMvc.perform(MockMvcRequestBuilders.put("/update/1")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(requestDTO)))
	                .andExpect(MockMvcResultMatchers.status().isBadRequest())
	                .andExpect(MockMvcResultMatchers.content().string("Invalid data"));
	    }
	
	    
//--------------------------------------------------------------------------------------------------------------------------------------	    



}
