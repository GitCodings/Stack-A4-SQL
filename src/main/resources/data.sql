DELETE FROM activity.student WHERE id > 0;
DELETE FROM activity.class WHERE id > 0;

ALTER TABLE activity.student AUTO_INCREMENT = 1;
ALTER TABLE activity.class AUTO_INCREMENT = 1;

INSERT INTO activity.student (id, first_name, last_name, year, gpa)
VALUES (1, 'John', 'Smith', 1, 3.0),
       (2, 'James', 'Smith', 3, 3.2);


INSERT INTO activity.class (id, name, units)
VALUES (1, 'Intro to CS', 4),
       (2, 'Data Structure', 6),
       (3, 'Intro to Python', 4),
       (4, 'Intro to Java', 4),
       (5, 'Intro to C++', 4);

INSERT INTO activity.student_class (student_id, class_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 5),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5);
