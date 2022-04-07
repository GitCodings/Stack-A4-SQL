# CS122B Activity 4 - SQL

- [Scehma](#schema)
- [JSON](#json)
- [Subqueries](#subqueries)
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

Lets first take this query for example:

```sql
SELECT c.id, c.name, c.units 
FROM class c
    JOIN student_class sc ON class.id = sc.class_id 
WHERE sc.student_id = :studentId
```

This query would require us to iterate over the ResultSet to be able to creat a `List` of `StudentClass`. But we can save some time by using two MySQL functions:
 - `JSON_ARRAYAGG()`
 - `JSON_OBJECT()`

Lets first rewrite the previous query using one of these two functions:

```sql
SELECT JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units)
FROM class c
    JOIN student_class sc ON class.id = sc.class_id 
WHERE sc.student_id = :studentId
```

`JSON_OBJECT` takes a list of arguments in the form: `key, value, key, value, ...` (As many key value pairs). And it will return a JSON String. So for the previous query it would return rows with a single column:

```json
{
    "id": 1,
    "name": "Intro to CS",
    "units": 4,
}
```

Now we are getting our data in the form of a JSON, but we still need to iterate over all the rows and grab *each* json string. we can remove the need to do that by using the `JSON_ARRAYAGG()` function like this:

```sql
SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units))
FROM class c
    JOIN student_class sc ON class.id = sc.class_id 
WHERE sc.student_id = :studentId
```

Now when we run this query we will get a JSON Array String composed of all the rows as JSON Objects:

```json
[
  {
      "id": 1,
      "name": "Intro to CS",
      "units": 4,
  },
  {
      "id": 2,
      "name": "Data Structure",
      "units": 6,
  },
  ....
]
```

So now we get a single JSON Array String that we can use to map to our data. Normally we had Spring automatically map JSON to Model for us, but we can do it mannually when dealing with JSON Directly by using the `ObjectMapper` class. (We get this by asking spring for it in our `@Autowired` constructor)

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

There are cases where we will want to retrieve a large collection of data from our database that may seem to required multiple queries. However we can utalize subqueries to be able to do this in a single query (given that the query is not too costly)

Lets say we wanted to get a student's details *as well* as a list of their classes. This would normally required two queries:

```sql
SELECT id, first_name, last_name, year, gpa
FROM student s 
WHERE s.id = :studentId;

SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units))
FROM class c
    JOIN student_class sc ON class.id = sc.class_id 
WHERE sc.student_id = :studentId
```

But if we were to rewrite the function with a subquery we could do this in one action:

```sql
SELECT id, first_name, last_name, year, gpa,
(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units))
 FROM
    (SELECT id, name, units
     FROM class
        JOIN student_class sc ON class.id = sc.class_id
     WHERE sc.student_id = :studentId) AS c) AS classes. -- Notice that we name this column as classes
FROM student s
WHERE s.id = :studentId;
```

This query would retrieve 6 colums: 
  - `id` - Integer
  - `first_name` - String
  - `last_name` - String
  - `year` - Integer
  - `gpa` - Double
  - `classes` - String (A JSON Array String we can map)

## Dynamic Queries

