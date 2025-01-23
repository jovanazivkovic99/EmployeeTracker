package com.example.employeetracker.serviceinterface;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.response.EmployeeResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse addEmployee(EmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);

    //Page<EmployeeResponse> searchEmployees(EmployeeRequest request);

    List<Employee> findAllEmployees(String personalId, String name);
}
