package com.example.employeetracker.service;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.EmployeeSpecification;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.exception.ResourceNotFoundException;
import com.example.employeetracker.mapper.EmployeeMapper;
import com.example.employeetracker.repository.EmployeeRepository;
import com.example.employeetracker.repository.TeamRepository;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.serviceinterface.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

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
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployeeById(id);

        if (request.personalId() != null) {
            employee.setPersonalId(request.personalId());
        }

        if (request.name() != null) {
            employee.setName(request.name());
        }

        if (request.teamId() != null) {
            Team newTeam = findTeamById(request.teamId());

            Team oldTeam = employee.getTeam();
            if (oldTeam != null) {
                oldTeam.getEmployees().remove(employee);
            }

            employee.setTeam(newTeam);
            newTeam.getEmployees().add(employee);
        }
        //Employee updatedEmployee = employeeRepository.save(employee);
        //teamRepository.save(updatedEmployee.getTeam());
        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

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
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        if (employee.getTeam() != null) {
            employee.getTeam().getEmployees().remove(employee);
        }
        employeeRepository.delete(employee);
    }

    /*@Override
    public Page<EmployeeResponse> searchEmployees(Map<String, String> params) {
        int offset = Integer.parseInt(params.get("offset"));
        int pageSize = Integer.parseInt(params.get("pageSize"));
        PageRequest pageable = PageRequest.of(offset,pageSize);

        params.remove("offset");
        params.remove("pageSize");

        Specification<Employee> specification = EmployeeSpecification.filterEmployee(params);

        final Page<Employee> employees = this.employeeRepository.findAll(specification,pageable);
        return employees.map(EmployeeMapper::employeeToEmployeeDto);
    }*/
    @Override
    public List<Employee> findAllEmployees(String personalId, String name) {
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
