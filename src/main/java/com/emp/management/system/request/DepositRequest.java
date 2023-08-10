package com.emp.management.system.request;

public class DepositRequest {

    private Integer employeeId;
    private Double amount;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String validate() {
        if (employeeId == null || amount == null) {
            return "Both employeeId and amount must be provided";
        }

        if (amount < 0) {
            return "Amount cannot be negative";
        }

        return null; // Return null if the request is valid
    }
}
