package com.example.employeetracker.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record EmployeeRequest(@NotBlank
                              @Pattern(regexp = "\\d{6}", message = "Personal id must be exactly 6 digits")
                              String personalId,
                              @NotBlank String name,
                              Long teamId) {
}
