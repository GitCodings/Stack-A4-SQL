package com.github.klefstad_teaching.cs122b.sql.model.data;

public class Student
{
    private Long    id;
    private String  firstName;
    private String  lastName;
    private Integer year;
    private Double  gpa;

    public Long getId()
    {
        return id;
    }

    public Student setId(Long id)
    {
        this.id = id;
        return this;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public Student setFirstName(String firstName)
    {
        this.firstName = firstName;
        return this;
    }

    public String getLastName()
    {
        return lastName;
    }

    public Student setLastName(String lastName)
    {
        this.lastName = lastName;
        return this;
    }

    public Integer getYear()
    {
        return year;
    }

    public Student setYear(Integer year)
    {
        this.year = year;
        return this;
    }

    public Double getGpa()
    {
        return gpa;
    }

    public Student setGpa(Double gpa)
    {
        this.gpa = gpa;
        return this;
    }
}
