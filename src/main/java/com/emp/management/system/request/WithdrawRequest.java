package com.emp.management.system.request;

public class WithdrawRequest {

    private Integer employeeId;
    private Double withdrawalAmount; // Use Double wrapper class

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Double getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Double withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public String validate() {
        if (employeeId == null || withdrawalAmount == null) {
            return "Both fields cannot be null";
        }

        if (withdrawalAmount < 0) {
            return "Withdrawal amount cannot be negative";
        }

        return null; // No validation error
    }
}
