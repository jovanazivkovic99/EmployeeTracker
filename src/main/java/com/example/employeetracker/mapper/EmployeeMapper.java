package com.example.employeetracker.mapper;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.response.EmployeeResponse;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMapper {

    public static EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }
            return EmployeeResponse.builder()
                .id(employee.getId())
                .personalId(employee.getPersonalId())
                .name(employee.getName())
                .teamId(employee.getTeam() != null ? employee.getTeam().getId() : null)
                .build();
    }

    public static List<EmployeeResponse> toResponses(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return new ArrayList<>();
        }

        return employees.stream()
                .map(employee -> EmployeeResponse.builder()
                        .id(employee.getId())
                        .personalId(employee.getPersonalId())
                        .name(employee.getName())
                        .teamId(employee.getTeam() != null ? employee.getTeam().getId() : null)
                        .build())
                .toList();
    }

}
