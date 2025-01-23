package com.example.employeetracker.controller;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.mapper.EmployeeMapper;
import com.example.employeetracker.mapper.TeamMapper;
import com.example.employeetracker.request.AddEmployeesRequest;
import com.example.employeetracker.request.TeamRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.response.TeamResponse;
import com.example.employeetracker.serviceinterface.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    public TeamResponse createTeam(@Valid @RequestBody TeamRequest request) {
        return teamService.createTeam(request);
    }

    @GetMapping("/{teamId}")
    public TeamResponse getTeamById(@PathVariable Long teamId) {
        return teamService.getTeamById(teamId);
    }

    @GetMapping
    public List<TeamResponse> getAllTeams() {
        return teamService.getAllTeams();
    }

    @PutMapping("/{teamId}")
    public TeamResponse updateTeam(@PathVariable Long teamId, @RequestBody TeamRequest updatedTeam) {
        return teamService.updateTeam(teamId, updatedTeam);
    }

    @DeleteMapping("/{teamId}")
    public void deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
    }

    @PostMapping("/{teamId}/add-employees")
    public TeamResponse addEmployeesToTeam(
            @PathVariable Long teamId,
            @RequestBody AddEmployeesRequest request) {
        return teamService.addEmployeesToTeam(teamId, request);
    }

    @PutMapping("/{teamId}/employee/{employeeId}/assign")
    public TeamResponse assignTeamLead(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        return teamService.assignTeamLead(teamId, employeeId);
    }

    @PutMapping("/{teamId}/employee/{employeeId}/remove")
    public TeamResponse removeEmployeeFromTeam(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        return teamService.removeEmployeeFromTeam(teamId, employeeId);
    }

    @GetMapping("/search")
    public List<TeamResponse> searchTeams(
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Long teamLeadId
    ) {
        List<Team> teams = teamService.searchTeams(teamName, teamLeadId);

        return teams.stream()
                .map(team -> TeamResponse.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .teamLead(EmployeeMapper.toResponse(team.getTeamLead()))
                        .employees(TeamMapper.mapToEmployeesList(team.getEmployees()))
                        .build()
                )
                .toList();

    }

}
