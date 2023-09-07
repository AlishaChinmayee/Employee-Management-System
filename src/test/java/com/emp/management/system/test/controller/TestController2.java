package com.emp.management.system.test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.emp.management.system.controller.EmployeeController;
import com.emp.management.system.exception.EmployeeNotFoundException;
import com.emp.management.system.request.CreateAccountRequest;
import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestController2 {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;


//----------------------------------------------GET EMPLOYEES BY MANAGERS ID API------------------------------------------------------
    @Test
    public void testGetEmployeesByManagerId_Success() throws Exception {
        int managerId = 101; // Replace with a valid manager ID

        // Create sample employee data
        EmployeeDTO employee1 = new EmployeeDTO();
        employee1.setEmployeeId(1);
        employee1.setName("Alisha");
        // Add more fields as needed...

        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setEmployeeId(2);
        employee2.setName("Pratik");
        // Add more fields as needed...

        // Create a list of employees and add the sample employees
        List<EmployeeDTO> employeeList = new ArrayList<>();
        employeeList.add(employee1);
        employeeList.add(employee2);
        // Add more employees as needed...

        ResponseEntity<List<EmployeeDTO>> responseEntity = ResponseEntity.ok(employeeList);
        Mockito.when(employeeService.getEmployeesByManagerId(managerId)).thenReturn(responseEntity);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/EMS/managers/" + managerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetEmployeesByManagerIdInvalid() throws Exception {
        int managerId = 200; 

        mockMvc.perform(MockMvcRequestBuilders
                .get("/EMS/managers/" + managerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Manager ID should be between 101 and 105"));
    }

//-----------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------GET ALL EMPLOYEES API----------------------------------------------------------    
    
    @Test
    public void testGetAllEmployees_Success() throws Exception {
        // Mock the service to return a ResponseEntity containing a list of employees
        List<EmployeeDTO> employeeList = new ArrayList<>();

        // Populate the employeeList with sample data
        EmployeeDTO employee1 = new EmployeeDTO();
        employee1.setEmployeeId(1);
        employee1.setName("Alisha");
        // Add more fields as needed...

        EmployeeDTO employee2 = new EmployeeDTO();
        employee2.setEmployeeId(2);
        employee2.setName("Pratik");
        // Add more fields as needed...

        // Add the sample employees to the list
        employeeList.add(employee1);
        employeeList.add(employee2);

        // Mock the service method using thenAnswer
        Mockito.when(employeeService.getAllEmployees()).thenAnswer((InvocationOnMock invocation) -> {
            return ResponseEntity.ok(employeeList);
        });

        mockMvc.perform(MockMvcRequestBuilders
                .get("/EMS/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetAllEmployees_Exception() throws Exception {
        // Mock the service to throw an exception when getAllEmployees is called
        Mockito.when(employeeService.getAllEmployees()).thenThrow(new EmployeeNotFoundException("Something went wrong"));

        mockMvc.perform(MockMvcRequestBuilders
                .get("/EMS/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError()) // Expect a 500 Internal Server Error
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Something went wrong")); // Validate the error message in the response JSON
    }

    

//-----------------------------------------------------------------------------------------------------------------------------------
//    @Test
//    public void testCreateAccount() throws Exception {
//        CreateAccountRequest request = new CreateAccountRequest();
//        request.setEmployeeId(1);
//        request.setAccountType("Savings");
//
//        when(employeeService.createAccount(any(Integer.class), any(String.class))).thenReturn("Account created successfully");
//
//        mockMvc.perform(MockMvcRequestBuilders
//                .post("/BMS/create-account")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(request)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Account created successfully"));
//    }
//      
//    
//    // Utility method to convert objects to JSON
//    private String asJsonString(final Object obj) {
//        try {
//            return new ObjectMapper().writeValueAsString(obj);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//------------------------------------------------------------------------------------------------------------------------------------
}
