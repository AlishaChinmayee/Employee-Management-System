package com.emp.management.system.response;


public class AccountResponse {
    private String status;
    private String accountNumber;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusByCode(String code) {
        switch (code) {
            case "00":
                this.status = "Account created successfully.";
                break;
            case "01":
                this.status = "Account already exists.";
                break;
            case "02":
                this.status = "Exception";
                break;
            case "03":
                this.status = "Invalid Inputs";
        }
    }
}

