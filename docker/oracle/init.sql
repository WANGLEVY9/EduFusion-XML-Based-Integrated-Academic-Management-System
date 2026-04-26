-- ============================================================
-- EduFusion - College B (Oracle) 数据库初始化
-- ============================================================
-- 用户已通过环境变量 APP_USER/APP_USER_PASSWORD 创建
ALTER SESSION SET CURRENT_SCHEMA = EDUFUSION_B;
GO

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
GO

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
GO

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
GO

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
GO

-- 管理员账号
MERGE INTO AdminB D USING (SELECT 'adminB' AS username FROM dual) S
ON (D.username = S.username)
WHEN NOT MATCHED THEN INSERT (username, password) VALUES ('adminB', 'admin123');
GO

COMMIT;
GO

PROMPT College B (Oracle) database initialized successfully.
