package com.example.university.domain;

// Begin Mongo

/**
 * Person encapsulates an individual's first and last name.
 * <p>
 * Created by maryellenbowman
 */
public class Person {

    private String firstName;

//Begin Master
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Person encapsulates an individual's first and last name.
 *
 * Created by maryellenbowman
 */
@Embeddable
public class Person {
    @Column
    private String firstName;

    @Column
// End Master
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    protected Person() {
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return  " firstName='" + firstName + '\'' +
                ", lastName='" + lastName + "\' ";
    }
}
