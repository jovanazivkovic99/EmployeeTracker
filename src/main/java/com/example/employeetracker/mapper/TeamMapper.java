package com.example.employeetracker.mapper;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.request.TeamRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.response.TeamResponse;

import java.util.List;

public class TeamMapper {

    public static TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .teamLead(mapToTeamLead(team.getTeamLead()))
                .employees(mapToEmployeesList(team.getEmployees()))
                .build();
    }

    private static EmployeeResponse mapToTeamLead(Employee teamLead) {
        if (teamLead == null) return null;
        return new EmployeeResponse(
                teamLead.getId(),
                teamLead.getPersonalId(),
                teamLead.getName(),
                teamLead.getTeam().getId()
        );
    }

    private static List<EmployeeResponse> mapToEmployeesList(List<Employee> employees) {
        if (employees == null) return List.of();
        return employees.stream()
                .map(emp -> new EmployeeResponse(emp.getId(), emp.getPersonalId(), emp.getName(), emp.getTeam().getId()))
                .toList();
    }

}
