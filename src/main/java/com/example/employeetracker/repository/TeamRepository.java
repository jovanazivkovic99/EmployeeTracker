package com.example.employeetracker.repository;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
