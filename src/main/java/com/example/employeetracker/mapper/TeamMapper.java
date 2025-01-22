package com.example.employeetracker.mapper;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.request.TeamRequest;
import com.example.employeetracker.response.TeamResponse;

import java.util.List;

public class TeamMapper {

    public static Team mapRequestToTeam(TeamRequest request, Employee teamLead) {
        Team team = new Team();
        team.setName(request.teamName());
        team.setTeamLead(teamLead);
        return team;
    }

    public static TeamResponse mapToTeamResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .teamName(team.getName())
                .teamLeadName(team.getTeamLead() != null ? team.getTeamLead().getName() : null)
                .employees(team.getEmployees() != null
                        ? team.getEmployees().stream()
                        .map(EmployeeMapper::mapToEmployeeResponse)
                        .toList()
                        : List.of())
                .build();
    }

}
