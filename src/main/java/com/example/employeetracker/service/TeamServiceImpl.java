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
import com.example.employeetracker.specifications.TeamSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Creates a new team with optional employees and a team lead
     *
     * @param request A request body that includes team name, optional employee IDs, and optional team lead ID
     * @return The newly created team in a response object
     */
    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {

        Team team = new Team();
        team.setName(request.teamName());

        // if we have employeeIds
        if (request.employeeIds() != null && !request.employeeIds().isEmpty()) {
            List<Employee> employees = employeeRepository.findAllById(request.employeeIds());


            for (Employee e : employees) {
                Team oldTeam = e.getTeam();
                if (oldTeam != null) {

                    // if employee was the lead of that oldTeam, remove them as lead
                    if (oldTeam.getTeamLead() != null && oldTeam.getTeamLead().getId().equals(e.getId())) {
                        oldTeam.setTeamLead(null);
                    }

                    // remove from old team's employees list
                    oldTeam.getEmployees().remove(e);
                }
                e.setTeam(team);
                team.getEmployees().add(e);
            }
        }

        // if we have teamLeadId
        if (request.teamLeadId() != null) {

            Employee teamLead = employeeRepository.findById(request.teamLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", request.teamLeadId()));
            team.setTeamLead(teamLead);

            // ensure the lead is in the new team as well
            if (!team.getEmployees().contains(teamLead)) {
                // remove from old team if needed
                if (teamLead.getTeam() != null) {
                    teamLead.getTeam().getEmployees().remove(teamLead);
                }
                teamLead.setTeam(team);
                team.getEmployees().add(teamLead);
            }
        }

        Team savedTeam = teamRepository.save(team);

        return TeamMapper.toResponse(savedTeam);
    }

    @Override
    public TeamResponse getTeamById(Long teamId) {
        Team team = findTeamById(teamId);
        return TeamMapper.toResponse(team);
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(TeamMapper::toResponse)
                .toList();
    }

    /**
     * Updates a team's info (like name, employees, or team lead)
     *
     * @param teamId   The team to update
     * @param updatedTeam  The new data for the team
     * @return The updated team in a response object
     */
    @Override
    public TeamResponse updateTeam(Long teamId, TeamRequest updatedTeam) {
        Team team = findTeamById(teamId);

        team.setName(updatedTeam.teamName());

        Team savedTeam = teamRepository.save(team);
        return TeamMapper.toResponse(savedTeam);
    }

    /**
     * Completely removes the specified team from the database,
     * and any employees tied to that team get "unassigned" first
     * <p>
     * We set each employee's 'team' field to null,
     * then save those employees, and finally delete the team itself
     *
     * @param teamId the numeric ID of the team to remove
     */
    @Override
    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = findTeamById(teamId);
        List<Employee> employees = team.getEmployees();
        employees.forEach(employee -> employee.setTeam(null));
        employeeRepository.saveAll(employees);
        teamRepository.delete(team);
    }

    @Transactional
    @Override
    public TeamResponse addEmployeesToTeam(Long teamId, AddEmployeesRequest request) {
        // Fetch the team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        List<Employee> employees = employeeRepository.findAllById(request.employeeIds());

        // remove from old team (if any) and set them to the new team
        for (Employee e : employees) {
            Team oldTeam = e.getTeam();
            if (oldTeam != null) {
                oldTeam.getEmployees().remove(e);
            }
            e.setTeam(team);
            team.getEmployees().add(e);
        }

        // if we have team lead to assign
        if (request.teamLeadId() != null) {

            // Check if the team already has a team lead
            if (team.getTeamLead() != null) {
                throw new IllegalStateException("This team already has a team lead.");
            }

            Employee newTeamLead = employeeRepository.findById(request.teamLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", request.teamLeadId()));


            team.setTeamLead(newTeamLead);
        }

        return TeamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    public TeamResponse assignTeamLead(Long teamId, Long employeeId) {
        Team team = findTeamById(teamId);
        Employee teamLead = findEmployeeById(employeeId);

        if (team.getTeamLead() != null) {
            throw new IllegalStateException("Team already has a lead. Remove the old lead first.");
        } else {
            teamLead.setTeam(team);
            team.getEmployees().add(teamLead);
        }

        team.setTeamLead(teamLead);
        return TeamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    public TeamResponse removeEmployeeFromTeam(Long teamId, Long employeeId) {
        Team team = findTeamById(teamId);
        Employee employee = findEmployeeById(employeeId);

        if(employee.getTeam() != null && employee.getTeam().getId().equals(teamId)){
            team.getEmployees().remove(employee);
            employee.setTeam(null);
            employeeRepository.save(employee);
        }
        return TeamMapper.toResponse(teamRepository.save(team));
    }

    /**
     * Lets you search for teams by name or team lead ID
     *
     * @param teamName   Text to look for in the team's name (case-insensitive)
     * @param teamLeadId If not null, we look for teams led by this ID
     * @return A list of simple DTOs with team info
     */
    @Override
    public List<Team> searchTeams(String teamName, Long teamLeadId){
        Specification<Team> spec = TeamSpecification.filterTeams(
                teamName,
                teamLeadId
        );
        return teamRepository.findAll(spec);
    }

    private Team findTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
}
