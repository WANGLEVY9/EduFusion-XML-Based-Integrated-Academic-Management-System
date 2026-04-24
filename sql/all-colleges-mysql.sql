CREATE DATABASE IF NOT EXISTS edufusion_iams CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edufusion_iams;
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS StudentA (
    sid VARCHAR(12) PRIMARY KEY,
    sname VARCHAR(20) NOT NULL,
    sex VARCHAR(2),
    dept VARCHAR(30),
    password VARCHAR(64) NOT NULL
);
CREATE TABLE IF NOT EXISTS CourseA (
    cid VARCHAR(10) PRIMARY KEY,
    cname VARCHAR(30) NOT NULL,
    credit INT NOT NULL,
    teacher VARCHAR(20),
    room VARCHAR(20),
    shareFlag CHAR(1) NOT NULL
);
CREATE TABLE IF NOT EXISTS SelectA (
    sid VARCHAR(12) NOT NULL,
    cid VARCHAR(10) NOT NULL,
    score INT NULL,
    PRIMARY KEY (sid, cid)
);
CREATE TABLE IF NOT EXISTS AdminA (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);
CREATE TABLE IF NOT EXISTS StudentB (
    sid VARCHAR(12) PRIMARY KEY,
    sname VARCHAR(20) NOT NULL,
    gender VARCHAR(2),
    major_name VARCHAR(30),
    passwd VARCHAR(64) NOT NULL
);
CREATE TABLE IF NOT EXISTS CourseB (
    cno VARCHAR(10) PRIMARY KEY,
    ctitle VARCHAR(40) NOT NULL,
    credit_num INT,
    instructor VARCHAR(30),
    classroom VARCHAR(20),
    share_flag CHAR(1) NOT NULL
);
CREATE TABLE IF NOT EXISTS SelectB (
    sid VARCHAR(12) NOT NULL,
    cno VARCHAR(10) NOT NULL,
    score INT,
    PRIMARY KEY (sid, cno)
);
CREATE TABLE IF NOT EXISTS AdminB (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);
CREATE TABLE IF NOT EXISTS StudentC (
    sid VARCHAR(12) PRIMARY KEY,
    sname VARCHAR(20) NOT NULL,
    sex VARCHAR(2),
    major_name VARCHAR(30),
    password VARCHAR(64) NOT NULL
);
CREATE TABLE IF NOT EXISTS CourseC (
    cid VARCHAR(10) PRIMARY KEY,
    cname VARCHAR(30) NOT NULL,
    credit INT NOT NULL,
    teacher VARCHAR(20),
    room VARCHAR(20),
    is_shared CHAR(1) NOT NULL
);
CREATE TABLE IF NOT EXISTS SelectC (
    sid VARCHAR(12) NOT NULL,
    cid VARCHAR(10) NOT NULL,
    score INT NULL,
    PRIMARY KEY (sid, cid)
);
CREATE TABLE IF NOT EXISTS AdminC (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);
INSERT INTO StudentA(sid, sname, sex, dept, password)
VALUES ('A001', 'AliceA', 'M', 'CS', '123456'),
    ('A002', 'BobA', 'F', 'Math', '123456'),
    ('A003', 'CarolA', 'F', 'Physics', '123456'),
    ('A004', 'DavidA', 'M', 'Chem', '123456'),
    ('A005', 'EvanA', 'M', 'CS', '123456'),
    ('A006', 'FionaA', 'F', 'SE', '123456'),
    ('A007', 'GraceA', 'F', 'CS', '123456'),
    ('A008', 'HenryA', 'M', 'Math', '123456'),
    ('A009', 'IrisA', 'F', 'EE', '123456'),
    ('A010', 'JackA', 'M', 'AI', '123456') ON DUPLICATE KEY
UPDATE sname =
VALUES(sname),
    sex =
VALUES(sex),
    dept =
VALUES(dept),
    password =
VALUES(password);
INSERT INTO StudentB(sid, sname, gender, major_name, passwd)
VALUES ('B001', 'AliceB', 'M', 'Computer', '123456'),
    ('B002', 'BobB', 'F', 'Finance', '123456'),
    ('B003', 'CarolB', 'F', 'Computer', '123456'),
    ('B004', 'DavidB', 'M', 'Finance', '123456'),
    ('B005', 'EvanB', 'M', 'Economics', '123456'),
    ('B006', 'FionaB', 'F', 'Statistics', '123456'),
    ('B007', 'GraceB', 'F', 'Management', '123456'),
    ('B008', 'HenryB', 'M', 'Accounting', '123456'),
    ('B009', 'IrisB', 'F', 'Law', '123456'),
    ('B010', 'JackB', 'M', 'Computer', '123456') ON DUPLICATE KEY
UPDATE sname =
VALUES(sname),
    gender =
VALUES(gender),
    major_name =
VALUES(major_name),
    passwd =
VALUES(passwd);
INSERT INTO StudentC(sid, sname, sex, major_name, password)
VALUES ('C001', 'AliceC', 'M', 'Data', '123456'),
    ('C002', 'BobC', 'F', 'AI', '123456'),
    ('C003', 'CarolC', 'F', 'Data', '123456'),
    ('C004', 'DavidC', 'M', 'AI', '123456'),
    ('C005', 'EvanC', 'M', 'Software', '123456'),
    ('C006', 'FionaC', 'F', 'Security', '123456'),
    ('C007', 'GraceC', 'F', 'Cloud', '123456'),
    ('C008', 'HenryC', 'M', 'IoT', '123456'),
    ('C009', 'IrisC', 'F', 'Data', '123456'),
    ('C010', 'JackC', 'M', 'AI', '123456') ON DUPLICATE KEY
