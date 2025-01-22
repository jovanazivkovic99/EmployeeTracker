package com.example.employeetracker.mapper;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.response.EmployeeResponse;

public class EmployeeMapper {

    public static Employee mapRequestToEmployee(EmployeeRequest request){
        Employee employee = new Employee();
        employee.setPersonalId(request.personalId());
        employee.setName(request.name());
        employee.setTeamLead(request.isTeamLead());
        return employee;
    }

    public static EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getPersonalId(),
                employee.getName(),
                employee.isTeamLead(),
                employee.getTeam() != null ? employee.getTeam().getName() : null
        );
    }
}
