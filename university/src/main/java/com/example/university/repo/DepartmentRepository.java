package com.example.university.repo;

import com.example.university.domain.Department;
// Begin Mongo DB
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * DataSource Management for the Departments at the University.
 * <p>
 * Created by maryellenbowman.
 */
public interface DepartmentRepository extends ReactiveCrudRepository<Department, String> {
// End Mongo DB
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DataSource Management for the Departments at the University.
 *
 * Created by maryellenbowman
 */
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
// End Normal JDBC

}
