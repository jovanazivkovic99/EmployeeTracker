package com.example.employeetracker.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record TeamRequest(@NotBlank String teamName,
                          List<Long> employeeIds,
                          Long teamLeadId) {
}
