
# Querying with Spring Data
### Property Expression Query methods

[LinkedIn Learning Course "Spring:Spring Data 2" by Mary Ellen Bowman](http://bit.ly/SpringData)


Simple Query Method Property Expression Rules

1. Return Type
2. findBy
3. Entity attribute name (use camel case)
4. Optionally, chain subattribute names (findByAttendeeLastName)
5. Parameter with datatype of the entity attribute

Unlike regular SQL, syntax errors are found at startup, not runtime.

```
List<Student> findByAttendeLastName(String name)
```
The Attende is mispelled.
PropertyReferenceException: No property attendLastName found for type Student!

QUery Method: Conditional Expressions

```
List<Student> findByFullTimeOrAgeLessThan(boolean fullTime, int maxAge);
List<Student> findByAttendeeFirstNameAndAttendeeLastName(String firstName, String lastName);
// Same results
List<Student> findByAttendee(Person person);
```

We have query methods using expressions with operators.
```
List<Student> findByAgeGreaterThan(int minimumAge);
List<Student> findByFullTimeOrAgeLessThan(boolean fullTime, int maxAge);
List<Student> findByAttendeeLastNameIgnoreCase(String lastName);

// WildCard search
List<Student> findByAttendeeLastNameLike(String likeString);
```
Query Method Expression Limiting and Ordering 

// Find highest student in the alphabet
```
Student findFirstByOrderByAttendeeLastNameAsc();
```

// Find the oldest student
```
Student findTopByOrderByAgeDesc();
```
// Find the 3 oldest student

```
List<Student> findTop3ByOrderByAgeDesc();
```

=======
# Spring:Spring Data 2
## by Mary Ellen Bowman
[LinkedIn Learning Course "Spring:Spring Data 2" by Mary Ellen Bowman](http://linkedin-learning.pxf.io/dvmPy)

[Mary Ellen's LinkedIn Profile](https://www.linkedin.com/in/mebowman/ "Mary Ellen's LinkedIn Page")

[Mary Ellen's LinkedIn Learning Author Page](https://www.linkedin.com/learning/instructors/mary-ellen-bowman "Mary Ellen's LinkedIn Page")

_Twitter: @MEllenBowman_

[Live Interview with Mary Ellen](http://bit.ly/MaryEllenBowman "Live Interview with Mary Ellen")


### Repository contain exercises from the course
### Branches correspond to videos (chapter-lesson)
3-3 : CrudRepository Interface for Entities

3-4 : JPA Repository

4-1 : Property Expression Query methods

4-2 : Query Method Clauses and Expressions

4-3 : @Query query methods

4-4 : Paging and Sorting

4-5 : QueryByExample

4-6 : Debug Query Method Errors

4-7 : Optional<> Query Response

5-1 : Spring Data MongoDB Example

5-2 : Spring Data JDBC Example

5-3 : Spring Data Reactive Repository Example

6-1 : Spring Data REST

6-4 : Read-Only Repository Pattern


 ### This is a note from Vanessa Tran who is learning Spring Data 2.
 
 Website to download the Spring Data Bundle: https://spring.io/projects/spring-data
 
A classic problem in programming is mapping the physical model to the logical model. Physical models are relational database repository, and a logical model are Java domain objects.

With an ORM framework a developer declares metadata to associate Java classes and attributes to database tables and columns. The framework then handles the actual database interactions. So the code will be abstracted away from the database.This makes it lighter and easier to maintain.


JPA is just a specification. It's not framework. Framework is Hibernate, TopLink...
If someone mentioned JPA, it is using another provider following the JPA spec. The spec includes metada declarations to map:
1. Java classes (Java Entities) <==> Tables
2. Java attributes <==> Columns/Fields

An EntityManager creates, reads, updates and deletes entities. Changes to the Entity's state are reflected back to the database.The entity metadata is in the form of .xml files, or within Java classes, via Java annotations. 

#### Map a Datatable to a Java Class

We have a table as below:

STUDENT |
--- |
student_id (INT 11) |
student_name (VARCHAR 45)|
student_fulltime TINYINT(1) |
student_age INT(11)|

Java class called Student

```
@Entity
@Table(name="STUDENT")
public class Student {
    @Id // Unique identifier attribute
    @GeneratedValue(strategy=GenerationType.IDENTITY) // Generate the ID once it is persisted
    @Column(name="student_id")
    private Integer studentId;

    @Column(name="student_name")
    private String name;

    @Column(name="student_fulltime")
    private boolean fullTime;

    @Column(name="student_age")
    private Integer age;

    public Student(String name, boolean fullTime, Integer age) {
        this.name = name;
        this.fullTime = fullTime;
        this.age = age;
    }
...
```
JPA will take care the physical world for us.

#### Map multiple tables to Java classes

STUDENT |
--- |
student_id (INT 11) |
student_name (VARCHAR 45)|
student_fulltime TINYINT(1) |
student_age INT(11)|

1 to many with:

Enrollment |
--- |
student_id (INT 11) |
course_id INT(11)|

many with:

COURSE |
--- |
course_id (INT 11) |
course_name (VARCHAR 45)|
course_dept_id INT(11 |

many with:

DEPARTMENT |
--- |
dept_id (INT 11) |
dept_name (VARCHAR 45)|

So we have 2 new entities:

```
@Entity
@Table(name="Course")
public class Course {
    @Id
    @GeneratedValue
    @Column(name="course_id")
    private Integer id;

    @Column(name="course_name")
    private String name;
    
    @ManyToOne // Many courses mapped to one department
    @JoinColumn(name="course_dept_id")
    private Department department;
    
    public Course(String name, Department department) {
        this.name = name;
        this.department = department;
    }

    @Override 
    public String toString() {
        return "Course{" + "id=" + id +", name=" + name + " department=" + department.getName();
    }
```

````
@Entity
@Table(name="Department")
public class Department {
    @Id
    @GeneratedValue
    @Column(name="dept_id")
    private Integer id;

    @Column(name="dept_name")
    private String name;

    @OneToMany(mappedBy="department", 
                fetch=FetchType.EAGER, // Not automatically fetch from database, override by this one. JPA populate associated courses
                cascade=CascadeType.ALL) // One department has many courses, Any changes to department will apply to courses.
    private List<Course> courses = new ArrayList<>();

    public Department(String name) {
        this.name = name;
    }

    ...
````
One student has many enrollments

```
@Entity
@Table(name="STUDENT")
public class Student {
    @Id // Unique identifier attribute
    @GeneratedValue(strategy=GenerationType.IDENTITY) // Generate the ID once it is persisted
    @Column(name="student_id")
    private Integer studentId;

    @Column(name="student_name")
    private String name;

    @Column(name="student_fulltime")
    private boolean fullTime;

    @Column(name="student_age")
    private Integer age;

    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinTable(name="Enrollment",
        joinColumns={@JoinColumn(name="student_id")},
        inverseJoinColumns={@JoinColumn(name="course_id")})
    private List<Course> courses = new ArrayList<>();

    public Student(String name, boolean fullTime, Integer age) {
        this.name = name;
        this.fullTime = fullTime;
        this.age = age;
    }
...
```

#### Java Persistence Query Language (JPQL)

Interact with entities and their persistent state.

The query is portable to any database management system. Syntax is similar to SQL.

JPQL - Entities and attributes

SQL - tables and columns 

SQL:
````
Select * from STUDENT;
````

Take all students from the students table

```
SELECT s.student_id, s.student_name, s.student_fulltime, s.student_age, e.course_id, c.course_name, d.dept_name
 FROM student s JOIN enrollment e ON s.student_id = e.student_id 
   JOIN course c ON c.course_id = e.course_id 
    JOIN department d on c.course_dept_id = d.dept_id
    WHERE s.student_id = 1;
```
Now with JPQL:

```
@PersistenceContext
private EntityManager entityManager;

public void printJane() {
    Student jane = entityManager.createQuery("Select s from Student s where s.name='jane'", Student.class).getSingleResult();
    System.out.println(jane);
```

Not only this JPQL query the object, but also pull all the courses because we set fetchType. Each course eargerly fetch all departments by default. 

#### Introduction to Spring Data JPA

Going from C and C++ to Java, now portable applications can be written once and run anywhere without rebuild. J2EE builds the enterprise solutions without needing to code the supporting infrastructure. 
Hibernate eliminated manually mapping logical objects to physical database. Asynchronous Javascript and XML, also known as AJAX, websites became truly interactive like desktop user interfaces. RESTful web services significantly 
reduce multi machine communication complexity.

Create Method:

```
@PersistenceContext
Entity Manager entityManager;

Student create(String name, boolean isFullTime, int age) {
    entityManager.getTransaction().begin();
    Student newStudent = new Student(name, isFullTime, age);
    entityManager.persist(newStudent);
    entityManager.getTransaction().commit();
    entityManager.close();
    return newStudent;
}
```

Update method:

```
@PersistenceContext
EntityManager entityManager;

void updateAge(int studentId, int age) {
    entityManager.getTransaction().begin();
    Student student = entityManager.find(Student.class, studentId);
    student.setAge(age);
    entityManager.persist(student);
    entityManager.getTransaction().commit();

```

Delete method:

```
@PersistenceContext
EntityManager entityManager;

void delete(int studentId) {
    entityManager.getTransaction().begin();
    Student student = entityManager.find(Student.class, studentId);
    entityManager.remove(student);
    entityManager.getTransaction().commit();
    entityManager.close();
}
```

Read method:

```
@PersistenceContext
EntityManager entityManager;

List<Student> read(String nameLike) {
    Query query = entityManager.createQuery(
    "select s from Student s where s.name LIKE :someName", Student.class);
    query.setParameter("someName", "%s" + nameLike + "%");
    List<Student> result = query.getResultList();
    entityManager.close();
    return result;
}
```
If we did as above with entityManager where we injected sql query, we may face a lot of problems such as Student.class is not matching with query...

#### Spring Data JPA

Spring Data Repository Interfaces, start with interface Repository:

```
public interface Repository<T, ID>
```
T is an entity class name, ID is a type of unique ID. This is a maker without any methods.

```
public interface CrudRepository<T, ID> extends Repository<T, ID>
```
Both repository and CRUDRepository are packaged in com.framework.data.repository;

To create or update, invoke:

```
T save(T entity);
Iterable<T> saveAll(Iterable<T> entity);
```
 
Delete Methods:

```
void deleteById(ID id)
void deleteAll(Iterable<?> extends T>)
void delete(T var1)
void deleteAll()
``` 

Find or lookup:

```
Optional<T> findById(ID id)
Iterable<T>
```

```
public interface StudentRepository extends CrudRepository<Student, Integer>

```

