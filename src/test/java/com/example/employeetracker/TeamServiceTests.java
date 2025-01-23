package com.example.employeetracker;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.repository.EmployeeRepository;
import com.example.employeetracker.repository.TeamRepository;
import com.example.employeetracker.request.TeamRequest;
import com.example.employeetracker.response.TeamResponse;
import com.example.employeetracker.service.TeamServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team createMockTeam(Long id, String name, Employee teamLead) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setEmployees(new ArrayList<>());
        team.setTeamLead(teamLead);
        return team;
    }

    private Employee createMockEmployee(Long id, String name, String personalId, Team team) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName(name);
        employee.setPersonalId(personalId);
        employee.setTeam(team);
        return employee;
    }

    @Test
    void createTeam_withTeamLeadAndEmployees_savesTeam() {
        
        Long teamLeadId = 1L;
        Long employeeId = 2L;

        Employee teamLead = createMockEmployee(teamLeadId, "John Doe", "12345", null);
        Employee employee = createMockEmployee(employeeId, "Jane Doe", "54321", null);

        TeamRequest request = TeamRequest.builder()
                .teamName("Engineering")
                .teamLeadId(teamLeadId)
                .employeeIds(List.of(employeeId))
                .build();

        when(employeeRepository.findById(teamLeadId)).thenReturn(Optional.of(teamLead));
        when(employeeRepository.findAllById(List.of(employeeId))).thenReturn(List.of(employee));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.createTeam(request);

        verify(teamRepository).save(any(Team.class));
        assertEquals("Engineering", response.name());
        assertEquals(teamLeadId, response.teamLead().id());
    }

    @Test
    void getTeamById_returnsTeam() {
        
        Long teamId = 1L;
        Team team = createMockTeam(teamId, "Engineering", null);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        
        TeamResponse response = teamService.getTeamById(teamId);

        
        assertEquals("Engineering", response.name());
    }

    @Test
    void getAllTeams_returnsListOfTeams() {
        
        Team team1 = createMockTeam(1L, "Engineering", null);
        Team team2 = createMockTeam(2L, "Marketing", null);

        when(teamRepository.findAll()).thenReturn(List.of(team1, team2));

        
        List<TeamResponse> responses = teamService.getAllTeams();

        
        assertEquals(2, responses.size());
        assertEquals("Engineering", responses.get(0).name());
        assertEquals("Marketing", responses.get(1).name());
    }

    @Test
    void updateTeam_updatesTeamDetails() {
        
        Long teamId = 1L;
        Long employeeId = 2L;
        Team team = createMockTeam(teamId, "Old Name", null);
        Employee employee = createMockEmployee(employeeId, "Jane Doe", "54321", null);

        TeamRequest request = TeamRequest.builder()
                .teamName("New Name")
                .employeeIds(List.of(employeeId))
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(employeeRepository.findAllById(List.of(employeeId))).thenReturn(List.of(employee));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        
        TeamResponse response = teamService.updateTeam(teamId, request);

        
        verify(teamRepository).save(team);
        assertEquals("New Name", response.name());
        assertEquals(1, team.getEmployees().size());
    }

    @Test
    void deleteTeam_removesTeamAndUnassignsEmployees() {
        
        Long teamId = 1L;
        Team team = createMockTeam(teamId, "Engineering", null);
        Employee employee = createMockEmployee(2L, "Jane Doe", "54321", team);
        team.getEmployees().add(employee);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        
        teamService.deleteTeam(teamId);

        
        verify(employeeRepository).saveAll(anyList());
        verify(teamRepository).delete(team);
        assertNull(employee.getTeam());
    }

    @Test
    void removeEmployeeFromTeam_removesEmployee() {
        
        Long teamId = 1L;
        Long employeeId = 2L;
        Team team = createMockTeam(teamId, "Engineering", null);
        Employee employee = createMockEmployee(employeeId, "Jane Doe", "54321", team);
        team.getEmployees().add(employee);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        
        TeamResponse response = teamService.removeEmployeeFromTeam(teamId, employeeId);

        
        verify(employeeRepository).save(employee);
        verify(teamRepository).save(team);
        assertNull(employee.getTeam());
        assertEquals(0, team.getEmployees().size());
    }

    @Test
    void searchTeams_returnsFilteredTeams() {
        
        Team team = createMockTeam(1L, "Engineering", null);
        when(teamRepository.findAll(any(Specification.class))).thenReturn(List.of(team));

        
        List<Team> teams = teamService.searchTeams("Engineering", null);

        
        assertEquals(1, teams.size());
        assertEquals("Engineering", teams.get(0).getName());
    }
}

