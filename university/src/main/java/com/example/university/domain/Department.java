package com.example.university.domain;

// This piece of code is using Mongo DB , each entity as a Document
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Mongo Document for a Department of study at the University.
 * <p>
 * Created by maryellenbowman
 */
@Document
public class Department {
    @Id
    private Integer id;

    private String name;

    @DBRef(db = "chair")
    private Staff chair;

    public Department(Integer id, String name, Staff chair) {
        this.id = id;
// End mongo DB
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for a Department of study at the University.
 *
 * Created by maryellenbowman
 */
@Entity
@Table(name="Department")
public class Department {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;

    @OneToOne
    private Staff chair;

    @OneToMany(fetch = FetchType.EAGER,mappedBy="department",
            cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    public Department(String name, Staff chair) {
// This piece of code is using normal JDBC
        this.name = name;
        this.chair = chair;
    }

    protected Department() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

// Normal
    public void addCourse(Course course) {
        courses.add(course);
    }
// Normal

    public void setName(String name) {
        this.name = name;
    }

    public void setChair(Staff chair) {
        this.chair = chair;
    }

    @Override
    public String toString() {
        return "Department{" +
                "chair=" + chair +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
