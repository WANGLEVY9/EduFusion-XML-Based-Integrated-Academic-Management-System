# EduFusion — 基于 XML 的异构教务数据集成系统

> **EduFusion XML-Based Integrated Academic Management System**
>
> 以 XML 为核心数据交换标准，实现 SQL Server / Oracle / MySQL 三类异构数据库的跨学院教务数据互通。
> 支持课程共享、跨院选课、统一退选与全局统计可视化。

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [技术栈](#3-技术栈)
4. [模块说明](#4-模块说明)
5. [两种运行模式](#5-两种运行模式)
6. [环境要求](#6-环境要求)
7. [快速启动 — 本地模式（单一 MySQL）](#7-快速启动--本地模式单一-mysql)
8. [异构数据库部署 — Docker 模式](#8-异构数据库部署--docker-模式)
9. [客户端操作手册](#9-客户端操作手册)
10. [XML API 参考](#10-xml-api-参考)
11. [数据库设计](#11-数据库设计)
12. [异构映射说明](#12-异构映射说明)
13. [账号与数据一览](#13-账号与数据一览)
14. [测试](#14-测试)
15. [常见问题](#15-常见问题)
16. [本阶段修改记录](#16-本阶段修改记录)
17. [附录：文件结构](#17-附录文件结构)

---

## 1. 项目概述

EduFusion 模拟了三所独立学院（A、B、C）的教学管理系统，各学院使用不同的数据库系统：
- **学院 A** — SQL Server
- **学院 B** — Oracle
- **学院 C** — MySQL

系统通过 **XML over HTTP** 实现异构数据库之间的数据互通，核心目标与能力：

| 需求 | 实现 |
|------|------|
| **课程共享** | 各学院将课程标记为"共享"，其他学院可查看和选修 |
| **跨院选课** | 学生选修其他学院的共享课程，选课记录自动写入课程所属学院数据库 |
| **统一退选** | 支持本院和跨院课程退选，结果同步至对应学院数据库 |
| **全局统计** | 集成服务器聚合三院数据，生成统计报表与图表可视化 |
| **异构屏蔽** | 各学院表结构/字段名/数据类型存在差异，通过 XSLT 转换为统一 XML 格式 |
| **GUI 客户端** | 三院分别提供 Swing 图形界面，支持登录、查询、选课、退课、统计查看 |

---

## 2. 系统架构

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                             客户端层 (Swing GUI)                              │
│   ┌──────────┐   ┌──────────┐   ┌──────────┐                                │
│   │Client A  │   │Client B  │   │Client C  │    ← 每院独立登录               │
│   └────┬─────┘   └────┬─────┘   └────┬─────┘                                │
│        │              │              │                                       │
│        └──────────────┼──────────────┘                                       │
│                       │ HTTP POST (XML 报文)                                 │
├───────────────────────┼──────────────────────────────────────────────────────┤
│                   集成服务层 (Integration Server)                              │
│   ┌───────────────────┴─────────────────────────────────────────────┐       │
│   │              IntegrationXmlHttpServer (:8080)                    │       │
│   │           /api/xml — XML 报文处理端点                             │       │
│   │           /api/health — 健康检查端点                              │       │
│   │   ┌────────────────────────────────────────────────────────┐   │       │
│   │   │                  IntegrationServer                       │   │       │
│   │   │    · 请求路由 (6 种 type)                                │   │       │
│   │   │    · XSD 校验                                           │   │       │
│   │   │    · 跨院编排 (按课程号首字母路由到对应学院)                │   │       │
│   │   │    · 数据聚合 (统计汇总三院数据)                          │   │       │
│   │   │    · 审计日志                                           │   │       │
│   │   └────────────────────────────────────────────────────────┘   │       │
│   └────────────────────────────────────────────────────────────────┘       │
│                       │ (HTTP POST XML / 或 JDBC 直连)                     │
├───────────────────────┼──────────────────────────────────────────────────────┤
│                   学院服务层 (College Servers)                                │
│   ┌──────────────────┼─────────────────────────────────────────────────┐   │
│   │  ┌───────────────┴────────┐  ┌───────────────┴──────────────┐     │   │
│   │  │ CollegeA (:8081)       │  │ CollegeB (:8082)             │     │   │
│   │  │ XML → JDBC → SQL Svr  │  │ XML → JDBC → Oracle          │     │   │
│   │  │ 表: StudentA, CourseA  │  │ 表: StudentB, CourseB        │     │   │
│   │  │     SelectA, AdminA    │  │     SelectB, AdminB          │     │   │
│   │  └────────────────────────┘  └──────────────────────────────┘     │   │
│   │  ┌────────────────────────────────────────────────────────────┐   │   │
│   │  │ CollegeC (:8083)                                          │   │   │
│   │  │ XML → JDBC → MySQL                                        │   │   │
│   │  │ 表: StudentC, CourseC, SelectC, AdminC                    │   │   │
│   │  └────────────────────────────────────────────────────────────┘   │   │
│   └────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────┘
                                        ↓ JDBC
┌──────────────────────────────────────────────────────────────────────────────┐
│                          数据库层 (异构)                                       │
│   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │
│   │ SQL Server 2022  │  │ Oracle XE 21c    │  │ MySQL 8.0        │          │
│   │ :1433            │  │ :1521            │  │ :3306 / :3307    │          │
│   │ edufusion_a      │  │ EDUFUSION_B      │  │ edufusion_iams   │          │
│   └──────────────────┘  └──────────────────┘  └──────────────────┘          │
└──────────────────────────────────────────────────────────────────────────────┘
```

### 跨院选课业务流

```
学生 A001 (学院 A) 选修学院 B 的共享课程 B104：

Client A                    Integration Server              College B Server
   │                              │                              │
   │  1. POST /api/xml            │                              │
   │     <type>shareCourse</type> │                              │
   │     <source>A</source>       │                              │
   │─────────────────────────────>│                              │
   │                              │                              │
   │  2. 集成服务器遍历三院网关       │                              │
   │     收集除 A 以外的共享课程      │                              │
   │                              │                              │
   │  <— 返回 B/C 学院的共享课程 —— │                              │
   │                              │                              │
   │  3. POST /api/xml            │                              │
   │     <type>crossSelect</type> │                              │
   │     <studentId>A001</sid>    │                              │
   │     <courseId>B104</cid>     │                              │
   │─────────────────────────────>│                              │
   │                              │  4. 根据 B104 → 学院 B       │
   │                              │     POST listStudentCourses  │
   │                              │─────────────────────────────>│
   │                              │     INSERT INTO SelectB     │
   │                              │     (sid=B, cno=B104)        │
   │                              │<—— 选课成功 ———————————————│
   │  <— 跨院选课成功 ——————————— │                              │
```

---

## 3. 技术栈

| 层级 | 技术 |
|------|------|
| 编程语言 | Java 8 (源码级别)，兼容 JDK 11/17 |
| 构建工具 | Maven 3.8+ (多模块) |
| 数据交换格式 | **XML** over HTTP (POST) |
| XML 解析/生成 | DOM4J 2.1.4 + Xerces 2.12.2 |
| XML 校验 | **XML Schema (XSD)** |
| 数据格式转换 | **XSLT** (6 个转换模板) |
| 学院 A 数据库 | **SQL Server 2022** (Docker) / MySQL (本地模式) |
| 学院 B 数据库 | **Oracle XE 21c** (Docker) / MySQL (本地模式) |
| 学院 C 数据库 | **MySQL 8.0** |
| JDBC 驱动 | mssql-jdbc 12.6.1.jre8 / ojdbc8 21.9.0.0 / mysql-connector-j 8.4.0 |
| GUI 框架 | Java Swing (JFreeChart 1.5.5 图表) |
| HTTP 服务器 | com.sun.net.httpserver (JDK 内置) |
| 单元测试 | JUnit 5.10.2 |
| CI | GitHub Actions |
| 容器化 | Docker Compose (3 数据库) |

---

## 4. 模块说明

| 模块 | 类型 | 说明 |
|------|------|------|
| **common** | JAR | 公共基础设施：数据模型、网关接口(`CollegeGateway`)、JDBC 通用组件、XML 工具、HTTP 服务器(`CollegeXmlHttpServer`)、Swing 通用面板(`CollegeDashboardFrame`)、统计图表(`Charts`) |
| **server-a** | JAR | A 学院服务：`CollegeAGateway`(SQL Server 字段映射)、`AuthServiceA`(认证)、启动入口 `CollegeAServerBootstrap`(:8081) |
| **server-b** | JAR | B 学院服务：`CollegeBGateway`(Oracle 字段映射)、`AuthServiceB`(认证)、启动入口 `CollegeBServerBootstrap`(:8082) |
| **server-c** | JAR | C 学院服务：`CollegeCGateway`(MySQL 字段映射)、`AuthServiceC`(认证)、启动入口 `CollegeCServerBootstrap`(:8083) |
| **integration-server** | JAR | 集成服务：请求路由与编排、XSD 校验、统计聚合、XML 端点 `/api/xml`、启动入口 `IntegrationServerBootstrap`(:8080) |
| **client-a** | JAR | A 学院 Swing 客户端：`LoginFrameA` → 认证后进入 `CollegeDashboardFrame` |
| **client-b** | JAR | B 学院 Swing 客户端：`LoginFrameB` → 认证后进入 `CollegeDashboardFrame` |
| **client-c** | JAR | C 学院 Swing 客户端：`LoginFrameC` → 认证后进入 `CollegeDashboardFrame` |

### 核心接口：`CollegeGateway`

所有学院网关实现此接口，提供 15 个统一操作：

```
authenticateStudent(username, password)  → boolean
authenticateAdmin(username, password)     → boolean
listAllCourses()                          → List<Course>
listSharedCourses()                       → List<Course>
listStudentCourses(studentId)             → List<Course>
selectCourse(studentId, courseId)         → boolean
dropCourse(studentId, courseId)           → boolean
countStudents() / countCourses()          → int
countSelections() / countSharedCourses()  → int
topCourses(topN)                          → List<CourseHeat>
getCollegeCode()                          → String
```

网关有 3 种实现：
- **`JdbcCollegeRepository`** — JDBC 直连数据库（参数化表名/列名）
- **`RemoteCollegeGateway`** — 通过 HTTP/XML 转发到学院服务
- **`InMemoryCollegeStore`** — 内存模拟（用于测试）

### 启动入口类一览

| 类名 | 模块 | 端口 | 命令 |
|------|------|------|------|
| `IntegrationServerBootstrap` | integration-server | 8080 | `mvn exec:java -pl integration-server` |
| `CollegeAServerBootstrap` | server-a | 8081 | `mvn exec:java -pl server-a` |
| `CollegeBServerBootstrap` | server-b | 8082 | `mvn exec:java -pl server-b` |
| `CollegeCServerBootstrap` | server-c | 8083 | `mvn exec:java -pl server-c` |
| `ClientAApp` | client-a | — | `mvn exec:java -pl client-a` |
| `ClientBApp` | client-b | — | `mvn exec:java -pl client-b` |
| `ClientCApp` | client-c | — | `mvn exec:java -pl client-c` |

---

## 5. 两种运行模式

| 模式 | 学院服务层 | 集成服务器 | 数据库 | 启动方式 | 适用场景 |
|------|-----------|-----------|--------|---------|---------|
| **本地模式**（默认） | 跳过 | JDBC 直连 | 单一 MySQL (:3306) | `mvn exec:java -pl integration-server` | 快速开发、单机调试 |
| **远程模式** | 独立 HTTP 服务 | HTTP/XML 转发 | 异构 (SQL Server + Oracle + MySQL) | 先启动三院服务，再加 `-Dcollege.remote=true` | 作业演示、生产部署 |

> **关键区别**：在本地模式下，`IntegrationServer` 使用 `JdbcCollegeRepository` 直接操作数据库；在远程模式下，使用 `RemoteCollegeGateway` 将请求以 XML 报文形式转发给各学院 HTTP 服务。两种模式对客户端完全透明。

---

## 6. 环境要求

### 基础软件

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 8 | 建议 JDK 11+（JDK 8 也能运行） |
| Maven | 3.8+ | 项目构建 |
| MySQL | 8.0 | 本地模式必需 |
| Docker Desktop | 最新版 | 异构模式可选，需支持 docker compose |

### 端口占用

| 端口 | 服务 | 模式 |
|------|------|------|
| 8080 | 集成服务器 | 全部 |
| 8081 | A 学院 HTTP 服务 | 远程模式 |
| 8082 | B 学院 HTTP 服务 | 远程模式 |
| 8083 | C 学院 HTTP 服务 | 远程模式 |
| 3306 | MySQL（本地开发） | 本地模式 |
| 3307 | MySQL（Docker，与本地错开） | Docker |
| 1433 | SQL Server（Docker） | Docker |
| 1521 | Oracle XE（Docker） | Docker |
| 5500 | Oracle Enterprise Manager（Docker） | Docker |

---

## 7. 快速启动 — 本地模式（单一 MySQL）

此模式使用单一 MySQL 数据库存放三院数据，无需 Docker，适合快速上手。

### 7.1 初始化数据库

```powershell
# 确保 MySQL 服务已启动（:3306），然后执行：
mysql -uroot -p123456 < sql\all-colleges-mysql.sql
```

该脚本将：
- 创建数据库 `edufusion_iams`
- 创建三院所有表（StudentA/B/C、CourseA/B/C、SelectA/B/C、AdminA/B/C）
- 插入 **60 名学生/学院**、**12 门课程/学院**、**每生 5 门选课**（共 900 条选课记录）
- 插入管理员账号

### 7.2 编译项目

```powershell
mvn clean compile -DskipTests
```

首次编译会下载 Maven 依赖，请保持网络通畅。

### 7.3 启动集成服务器

```powershell
# 本地模式（默认，college.remote=false）
mvn exec:java -pl integration-server
```

启动成功后看到日志：
```
Integration XML HTTP server started at http://localhost:8080/api/xml
```

### 7.4 启动客户端

每个客户端在独立终端窗口中运行（可同时启动三个）：

```powershell
# 终端 1：A 学院客户端
mvn exec:java -pl client-a

# 终端 2：B 学院客户端
mvn exec:java -pl client-b

# 终端 3：C 学院客户端
mvn exec:java -pl client-c
```

### 7.5 登录测试

参见 [第 13 章 — 账号与数据一览](#13-账号与数据一览)。

---

## 8. 异构数据库部署 — Docker 模式

此模式使用 Docker 运行 SQL Server、Oracle XE、MySQL 三个异构数据库容器，
每院连接各自独立的数据库系统，展示真正的异构数据集成能力。

### 8.1 启动数据库容器

```powershell
# 从项目根目录执行（不要 cd docker）
docker compose -f docker/docker-compose.yml up -d
```

启动三个容器：

| 容器名 | 数据库 | 端口 | 用户/密码 |
|--------|--------|------|----------|
| `edufusion-sqlserver` | SQL Server 2022 | 1433 | `sa` / `EduFusion123!` |
| `edufusion-oracle` | Oracle XE 21c | 1521 | `EDUFUSION_B` / `EduFusion123` |
| `edufusion-mysql` | MySQL 8.0 | 3307 | `root` / `123456` |

> **Oracle 注意**：首次启动约需 2–5 分钟初始化。监控日志：
> ```powershell
> docker logs -f edufusion-oracle
> ```
> 看到 `DATABASE IS READY TO USE!` 即就绪。

### 8.2 数据库初始化

各容器支持自动建表：

| 容器 | 初始化方式 | 脚本 |
|------|-----------|------|
| SQL Server | 通过 `sqlserver-init` 配置器容器自动执行 | `docker/sqlserver/init.sql` |
| Oracle | 通过 `/docker-entrypoint-initdb.d/` 自动执行 | `docker/oracle/init.sql` |
| MySQL | 通过 `/docker-entrypoint-initdb.d/` 自动执行 | `docker/mysql/init.sql` |

初始化脚本默认仅创建**表结构和管理员账号**。启动完成后，使用一键导入脚本填充差异化数据：

```powershell
# 脚本均在项目根目录执行

# 导入 College A — SQL Server（60 学生 + 52 课程 + 360 选课）
.\seed-college-a.cmd

# 导入 College B — Oracle（55 学生 + 52 课程 + 165 选课）
.\seed-college-b.cmd

# 导入 College C — MySQL（65 学生 + 52 课程 + 390 选课）
.\seed-college-c.cmd
```

每个脚本会自动检测 Docker 容器是否运行，并通过管道将 SQL 文件传入容器执行。

> **脚本文件对应关系**：
> - `seed-college-a.cmd` → `docker/sqlserver/insert-data-a-differentiated.sql`
> - `seed-college-b.cmd` → `docker/oracle/insert-data-b-differentiated.sql`
> - `seed-college-c.cmd` → `docker/mysql/insert-data-c-differentiated.sql`

三个学院的数据经过差异化设计，具有不同的学科方向、学生规模、选课密度，使得统计图表呈现出真实的差异化分布。

> **旧版数据脚本**（`insert-data-a.sql` / `insert-data-b.sql` / `insert-data-c.sql`）仍保留在对应目录中，每院 30 学生 + 12 课程 + 150 选课。建议使用新版 `*-differentiated.sql` 脚本获取更丰富的演示数据。

### 8.3 启动学院服务（远程模式）

按顺序分别启动三个学院 HTTP 服务（每个一个独立终端）：

```powershell
# 终端 1：A 学院服务 → 连接 Docker SQL Server
mvn exec:java -pl server-a

# 终端 2：B 学院服务 → 连接 Docker Oracle
mvn exec:java -pl server-b

# 终端 3：C 学院服务 → 连接 Docker MySQL
mvn exec:java -pl server-c
```

### 8.4 启动集成服务器（远程模式）

```powershell
# 终端 4：远程模式，通过 HTTP 转发到学院服务
mvn exec:java -pl integration-server "-Dcollege.remote=true"
```

### 8.5 JDBC 配置说明

各学院服务加载的 properties 文件（位于 `src/main/resources/db/`）：

| 服务 | 配置文件 | 连接目标 |
|------|---------|---------|
| A (SQL Server) | `college-a.properties` | `jdbc:sqlserver://localhost:1433;databaseName=edufusion_a` |
| B (Oracle) | `college-b.properties` | `jdbc:oracle:thin:@localhost:1521/EDUFUSION_B` |
| C (MySQL) | `college-c.properties` | `jdbc:mysql://localhost:3307/edufusion_iams_c?useSSL=false&characterEncoding=utf8` |

> 切换本地模式时，需将 properties 文件指向本地 MySQL（参见 `sql/all-colleges-mysql.sql`）。

### 8.6 验证服务健康

```powershell
# 检查各服务健康状态
curl http://localhost:8080/api/health   # 集成服务器
curl http://localhost:8081/api/health   # 学院 A
curl http://localhost:8082/api/health   # 学院 B
curl http://localhost:8083/api/health   # 学院 C
# 均返回 OK 即正常
```

### 8.7 停止环境

```powershell
# 停止客户端：关闭 Swing 窗口

# 停止服务：Ctrl+C 终止各终端进程

# 停止并删除数据库容器
docker compose -f docker/docker-compose.yml down

# 停止并删除容器 + 数据卷（⚠ 谨慎：会丢失数据）
docker compose -f docker/docker-compose.yml down -v
```

---

## 9. 客户端操作手册

### 9.1 登录界面

启动客户端后显示登录窗口：

| 字段 | 说明 | 示例值 |
|------|------|--------|
| 用户名 | 学生编号或管理员账号 | `A001` / `adminA` |
| 密码 | 登录密码 | `123456`（学生）/ `admin123`（管理员） |
| 身份 | 下拉选择 | 学生 / 管理员 |
| 学院 | 固定显示当前客户端（只读） | A / B / C |

### 9.2 主面板布局

```
┌──────────────────────────────────────────────────────────────────┐
│  学院: A  |  学生编号: A001  |  课程编号: [________]  |  服务地址  │  ← 顶部信息栏
├──────────────────────────────────────────────────────────────────┤
│ [课程操作] [数据统计]                                              │  ← Tab 标签页
├──────────────────────────────────────────────────────────────────┤
│ [课程查询] [共享课程选课] [我的选课] [退课] [统计报表]             │  ← 功能按钮
├──────────────────────────────────────────────────────────────────┤
│  筛选: [______________]  每页: [10▼] [上一页] [下一页] 第1/1页   │  ← 表格控制栏
│                         [导出当前筛选]                            │
├──────────────────────────────────────────────────────────────────┤
│  课程号  |  课程名  |  学分  |  教师  |  地点  |  学院  |  共享  │  ← 数据表格
│  ──────────────────────────────────────────────────────────────── │
│  A101   |  高等数学  |  4   |  张教授 | A-101  |  A   |  是     │
│  A102   |  线性代数  |  3   |  李教授 | A-102  |  A   |  否     │
│  ...                                                             │
├──────────────────────────────────────────────────────────────────┤
│  文本输出区（操作结果与详情）                                      │
└──────────────────────────────────────────────────────────────────┘
```

### 9.3 功能按钮说明

| 按钮 | 功能 | 操作流程 |
|------|------|---------|
| **课程查询** | 查看本院全部课程 | 直接点击 → 表格显示本院课程列表 |
| **共享课程选课** | 跨院选修共享课程（两步） | ① 课程编号为空时点击 → 加载其他学院共享课<br>② 点击表格行自动填充课程编号<br>③ 再次点击按钮 → 确认对话框 → 确认选课 |
| **我的选课** | 查看当前学生已选课程 | 直接点击 → 表格显示选课记录，文本区显示统计 |
| **退课** | 退选已选课程 | ① 点击"我的选课"查看已选课程<br>② 点击目标课程行 → 自动填充课程编号<br>③ 点击"退课" → 确认对话框 → 确认退课 |
| **统计报表** | 查看全局统计数据 | 直接点击 → 切换到"数据统计"Tab → 显示统计卡片 + 图表 + 文字详情 |

### 9.4 表格交互功能

所有课程列表表格支持：

| 功能 | 使用方式 |
|------|---------|
| **分页** | 下拉选择每页 5/10/20/50 条；上一页/下一页按钮；显示页码 |
| **关键字筛选** | 筛选框输入即过滤，不区分大小写，匹配全部列 |
| **导出 CSV** | 点击"导出当前筛选"→ 保存为 CSV 文件（可用 Excel 打开） |
| **点击填充** | 点击表格行 → 课程编号自动填充到顶部输入框，便于选课/退课 |

### 9.5 典型业务流

**跨院选课完整流程：**
```
1. 学生登录 A 学院客户端
2. 点击「课程查询」→ 查看本院课程
3. 清空「课程编号」输入框
4. 点击「共享课程选课」→ 加载 B、C 学院的共享课程列表
5. 在表格中浏览并点击目标课程（如 B104 Web开发技术）→ 编号自动填充
6. 再次点击「共享课程选课」→ 弹出确认对话框 → 点击"是"
7. 系统反馈选课结果，自动刷新「我的选课」
```

**退课流程：**
```
1. 点击「我的选课」→ 查看已选课程
2. 在表格中点击要退选的课程行 → 编号自动填充
3. 点击「退课」→ 确认对话框 → 点击"是"
4. 系统反馈退课结果，自动刷新「我的选课」
```

### 9.6 统计报表

统计报表 Tab 提供完整的可视化分析体验，采用 "卡片画廊 → 点击详情" 的导航模式：

**概览卡片**（4 张展示在顶部）：
- 总学生数 / 总课程数 / 总选课数 / 共享课程数

**图表画廊**（点击任意图表进入详情视图）：

| 图表 | 类型 | 说明 |
|------|------|------|
| 三学院指标对比 | 分组柱状图 | 学生数/课程数/选课数三院横向对比 |
| 选课比例分布 | 饼图 | 各学院选课数占比 |
| 课程热度 TOP10 | 水平柱状图 | 按选课人数排序的热门课程 |
| 学生选课分布 | 环形图 | 各院学生人均选课密度对比 |
| 三学院指标堆积 | 堆叠柱状图 | 三院各指标累积对比 |
| 指标变化趋势 | 折线图 | 课程热度排名变化趋势 |
| 三学院指标面积 | 面积图 | 三院指标面积对比 |
| 学分分布散点 | 散点图 | 课程学分 × 选课人数分布 |
| 共享课程对比 | 柱状图 | 三院共享课程数量对比 |
| 人均选课密度 | 柱状图 | 每院选课总数 ÷ 学生数 |
| 课程分类分布 | 饼图 | 各学院全部课程比例分布 |

**管理员专属图表**（以管理员身份登录时额外显示）：

| 图表 | 类型 | 说明 |
|------|------|------|
| 教师授课量统计 | 柱状图 | 各教师承担课程数量 |
| 学分分布统计 | 柱状图 | 不同学分区间的课程数量 |
| 共享课程占比 | 环形图 | 共享课程 vs 非共享课程比例 |

**详情视图交互功能**：
- 学院筛选：勾选/取消勾选学院复选框，动态过滤图表数据
- 教师筛选：从下拉菜单选择特定教师，聚焦其授课数据
- 「应用」按钮：应用当前筛选条件重建图表
- 「重置」按钮：恢复默认显示全部数据
- 「返回画廊」按钮：回到图表选择画廊

> 管理员模式在登录时选择身份为"管理员"即可激活，可查看更宏观的全局数据统计。

---

## 10. XML API 参考

所有 API 通过 `POST http://localhost:8080/api/xml` 访问，Content-Type 为 `application/xml`，请求体须符合 `xsd/request.xsd` 规范。

### 10.1 请求格式

```xml
<request>
    <type>queryCourses</type>        <!-- 必填：请求类型 -->
    <college>A</college>             <!-- 学院代码（查询时使用） -->
    <source>A</source>               <!-- 来源学院（共享课程时使用） -->
    <studentId>A001</studentId>      <!-- 学生编号 -->
    <courseId>B101</courseId>        <!-- 课程编号 -->
</request>
```

### 10.2 请求类型

| type 值 | 必填参数 | 可选参数 | 说明 |
|---------|---------|---------|------|
| `queryCourses` | `<college>` | — | 查询指定学院的全部课程 |
| `shareCourse` | `<source>` | — | 获取其他学院向 source 学院开放的共享课程 |
| `myCourses` | `<college>`, `<studentId>` | — | 查询学生在指定学院的选课记录 |
| `crossSelect` | `<studentId>`, `<courseId>` | — | 跨院选课（按课程号首字母路由目标学院） |
| `dropCourse` | `<studentId>`, `<courseId>` | — | 退选课程（按课程号首字母路由目标学院） |
| `statistics` | — | — | 获取全局统计数据 |

### 10.3 课程列表响应

```xml
<response>
    <success>true</success>
    <message>Courses fetched</message>
    <courses>
        <course>
            <id>A101</id>
            <name>高等数学</name>
            <credit>4</credit>
            <teacher>张教授</teacher>
            <location>A-101</location>
            <college>A</college>
            <shared>true</shared>
        </course>
    </courses>
</response>
```

### 10.4 统计响应

```xml
<response>
    <success>true</success>
    <message>Statistics generated</message>
    <statistics>
        <totalStudents>90</totalStudents>
        <totalCourses>36</totalCourses>
        <totalSelections>450</totalSelections>
        <totalSharedCourses>24</totalSharedCourses>
        <topCourses>
            <course>
                <id>A104</id>
                <name>计算机网络</name>
                <college>A</college>
                <selectedCount>26</selectedCount>
            </course>
        </topCourses>
        <colleges>
            <college>
                <code>A</code>
                <students>30</students>
                <courses>12</courses>
                <selections>150</selections>
                <sharedCourses>8</sharedCourses>
            </college>
        </colleges>
    </statistics>
</response>
```

### 10.5 curl 测试示例

```powershell
# 查询 A 学院课程
curl -X POST http://localhost:8080/api/xml `
    -H "Content-Type: application/xml" `
    -d "<request><type>queryCourses</type><college>A</college></request>"

# 全局统计
curl -X POST http://localhost:8080/api/xml `
    -H "Content-Type: application/xml" `
    -d "<request><type>statistics</type></request>"

# 跨院选课
curl -X POST http://localhost:8080/api/xml `
    -H "Content-Type: application/xml" `
    -d "<request><type>crossSelect</type><studentId>A001</studentId><courseId>B104</courseId></request>"
```

### 10.6 学院直接 API

每个学院 HTTP 服务也提供 `/api/xml` 端点，支持更细粒度的操作：

| type | 说明 |
|------|------|
| `listAllCourses` | 列出本学院所有课程 |
| `listSharedCourses` | 列出本学院共享课程 |
| `listStudentCourses` | 列出某学生的选课 |
| `selectCourse` | 选课 |
| `dropCourse` | 退课 |
| `authenticateStudent` | 学生认证 |
| `authenticateAdmin` | 管理员认证 |
| `countStudents` / `countCourses` | 计数 |
| `countSelections` / `countSharedCourses` | 计数 |
| `topCourses` | 热门课程排行 |

---

## 11. 数据库设计

### 11.1 Docker 模式表结构（每院独立数据库）

#### 学院 A — SQL Server (`edufusion_a`)

```sql
CREATE TABLE StudentA (
    sid     VARCHAR(12) PRIMARY KEY,
    sname   NVARCHAR(20) NOT NULL,    -- 支持中文
    sex     NVARCHAR(2),
    dept    NVARCHAR(30),
    password VARCHAR(64) NOT NULL
);

CREATE TABLE CourseA (
    cid      VARCHAR(10) PRIMARY KEY,
    cname    NVARCHAR(30) NOT NULL,
    credit   INT NOT NULL,
    teacher  NVARCHAR(20),
    room     NVARCHAR(20),
    shareFlag CHAR(1) NOT NULL         -- '1'=共享, '0'=本校
);

CREATE TABLE SelectA (
    sid   VARCHAR(12) NOT NULL REFERENCES StudentA(sid),
    cid   VARCHAR(10) NOT NULL REFERENCES CourseA(cid),
    score INT NULL,
    PRIMARY KEY (sid, cid)
);

CREATE TABLE AdminA (
    username NVARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);
```

#### 学院 B — Oracle (`EDUFUSION_B`)

```sql
CREATE TABLE StudentB (
    sid       VARCHAR2(12) PRIMARY KEY,
    sname     VARCHAR2(20) NOT NULL,
    gender    VARCHAR2(2),
    major_name VARCHAR2(30),
    passwd    VARCHAR2(64) NOT NULL     -- 列名区别于 A/C
);

CREATE TABLE CourseB (
    cno        VARCHAR2(10) PRIMARY KEY,  -- 列名区别于 A/C
    ctitle     VARCHAR2(40) NOT NULL,     -- 列名区别于 A/C
    credit_num NUMBER(3),                 -- 列名区别于 A/C
    instructor VARCHAR2(30),              -- 列名区别于 A/C
    classroom  VARCHAR2(20),              -- 列名区别于 A/C
    share_flag CHAR(1) NOT NULL           -- 列名区别于 A/C
);

CREATE TABLE SelectB (
    sid   VARCHAR2(12) NOT NULL,
    cno   VARCHAR2(10) NOT NULL,          -- 列名区别于 A/C
    score NUMBER(3),
    PRIMARY KEY (sid, cno)
);

CREATE TABLE AdminB (
    username VARCHAR2(32) PRIMARY KEY,
    password VARCHAR2(64) NOT NULL
);
```

#### 学院 C — MySQL (`edufusion_iams_c`)

```sql
CREATE TABLE StudentC (
    sid       VARCHAR(12) PRIMARY KEY,
    sname     VARCHAR(20) NOT NULL,
    sex       VARCHAR(2),
    major_name VARCHAR(30),
    password  VARCHAR(64) NOT NULL
);

CREATE TABLE CourseC (
    cid       VARCHAR(10) PRIMARY KEY,
    cname     VARCHAR(30) NOT NULL,
    credit    INT NOT NULL,
    teacher   VARCHAR(20),
    room      VARCHAR(20),
    is_shared CHAR(1) NOT NULL            -- 列名区别于 A/B
);

CREATE TABLE SelectC (
    sid   VARCHAR(12) NOT NULL,
    cid   VARCHAR(10) NOT NULL,
    score INT NULL,
    PRIMARY KEY (sid, cid)
);

CREATE TABLE AdminC (
    username VARCHAR(32) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
) CHARACTER SET utf8mb4;
```

### 11.2 本地模式（单一 MySQL）

参见 `sql/all-colleges-mysql.sql`，在同一数据库 `edufusion_iams` 中创建全部 12 张表（StudentA/B/C、CourseA/B/C、SelectA/B/C、AdminA/B/C），每院 60 名学生、12 门课程，合计 180 名学生、36 门课程、900 条选课记录。

---

## 12. 异构映射说明

### 12.1 表名与列名差异

| 概念 | 学院 A (SQL Server) | 学院 B (Oracle) | 学院 C (MySQL) |
|------|-------------------|----------------|---------------|
| 学生表 | `StudentA` | `StudentB` | `StudentC` |
| 学生编号列 | `sid` | `sid` | `sid` |
| 学生姓名列 | `sname` | `sname` | `sname` |
| 密码列 | `password` | **`passwd`** | `password` |
| 性别列 | `sex` | **`gender`** | `sex` |
| 专业列 | `dept` | **`major_name`** | **`major_name`** |
| 课程表 | `CourseA` | `CourseB` | `CourseC` |
| 课程编号列 | `cid` | **`cno`** | `cid` |
| 课程名列 | `cname` | **`ctitle`** | `cname` |
| 学分数列 | `credit` | **`credit_num`** | `credit` |
| 教师列 | `teacher` | **`instructor`** | `teacher` |
| 教室列 | `room` | **`classroom`** | `room` |
| 共享标记列 | **`shareFlag`** | **`share_flag`** | **`is_shared`** |
| 共享值 | `'1'` | `'1'` | `'1'` |
| 选课表 | `SelectA` | `SelectB` | `SelectC` |
| 选课课程列 | `cid` | **`cno`** | `cid` |

### 12.2 XSLT 转换体系

项目在 `xslt/` 目录下提供 10 个 XSLT 转换模板：

**学院原生格式 → 统一格式：**
- `college-a-to-unified.xslt` — SQL Server 字段 → 通用字段
- `college-b-to-unified.xslt` — Oracle 字段 → 通用字段
- `college-c-to-unified.xslt` — MySQL 字段 → 通用字段

**统一格式 → 学院原生格式：**
- `unified-to-college-a.xslt` — 通用字段 → SQL Server 字段
- `unified-to-college-b.xslt` — 通用字段 → Oracle 字段
- `unified-to-college-c.xslt` — 通用字段 → MySQL 字段

**中文标签格式 ↔ 标准格式（备选）：**
- `course-a-to-standard.xsl`、`course-b-to-standard.xsl`、`course-c-to-standard.xsl`
- `standard-to-a.xsl`

### 12.3 XSD 校验

所有发往集成服务器的请求报文需通过 `xsd/request.xsd` 校验：

```xml
<xs:simpleType name="RequestType">
    <xs:restriction base="xs:string">
        <xs:enumeration value="shareCourse"/>
        <xs:enumeration value="crossSelect"/>
        <xs:enumeration value="dropCourse"/>
        <xs:enumeration value="statistics"/>
        <xs:enumeration value="queryCourses"/>
        <xs:enumeration value="myCourses"/>
    </xs:restriction>
</xs:simpleType>
```

### 12.4 跨院路由规则

集成服务器根据课程号**首字母**决定目标学院：
- `A*` → College A (SQL Server)
- `B*` → College B (Oracle)
- `C*` → College C (MySQL)

---

## 13. 账号与数据一览

### 13.1 Docker 模式账号

| 学院 | 学生范围 | 学生密码 | 管理员账号 | 管理员密码 |
|------|---------|---------|-----------|-----------|
| A | A001 ~ A120 | `123456` | `adminA` | `admin123` |
| B | B001 ~ B120 | `123456` | `adminB` | `admin123` |
| C | C001 ~ C120 | `123456` | `adminC` | `admin123` |

### 13.2 本地模式账号

| 学院 | 学生范围 | 学生密码 | 管理员账号 |
|------|---------|---------|-----------|
| A | A001 ~ A120 | `123456` | `adminA` / `admin123` |
| B | B001 ~ B120 | `123456` | `adminB` / `admin123` |
| C | C001 ~ C120 | `123456` | `adminC` / `admin123` |

### 13.3 Docker 模式数据规格（差异化数据）

> 以下为运行 `seed-college-*.cmd` 后导入的数据量。每院数据经过差异化设计，具有不同的学科方向、学生规模、选课密度。

| 统计项 | 学院 A (SQL Server) | 学院 B (Oracle) | 学院 C (MySQL) | 合计 |
|--------|:--------:|:-------:|:-------:|:------:|
| 学生数 | **60** | **55** | **65** | **180** |
| 课程数 | **52** | **52** | **52** | **156** |
| 选课记录 | **360** | **165** | **390** | **915** |
| 共享课程 | **33** | **29** | **30** | **92** |
| 人均选课密度 | **6.0** | **3.0** | **6.0** | **5.08** |
| 学科方向 | 计算机/IT | 经济学/管理 | 电子/自动化 | — |

各院学科方向与选课密度均不同，统计图表将呈现真实的差异化分布。

### 13.4 本地模式数据规格

每学院 60 名学生、12 门课程、每生 5 门选课，合计 180 名学生、36 门课程、900 条选课记录。

### 13.5 作业 4 数据上传规格（hw4 远程服务器）

按助教要求，将本地差异化数据扩展后上传至远程 MySQL 服务器，用于布置作业 4。

| 项目 | 内容 |
|------|------|
| 目标服务器 | `10.60.254.44:3306` |
| 数据库名 | `hw4` |
| 组号 | `2` |
| 上载工具 | `generate_hw4_data.py`（Python 自动生成 + mysql CLI 执行） |

**数据规格：**

| 统计项 | 学院 A (dept_no=A) | 学院 B (dept_no=B) | 学院 C (dept_no=C) | 合计 |
|--------|:--------:|:-------:|:-------:|:------:|
| 学生数 | **120** | **120** | **120** | **360** |
| 课程数 | **104** | **104** | **104** | **312** |
| 选课记录 | **840** | **720** | **840** | **2400** |
| 人均选课密度 | **7.0** | **6.0** | **7.0** | **6.67** |
| 学科方向 | 计算机/IT + 数字媒体 + 信息科学 + 科技人文 | 经管 + 社会学 + 心理学 + 新闻传播 + 教育 | 工程 + 航空航天 + 生物医学 + 材料 + 环境 |

**表结构（统一格式，含组号与院系编号）：**

```sql
CREATE TABLE student (
    student_id   VARCHAR(12) PRIMARY KEY,
    student_name VARCHAR(10) NOT NULL,
    gender       VARCHAR(2),
    department   VARCHAR(16),
    account      VARCHAR(10),
    password     VARCHAR(6),
    group_no     VARCHAR(10) DEFAULT '2',   -- 组号
    dept_no      VARCHAR(10)                -- 院系编号 A/B/C
);

CREATE TABLE course (
    course_id      VARCHAR(8) PRIMARY KEY,
    course_name    VARCHAR(16) NOT NULL,
    credit         VARCHAR(2),
    teacher_name   VARCHAR(20),
    location       VARCHAR(20),
    share_flag     CHAR(1),
    class_hours    VARCHAR(10),               -- 理论学时
    practice_hours VARCHAR(10),               -- 实践学时
    group_no       VARCHAR(10) DEFAULT '2',
    dept_no        VARCHAR(10)
);

CREATE TABLE sc (
    course_id  VARCHAR(8),
    student_id VARCHAR(12),
    score      VARCHAR(3),
    group_no   VARCHAR(10) DEFAULT '2',
    dept_no    VARCHAR(10),
    PRIMARY KEY (course_id, student_id)
);
```

**数据字段映射规则：**

| hw4 字段 | 学院 A 源字段 | 学院 B 源字段 | 学院 C 源字段 |
|----------|-------------|-------------|-------------|
| student_id | sid | sid | sid |
| student_name | sname | sname | sname |
| gender | sex | gender | sex |
| department | dept | major_name | major_name |
| account | sid（同 student_id） | sid | sid |
| password | password | passwd | password |
| course_id | cid | cno | cid |
| course_name | cname | ctitle | cname |
| credit | credit | credit_num | credit |
| teacher_name | teacher | instructor | teacher |
| location | room | classroom | room |
| share_flag | shareFlag | share_flag | is_shared |

所有记录 `group_no = '2'`，各学院 `dept_no` 对应 `'A'`/`'B'`/`'C'`。

**生成与上载脚本：**
- `generate_hw4_data.py` — 数据生成脚本（含所有课程定义、学生姓名库、选课策略）
- `upload_hw4.sql` — 生成的完整 SQL 文件（可用于重新导入）

---

## 14. 测试

### 14.1 运行全部测试

```powershell
mvn clean test
```

### 14.2 运行特定模块测试

```powershell
# 公共模块测试
mvn test -pl common

# 集成服务测试
mvn test -pl integration-server
```

### 14.3 测试覆盖

| 测试类 | 模块 | 覆盖内容 |
|--------|------|---------|
| `Dom4jXmlServiceTest` | common | DOM4J 创建/解析/XSD 校验/XSLT 转换 |
| `XmlUtilTest` | common | W3C DOM 工具兼容性 |
| `CollegeXmlHttpServerTest` | common | 学院 HTTP 服务 + 远程网关全链路 |
| `ChartsTest` | common | 统计图表构建与标题校验 |
| `StatisticsPanelTest` | common | 统计报表解析与 UI 组件填充 |
| `IntegrationServerTest` | integration-server | 集成服务路由/统计/跨院选课 |

### 14.4 测试脚本

```powershell
# Windows
.\scripts\run-tests.ps1

# Linux/macOS
bash ./scripts/run-tests.sh
```

### 14.5 构建清理

```powershell
# 增量编译
mvn compile -pl <模块名> -am

# 完全清理重建
mvn clean install -DskipTests

# 更新本地仓库（修改 properties 或 pom.xml 后必需）
mvn clean install -DskipTests
```

> **重要**：如果修改了 `server-*/` 模块中的 `db/college-*.properties` 配置文件，
> 依赖该模块的客户端（`client-*`）需要执行 `mvn install -DskipTests` 更新本地仓库，
> 否则客户端运行时会使用旧的 jar 包。

---

## 15. 常见问题

### 15.1 登录无反应 / 登录失败

**现象**：点击登录按钮后无任何提示，或弹出"用户名或密码错误"。

**检查清单**：
1. 确认集成服务器（:8080）已启动
2. 确认使用的客户端与账号学院匹配（B 学院客户端登录 B 系账号）
3. 远程模式下确认对应的学院服务已启动（:8081/:8082/:8083）
4. 点击登录无反应 → 检查对应学院的数据库连接配置是否正确
5. 如果修改过 properties 文件 → 执行 `mvn clean install -DskipTests` 更新本地仓库

### 15.2 端口占用

```powershell
netstat -ano | findstr "8080 8081 8082 8083"
taskkill /PID <PID> /F
```

### 15.3 Docker 容器问题

```powershell
# 查看日志
docker logs edufusion-sqlserver
docker logs edufusion-oracle
docker logs edufusion-mysql

# 进入容器
docker exec -it edufusion-sqlserver bash
docker exec -it edufusion-oracle bash
docker exec -it edufusion-mysql bash
```

### 15.4 Oracle 中文乱码

Oracle 容器使用 `AL32UTF8` 字符集，JDBC URL 为 `jdbc:oracle:thin:@localhost:1521/EDUFUSION_B`。
如遇中文显示问题，确认数据插入时使用 PDB (`EDUFUSION_B`) 而非 CDB (`/XE`)。

### 15.5 XSD 校验失败

- 缺少必填元素 `<type>`
- `type` 值不在枚举列表中
- XML 格式错误（未闭合标签、编码问题）

### 15.6 日志文件

日志写入项目根目录的 `logs/` 文件夹：

- `logs/audit.log` — 操作审计（选课、退课、查询等）
- `logs/error.log` — 异常追踪（HTTP 调用失败、连接异常等）

---

## 16. 本阶段修改记录

### 配置变更

| 文件 | 变更内容 |
|------|---------|
| `server-b/src/main/resources/db/college-b.properties` | MySQL → **Oracle** (`oracle.jdbc.OracleDriver` + `jdbc:oracle:thin:@localhost:1521/EDUFUSION_B`) |
| `server-b/src/main/resources/db/college-b-docker.properties` | `/XE` → `/EDUFUSION_B`（PDB 修正） |
| `server-a/src/main/resources/db/college-a.properties` | MySQL → **SQL Server** (`com.microsoft.sqlserver.jdbc.SQLServerDriver` + `jdbc:sqlserver://localhost:1433;databaseName=edufusion_a`) |
| `server-c/src/main/resources/db/college-c.properties` | MySQL :3306 → **Docker MySQL :3307** |

### 数据库脚本变更

| 文件 | 变更内容 |
|------|---------|
| `docker/sqlserver/init.sql` | `VARCHAR` → **`NVARCHAR`**（支持中文存储） |
| `docker/oracle/full-init-b.sql` | **新建** — Oracle PDB 完整初始化（建表+数据填充） |
| `docker/oracle/insert-data-b.sql` | **新建** — Oracle 数据填充脚本（30 学生 + 12 课程 + 150 选课） |
| `docker/sqlserver/insert-data-a.sql` | **新建** — SQL Server 数据填充脚本 |
| `docker/mysql/insert-data-c.sql` | **新建** — MySQL 数据填充脚本 |

### 数据修复

| 问题 | 原因 | 修复 |
|------|------|------|
| SQL Server 中文 `?` | `VARCHAR` 不支持 Unicode | 改为 `NVARCHAR`，重建表后重插数据 |
| MySQL 中文双编码 | 插入时 `mysql` CLI 使用 `latin1` 连接 | 指定 `--default-character-set=utf8mb4` 重插 |
| 客户端 B 登录无反应 | Maven 本地仓库 `server-b` jar 中 properties 为旧 MySQL 配置 | `mvn clean install -DskipTests` 更新仓库 |

### 逻辑修复（2026-05-29）

| 问题 | 原因 | 修复 |
|------|------|------|
| 跨院选课后「我的课程」不显示 | `myCourses()` 只查学生所属学院的选课表 | 改为**遍历所有学院网关**聚合查询结果 (IntegrationServer.java:125-135) |
| 非共享课程可被跨院选修 | `crossSelect()` 未校验课程的共享标识 | 在选课前调用 `listSharedCourses()` 验证目标课程是否为共享 (IntegrationServer.java:54-59) |
| SQL Server SelectA 外键阻止跨院选课 | SelectA.sid 有 FK → StudentA.sid，外院学生 ID 无法插入 | **去掉 FK 约束**（SelectA 改为仅主键，与 SelectB/SelectC 一致）(docker/sqlserver/init.sql:40-51) |
| 「共享课程选课」按钮逻辑混乱 | 一个按钮同时承担"查看"和"确认选课"两个功能 | 拆分为 **「查看共享课程」**和 **「跨院选课」** 两个独立按钮 (CollegeDashboardFrame.java:90-111) |
| 选课成功后课程编号未清空 | `renderSimpleResult()` 不清空 courseIdField | `crossSelect` 成功后执行 `courseIdField.setText("")` |

### UI/UX 改进

- 按钮按功能分组（查询组 / 操作组 / 统计组），间距更清晰
- 底部**状态栏**显示学院、用户、当前操作状态
- 课程编号输入框支持**回车键直接选课**
- 表格支持**列排序**（`setAutoCreateRowSorter(true)`）
- 操作结果输出区使用即时刷新方式替代追加堆积
- 表格行高、表头字体、整体间距优化

### 代码清理

- 移除 `IntegrationServerBootstrap.java` 中未使用的 `server` 变量（死代码）
- 移除所有 `.bak` 备份文件
- 更新 `.gitignore` 添加 `logs/`、`*.bak`、`db/` 规则

### 统计面板重构（2026-05-30）

| 文件 | 变更内容 |
|------|---------|
| `common/.../StatisticsPanel.java` | **重构**：堆叠全尺寸图表 → "卡片画廊 → 点击详情" 导航模式；新增 CardLayout 三卡片切换（占位/画廊/详情）；新增交互筛选器（学院复选框 + 教师下拉）；新增 3 个管理员图表 |
| `common/.../Charts.java` | **新增** 3 个管理员图表方法（`createTeacherWorkloadChart`、`createCreditDistributionChart`、`createSharedRatioPieChart`）；**修复** JFreeChart 中文渲染问题（全局 StandardChartTheme 设置中文字体） |
| `common/.../CollegeDashboardFrame.java` | **修改**：接收 Role 参数传递给 StatisticsPanel |
| `client-*/.../LoginFrame*.java` | **修改**：传递 Role 作为第 6 个参数 |
| `integration-server/.../IntegrationServer.java` | **增强**：统计接口新增 `<allCourses>` 段（含完整课程详情）和 per-college `<sharedCourses>` 计数 |

### 差异化数据脚本（2026-05-30）

| 文件 | 变更内容 |
|------|---------|
| `docker/sqlserver/insert-data-a-differentiated.sql` | **新建** — 60 学生（6 专业，计算机/IT 方向）+ 52 课程 + 360 选课 |
| `docker/oracle/insert-data-b-differentiated.sql` | **新建** — 55 学生（5 专业，经管方向）+ 52 课程 + 165 选课 |
| `docker/mysql/insert-data-c-differentiated.sql` | **新建** — 65 学生（5 专业，工程方向）+ 52 课程 + 390 选课 |
| `seed-college-a.cmd` | **新建** — 一键导入 SQL Server 差异化数据 |
| `seed-college-b.cmd` | **新建** — 一键导入 Oracle 差异化数据 |
| `seed-college-c.cmd` | **新建** — 一键导入 MySQL 差异化数据 |

### 统计面板增强详情

**交互模式变更：**
- 旧版：全部图表垂直堆叠排列，一次性加载所有图表
- 新版：顶部 4 张概览卡片 + 下方图标画廊（点击进入详情视图）

**详情视图功能：**
- 图表区域（800×500 首选项大小，含缩放工具栏）
- 学院筛选复选框（动态勾选/取消各学院）
- 教师下拉筛选（从全部课程中提取教师列表）
- 「应用」按钮应用筛选条件重建图表
- 「重置」按钮恢复默认显示
- 「返回画廊」按钮回到图表选择界面

**管理员模式：**
- 登录时选择"管理员"身份，传递 Role.ADMIN 到 StatisticsPanel
- 画廊中额外显示 3 张管理图表：教师授课量、学分分布统计、共享课程占比
- 管理员图表提供宏观全局视角，适合教学管理决策

**中文渲染修复：**
- JFreeChart 默认字体不包含中文字符
- 通过 `StandardChartTheme` 将全局字体设置为 Microsoft YaHei / SimHei 等中文字体
- 使用 `Font.deriveFont()` 保持字体样式统一

### 差异化数据设计

| 维度 | 学院 A | 学院 B | 学院 C |
|------|--------|--------|--------|
| 学科方向 | 计算机科学 / 软件工程 / 人工智能 / 数据科学 / 网络工程 / 信息安全 | 经济学 / 金融学 / 会计学 / 国际贸易 / 管理学 | 电子工程 / 通信工程 / 自动化 / 物联网 / 机器人工程 |
| 课程体系 | 数据结构、机器学习、计算机网络等 52 门 | 微观经济学、金融学原理、会计学原理等 52 门 | 电路分析、自动控制原理、机器人学基础等 52 门 |
| 选课密度 | 6.0（高密度，CS 学生选课积极） | 3.0（低密度，经管学生选课精简） | 6.0（高密度，工程学生选课饱满） |
| 学生规模 | 60 人，6 专业 × 10 人 | 55 人，5 专业 × 11 人 | 65 人，5 专业 × 13 人 |

### hw4 数据上载（2026-06-05）

按助教要求完成组号 2 的数据上传至 `10.60.254.44:3306/hw4`：

| 变化 | 说明 |
|------|------|
| **数据翻倍** | 每院学生从 55-65 扩展到 **120 人**，课程从 52 扩展到 **104 门**，选课从 165-390 扩展到 **720-840 条** |
| **学科扩展** | A 院新增数字媒体、生物信息、科技传播等；B 院新增社会学、心理学、新闻传播、教育等；C 院新增航空航天、生物医学、材料、环境等 |
| **学时字段** | course 表增加 `class_hours` 和 `practice_hours`，赋值多种模式（48+16、32+16、32+32 等） |
| **新脚本** | `generate_hw4_data.py` — 自动生成 SQL 并上传至远程服务器 |

新增/修改文件：

| 文件 | 说明 |
|------|------|
| `generate_hw4_data.py` | **新建** — hw4 数据生成与上载脚本 |
| `upload_hw4.sql` | **新建** — 生成的完整上载 SQL（123.5 KB） |
| `README.md` | **更新** — 数据规格、新增 hw4 章节、文件结构 |

---

## 17. 附录：文件结构

```
EduFusion-XML-Based-Integrated-Academic-Management-System/
│
├── pom.xml                        # 父 POM（多模块聚合）
├── README.md                      # 本文件
│
├── common/                        # 公共模块
│   └── src/main/java/edu/fusion/common/
│       ├── model/                 # 数据模型 (Course, Student, Result, ...)
│       ├── service/               # 网关接口 + JdbcCollegeRepository + RemoteCollegeGateway
│       ├── server/                # CollegeXmlHttpServer (通用 HTTP 服务)
│       ├── ui/                    # CollegeDashboardFrame, Charts, StatisticsPanel
│       └── util/                  # Dom4jXmlService, DbUtil, JdbcConfigLoader, AuditLogger
│
├── server-a/                      # A 学院服务
│   ├── src/main/java/.../CollegeAGateway.java
│   └── src/main/resources/db/college-a.properties
│
├── server-b/                      # B 学院服务
│   ├── src/main/java/.../CollegeBGateway.java
│   └── src/main/resources/db/college-b.properties
│
├── server-c/                      # C 学院服务
│   ├── src/main/java/.../CollegeCGateway.java
│   └── src/main/resources/db/college-c.properties
│
├── integration-server/            # 集成服务器
│   ├── src/main/java/.../IntegrationServer.java
│   └── src/main/java/.../IntegrationXmlHttpServer.java
│
├── client-a/                      # A 学院 Swing 客户端
├── client-b/                      # B 学院 Swing 客户端
├── client-c/                      # C 学院 Swing 客户端
│
├── seed-college-a.cmd             # 一键导入 College A 差异化数据（SQL Server）
├── seed-college-b.cmd             # 一键导入 College B 差异化数据（Oracle）
├── seed-college-c.cmd             # 一键导入 College C 差异化数据（MySQL）
│
├── generate_hw4_data.py           # hw4 远程数据库上载生成脚本
├── upload_hw4.sql                 # 生成的 hw4 上载 SQL 文件
│
├── docker/                        # Docker 异构数据库部署
│   ├── docker-compose.yml
│   ├── sqlserver/
│   │   ├── init.sql                           # 建表 + 管理账号
│   │   ├── insert-data-a.sql                  # 旧版：30 学生 + 12 课程 + 150 选课
│   │   └── insert-data-a-differentiated.sql   # 新版：60 学生 + 52 课程 + 360 选课（差异化）
│   ├── oracle/
│   │   ├── init.sql                           # 建表 + 管理账号
│   │   ├── full-init-b.sql                    # 旧版：建表+30 学生+12 课程+150 选课
│   │   ├── insert-data-b.sql                  # 旧版：30 学生 + 12 课程 + 150 选课
│   │   └── insert-data-b-differentiated.sql   # 新版：55 学生 + 52 课程 + 165 选课（差异化）
│   └── mysql/
│       ├── init.sql                           # 建表 + 管理账号
│       ├── insert-data-c.sql                  # 旧版：30 学生 + 12 课程 + 150 选课
│       └── insert-data-c-differentiated.sql   # 新版：65 学生 + 52 课程 + 390 选课（差异化）
│
├── sql/                           # 本地模式 SQL 脚本
│   └── all-colleges-mysql.sql     # 全部学院表结构+数据（单一 MySQL）
│
├── xsd/                           # XML Schema 定义
│   ├── request.xsd                # 集成请求校验
│   ├── select.xsd                 # 选课请求校验
│   ├── drop.xsd                   # 退课请求校验
│   └── courses-standard.xsd       # 课程列表标准格式
│
├── xslt/                          # XSLT 转换模板（10 个）
│   ├── college-a-to-unified.xslt
│   ├── college-b-to-unified.xslt
│   ├── college-c-to-unified.xslt
│   ├── unified-to-college-a.xslt
│   ├── unified-to-college-b.xslt
│   ├── unified-to-college-c.xslt
│   ├── course-a-to-standard.xsl
│   ├── course-b-to-standard.xsl
│   ├── course-c-to-standard.xsl
│   └── standard-to-a.xsl
│
├── xml/                           # XML 请求/响应示例
├── scripts/                       # 测试脚本
├── .github/workflows/             # CI 配置
└── report/                        # 项目报告
```

---

> **版本**：2.1.0  
> **技术栈**：Java 8 + Swing + XML over HTTP (DOM4J + Xerces) + XSD + XSLT + JFreeChart 1.5.5 + Python 3  
> **数据库**：SQL Server 2022 / Oracle XE 21c / MySQL 8.0（Docker 异构模式）+ 远程 MySQL 8.0 (hw4)  
> **项目状态**：✅ 全部功能完成，三院客户端可同时登录操作，支持管理员模式  
> **测试通过率**：48/48 全覆盖测试通过（含 6 种请求类型 × 3 学院 × 边界/异常场景）
