package com.example.employeetracker.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Builder
public record AddEmployeesRequest (
        List<Long> employeeIds,
        Long teamLeadId
){
}
