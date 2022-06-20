# Stack Activity 4 - SQL

- [Schema](#schema)
- [JSON](#json)
- [ObjectMapper](#objectmapper)
- [Subqueries](#subqueries)
- [Wildcard String Matching](#wildcard-string-matching)
- [Dynamic Queries](#dynamic-queries)

## Schema

Here is our schema we will be using for this activity

```sql
CREATE TABLE activity.student
(
    id         INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(256) NOT NULL,
    last_name  VARCHAR(256) NOT NULL,
    year       INT          NOT NULL,
    gpa        DECIMAL      NOT NULL
);

CREATE TABLE activity.class
(
    id    INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(256) NOT NULL UNIQUE,
    units INT          NOT NULL
);

CREATE TABLE activity.student_class
(
    student_id INT NOT NULL,
    class_id   INT NOT NULL,
    PRIMARY KEY (student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES activity.student (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES activity.class (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);
```

## JSON

MySQL Provides us with a good amount of functions for creating JSON strings. This can be very usefull when mapping data to our Models. 

Let us first take this query for example:

```sql
SELECT c.id, c.name, c.units 
FROM class c
    JOIN student_class sc ON c.id = sc.class_id 
WHERE sc.student_id = :studentId
```

This query would require us to iterate over the ResultSet to be able to creat a `List` of `StudentClass`. But we can save some time by using two MySQL functions:
 - `JSON_ARRAYAGG()`
 - `JSON_OBJECT()`

Lets first rewrite the previous query using one of these two functions:

```sql
SELECT JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units) as jsonString
FROM class c
    JOIN student_class sc ON c.id = sc.class_id 
WHERE sc.student_id = :studentId
```

`JSON_OBJECT` takes a list of arguments in the form: `key, value, key, value, ...` (As many key value pairs). And it will return a JSON String. So for the previous query it calling `rs.getString("jsonString")` would return this:

```json
{
    "id": 1,
    "name": "Intro to CS",
    "units": 4
}
```

Now we are getting our data in the form of a JSON, but we still need to iterate over all the rows and grab *each* json string. we can remove the need to do that by using the `JSON_ARRAYAGG()` function like this:

```sql
SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units)) as jsonArrayString
FROM class c
    JOIN student_class sc ON c.id = sc.class_id 
WHERE sc.student_id = :studentId
```

Now when we run this query and do `rs.getString("jsonArrayString")` we will get a JSON Array String composed of all the rows as JSON Objects:

```json
[
  {
      "id": 1,
      "name": "Intro to CS",
      "units": 4
  },
  {
      "id": 2,
      "name": "Data Structure",
      "units": 6
  }
]
```

So now we get a single JSON Array String that we can use to map to our data. Normally we had Spring automatically map JSON to Model for us, but we can do it manually when dealing with JSON Directly by using the `ObjectMapper` class. 

## ObjectMapper

We get our ObjectMapper by asking spring for it in our `@Autowired` constructor like so:

```java
@RestController
public class SQLController
{
    private final ObjectMapper objectMapper;
    
    @Autowired
    public SQLController(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }
}
```

ObjectMapper has a function: `ObjectMapper::readValue(String jsonString, Class<?> modelClass)` \
This function takes two arguments:
 1. String jsonString - the JSON String to map
 2. Class<?> modelClass - the Model's class we want to map to.

```java
ObjectMapper objectMapper; // We get this from our @Autowired constructor

String jsonArray = this.template(...); // We execture our query to get the JSON Array String

StudentClass[] studentClassArray = 
    objectMapper.readValue(jsonArrayString, StudentClass[].class); 
    // Notice we use Object[].class notation for Arrays
    // If we just had a JSON Object rather than a array we would just use StudentClass.class
```

## Subqueries

There are cases where we will want to retrieve a large collection of data from our database that may seem to required multiple queries. However, we can utilize subqueries to be able to do this in a single query (given that the query is not too costly)

Lets say we wanted to get a student's details *as well* as a list of their classes. This would normally require two queries:

```sql
SELECT id, first_name, last_name, year, gpa
FROM student s 
WHERE s.id = :studentId;

SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units)) AS jsonArrayString
FROM class c
    JOIN student_class sc ON c.id = sc.class_id 
WHERE sc.student_id = :studentId
```

But if we were to rewrite the function with a subquery we could do this in one action:

```sql
SELECT id, first_name, last_name, year, gpa,
(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units))
 FROM class c 
     JOIN student_class sc ON c.id = sc.class_id
 WHERE sc.student_id = :studentId) AS jsonArrayString  -- Notice that we name this column as classes
FROM student s
WHERE s.id = :studentId;
```

Sometimes we need to do more with the subquery, such as label as `DISTINCT` and give a `ORDER BY` we can do this by having another subquery:

```sql
SELECT id, first_name, last_name, year, gpa,
(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units))
 FROM (SELECT DISTINCT c.id, c.name, c.units
       FROM class c 
           JOIN student_class sc ON c.id = sc.class_id
       WHERE sc.student_id = :studentId
       ORDER BY c.name) as c) AS jsonArrayString
FROM student s
WHERE s.id = :studentId;
```

This query would retrieve 6 columns: 
  - `id` - Integer
  - `first_name` - String
  - `last_name` - String
  - `year` - Integer
  - `gpa` - Double
  - `classes` - String (A JSON Array String we can map)

## Wildcard String Matching

Sometimes when searching our database for strings we want to search by *sub-string* rather than an *exact* match. We do this by using both the `LIKE` operator (rather than the `=` operator) and having the string parameter start and end with the `%` character, like so:


```java
public Student studentByFirstName(String firstName)
{
    String SQL = 
        "SELECT id, first_name, last_name, year, gpa " + 
        "FROM student s " + 
        "WHERE first_name LIKE :firstName; ";

    // We add the '%' to the value rather than in the SQL Query
    String wildcardSearch = '%' + firstName + '%';

    MapSqlParameterSource source = new MapSqlParameterSource()
        .addValue("firstName", wildcardSearch, Types.VARCHAR);

    return this.template.queryForObject(SQL, source, this::mapToStudent);
}
```


## Dynamic Queries

For some queries we will not be able to use a 'static' SQL String because the `FROM` and `WHERE` clause will depend on the users request.

Lets say a user wants to search for students depending on their firstName, we would use this query:

```sql
SELECT id, first_name, last_name, year, gpa
FROM student s
WHERE first_name LIKE :firstName;
```

But then lets say the user wants to search for students depending on their first name **and** the classes they are in, meaning we would now need to `JOIN` the class table to our query like so:

```sql
SELECT DISTINCT id, first_name, last_name, year, gpa
FROM student s 
    JOIN student_class sc ON s.id = sc.student_id
    JOIN class c ON sc.class_id = c.id
WHERE c.name LIKE :className AND s.first_name LIKE :lastName;
```

Because of this we would need to *dynmaicly* create the 'WHERE' and 'FROM' clauses.

Here we prepair two queries:

This one for when the user is not searching by class:
```sql
SELECT id, first_name, last_name, year, gpa
FROM student s
```

This one for when the user is searching for class:
```sql
SELECT DISTINCT id, first_name, last_name, year, gpa
FROM student s 
    JOIN student_class sc ON s.id = sc.student_id
    JOIN class c ON sc.class_id = c.id
```

Then we would add the where clause depending on the search parameters the user gives us. We do this by using Java's `StringBuilder`:

```java
public List<Student> search(SearchRequest request)
{
    StringBuilder         sql;
    MapSqlParameterSource source     = new MapSqlParameterSource();
    boolean               whereAdded = false;

    // Here we create the inital StringBuilder depending on the query that requires it
    // In this case the need to search by classname requires we have the JOIN class clause
    if (request.getClassName() != null) {
        sql = new StringBuilder(STUDENT_WITH_CLASS);
        sql.append(" WHERE c.name LIKE :className ");

        // This allows for WILDCARD Search
        String wildcardSearch = '%' + request.getClassName() + '%';

        source.addValue("className", wildcardSearch, Types.VARCHAR);
        whereAdded = true;
    } else {
        sql = new StringBuilder(STUDENT_NO_CLASS);
    }

    ...
}
```

Notice that we also have a boolean `whereAdded`. We have this because we need to know if we need to start the where clause (with a `WHERE`) or if we need to add to the clause (with a `AND`) like so:

```java
public List<Student> search(SearchRequest request)
{
    ... // From previous example

     if (request.getFirstName() != null) {
        if (whereAdded) {
            sql.append(" AND ");
        } else {
            sql.append(" WHERE ");
            whereAdded = true;
        }

        sql.append(" s.first_name LIKE :firstName ");
        source.addValue("firstName", request.getFirstName(), Types.VARCHAR);
    }

    ... // We repeat as neccessary for each value that we need to search by

    // We then 'build' the sql using StringBuilder::toString()
    List<Student> students = this.template.query(
            sql.toString(),
            source,
            (rs, rowNum) ->
                new Student()
                    .setId(rs.getLong("id"))
                    .setFirstName(rs.getString("first_name"))
                    .setLastName(rs.getString("first_name"))
                    .setYear(rs.getInt("year"))
                    .setGpa(rs.getDouble("gpa"))
        );

    return students;
}
```

