-- ============================================================
-- EduFusion - College B 差异化数据填充 (Oracle XE)
-- 学生: 55人 (B001-B055) | 课程: 52门 (B101-B152) | 选课: 165条
-- 特点: 低选课密度 (人均3门)，偏经管/商科
-- ============================================================
ALTER SESSION SET CURRENT_SCHEMA = EDUFUSION_B;

-- 清除旧数据（保留管理员）
DELETE FROM SelectB;
DELETE FROM CourseB;
DELETE FROM StudentB;
COMMIT;

-- ============================================================
-- 管理员
-- ============================================================
MERGE INTO AdminB D USING (SELECT 'adminB' AS username FROM dual) S
ON (D.username = S.username)
WHEN NOT MATCHED THEN INSERT (username, password) VALUES ('adminB', 'admin123');

-- ============================================================
-- 学生数据 (55人)
-- ============================================================
INSERT ALL
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B001', '林伟', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B002', '林芳', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B003', '林娜', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B004', '林涛', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B005', '林洋', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B006', '林雪', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B007', '林峰', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B008', '林雨', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B009', '林浩', 'M', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B010', '林悦', 'F', '金融学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B011', '林志远', 'M', '金融学', '123456')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B012', '何伟', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B013', '何芳', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B014', '何娜', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B015', '何涛', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B016', '何洋', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B017', '何晶', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B018', '昊天', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B019', '何雨', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B020', '何明', 'M', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B021', '何欣', 'F', '经济学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B022', '何晨', 'M', '经济学', '123456')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B023', '郭伟', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B024', '郭芳', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B025', '郭娜', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B026', '郭涛', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B027', '郭洋', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B028', '郭雪', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B029', '郭鹏', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B030', '郭婷', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B031', '郭浩', 'M', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B032', '郭静', 'F', '管理学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B033', '郭凯', 'M', '管理学', '123456')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B034', '马伟', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B035', '马芳', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B036', '马娜', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B037', '马涛', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B038', '马洋', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B039', '马晶', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B040', '马超', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B041', '马丽', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B042', '马云', 'M', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B043', '马慧', 'F', '会计学', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B044', '马强', 'M', '会计学', '123456')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B045', '罗伟', 'M', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B046', '罗芳', 'F', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B047', '罗娜', 'F', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B048', '罗涛', 'M', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B049', '罗洋', 'M', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B050', '罗晶', 'F', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B051', '罗浩', 'M', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B052', '罗悦', 'F', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B053', '罗凯', 'M', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B054', '罗琳', 'F', '国际贸易', '123456')
INTO StudentB(sid, sname, gender, major_name, passwd) VALUES ('B055', '罗杰', 'M', '国际贸易', '123456')
SELECT * FROM dual;
COMMIT;

-- ============================================================
-- 课程数据 (52门，含共享标记 1=共享 0=非共享)
-- ============================================================
INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B101', '微观经济学', 3, '何教授', 'B-101', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B102', '宏观经济学', 3, '郭教授', 'B-102', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B103', '计量经济学', 4, '马教授', 'B-103', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B104', '国际经济学', 3, '罗教授', 'B-104', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B105', '金融学原理', 3, '林教授', 'B-105', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B106', '公司金融', 3, '赵教授', 'B-106', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B107', '投资学', 4, '钱教授', 'B-107', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B108', '金融市场与机构', 3, '孙教授', 'B-108', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B109', '商业银行管理', 3, '李教授', 'B-109', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B110', '风险管理', 3, '周教授', 'B-110', '1')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B111', '会计学原理', 4, '吴教授', 'B-111', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B112', '中级财务会计', 3, '郑教授', 'B-112', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B113', '高级财务管理', 3, '王教授', 'B-113', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B114', '审计学', 3, '冯教授', 'B-114', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B115', '管理会计', 3, '陈教授', 'B-115', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B116', '税务筹划', 2, '褚教授', 'B-116', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B117', '市场营销学', 3, '卫教授', 'B-117', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B118', '消费者行为学', 2, '蒋教授', 'B-118', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B119', '品牌管理', 2, '沈教授', 'B-119', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B120', '电子商务概论', 2, '韩教授', 'B-120', '1')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B121', '组织行为学', 3, '杨教授', 'B-121', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B122', '人力资源管理', 3, '朱教授', 'B-122', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B123', '绩效管理', 2, '秦教授', 'B-123', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B124', '薪酬管理', 2, '许教授', 'B-124', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B125', '运营管理', 3, '潘教授', 'B-125', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B126', '供应链管理', 3, '苏教授', 'B-126', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B127', '物流管理', 3, '余教授', 'B-127', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B128', '质量管理', 2, '夏教授', 'B-128', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B129', '战略管理学', 3, '石教授', 'B-129', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B130', '商务统计学', 3, '崔教授', 'B-130', '1')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B131', '管理信息系统', 3, '钟教授', 'B-131', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B132', '数据分析与决策', 3, '范教授', 'B-132', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B133', '商业预测', 2, '方教授', 'B-133', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B134', '经济法学', 3, '谭教授', 'B-134', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B135', '国际商法', 2, '廖教授', 'B-135', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B136', '知识产权法', 2, '熊教授', 'B-136', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B137', '商务英语', 3, '陆教授', 'B-137', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B138', '国际贸易实务', 3, '曾教授', 'B-138', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B139', '国际金融', 3, '肖教授', 'B-139', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B140', '跨文化管理', 2, '贾教授', 'B-140', '0')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B141', '创业管理', 2, '田教授', 'B-141', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B142', '创新管理', 2, '魏教授', 'B-142', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B143', '项目管理', 3, '任教授', 'B-143', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B144', '客户关系管理', 2, '姚教授', 'B-144', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B145', '商业伦理', 2, '卢教授', 'B-145', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B146', '证券投资分析', 3, '傅教授', 'B-146', '1')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B147', '保险学原理', 3, '丁教授', 'B-147', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B148', '房地产金融', 2, '程教授', 'B-148', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B149', '固定收益证券', 3, '彭教授', 'B-149', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B150', '兼并与收购', 2, '翁教授', 'B-150', '1')
SELECT * FROM dual;
COMMIT;

