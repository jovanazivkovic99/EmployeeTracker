package com.example.employeetracker.specifications;

import com.example.employeetracker.domain.Employee;
import com.example.employeetracker.domain.Team;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeSpecification {

    /**
     * Creates a dynamic query for {@link Employee}.
     *
     * @param personalId   Optional piece of text we match against the team's personal id (case-insensitive).
     * @param name Optional team name.
     * @return {@link Specification} of a Team
     */
    public static Specification<Employee> filterEmployee(String personalId, String name) {
        return (root, query, criteriaBuilder) -> {
            Predicate personalIdPredicate =
                    criteriaBuilder.like(root.get("personalId"), StringUtils.isBlank(personalId)
                            ? likePattern("") : personalId);
            Predicate namePredicate =
                    criteriaBuilder.like(root.get("name"), StringUtils.isBlank(name)
                            ? likePattern("") : name);
            return criteriaBuilder.and(personalIdPredicate, namePredicate);
        };
    }

    private static String likePattern(String value) {
        return "%" + value + "%";
    }
}
