package com.example.employeetracker.controller;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.request.EmployeeUpdateRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.serviceinterface.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public EmployeeResponse addEmployee(@Valid @RequestBody EmployeeRequest employeeRequest) {
        return employeeService.addEmployee(employeeRequest);
    }

    @GetMapping
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping("/search")
    public List<EmployeeResponse> searchEmployees(
            @RequestParam(required = false) String personalId,
            @RequestParam(required = false) String name

            ) {
        List<Employee> employees = employeeService.searchEmployees(personalId, name);
        return employees.stream()
                .map(e -> new EmployeeResponse(
                        e.getId(),
                        e.getPersonalId(),
                        e.getName(),
                        (e.getTeam() != null) ? e.getTeam().getId() : null
                ))
                .toList();
    }
}
