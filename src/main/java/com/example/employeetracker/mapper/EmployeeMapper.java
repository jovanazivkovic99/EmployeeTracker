package com.example.employeetracker.mapper;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.response.EmployeeResponse;

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
}
