package com.emp.management.system.request;

public class TransactionRequest {
 private Integer employeeId;

 public TransactionRequest() {
 }

 public TransactionRequest(Integer employeeId) {
     this.employeeId = employeeId;
 }
 public Integer getEmployeeId() {
     return employeeId;
 }

 public void setEmployeeId(Integer employeeId) {
     this.employeeId = employeeId;
 }
}
