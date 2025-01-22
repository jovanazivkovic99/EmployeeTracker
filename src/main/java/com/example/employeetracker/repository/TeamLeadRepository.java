package com.example.employeetracker.repository;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.TeamLead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamLeadRepository extends JpaRepository<TeamLead, Long> {
}
