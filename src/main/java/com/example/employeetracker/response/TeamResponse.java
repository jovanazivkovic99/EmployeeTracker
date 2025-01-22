package com.example.employeetracker.response;

import com.example.employeetracker.domain.Employee;
import lombok.Builder;

import java.util.List;

@Builder
public record TeamResponse(Long id,
                           String teamName,
                           String teamLeadName,
                           List<EmployeeResponse> employees) {
}
