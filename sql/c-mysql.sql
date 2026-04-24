CREATE TABLE StudentC (
    sid VARCHAR(12) PRIMARY KEY,
    sname VARCHAR(20) NOT NULL,
    sex VARCHAR(2),
    major_name VARCHAR(30),
    password VARCHAR(64) NOT NULL
);
CREATE TABLE CourseC (
    cid VARCHAR(10) PRIMARY KEY,
    cname VARCHAR(30) NOT NULL,
    credit INT NOT NULL,
    teacher VARCHAR(20),
    room VARCHAR(20),
    is_shared CHAR(1) NOT NULL
);
CREATE TABLE SelectC (
    sid VARCHAR(12) NOT NULL,
    cid VARCHAR(10) NOT NULL,
    score INT NULL,
    PRIMARY KEY (sid, cid)
);
CREATE TABLE AdminC (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);
INSERT INTO AdminC(username, password)
VALUES ('adminC', 'admin123');