INSERT ALL
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B151', '财富管理', 2, '邹教授', 'B-151', '0')
INTO CourseB(cno, ctitle, credit_num, instructor, classroom, share_flag) VALUES ('B152', '金融科技导论', 2, '常教授', 'B-152', '1')
SELECT * FROM dual;
COMMIT;

-- ============================================================
-- 选课数据 (55人 x 人均3门 = 165条)
-- College B 选课密度较低，人均仅选3门核心课
-- 基础核心课(B101-B110)选课人数集中
-- 专业课(B111-B140)选课人数中等
-- 选修课(B141-B152)选课人数较少
-- ============================================================

-- 第1组: B001-B011 (金融学) 选金融核心课
INSERT ALL
INTO SelectB(sid, cno, score) VALUES ('B001','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B001','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B001','B107',NULL)
INTO SelectB(sid, cno, score) VALUES ('B002','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B002','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B002','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B003','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B003','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B003','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B004','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B004','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B004','B139',NULL)
INTO SelectB(sid, cno, score) VALUES ('B005','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B005','B107',NULL) INTO SelectB(sid, cno, score) VALUES ('B005','B146',NULL)
INTO SelectB(sid, cno, score) VALUES ('B006','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B006','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B006','B108',NULL)
INTO SelectB(sid, cno, score) VALUES ('B007','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B007','B107',NULL) INTO SelectB(sid, cno, score) VALUES ('B007','B139',NULL)
INTO SelectB(sid, cno, score) VALUES ('B008','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B008','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B008','B106',NULL)
INTO SelectB(sid, cno, score) VALUES ('B009','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B009','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B009','B110',NULL)
INTO SelectB(sid, cno, score) VALUES ('B010','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B010','B107',NULL) INTO SelectB(sid, cno, score) VALUES ('B010','B146',NULL)
INTO SelectB(sid, cno, score) VALUES ('B011','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B011','B105',NULL) INTO SelectB(sid, cno, score) VALUES ('B011','B108',NULL)
SELECT * FROM dual;
COMMIT;

-- 第2组: B012-B022 (经济学) 选经济核心课
INSERT ALL
INTO SelectB(sid, cno, score) VALUES ('B012','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B012','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B012','B130',NULL)
INTO SelectB(sid, cno, score) VALUES ('B013','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B013','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B013','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B014','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B014','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B014','B137',NULL)
INTO SelectB(sid, cno, score) VALUES ('B015','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B015','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B015','B134',NULL)
INTO SelectB(sid, cno, score) VALUES ('B016','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B016','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B016','B130',NULL)
INTO SelectB(sid, cno, score) VALUES ('B017','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B017','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B017','B104',NULL)
INTO SelectB(sid, cno, score) VALUES ('B018','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B018','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B018','B137',NULL)
INTO SelectB(sid, cno, score) VALUES ('B019','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B019','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B019','B132',NULL)
INTO SelectB(sid, cno, score) VALUES ('B020','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B020','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B020','B134',NULL)
INTO SelectB(sid, cno, score) VALUES ('B021','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B021','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B021','B138',NULL)
INTO SelectB(sid, cno, score) VALUES ('B022','B101',NULL) INTO SelectB(sid, cno, score) VALUES ('B022','B102',NULL) INTO SelectB(sid, cno, score) VALUES ('B022','B130',NULL)
SELECT * FROM dual;
COMMIT;

