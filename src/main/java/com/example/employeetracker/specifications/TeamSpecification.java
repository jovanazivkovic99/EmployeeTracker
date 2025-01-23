package com.example.employeetracker.specifications;

import com.example.employeetracker.domain.Team;
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

    /**
     * Creates a dynamic query for {@link Team}. Pass in a partial team name
     * or a team lead's ID to filter down the results.
     *
     * @param teamName   Optional piece of text we match against the team's name (case-insensitive).
     * @param teamLeadId Optional ID of the team's lead.
     * @return {@link Specification} of a Team
     */
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
                predicates.add(cb.equal(
                        root.get("teamLead").get("id"),
                        teamLeadId
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
