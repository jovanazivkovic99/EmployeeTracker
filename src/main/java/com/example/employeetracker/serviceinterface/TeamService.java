package com.example.employeetracker.serviceinterface;

import com.example.employeetracker.domain.Team;
import com.example.employeetracker.request.AddEmployeesRequest;
import com.example.employeetracker.request.TeamRequest;
import com.example.employeetracker.response.TeamResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeamService {

    TeamResponse createTeam(TeamRequest request);
    TeamResponse getTeamById(Long teamId);
    List<TeamResponse> getAllTeams();
    TeamResponse updateTeam(Long teamId, TeamRequest updatedTeam);
    void deleteTeam(Long teamId);

    TeamResponse removeEmployeeFromTeam(Long teamId, Long employeeId);

    List<Team> searchTeams(String teamName, Long teamLeadId);
}