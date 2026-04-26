-- ============================================================
-- EduFusion - College C (MySQL Docker) 数据库初始化
-- ============================================================
CREATE DATABASE IF NOT EXISTS edufusion_iams_c CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edufusion_iams_c;

-- 学生表
CREATE TABLE IF NOT EXISTS StudentC (
    sid VARCHAR(12) PRIMARY KEY,
    sname VARCHAR(20) NOT NULL,
    sex VARCHAR(2),
    major_name VARCHAR(30),
    password VARCHAR(64) NOT NULL
);

-- 课程表
CREATE TABLE IF NOT EXISTS CourseC (
    cid VARCHAR(10) PRIMARY KEY,
    cname VARCHAR(30) NOT NULL,
    credit INT NOT NULL,
    teacher VARCHAR(20),
    room VARCHAR(20),
    is_shared CHAR(1) NOT NULL
);

-- 选课表
CREATE TABLE IF NOT EXISTS SelectC (
    sid VARCHAR(12) NOT NULL,
    cid VARCHAR(10) NOT NULL,
    score INT NULL,
    PRIMARY KEY (sid, cid)
);

-- 管理员表
CREATE TABLE IF NOT EXISTS AdminC (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);

-- 管理员账号
INSERT IGNORE INTO AdminC(username, password) VALUES ('adminC', 'admin123');

SELECT 'College C (MySQL Docker) database initialized successfully.' AS status;
