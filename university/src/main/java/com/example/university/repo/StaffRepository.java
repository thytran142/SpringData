package com.example.university.repo;

import com.example.university.domain.Staff;
// Begin Mongo DB Reactive Crud Repository
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * DataSource Management for the Staff at the University.
 * <p>
 * Created by maryellenbowman.
 */
public interface StaffRepository extends ReactiveCrudRepository<Staff, Integer> {
    Flux<Staff> findByMemberLastName(String lastName);
// Begin Normal JDBC
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * DataSource Management for the Staff at the University.
 *
 * Created by maryellenbowman
 */
public interface StaffRepository extends PagingAndSortingRepository<Staff,Integer> {
}
