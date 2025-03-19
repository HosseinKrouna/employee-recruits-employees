package com.empfehlo.empfehlungsapp.repositories;

import com.empfehlo.empfehlungsapp.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
}
