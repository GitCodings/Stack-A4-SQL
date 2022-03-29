package com.github.klefstad_teaching.cs122b.sql.model.response;


import com.github.klefstad_teaching.cs122b.sql.model.data.Student;

import java.util.List;

public class StudentSearchResponse
{
    private List<Student> students;

    public List<Student> getStudents()
    {
        return students;
    }

    public StudentSearchResponse setStudents(List<Student> students)
    {
        this.students = students;
        return this;
    }
}
