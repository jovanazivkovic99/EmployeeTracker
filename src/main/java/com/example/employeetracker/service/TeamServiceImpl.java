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

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Creates a new team, optionally assigning employees and a team lead.
     * <p>
     * The method does:
     * <ul>
     *   <li>Creates a new {@link Team} entity with the provided name.</li>
     *   <li>Adds the specified employees to the team, removing them from their old teams if needed.</li>
     *   <li>Assigns a team lead if a valid {@code teamLeadId} is provided. If the team already has a lead,
     *       it replaces the old lead with the new one</li>
     *   <li>Saves the team and returns the newly created team as a {@link TeamResponse}</li>
     * </ul>
     *
     * @param request A {@link TeamRequest} object containing the team name, employee IDs,
     *                and an optional team lead ID.
     * @return A {@link TeamResponse} object representing the newly created team, including
     *         any assigned employees and lead details.
     * @throws ResourceNotFoundException If any of the employee or team lead IDs do not exist.
     */
    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {

        Team team = new Team();
        team.setName(request.teamName());

        // add employees

        if (request.employeeIds() != null && !request.employeeIds().isEmpty()) {
            addEmployeesToTeam(request.employeeIds(), team);
        }

        // assign team lead, if provided
        if (request.teamLeadId() != null) {
            internalAssignLead(request.teamLeadId(), team);
        }

        Team savedTeam = teamRepository.save(team);

        return TeamMapper.toResponse(savedTeam);
    }

    /**
     * Fetches the team with the given ID
     * <p>
     * If the team doesn’t exist, we throw a ResourceNotFoundException
     *
     * @param teamId The ID of the team
     * @return A response with the team's details
     */
    @Override
    public TeamResponse getTeamById(Long teamId) {
        Team team = findTeamById(teamId);
        return TeamMapper.toResponse(team);
    }


    /**
     * Fetches all teams from the database
     *
     * @return A list of all teams wrapped in a response DTO
     */
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

        if (updatedTeam.teamName() != null && !updatedTeam.teamName().isBlank()) {
            team.setName(updatedTeam.teamName());
        }

        if (updatedTeam.employeeIds() != null && !updatedTeam.employeeIds().isEmpty()) {
            // add employees who are not already part of the team
            updatedTeam.employeeIds().forEach(employeeId -> {
                if (team.getEmployees().stream().noneMatch(employee -> employee.getId().equals(employeeId))) {
                    addEmployeesToTeam(Collections.singletonList(employeeId), team);
                }
            });
        }

        if (updatedTeam.teamLeadId() != null) {
            internalAssignLead(updatedTeam.teamLeadId(), team);
        }

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

    /**
     * Removes a given employee from the team,
     * if they're currently assigned there
     * <p>
     * After removing them from the team's employee list,
     * we set their {@code team} field to {@code null} so
     * there's no left behind reference. Finally, we save both the
     * employee and the team, then return the updated team data
     *
     * @param teamId     The team’s ID from which to remove an employee
     * @param employeeId Which employee to remove
     * @return The updated {@link TeamResponse}
     */
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

    /**
     * Internally sets the given employee as the new lead for the given team
     * <p></p>
     *   <ul>
     *     <li>If the team already has a lead, the old lead is replaced with the new one</li>
     *    <li>If the new lead is part of another team, they are removed from the old team</li>
     *    <li>The new lead is added to the team's employee list if not already present</li>
     *   </ul>
     *
     */
    private void internalAssignLead(Long teamLeadId, Team team) {
        Employee newTeamLead = findEmployeeById(teamLeadId);

        team.setTeamLead(newTeamLead);
        newTeamLead.setTeam(team);
    }

    /**
     * Relocates the given employees to the specified new team.
     * This means removing them from their old team (if any),
     * and clearing the old team's lead reference if they were it.
     */
    private void addEmployeesToTeam(List<Long> employeeIds, Team team) {

        List<Employee> employees = employeeRepository.findAllById(employeeIds);

        for (Employee employee : employees) {
            employee.setTeam(team);
            if (!team.getEmployees().contains(employee)) {
                team.getEmployees().add(employee);
            }
        }
    }

}
