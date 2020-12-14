
### More Repository Types

Instead of JPA entities, MongoDB persists what it calls documents.

```
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
		</dependency>

		<dependency>
			<groupId>de.flapdoodle.embed</groupId>
			<artifactId>de.flapdoodle.embed.mongo</artifactId>
		</dependency>

```
Flapdoodle is an in-memory MongoDB Repository that requires no configuration.

In the domain folder, we have 3 domain model objects, Department, Person and Staff. Staff is a MongoDB Document which encapsulates Person as a member.
Department is the other MongoDB document, it has an ID and a name and a DBRef to staff called chair.

```
@Document
public class Department {
    @Id
    private Integer id;
    
    private String name;
    
    @DBRef(db = "chair")
    private Staff chair;

    public Department(Integer id, String name, Staff chair) {
        this.id = id;
        this.name = name;
        this.chair = chair;
    }
```

For each document, there is a repository:

```
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByName(String name);
    @Query("{'name': {$regex:?0}}")
    List<Department> findNameByPattern(String pattern);

    List<Department> findByChairMemberLastName(String lastName);
```

```
public interface StaffRepository extends PagingAndSortingRepository<Staff, Integer> {
    List<Staff> findByMemberLastName(String lastName);

    @Query("{member.firstName': ?0 }")
    List<Staff> findByFirstName(String firstName);
}
```

However this one will fail:

```
// This method is failed because cannot perform Join across DBRef's
List<Department> findByChairMemberLastName(String lastName);
```
Unlike JPA, MongoDB does not permit joined queries.

### Java Persistence API (Hibernate)
Like Spring Data JPA Repositories, JDBC repositories interact with relational databases. However, it does not rely on JPA.
JPA offers such features as lazy loading, caching and dirty tracking. While useful, JPA can make an application more complex than necessary. Lazy loading may trigger expensive SQL statements or unexpected exception, caching hides recent updates to the databaae and dirty entity makes it difficult to locate the point of operation persistence.
Spring JDBC Repositories bypass caching, and dirty tracking in favor of a simpler model:

1. Simpler model, SQL issued when needed, fully loaded object.

Cons:
1. Many-to-one or many-to-many relationships not supported.

Remember!
1. The parent and child object lifecycles coupled.
2. Point of operator is not obvious.

### Spring Data reactive repository example
Spring Boot 2.0 introduced a new technology to provide non-blocking IO called the Reactive Stack. This new reactive stack leverages multi-core processors to handle several concurrent connections. This original Servlet Stack provides synchronous blocking IO with 1 request per thread. Spring data now provides reactive repository interface that interact with non-blocking data sources such as Mongo, Cassandra, Redis and Couchbase.

```
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
		</dependency>
```
In the DepartmentRepository extends:
```
public interface DepartmentRepository extends ReactiveCrudRepository<Department, String> {
}
```

In the return types:
```
<S extends T>Mono<S> save(S var1);
Mono<Boolean> existsById(ID var1);
Flux<T> findAll();
```
A Mono is an asynchronous publisher of 0 or 1 result.A Flux is a publisher of an asynchronous sequence of 0 to any number of results.

Example:
```
public interface StaffRepository extends ReactiveCrudRepository<Staff, Integer> {
    Flux<Staff> findByMemberLastName(String lastName);
}
```
We have a query method and this method must either return a Flux or a Mono, otherwise a run-time error is thrown at invocation.
 
```
public void mongoQueryMethods() {
        //Create 2 Mono Staff publishers, data not persisted yet!
        Mono<Staff> deanJonesMono = staffRepository.save(new Staff(1, new Person("John", "Jones")));
        Mono<Staff> deanMartinMono = staffRepository.save(new Staff(2, new Person("John", "Martin")));

        Flux<Staff> staffFlux = staffRepository.findAll();
        // At this moment, nothing is actually saved in the database.
        System.out.println("Staff count found in DB BEFORE subscription: " + staffFlux.count().block());

        //Subscribe with block(), returns entity. We will block deanJones until it's completed.
        Staff deanJones = deanJonesMono.block();
        
        //Subscribe with subscribe(), does not return entity.Now we create a staff in the database.
        deanMartinMono.subscribe();

        System.out.println("Staff count found in DB AFTER subscription:" + staffFlux.count().block()); // 2 staffs here in DB.

        //Query returns a Flux publisher
        Flux<Staff> deanMartinFindByFlux = staffRepository.findByMemberLastName("Martin");
        Staff deanMartin = deanMartinFindByFlux.blockFirst();

        //Departments

        //Create an asynchronous publisher that gets the total number of Departments
        Mono<Long> departmentCountMono = departmentRepository.findAll().count(); // It does not execute in the database : the query/

        //Create a asynchronouse Flux publisher that persists 3 new Departments
        Flux<Department> departmentFlux = departmentRepository.saveAll(
                Arrays.asList(new Department(100, "Humanities", deanJones),
                            new Department(200, "Natural Sciences", deanMartin),
                            new Department(300, "Social Sciences", deanJones))); // No records are saved.

        System.out.println("Departments found in DB BEFORE subscription: "
                + departmentCountMono.block());

        //Persist the 3 departments, and block until complete
        departmentFlux.blockLast(); //save till the last one is saved.

        System.out.println(("Departments found in DB AFTEr subscription: "
                + departmentCountMono.block()));
	}
```
=======

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
