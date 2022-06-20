package com.gitcodings.stack.sql.model.data;

public class StudentClass
{
    private Long    id;
    private String  name;
    private Integer units;

    public Long getId()
    {
        return id;
    }

    public StudentClass setId(Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public StudentClass setName(String name)
    {
        this.name = name;
        return this;
    }

    public Integer getUnits()
    {
        return units;
    }

    public StudentClass setUnits(Integer units)
    {
        this.units = units;
        return this;
    }
}
