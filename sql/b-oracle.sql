CREATE TABLE StudentB (
    sid VARCHAR2(12) PRIMARY KEY,
    sname VARCHAR2(20) NOT NULL,
    gender VARCHAR2(2),
    major_name VARCHAR2(30),
    passwd VARCHAR2(64) NOT NULL
);
CREATE TABLE CourseB (
    cno VARCHAR2(10) PRIMARY KEY,
    ctitle VARCHAR2(40) NOT NULL,
    credit_num NUMBER(3),
    instructor VARCHAR2(30),
    classroom VARCHAR2(20),
    share_flag CHAR(1) NOT NULL
);
CREATE TABLE SelectB (
    sid VARCHAR2(12) NOT NULL,
    cno VARCHAR2(10) NOT NULL,
    score NUMBER(3),
    CONSTRAINT PK_SelectB PRIMARY KEY (sid, cno)
);
CREATE TABLE AdminB (
    username VARCHAR2(32) PRIMARY KEY,
    password VARCHAR2(64) NOT NULL
);
INSERT INTO AdminB(username, password)
VALUES ('adminB', 'admin123');