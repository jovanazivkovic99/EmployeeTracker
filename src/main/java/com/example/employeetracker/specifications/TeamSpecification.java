package com.example.employeetracker.specifications;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TeamSpecification {
    public static Specification<Team> filterTeams(
            String teamName,
            Long teamLeadId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (teamName != null && !teamName.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + teamName.toLowerCase() + "%"
                ));
            }

            if (teamLeadId != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("teamLeadId")),
                        "%" + teamLeadId + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
