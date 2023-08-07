package com.emp.management.system.request;

import java.util.Date;

public class AccountHistoryRequest {

	private Integer employeeId;
	private Date startDate;
	private Date endDate;
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public void validate(AccountHistoryRequest req) {
		if(req.getEmployeeId() == null) {
			throw new IllegalArgumentException("EmployeeId cannot be null.");
		}
		if(req.getStartDate() == null) {
			throw new IllegalArgumentException("StartDate cannot be null.");
		}
		if(req.getEndDate() == null) {
			throw new IllegalArgumentException("EndDate cannot be null.");
		}
	}
	
}