package com.example.employeetracker.serviceinterface;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.request.EmployeeUpdateRequest;
import com.example.employeetracker.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse addEmployee(EmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);
    void deleteEmployee(Long id);
    List<Employee> searchEmployees(String personalId, String name);
}
