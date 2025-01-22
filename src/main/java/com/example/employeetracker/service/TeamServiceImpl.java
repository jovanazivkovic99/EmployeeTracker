package com.example.employeetracker.service;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.exception.ResourceNotFoundException;
import com.example.employeetracker.mapper.TeamMapper;
import com.example.employeetracker.repository.EmployeeRepository;
import com.example.employeetracker.repository.TeamRepository;
import com.example.employeetracker.request.AddEmployeesRequest;
import com.example.employeetracker.request.TeamRequest;
import com.example.employeetracker.response.TeamResponse;
import com.example.employeetracker.serviceinterface.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        Employee teamLead = null;
        if (request.teamLeadId() != null) {
            teamLead = employeeRepository.findById(request.teamLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", request.teamLeadId()));

            // todo napraviti custom exception
            boolean isAlreadyTeamLead = teamRepository.existsByTeamLead(teamLead);
            if (isAlreadyTeamLead) {
                throw new IllegalStateException("The specified team lead is already assigned to another team.");
            }
        }

        Team savedTeam = teamRepository.save(TeamMapper.mapRequestToTeam(request, teamLead));
        if (teamLead != null) {
            teamLead.setTeam(savedTeam);
            teamLead.setTeamLead(true);
            employeeRepository.save(teamLead);
        }

        return TeamMapper.mapToTeamResponse(savedTeam);
    }

    @Override
    public TeamResponse getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        return TeamMapper.mapToTeamResponse(team);
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(TeamMapper::mapToTeamResponse)
                .toList();
    }

    @Override
    public TeamResponse updateTeam(Long teamId, TeamRequest updatedTeam) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        team.setName(updatedTeam.teamName());

        Team savedTeam = teamRepository.save(team);
        return TeamMapper.mapToTeamResponse(savedTeam);
    }

    @Override
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        teamRepository.delete(team);
    }

    @Transactional
    @Override
    public TeamResponse addEmployeesToTeam(Long teamId, AddEmployeesRequest request) {
        // Fetch the team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        List<Employee> employees = employeeRepository.findAllById(request.employeeIds());
        if (employees.size() != request.employeeIds().size()) {
            throw new ResourceNotFoundException("Some employees were not found.", teamId);
        }

        employees.forEach(employee -> employee.setTeam(team));
        employeeRepository.saveAll(employees);

        if (request.teamLeadId() != null) {
            Employee teamLead = employees.stream()
                    .filter(employee -> employee.getId().equals(request.teamLeadId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The teamLeadId must be one of the provided employee IDs."));

            // Check if the team already has a team lead
            if (team.getTeamLead() != null) {
                throw new IllegalStateException("This team already has a team lead.");
            }

            teamLead.setTeamLead(true);
            team.setTeamLead(teamLead);
            employeeRepository.save(teamLead);
        }

        teamRepository.save(team);

        return TeamMapper.mapToTeamResponse(team);
    }
}
