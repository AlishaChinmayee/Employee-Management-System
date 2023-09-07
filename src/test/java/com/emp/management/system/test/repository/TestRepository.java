package com.emp.management.system.test.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.emp.management.system.model.Employee;
import com.emp.management.system.repository.EmployeeRepository;

@DataJpaTest
@ActiveProfiles("test")
public class TestRepository {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    public void testFindByIdWithDetails() {
        // Create and save a sample employee with phone numbers
        Employee employee = new Employee();
        // Set employee properties
        employee.setEmployeeId(1);
        employee.setName("Alisha");
        employee.setDob(LocalDate.of(2000, 1, 1));

        employeeRepository.save(employee);

        Optional<Employee> result = employeeRepository.findByIdWithDetails(employee.getEmployeeId());
        assertTrue(result.isPresent());

        Employee foundEmployee = result.get();
        assertEquals(employee.getEmployeeId(), foundEmployee.getEmployeeId());

        // Additional assertions on employee details
        assertEquals("Alisha", foundEmployee.getName());
        assertEquals(LocalDate.of(2000, 1, 1), foundEmployee.getDob());
        
    }


    @Test
    public void testFindByManagerId() {
        // Create and save employees with different managerIds
        Employee manager = new Employee();
        manager.setEmployeeId(101); // Manually assign manager's ID
        employeeRepository.save(manager);

        Employee employee1 = new Employee();
        employee1.setEmployeeId(102); // Manually assign employee's ID
        employee1.setManagerId(manager.getEmployeeId()); // Use actual manager's ID
        employeeRepository.save(employee1);

        List<Employee> employees = employeeRepository.findByManagerId(manager.getEmployeeId());

        // Assertions for the returned employees
        assertEquals(1, employees.size()); // Assuming one employee is associated with this manager
        assertEquals(employee1.getEmployeeId(), employees.get(0).getEmployeeId());
       
    }


    @Test
    public void testFindByEmailId() {
        // Create and save an employee with a specific emailId
        Employee employee = new Employee();
        employee.setEmployeeId(1); 
        employee.setEmailId("example@example.com");
        employeeRepository.save(employee);

        Employee foundEmployee = employeeRepository.findByEmailId("example@example.com");

        assertNotNull(foundEmployee);
        assertEquals("example@example.com", foundEmployee.getEmailId());
    }


    @Test
    public void testFindBySalaryGreaterThan() {
        // Create and save employees with different salaries
        Employee employee1 = new Employee();
        employee1.setEmployeeId(1); 
        employee1.setSalary(new BigDecimal("50000"));
        employeeRepository.save(employee1);

        Employee employee2 = new Employee();
        employee2.setEmployeeId(2); 
        employee2.setSalary(new BigDecimal("70000"));
        employeeRepository.save(employee2);

        List<Employee> highSalaryEmployees = employeeRepository.findBySalaryGreaterThan(new BigDecimal("60000"));

        // Assertions for high salary employees
        assertEquals(1, highSalaryEmployees.size()); // Assuming one employee has salary greater than 60000
        assertEquals(employee2.getEmployeeId(), highSalaryEmployees.get(0).getEmployeeId());
    }

    @Test
    @Transactional
    @DirtiesContext
    public void testFindByVoterIdIsNull() {
        // Create and save employees with and without voter IDs
        Employee employee1 = new Employee();
        employee1.setEmployeeId(1); 
        Employee employee2 = new Employee();
        employee2.setEmployeeId(2); 
        employeeRepository.saveAll(Arrays.asList(employee1, employee2));

        // Set Voter ID to null
        employee2.setVoterID(null);
        System.out.println("Before query:");
        for (Employee employee : employeeRepository.findAll()) {
            System.out.println("Employee ID: " + employee.getEmployeeId() + ", Voter ID: " + employee.getVoterID());
        }

        List<Employee> employeesWithoutVoterId = employeeRepository.findByVoterIdIsNull();

        System.out.println("After query:");
        for (Employee employee : employeesWithoutVoterId) {
            System.out.println("Employee ID: " + employee.getEmployeeId() + ", Voter ID: " + employee.getVoterID());
        }
        
        // Assertions for employees without voter IDs
        assertEquals(1, employeesWithoutVoterId.size()); // Assuming one employee has no voter ID
        assertEquals(employee2.getEmployeeId(), employeesWithoutVoterId.get(0).getEmployeeId());
    }

    @Test
    public void testFindByEmployeeId() {
        // Create an employee with a specific employeeId
        Employee employee = new Employee();
        // Manually assign an employeeId
        employee.setEmployeeId(1); // Replace with the desired ID
        employeeRepository.save(employee);

        // Find the employee by employeeId
        Employee foundEmployee = employeeRepository.findByEmployeeId(employee.getEmployeeId());

        assertNotNull(foundEmployee);
        assertEquals(employee.getEmployeeId(), foundEmployee.getEmployeeId());
    }

//    @Test
//    public void testNonExistentEmployee() {
//        // Try to find an employee with an employeeId that doesn't exist
//        Optional<Employee> result = employeeRepository.findByIdWithDetails(nonExistentEmployeeId); // Use actual nonExistentEmployeeId
//
//        assertFalse(result.isPresent());
//    }

    // Additional exception testing scenarios can be added here
}
