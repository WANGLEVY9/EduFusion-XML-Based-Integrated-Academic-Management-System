CREATE TABLE StudentA (
    sid VARCHAR(12) PRIMARY KEY,
    sname VARCHAR(20) NOT NULL,
    sex VARCHAR(2),
    dept VARCHAR(30),
    password VARCHAR(64) NOT NULL
);
CREATE TABLE CourseA (
    cid VARCHAR(10) PRIMARY KEY,
    cname VARCHAR(30) NOT NULL,
    credit INT NOT NULL,
    teacher VARCHAR(20),
    room VARCHAR(20),
    shareFlag CHAR(1) NOT NULL
);
CREATE TABLE SelectA (
    sid VARCHAR(12) NOT NULL,
    cid VARCHAR(10) NOT NULL,
    score INT NULL,
    CONSTRAINT PK_SelectA PRIMARY KEY (sid, cid)
);
CREATE TABLE AdminA (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);
INSERT INTO AdminA(username, password)
VALUES ('adminA', 'admin123');