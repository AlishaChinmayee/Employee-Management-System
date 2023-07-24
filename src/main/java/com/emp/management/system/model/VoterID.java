package com.emp.management.system.model;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name="VoterID")
@Table(name="VoterID")
public class VoterID {
    @Id
    @Column(name="VOTER_ID")
    private Integer voterId;
    
    @Column(name="EMPLOYEE_ID", insertable=false, updatable=false)
    private int employeeId;
    
    @Column(name="VOTER_NUMBER")
    private String voterNumber;
    
    @Column(name="CITY")
    private String city;
    
    @OneToOne(targetEntity=Employee.class, cascade = CascadeType.ALL)
    @JoinColumn(name="EMPLOYEE_ID", referencedColumnName="EMPLOYEE_ID")
    private Employee employee;

    public VoterID() {
        super();
    }

    public Integer getVoterId() {
        return voterId;
    }

    public void setVoterId(Integer voterId) {
        this.voterId = voterId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
