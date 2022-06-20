package com.gitcodings.stack.sql.rest;

import com.gitcodings.stack.sql.model.data.Student;
import com.gitcodings.stack.sql.model.data.StudentOrderBy;
import com.gitcodings.stack.sql.model.response.StudentSearchRequest;
import com.gitcodings.stack.sql.model.response.StudentSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Types;
import java.util.List;

@RestController
public class SQLSearchController
{
    private static final Logger LOG = LoggerFactory.getLogger(SQLSearchController.class);

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public SQLSearchController(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    //language=sql
    private final static String STUDENT_NO_CLASS =
        "SELECT id, first_name, last_name, year, gpa " +
        "FROM student s ";

    //language=sql
    private final static String STUDENT_WITH_CLASS =
        "SELECT DISTINCT s.id, first_name, last_name, year, gpa " +
        "FROM student s " +
        "    JOIN student_class sc ON s.id = sc.student_id " +
        "    JOIN class c ON sc.class_id = c.id ";

    @GetMapping("/student/search")
    public ResponseEntity<StudentSearchResponse> studentSearch(StudentSearchRequest request)
    {
        StringBuilder         sql;
        MapSqlParameterSource source     = new MapSqlParameterSource();
        boolean               whereAdded = false;

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

        if (request.getLastName() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            sql.append(" s.last_name LIKE :lastName ");
            source.addValue("lastName", request.getLastName(), Types.VARCHAR);
        }

        if (request.getYear() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            sql.append(" s.year = :year ");
            source.addValue("year", request.getYear(), Types.INTEGER);
        }

        if (request.getGpa() != null) {
            if (whereAdded) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                whereAdded = true;
            }

            sql.append(" s.gpa > :gpa ");
            source.addValue("gpa", request.getGpa(), Types.DOUBLE);
        }

        // We cannot have variable replacement for the ORDER BY clause
        // We fix that by having a ENUM of all the possible ORDER BY clauses
        // there could be and selecting one.
        StudentOrderBy orderBy = StudentOrderBy.fromString(request.getOrderBy());
        sql.append(orderBy.toSql());

        LOG.info(sql.toString());
        LOG.info(source.toString());

        List<Student> students = this.template.query(
            sql.toString(),
            source,
            (rs, rowNum) ->
                new Student()
                    .setId(rs.getLong("id"))
                    .setFirstName(rs.getString("first_name"))
                    .setLastName(rs.getString("last_name"))
                    .setYear(rs.getInt("year"))
                    .setGpa(rs.getDouble("gpa"))
        );

        StudentSearchResponse response = new StudentSearchResponse()
            .setStudents(students);

        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }
}
