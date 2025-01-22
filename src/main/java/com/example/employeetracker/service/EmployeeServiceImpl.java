package com.example.employeetracker.service;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.exception.ResourceNotFoundException;
import com.example.employeetracker.mapper.EmployeeMapper;
import com.example.employeetracker.repository.EmployeeRepository;
import com.example.employeetracker.repository.TeamRepository;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.serviceinterface.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    @Override
    public EmployeeResponse addEmployee(EmployeeRequest request) {

        if (request.personalId() == null || request.name() == null) {
            throw new IllegalArgumentException("Personal ID and Name are required.");
        }

        if (request.isTeamLead() && request.teamId() == null) {
            throw new IllegalArgumentException("A team lead must be associated with a team. Please provide a teamId.");
        }

        Team team = null;
        if(request.teamId() != null){
            team = findTeamById2(request.teamId());

            if(request.isTeamLead() && team.getTeamLead() != null){
                throw new IllegalStateException("This team already has a team lead.");
            }
        }

        Employee employee = EmployeeMapper.mapRequestToEmployee(request);

        employee.setTeam(team);

        Employee savedEmployee = employeeRepository.save(employee);

        return EmployeeMapper.mapToEmployeeResponse(savedEmployee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(EmployeeMapper::mapToEmployeeResponse)// kako napisati drugacije
                .toList();
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = findEmployeeById(id);
        return EmployeeMapper.mapToEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployeeById(id);

        if (request.personalId() != null) {
            employee.setPersonalId(request.personalId());
        }
        if (request.name() != null) {
            employee.setName(request.name());
        }
        employee.setTeamLead(request.isTeamLead());
        if (request.teamId() != null) {
            Team team = findTeamById(request);
            team.setTeamLead(employee);
            employee.setTeam(team);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        teamRepository.save(updatedEmployee.getTeam());
        return EmployeeMapper.mapToEmployeeResponse(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        employeeRepository.delete(employee);
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
    private Team findTeamById2(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
    private Team findTeamById(EmployeeRequest request) {
        return teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", request.teamId()));
    }
}
