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
