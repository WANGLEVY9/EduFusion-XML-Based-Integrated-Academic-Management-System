-- ============================================================
-- EduFusion - College A (SQL Server) 数据库初始化
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'edufusion_a')
BEGIN
    CREATE DATABASE edufusion_a;
END
GO

USE edufusion_a;
GO

-- 学生表
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentA')
BEGIN
    CREATE TABLE StudentA (
        sid VARCHAR(12) PRIMARY KEY,
        sname VARCHAR(20) NOT NULL,
        sex VARCHAR(2),
        dept VARCHAR(30),
        password VARCHAR(64) NOT NULL
    );
END
GO

-- 课程表
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'CourseA')
BEGIN
    CREATE TABLE CourseA (
        cid VARCHAR(10) PRIMARY KEY,
        cname VARCHAR(30) NOT NULL,
        credit INT NOT NULL,
        teacher VARCHAR(20),
        room VARCHAR(20),
        shareFlag CHAR(1) NOT NULL
    );
END
GO

-- 选课表
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SelectA')
BEGIN
    CREATE TABLE SelectA (
        sid VARCHAR(12) NOT NULL,
        cid VARCHAR(10) NOT NULL,
        score INT NULL,
        PRIMARY KEY (sid, cid),
        FOREIGN KEY (sid) REFERENCES StudentA(sid),
        FOREIGN KEY (cid) REFERENCES CourseA(cid)
    );
END
GO

-- 管理员表
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminA')
BEGIN
    CREATE TABLE AdminA (
        username VARCHAR(32) PRIMARY KEY,
        password VARCHAR(64) NOT NULL
    );
END
GO

-- 管理员账号
IF NOT EXISTS (SELECT 1 FROM AdminA WHERE username = 'adminA')
    INSERT INTO AdminA(username, password) VALUES ('adminA', 'admin123');
GO

PRINT 'College A (SQL Server) database initialized successfully.';
GO
