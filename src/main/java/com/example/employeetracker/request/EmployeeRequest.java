package com.example.employeetracker.request;

import lombok.Builder;

@Builder
public record EmployeeRequest(String personalId,
                              String name,
                              boolean isTeamLead,
                              Long teamId) {
}
