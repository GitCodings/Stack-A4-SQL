package com.github.klefstad_teaching.cs122b.sql.model.data;

import java.util.Locale;

public enum StudentOrderBy
{
    GPA(" ORDER BY s.gpa "),
    FIRST_NAME(" ORDER BY s.first_name ");

    private final String sql;

    StudentOrderBy(String sql)
    {
        this.sql = sql;
    }

    public String toSql()
    {
        return sql;
    }

    public static StudentOrderBy fromString(String orderBy)
    {
        if (orderBy == null)
            return GPA;

        switch (orderBy.toUpperCase(Locale.ROOT))
        {
            case "GPA":
                return GPA;
            case "FIRSTNAME":
                return FIRST_NAME;
            default:
                throw new RuntimeException("No StudentOrderBy value for: " + orderBy);
        }
    }
}
