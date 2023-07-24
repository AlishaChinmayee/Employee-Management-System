package com.emp.management.system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.emp.management.system.model.PhoneNumber;



public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Integer> {
    @Query("SELECT p FROM PhoneNumber p WHERE p.employee.employeeId = :employeeId")
    List<PhoneNumber> findByEmployeeId(@Param("employeeId") Integer employeeId);
}