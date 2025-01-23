package com.example.employeetracker.request;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record EmployeeUpdateRequest(@Pattern(regexp = "\\d{6}", message = "Personal id must be exactly 6 digits")
                                    String personalId,
                                    String name,
                                    Long teamId) {
}
