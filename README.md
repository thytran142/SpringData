### Spring Data REST
Spring Data rest is a module for exposing Spring Data repositories as hypermedia-driven restful web services. 
1. Find all Spring Data Repositories (Bootstrap)
2. Create an endpoint that matches the entity name.
3. Append an s.
4. Exposes the operations as a RESTful Resource API over HTTP.

API to CrudRepository Method Mapping:

1. HTTP GET /resource CrudRepository.findAll()
2. HTTP GET /students StudentRepository.findAll()
3. HTTP GET /resource/<id> CrudRepository.findOne(id)
4. HTTP GET /resource/search/<queryMethod>/?param CrudRepository.querymethod(param)
5. HTTP POST /resource CrudRepository.save(entity)
6. HTTP PUT /resource/<id>, CrudRepository.save(entity)
7. HTTP PATCH /resource/<id> CrudRepository.save(entity)
8. HTTP Delete /resource/<id> CrudRepository.delete(entity)

#### Query Methods are static
```
public interface StudentRepository extends CrudRepository<Student, Integer> {
    List<Student> findByAttendeeLastName(String lastName);
    List<Student> findByFullTime(Boolean isFullTime);
...
}
```
Querydsl Extension is a dynamic search criteria framework. Spring Data comments provide an extension to querydsl via the querydsl predicate executor interface. A repository extending from querydsl predicate executor overloads the find and the count and exist methods. Predicate is the search criteria.

```
public interface StudentRepository extends CrudRepository<Student, Integer> QueryDslPredicateExecutor {
    Student findOne(Predicate predicate); // Predicate is the search criteria
}
```

Dynamic Queries:

```
public class StudentExpressions {
    public static BooleanExpression hasLastName(String lastName) {
        return QStudent.student.attendee.lastName.eq(lastName);
    }
    public static BooleanExpression isFullTime() {
        return QStudent.student.fullTime.eq(true);
    }
    public static BooleanExpression isOlderThan(int age) {
        return QStudent.student.age.gt(age);
    }
```
Qclasses are search criteria helpers.

Example to use them:
```
studentRepository.findAll(hasLastName("Smith").and(isFullTime()).and(isOlderThan(20));
studentRepository.findAll(isFullTime().or(isOlderThan(20));
studentRepository.findAll(hasLastName("Smith").and(isOlderThan(20));
```
 www.querydsl.com
 
Some applications must track when an entity was created and modified and which user did it. This is called auditing. 
```
@CreatedDate
@Column
private ZonedDateTime createdAt;

@LastModifiedBy
@Column
private User updatedBy;

@CreatedBy
@Column
private User createdBy;

@LastModifiedDate
```

Otherway is:
```
public class Staff extends AbstractAuditable<User, Integer> implements Auditable<User, Integer> {...}
```

```
public class SpringSecurityAuditorAware implements AuditorAware<User> {
      public User getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return ((MyUserDetails) authentication.getPrincipal()).getUser();
    }
```

#### Create a "No Repository Bean" interface

1. Create a new repository interface that extends from org.springframework.data.repository.
2. Annotate it with @NoRepositoryBean.
3. Add signatures of desired methods.

```
@NoRepositoryBean
public interface ReadOnlyRepository<T, ID extends Serializable> extends Repository<T,ID> {
    Optional<T> findById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> iterable);
    Iterable<T> findAll(Sort sort);
    Page<T> findAll(Pageable pageable);
    long count();
    boolean existsById(ID id);
}
```
