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
