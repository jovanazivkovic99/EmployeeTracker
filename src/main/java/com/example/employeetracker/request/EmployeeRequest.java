package com.example.employeetracker.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record EmployeeRequest(@NotBlank String personalId,
                              @NotBlank String name,
                              Long teamId) {
}
