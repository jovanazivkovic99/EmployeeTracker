package com.example.employeetracker;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import com.example.employeetracker.repository.EmployeeRepository;
import com.example.employeetracker.repository.TeamRepository;
import com.example.employeetracker.request.EmployeeRequest;
import com.example.employeetracker.request.EmployeeUpdateRequest;
import com.example.employeetracker.response.EmployeeResponse;
import com.example.employeetracker.service.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTests {

	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private TeamRepository teamRepository;

	@InjectMocks
	private EmployeeServiceImpl employeeService;

	@Test
	void addEmployee_withTeam_savesEmployeeWithTeam() {
		Long teamId = 1L;
		Team team = createMockTeam(teamId, "Engineering");

		EmployeeRequest request = EmployeeRequest.builder()
				.personalId("12345")
				.name("John Doe")
				.teamId(teamId)
				.build();

		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeResponse response = employeeService.addEmployee(request);

		verify(employeeRepository).save(any(Employee.class));
		assertEquals("John Doe", response.name());
		assertEquals("12345", response.personalId());
		assertEquals(teamId, response.teamId());
	}

	@Test
	void addEmployee_withoutTeam_savesEmployeeWithoutTeam() {
		// Arrange
		EmployeeRequest request = EmployeeRequest.builder()
				.personalId("12345")
				.name("Jane Doe")
				.build();

		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeResponse response = employeeService.addEmployee(request);

		verify(employeeRepository).save(any(Employee.class));
		assertEquals("Jane Doe", response.name());
		assertEquals("12345", response.personalId());
		assertNull(response.teamId());
	}

	@Test
	void updateEmployee_updatesEmployeeDetails() {

		Long employeeId = 1L;
		Employee employee = createMockEmployee(employeeId, "Old Name", "54321", null);

		EmployeeUpdateRequest request = EmployeeUpdateRequest.builder()
				.name("New Name")
				.personalId("12345")
				.build();

		when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
		when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

		EmployeeResponse response = employeeService.updateEmployee(employeeId, request);

		verify(employeeRepository).save(employee);
		assertEquals("New Name", response.name());
		assertEquals("12345", response.personalId());
	}

	@Test
	void deleteEmployee_removesTeamLeadAndDeletesEmployee() {

		Long employeeId = 1L;
		Team team = createMockTeam(1L, "Engineering");
		Employee employee = createMockEmployee(employeeId, "John Doe", "12345", team);
		team.setTeamLead(employee);

		when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

		employeeService.deleteEmployee(employeeId);

		verify(employeeRepository).delete(employee);
		assertNull(team.getTeamLead());
	}

	@Test
	void getAllEmployees_returnsEmployeeList() {
		// Arrange
		Employee employee1 = createMockEmployee(1L, "John Doe", "12345", null);
		Employee employee2 = createMockEmployee(2L, "Jane Doe", "54321", null);

		when(employeeRepository.findAll()).thenReturn(List.of(employee1, employee2));


		List<EmployeeResponse> responses = employeeService.getAllEmployees();


		assertEquals(2, responses.size());
		assertEquals("John Doe", responses.get(0).name());
		assertEquals("Jane Doe", responses.get(1).name());
	}

	@Test
	void getEmployeeById_returnsEmployee() {
		// Arrange
		Long employeeId = 1L;
		Employee employee = createMockEmployee(employeeId, "John Doe", "12345", null);

		when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));


		EmployeeResponse response = employeeService.getEmployeeById(employeeId);


		assertEquals("John Doe", response.name());
		assertEquals("12345", response.personalId());
	}

	@Test
	void searchEmployees_filtersEmployeesBasedOnCriteria() {
		// Arrange
		String personalId = "12345";
		String name = "John Doe";
		Employee employee = createMockEmployee(1L, name, personalId, null);

		when(employeeRepository.findAll(any(Specification.class))).thenReturn(List.of(employee));

		List<Employee> employees = employeeService.searchEmployees(personalId, name);

		assertEquals(1, employees.size());
		assertEquals("John Doe", employees.get(0).getName());
		assertEquals("12345", employees.get(0).getPersonalId());
	}

	private Employee createMockEmployee(Long id, String name, String personalId, Team team) {
		Employee employee = new Employee();
		employee.setId(id);
		employee.setName(name);
		employee.setPersonalId(personalId);
		employee.setTeam(team);
		return employee;
	}

	private Team createMockTeam(Long id, String name) {
		Team team = new Team();
		team.setId(id);
		team.setName(name);
		team.setEmployees(new ArrayList<>());
		return team;
	}
}
