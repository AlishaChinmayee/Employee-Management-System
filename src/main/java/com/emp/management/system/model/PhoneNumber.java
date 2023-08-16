package com.emp.management.system.model;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



@Entity(name="PhoneNumber")
@Table(name = "phone_number")
public class PhoneNumber {
    @Id
    @Column(name = "PHONE_ID")
    private Integer phoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID")
    @JsonIgnoreProperties("phoneNumbers")
    private Employee employee;



    
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "PROVIDER")
    private String provider;

    @Column(name = "TYPE")
    private String type;

    public PhoneNumber() {
        super();
    }

	public Integer getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(Integer phoneId) {
		this.phoneId = phoneId;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
		 if (employee != null) {
	            employee.getPhoneNumbers().add(this); // Add the phone number to the employee's phone number list
	        }
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    
}