-- 第3组: B023-B033 (管理学) 选管理核心课
INSERT ALL
INTO SelectB(sid, cno, score) VALUES ('B023','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B023','B121',NULL) INTO SelectB(sid, cno, score) VALUES ('B023','B129',NULL)
INTO SelectB(sid, cno, score) VALUES ('B024','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B024','B121',NULL) INTO SelectB(sid, cno, score) VALUES ('B024','B125',NULL)
INTO SelectB(sid, cno, score) VALUES ('B025','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B025','B121',NULL) INTO SelectB(sid, cno, score) VALUES ('B025','B143',NULL)
INTO SelectB(sid, cno, score) VALUES ('B026','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B026','B122',NULL) INTO SelectB(sid, cno, score) VALUES ('B026','B125',NULL)
INTO SelectB(sid, cno, score) VALUES ('B027','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B027','B122',NULL) INTO SelectB(sid, cno, score) VALUES ('B027','B129',NULL)
INTO SelectB(sid, cno, score) VALUES ('B028','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B028','B121',NULL) INTO SelectB(sid, cno, score) VALUES ('B028','B141',NULL)
INTO SelectB(sid, cno, score) VALUES ('B029','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B029','B125',NULL) INTO SelectB(sid, cno, score) VALUES ('B029','B129',NULL)
INTO SelectB(sid, cno, score) VALUES ('B030','B121',NULL) INTO SelectB(sid, cno, score) VALUES ('B030','B122',NULL) INTO SelectB(sid, cno, score) VALUES ('B030','B143',NULL)
INTO SelectB(sid, cno, score) VALUES ('B031','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B031','B125',NULL) INTO SelectB(sid, cno, score) VALUES ('B031','B131',NULL)
INTO SelectB(sid, cno, score) VALUES ('B032','B121',NULL) INTO SelectB(sid, cno, score) VALUES ('B032','B122',NULL) INTO SelectB(sid, cno, score) VALUES ('B032','B141',NULL)
INTO SelectB(sid, cno, score) VALUES ('B033','B117',NULL) INTO SelectB(sid, cno, score) VALUES ('B033','B129',NULL) INTO SelectB(sid, cno, score) VALUES ('B033','B131',NULL)
SELECT * FROM dual;
COMMIT;

-- 第4组: B034-B044 (会计学) 选会计核心课
INSERT ALL
INTO SelectB(sid, cno, score) VALUES ('B034','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B034','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B034','B115',NULL)
INTO SelectB(sid, cno, score) VALUES ('B035','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B035','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B035','B114',NULL)
INTO SelectB(sid, cno, score) VALUES ('B036','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B036','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B036','B116',NULL)
INTO SelectB(sid, cno, score) VALUES ('B037','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B037','B113',NULL) INTO SelectB(sid, cno, score) VALUES ('B037','B115',NULL)
INTO SelectB(sid, cno, score) VALUES ('B038','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B038','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B038','B113',NULL)
INTO SelectB(sid, cno, score) VALUES ('B039','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B039','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B039','B114',NULL)
INTO SelectB(sid, cno, score) VALUES ('B040','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B040','B113',NULL) INTO SelectB(sid, cno, score) VALUES ('B040','B115',NULL)
INTO SelectB(sid, cno, score) VALUES ('B041','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B041','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B041','B116',NULL)
INTO SelectB(sid, cno, score) VALUES ('B042','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B042','B113',NULL) INTO SelectB(sid, cno, score) VALUES ('B042','B114',NULL)
INTO SelectB(sid, cno, score) VALUES ('B043','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B043','B112',NULL) INTO SelectB(sid, cno, score) VALUES ('B043','B113',NULL)
INTO SelectB(sid, cno, score) VALUES ('B044','B111',NULL) INTO SelectB(sid, cno, score) VALUES ('B044','B115',NULL) INTO SelectB(sid, cno, score) VALUES ('B044','B116',NULL)
SELECT * FROM dual;
COMMIT;

-- 第5组: B045-B055 (国际贸易) 选国贸核心课
INSERT ALL
INTO SelectB(sid, cno, score) VALUES ('B045','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B045','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B045','B139',NULL)
INTO SelectB(sid, cno, score) VALUES ('B046','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B046','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B046','B137',NULL)
INTO SelectB(sid, cno, score) VALUES ('B047','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B047','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B047','B135',NULL)
INTO SelectB(sid, cno, score) VALUES ('B048','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B048','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B048','B120',NULL)
INTO SelectB(sid, cno, score) VALUES ('B049','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B049','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B049','B139',NULL)
INTO SelectB(sid, cno, score) VALUES ('B050','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B050','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B050','B135',NULL)
INTO SelectB(sid, cno, score) VALUES ('B051','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B051','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B051','B137',NULL)
INTO SelectB(sid, cno, score) VALUES ('B052','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B052','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B052','B139',NULL)
INTO SelectB(sid, cno, score) VALUES ('B053','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B053','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B053','B145',NULL)
INTO SelectB(sid, cno, score) VALUES ('B054','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B054','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B054','B140',NULL)
INTO SelectB(sid, cno, score) VALUES ('B055','B104',NULL) INTO SelectB(sid, cno, score) VALUES ('B055','B138',NULL) INTO SelectB(sid, cno, score) VALUES ('B055','B145',NULL)
SELECT * FROM dual;
COMMIT;

PROMPT College B differentiated data inserted successfully.
