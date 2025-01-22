package com.example.employeetracker.response;

import com.example.employeetracker.domain.Employee;
import lombok.Builder;

import java.util.List;

@Builder
public record TeamResponse(Long id,
                           String name,
                           EmployeeResponse teamLead,
                           List<EmployeeResponse> employees) {
}
