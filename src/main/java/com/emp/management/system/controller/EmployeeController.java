package com.emp.management.system.controller;


import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emp.management.system.request.EmployeeDTO;
import com.emp.management.system.request.EmployeeUpdateRequestDTO;
import com.emp.management.system.service.EmployeeService;
import com.emp.management.system.utils.LoggingUtil;


@RestController
@RequestMapping("/employees")
public class EmployeeController {
	
	@Autowired
    private  EmployeeService employeeService;

 

    @PostMapping("/create")
    public ResponseEntity<?> createEmployee(@RequestBody @Valid EmployeeDTO employeeDTO, BindingResult bindingResult) {
        LoggingUtil.logInfo("Request received to create employee");
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return employeeService.createEmployee(employeeDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        LoggingUtil.logInfo("Request received to get employee by ID");
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/managers/{managerId}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManagerId(@PathVariable Integer managerId) {
        LoggingUtil.logInfo("Request received to get employees by manager ID");
        return employeeService.getEmployeesByManagerId(managerId);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateEmployeeDetails(@PathVariable Integer id,
                                                        @RequestBody @Valid EmployeeUpdateRequestDTO employeeUpdateRequestDTO,
                                                        BindingResult bindingResult) {
        LoggingUtil.logInfo("Request received to update employee details");
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation errors: " + bindingResult.getAllErrors());
        }
        return employeeService.updateEmployeeDetails(id, employeeUpdateRequestDTO);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Integer id) {
        LoggingUtil.logInfo("Request received to delete employee");
        return employeeService.deleteEmployee(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        LoggingUtil.logInfo("Request received to get all employees");
        return employeeService.getAllEmployees();
    }
}
