package com.gitcodings.stack.sql.model.response;


import com.gitcodings.stack.sql.model.data.Student;
import com.gitcodings.stack.sql.model.data.StudentClass;

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
