package com.example.university.domain;

// Begin Mongo DB: Each entity is a @Document
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * Mongo Document representing a staff member of a department.
 * <p>
 * Created by maryellenbowman
 */
@Document
public class Staff {
    @Id
    private Integer id;

    private Person member;

    public Staff(Integer id, Person member) {
        this.id = id;
        this.member = member;
    }

    public Integer getId() {
        return id;
//Begin Normal JDBC
import javax.persistence.*;
import javax.persistence.Id;

/**
 * JPA Entity representing a staff member of a department.
 *
 * Created by maryellenbowman
 */
@Entity
@Table(name="Staff_member")
public class Staff {
    @Id
    @GeneratedValue
    private Integer id;

    @Embedded
    private Person member;

    public Staff(Person member) {
        this.member = member;
    }

    protected Staff() {
    }

    public Person getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", member=" + member +
                '}';
    }
}
