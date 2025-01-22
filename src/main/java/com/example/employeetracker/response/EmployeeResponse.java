package com.example.employeetracker.response;

import lombok.Builder;

@Builder
public record EmployeeResponse(Long id,
                               String personalId,
                               String name,
                               boolean isTeamLead,
                               String teamName) {
}
