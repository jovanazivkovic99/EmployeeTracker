package com.example.employeetracker.request;

import lombok.Builder;

@Builder
public record TeamRequest(String teamName,
                          Long teamLeadId) {
}
