package com.emp.management.system.request;

public class VoterIDDTO {
	
	    private Integer voterId;
	    private Integer employeeId;
	    private String voterNumber;
	    private String city;
		public Integer getVoterId() {
			return voterId;
		}
		public void setVoterId(Integer voterId) {
			this.voterId = voterId;
		}
		public Integer getEmployeeId() {
			return employeeId;
		}
		public void setEmployeeId(Integer employeeId) {
			this.employeeId = employeeId;
		}
		public String getVoterNumber() {
			return voterNumber;
		}
		public void setVoterNumber(String voterNumber) {
			this.voterNumber = voterNumber;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public VoterIDDTO() {
			super();
		}

	    // Getters and setters

	    // Additional methods if needed
	}
