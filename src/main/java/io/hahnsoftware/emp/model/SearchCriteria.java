package io.hahnsoftware.emp.model;

import java.time.LocalDate;

public class SearchCriteria {
    private String name;
    private String department;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String jobTitle;
    private String employeeId;
    
    // Pagination fields
    private int page = 0;
    private int size = 10;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    // Builder pattern for fluent API
    public SearchCriteria withName(String name) {
        this.name = name;
        return this;
    }
    
    public SearchCriteria withDepartment(String department) {
        this.department = department;
        return this;
    }
    
    public SearchCriteria withStatus(String status) {
        this.status = status;
        return this;
    }
    
    public SearchCriteria withDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }
    
    public SearchCriteria withJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }
    
    public SearchCriteria withEmployeeId(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }
    
    public SearchCriteria withPagination(int page, int size) {
        this.page = page;
        this.size = size;
        return this;
    }
}