UPDATE sname =
VALUES(sname),
    sex =
VALUES(sex),
    major_name =
VALUES(major_name),
    password =
VALUES(password);
INSERT INTO CourseA(cid, cname, credit, teacher, room, shareFlag)
VALUES (
        'A101',
        'AdvancedMath',
        4,
        'TeacherA1',
        'A-101',
        '1'
    ),
    (
        'A102',
        'LinearAlgebra',
        3,
        'TeacherA2',
        'A-102',
        '0'
    ),
    (
        'A103',
        'DataStructure',
        4,
        'TeacherA3',
        'A-103',
        '1'
    ),
    (
        'A104',
        'ComputerNetwork',
        3,
        'TeacherA4',
        'A-104',
        '1'
    ),
    (
        'A105',
        'Probability',
        3,
        'TeacherA5',
        'A-105',
        '0'
    ),
    (
        'A106',
        'Compiler',
        4,
        'TeacherA6',
        'A-106',
        '1'
    ) ON DUPLICATE KEY
UPDATE cname =
VALUES(cname),
    credit =
VALUES(credit),
    teacher =
VALUES(teacher),
    room =
VALUES(room),
    shareFlag =
VALUES(shareFlag);
INSERT INTO CourseB(
        cno,
        ctitle,
        credit_num,
        instructor,
        classroom,
        share_flag
    )
VALUES (
        'B101',
        'DatabaseSystems',
        3,
        'TeacherB1',
        'B-201',
        '1'
    ),
    (
        'B102',
        'OperatingSystems',
        4,
        'TeacherB2',
        'B-202',
        '0'
    ),
    (
        'B103',
        'SoftwareEngineering',
        3,
        'TeacherB3',
        'B-203',
        '1'
    ),
    (
        'B104',
        'WebDevelopment',
        2,
        'TeacherB4',
        'B-204',
        '1'
    ),
    (
        'B105',
        'DataMining',
        3,
        'TeacherB5',
        'B-205',
        '0'
    ),
    (
        'B106',
        'CloudComputing',
        3,
        'TeacherB6',
        'B-206',
        '1'
    ) ON DUPLICATE KEY
UPDATE ctitle =
VALUES(ctitle),
    credit_num =
VALUES(credit_num),
    instructor =
VALUES(instructor),
    classroom =
VALUES(classroom),
    share_flag =
VALUES(share_flag);
INSERT INTO CourseC(cid, cname, credit, teacher, room, is_shared)
VALUES (
        'C101',
        'MachineLearning',
        3,
        'TeacherC1',
        'C-301',
        '1'
    ),
    (
        'C102',
        'BigDataIntro',
        2,
        'TeacherC2',
        'C-302',
        '0'
    ),
    (
        'C103',
        'DeepLearning',
        3,
        'TeacherC3',
        'C-303',
        '1'
    ),
    (
        'C104',
        'PythonProgram',
        2,
        'TeacherC4',
        'C-304',
        '1'
    ),
    (
        'C105',
        'DistributedSystem',
        4,
        'TeacherC5',
        'C-305',
        '0'
    ),
    (
        'C106',
        'InformationSecurity',
        3,
        'TeacherC6',
        'C-306',
        '1'
    ) ON DUPLICATE KEY
UPDATE cname =
VALUES(cname),
    credit =
VALUES(credit),
    teacher =
VALUES(teacher),
    room =
VALUES(room),
    is_shared =
VALUES(is_shared);
INSERT INTO AdminA(username, password)
VALUES ('adminA', 'admin123') ON DUPLICATE KEY
UPDATE password =
VALUES(password);
INSERT INTO AdminB(username, password)
VALUES ('adminB', 'admin123') ON DUPLICATE KEY
UPDATE password =
VALUES(password);
INSERT INTO AdminC(username, password)
VALUES ('adminC', 'admin123') ON DUPLICATE KEY
UPDATE password =
VALUES(password);
INSERT INTO SelectA(sid, cid, score)
VALUES ('A001', 'A101', NULL),
    ('A001', 'A103', NULL),
    ('A002', 'A102', NULL),
    ('A003', 'A104', NULL),
    ('A004', 'A105', NULL),
    ('A005', 'A101', NULL),
    ('A006', 'A106', NULL),
    ('A007', 'A103', NULL),
    ('A008', 'A104', NULL),
    ('A009', 'A105', NULL),
    ('A010', 'A106', NULL) ON DUPLICATE KEY
UPDATE score =
VALUES(score);
INSERT INTO SelectB(sid, cno, score)
VALUES ('B001', 'B101', NULL),
    ('B001', 'B103', NULL),
    ('B002', 'B102', NULL),
    ('B003', 'B104', NULL),
    ('B004', 'B105', NULL),
    ('B005', 'B106', NULL),
    ('B006', 'B101', NULL),
    ('B007', 'B103', NULL),
    ('B008', 'B104', NULL),
    ('B009', 'B105', NULL),
    ('B010', 'B106', NULL) ON DUPLICATE KEY
UPDATE score =
VALUES(score);
INSERT INTO SelectC(sid, cid, score)
VALUES ('C001', 'C101', NULL),
    ('C001', 'C103', NULL),
    ('C002', 'C102', NULL),
    ('C003', 'C104', NULL),
    ('C004', 'C105', NULL),
    ('C005', 'C106', NULL),
    ('C006', 'C101', NULL),
    ('C007', 'C103', NULL),
    ('C008', 'C104', NULL),
    ('C009', 'C105', NULL),
    ('C010', 'C106', NULL) ON DUPLICATE KEY
UPDATE score =
VALUES(score);