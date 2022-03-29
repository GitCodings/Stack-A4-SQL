package com.github.klefstad_teaching.cs122b.sql.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.klefstad_teaching.cs122b.sql.model.data.Student;
import com.github.klefstad_teaching.cs122b.sql.model.data.StudentClass;
import com.github.klefstad_teaching.cs122b.sql.model.response.StudentDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SQLDetailController
{
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper               objectMapper;

    @Autowired
    public SQLDetailController(NamedParameterJdbcTemplate template, ObjectMapper objectMapper)
    {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    //language=sql
    private final static String STUDENT_WITH_CLASS_LIST =
        "SELECT id, first_name, last_name, year, gpa," +
        "(SELECT JSON_ARRAYAGG(JSON_OBJECT('id', c.id, 'name', c.name, 'units', c.units)) " +
        " FROM" +
        "    (SELECT id, name, units " +
        "     FROM class " +
        "        JOIN student_class sc ON class.id = sc.class_id " +
        "     WHERE sc.student_id = :studentId) AS c) AS classes " +
        "FROM student s " +
        "WHERE s.id = :studentId;";

    @GetMapping("/student/detail/{studentId}")
    public ResponseEntity<StudentDetailResponse> studentSearch(
        @PathVariable("studentId") Long studentId)
    {
        StudentDetailResponse response =
            this.template.queryForObject(
                STUDENT_WITH_CLASS_LIST,
                new MapSqlParameterSource().addValue("studentId", studentId, Types.INTEGER),
                this::methodInsteadOfLambdaForMapping
            );

        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }

    private StudentDetailResponse methodInsteadOfLambdaForMapping(ResultSet rs, int rowNumber)
        throws SQLException
    {
        List<StudentClass> classes = null;

        try {
            String jsonArrayString = rs.getString("classes");

            StudentClass[] studentClassArray =
                objectMapper.readValue(jsonArrayString, StudentClass[].class);

            classes = Arrays.stream(studentClassArray).collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map 'classes' to StudentClass[]");
        }

        Student student =
            new Student()
                .setId(rs.getLong("id"))
                .setFirstName(rs.getString("first_name"))
                .setLastName(rs.getString("first_name"))
                .setYear(rs.getInt("year"))
                .setGpa(rs.getDouble("gpa"));

        return new StudentDetailResponse()
            .setStudent(student)
            .setClasses(classes);
    }
}
