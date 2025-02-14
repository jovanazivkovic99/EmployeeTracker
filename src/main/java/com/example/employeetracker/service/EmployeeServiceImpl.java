package com.example.employeetracker.service;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.request.EmployeeUpdateRequest;
import com.example.employeetracker.specifications.EmployeeSpecification;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.exception.ResourceNotFoundException;
import com.example.employeetracker.mapper.EmployeeMapper;
import com.example.employeetracker.repository.EmployeeRepository;
import com.example.employeetracker.repository.TeamRepository;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.serviceinterface.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    /**
     * Adds a new employee.
     * If a teamId is provided, we assign them to that team right away
     *
     * @param request The new employee's details
     * @return A response with the employee data
     */
    @Override
    public EmployeeResponse addEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setPersonalId(request.personalId());
        employee.setName(request.name());

        if (request.teamId() != null) {
            Team team = findTeamById(request.teamId());
            employee.setTeam(team);
            team.getEmployees().add(employee);
        }

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    public List<EmployeeResponse> addEmployees(List<EmployeeRequest> requests) {
        List<Employee> employees = new ArrayList<>();
        for (EmployeeRequest e :  requests){
            Employee employee = new Employee();
            employee.setPersonalId(e.personalId());
            employee.setName(e.name());

            if (e.teamId() != null) {
                Team team = findTeamById(e.teamId());
                employee.setTeam(team);
                team.getEmployees().add(employee);
            }

            employees.add(employee);

        }

        List<Employee> savedEmployees = employeeRepository.saveAll(employees);

        return EmployeeMapper.toResponses(savedEmployees);
    }

    /**
     * Updates the details of an existing employee.
     * <p>
     * The method does:
     * <ul>
     *   <li>1) Updates the employee's personal ID, if provided</li>
     *   <li>2) Updates the employee's name, if provided</li>
     *   <li>3) Updates the employee's team assignment, if a valid {@code teamId} is provided
     *       <ul>
     *           <li>If the employee is already assigned to a different team, they are removed from the old team.</li>
     *           <li>The employee is then added to the new team</li>
     *       </ul>
     *   </li>
     * </ul>
     * Finally, the updated employee is saved, and a response object is returned.
     * </p>
     *
     * @param id      The ID of the employee to update.
     * @param request {@link EmployeeRequest}
     * @return {@link EmployeeResponse} representing the updated employee
     * @throws ResourceNotFoundException If the employee or the new team (if specified) does not exist
     */
    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = findEmployeeById(id);

        if (request.personalId() != null) {
            employee.setPersonalId(request.personalId());
        }

        if (request.name() != null) {
            employee.setName(request.name());
        }

        if (request.teamId() != null) {
            Team newTeam = findTeamById(request.teamId());
            employee.setTeam(newTeam);
        }

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    /**
     * Gets a list of all employees
     *
     * @return A list of EmployeeResponse
     */
    @Override
    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = findEmployeeById(id);
        return EmployeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        if (employee.getTeam() != null) {
            Team team = employee.getTeam();
            if (team.getTeamLead() != null && team.getTeamLead().getId().equals(employee.getId())) {
                // remove the employee as the team lead
                team.setTeamLead(null);
            }
        }
        employeeRepository.delete(employee);
    }

    @Override
    public List<Employee> searchEmployees(String personalId, String name) {
        final Specification<Employee> specification =
                EmployeeSpecification.filterEmployee(
                        personalId, name);
        return employeeRepository.findAll(specification);
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    private Team findTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}
