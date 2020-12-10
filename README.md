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

