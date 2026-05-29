-- ============================================================
-- EduFusion - College B 直接 PDB 初始化 + 数据填充
-- 直接创建表并插入数据 (在 PDB EDUFUSION_B 中执行)
-- ============================================================

-- 学生表
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE StudentB (
        sid VARCHAR2(12) PRIMARY KEY,
        sname VARCHAR2(20) NOT NULL,
        gender VARCHAR2(2),
        major_name VARCHAR2(30),
        passwd VARCHAR2(64) NOT NULL
    )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

-- 课程表
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE CourseB (
        cno VARCHAR2(10) PRIMARY KEY,
        ctitle VARCHAR2(40) NOT NULL,
        credit_num NUMBER(3),
        instructor VARCHAR2(30),
        classroom VARCHAR2(20),
        share_flag CHAR(1) NOT NULL
    )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

-- 选课表
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE SelectB (
        sid VARCHAR2(12) NOT NULL,
        cno VARCHAR2(10) NOT NULL,
        score NUMBER(3),
        CONSTRAINT PK_SelectB PRIMARY KEY (sid, cno)
    )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

-- 管理员表
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE AdminB (
        username VARCHAR2(32) PRIMARY KEY,
        password VARCHAR2(64) NOT NULL
    )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

-- 管理员
MERGE INTO AdminB D USING (SELECT 'adminB' AS username FROM dual) S
ON (D.username = S.username)
WHEN NOT MATCHED THEN INSERT (username, password) VALUES ('adminB', 'admin123');
COMMIT;

-- 学生 (30名)
INSERT ALL
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B001', '林伟', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B002', '林芳', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B003', '林娜', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B004', '林涛', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B005', '林洋', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B006', '何伟', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B007', '何芳', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B008', '何娜', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B009', '何涛', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B010', '何洋', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B011', '郭伟', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B012', '郭芳', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B013', '郭娜', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B014', '郭涛', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B015', '郭洋', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B016', '马伟', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B017', '马芳', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B018', '马娜', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B019', '马涛', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B020', '马洋', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B021', '罗伟', 'M', '法学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B022', '罗芳', 'F', '法学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B023', '罗娜', 'F', '法学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B024', '罗涛', 'M', '法学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B025', '罗洋', 'M', '法学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B026', '梁伟', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B027', '梁芳', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B028', '梁涛', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B029', '梁洋', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B030', '宋伟', 'M', '经济学', '123456')
SELECT * FROM dual;
COMMIT;

-- 课程 (12门)
INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B101', '数据库系统', 3, '赵教授', 'B-201', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B102', '操作系统原理', 4, '钱教授', 'B-202', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B103', '软件工程实践', 3, '孙教授', 'B-203', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B104', 'Web开发技术', 2, '李教授', 'B-204', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B105', '数据挖掘', 3, '周教授', 'B-205', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B106', '云计算技术', 3, '吴教授', 'B-206', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B107', '大数据技术', 3, '郑教授', 'B-207', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B108', '网络安全', 3, '王教授', 'B-208', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B109', '移动应用开发', 2, '冯教授', 'B-209', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B110', '分布式系统', 4, '陈教授', 'B-210', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B111', '人机交互设计', 2, '褚教授', 'B-211', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B112', '项目管理', 2, '卫教授', 'B-212', '1')
SELECT * FROM dual;
COMMIT;

-- 选课 (每生5门)
INSERT ALL
INTO SelectB(sid, cno, score) VALUES ('B001','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B001','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B001','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B001','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B001','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B002','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B002','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B002','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B002','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B002','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B003','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B003','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B003','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B003','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B003','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B004','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B004','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B004','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B004','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B004','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B005','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B005','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B005','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B005','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B005','B109',NULL)
INTO SelectB(sid, cno, score) VALUES ('B006','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B006','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B006','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B006','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B006','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B007','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B007','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B007','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B007','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B007','B111',NULL)
INTO SelectB(sid, cno, score) VALUES ('B008','B101',NULL)
INTO SelectB(sid, cno, score) VALUES ('B008','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B008','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B008','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B008','B112',NULL)
INTO SelectB(sid, cno, score) VALUES ('B009','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B009','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B009','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B009','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B009','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B010','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B010','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B010','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B010','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B010','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B011','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B011','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B011','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B011','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B011','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B012','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B012','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B012','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B012','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B012','B109',NULL)
INTO SelectB(sid, cno, score) VALUES ('B013','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B013','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B013','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B013','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B013','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B014','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B014','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B014','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B014','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B014','B111',NULL)
INTO SelectB(sid, cno, score) VALUES ('B015','B102',NULL)
INTO SelectB(sid, cno, score) VALUES ('B015','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B015','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B015','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B015','B112',NULL)
INTO SelectB(sid, cno, score) VALUES ('B016','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B016','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B016','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B016','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B016','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B017','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B017','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B017','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B017','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B017','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B018','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B018','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B018','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B018','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B018','B109',NULL)
INTO SelectB(sid, cno, score) VALUES ('B019','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B019','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B019','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B019','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B019','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B020','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B020','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B020','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B020','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B020','B111',NULL)
INTO SelectB(sid, cno, score) VALUES ('B021','B103',NULL)
INTO SelectB(sid, cno, score) VALUES ('B021','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B021','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B021','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B021','B112',NULL)
INTO SelectB(sid, cno, score) VALUES ('B022','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B022','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B022','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B022','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B022','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B023','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B023','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B023','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B023','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B023','B109',NULL)
INTO SelectB(sid, cno, score) VALUES ('B024','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B024','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B024','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B024','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B024','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B025','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B025','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B025','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B025','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B025','B111',NULL)
INTO SelectB(sid, cno, score) VALUES ('B026','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B026','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B026','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B026','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B026','B112',NULL)
INTO SelectB(sid, cno, score) VALUES ('B027','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B027','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B027','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B027','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B027','B109',NULL)
INTO SelectB(sid, cno, score) VALUES ('B028','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B028','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B028','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B028','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B028','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B029','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B029','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B029','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B029','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B029','B111',NULL)
INTO SelectB(sid, cno, score) VALUES ('B030','B105',NULL)
INTO SelectB(sid, cno, score) VALUES ('B030','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B030','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B030','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B030','B112',NULL)
SELECT * FROM dual;
COMMIT;

PROMPT College B (Oracle) full init and data inserted successfully.
