package com.github.klefstad_teaching.cs122b.sql.model.response;


import com.github.klefstad_teaching.cs122b.sql.model.data.Student;
import com.github.klefstad_teaching.cs122b.sql.model.data.StudentClass;

import java.util.List;

public class StudentDetailResponse
{
    private Student            student;
    private List<StudentClass> classes;

    public Student getStudent()
    {
        return student;
    }

    public StudentDetailResponse setStudent(Student student)
    {
        this.student = student;
        return this;
    }

    public List<StudentClass> getClasses()
    {
        return classes;
    }

    public StudentDetailResponse setClasses(List<StudentClass> classes)
    {
        this.classes = classes;
        return this;
    }
}
