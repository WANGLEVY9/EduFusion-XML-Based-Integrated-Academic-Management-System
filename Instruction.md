# EduFusion — 异构教务数据集成系统使用手册

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [模块功能说明](#3-模块功能说明)
4. [运行环境要求](#4-运行环境要求)
5. [快速启动（纯本地模式）](#5-快速启动纯本地模式)
6. [异构数据库部署（Docker 模式）](#6-异构数据库部署docker-模式)
7. [运行模式详解](#7-运行模式详解)
8. [客户端操作说明](#8-客户端操作说明)
9. [测试](#9-测试)
10. [代码变更后的构建清理](#10-代码变更后的构建清理)
11. [常见问题](#11-常见问题)

---

## 1. 项目概述

EduFusion 是一个基于 **XML 数据交换标准**的跨学院教务集成系统。项目模拟了三所独立学院（A、B、C）的教学管理系统，通过 **XML over HTTP** 实现异构数据库之间的数据互通，核心功能包括：

- **课程共享**：学院间共享课程资源，支持跨院查看与申请
- **跨院选课**：学生可选修其他学院的共享课程，选课记录自动写入课程所属学院
- **统一退选**：支持本院课程退选与跨院课程退选，数据同步至对应学院
- **全局统计**：集成服务器聚合三院数据，生成全局统计报表
- **异构集成**：各学院使用不同数据库（SQL Server / Oracle / MySQL），通过 XML 统一数据格式

### 技术栈

| 层级 | 技术 |
|------|------|
| 编程语言 | Java 8 |
| 构建工具 | Maven 3.8+ |
| 数据交换 | XML over HTTP（DOM4J + Xerces） |
| 数据校验 | XML Schema (XSD) |
| 数据转换 | XSLT |
| 数据库 | MySQL 8.0 / SQL Server 2022 / Oracle XE 21c |
| JDBC 驱动 | mysql-connector-j / mssql-jdbc / ojdbc8 |
| GUI 框架 | Java Swing |
| 测试框架 | JUnit 5 |
| 容器化 | Docker Compose |

---

## 2. 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        客户端层 (Swing GUI)                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                         │
│  │Client A  │  │Client B  │  │Client C  │  ← 每院独立登录界面      │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘                         │
│       │             │             │                                │
│       └─────────────┼─────────────┘                                │
│                     │ HTTP POST (XML 报文)                         │
├─────────────────────┼─────────────────────────────────────────────┤
│              集成服务层 (Integration Server)                        │
│  ┌──────────────────┴──────────────────────────────────────────┐  │
│  │            IntegrationXmlHttpServer (:8080)                  │  │
│  │              /api/xml — XML 报文处理端点                      │  │
│  │              /api/health — 健康检查端点                       │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │              IntegrationServer                          │ │  │
│  │  │  请求路由 · 数据聚合 · XSD 校验 · 跨院编排               │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                     │ HTTP POST (XML 报文)                         │
├─────────────────────┼─────────────────────────────────────────────┤
│              学院服务层 (College Servers)                           │
│  ┌──────────────────┼──────────────────────────────────────────┐  │
│  │  ┌───────────────┴────────┐  ┌───────────────┴───────────┐  │  │
│  │  │ CollegeA (:8081)       │  │ CollegeB (:8082)          │  │  │
│  │  │ XML → JDBC → SQL Svr  │  │ XML → JDBC → Oracle       │  │  │
│  │  └────────────────────────┘  └───────────────────────────┘  │  │
│  │  ┌────────────────────────┐  ┌───────────────────────────┐  │  │
│  │  │ CollegeC (:8083)       │  │ (开发模式可跳过学院服务层  │  │  │
│  │  │ XML → JDBC → MySQL    │  │  直接通过 JDBC 访问数据库) │  │  │
│  │  └────────────────────────┘  └───────────────────────────┘  │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 两种运行模式

| 模式 | 学院服务层 | 集成服务器 | 适用场景 |
|------|-----------|-----------|---------|
| **本地模式** (默认) | 跳过 | 直接 JDBC 调用 | 快速开发、单机调试 |
| **远程模式** | 独立 HTTP 服务 | 通过 HTTP 转发至学院服务 | 异构数据库部署、生产 |

---

## 3. 模块功能说明

| 模块 | 功能 |
|------|------|
| **`common`** | 公共基础设施：数据模型(Course/Student/Result)、XML 工具(Dom4jXmlService/XmlUtil)、JDBC 工具(DbUtil/JdbcConfig)、网关接口(CollegeGateway)、HTTP 客户端(IntegrationXmlHttpClient/RemoteCollegeGateway)、学院 HTTP 服务器(CollegeXmlHttpServer)、通用客户端面板(CollegeDashboardFrame) |
| **`server-a`** | A 学院服务：CollegeAGateway（SQL Server 风格字段映射）、AuthServiceA（认证） |
| **`server-b`** | B 学院服务：CollegeBGateway（Oracle 风格字段映射）、AuthServiceB（认证） |
| **`server-c`** | C 学院服务：CollegeCGateway（MySQL 风格字段映射）、AuthServiceC（认证） |
| **`integration-server`** | 集成服务：请求路由与编排、XSD 校验、统计聚合、HTTP XML 端点 |
| **`client-a`** | A 学院 Swing 客户端（登录 + 主面板） |
| **`client-b`** | B 学院 Swing 客户端（登录 + 主面板） |
| **`client-c`** | C 学院 Swing 客户端（登录 + 主面板） |
| **`sql/`** | 数据库初始化脚本 |
| **`xsd/`** | XML Schema 定义 |
| **`xslt/`** | XSLT 数据转换模板 |
| **`xml/`** | XML 示例报文 |
| **`docker/`** | Docker Compose 编排与数据库初始化脚本 |

### 启动入口类一览

| 类名 | 所属模块 | 端口 | 说明 |
|------|---------|------|------|
| `IntegrationServerBootstrap` | integration-server | 8080 | 集成服务器（含本地开发环境全启动） |
| `CollegeAServerBootstrap` | server-a | 8081 | A 学院独立服务 |
| `CollegeBServerBootstrap` | server-b | 8082 | B 学院独立服务 |
| `CollegeCServerBootstrap` | server-c | 8083 | C 学院独立服务 |
| `ClientAApp` | client-a | — | A 学院客户端（Swing） |
| `ClientBApp` | client-b | — | B 学院客户端（Swing） |
| `ClientCApp` | client-c | — | C 学院客户端（Swing） |

---

## 4. 运行环境要求

### 基础软件

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 8 | 建议 JDK 11+（JDK 8 也能运行） |
| Maven | 3.8+ | 项目构建管理 |
| MySQL | 8.0 | 本地开发数据库 |

### 端口占用

默认占用以下端口，启动前请确保未被占用：

| 端口 | 服务 |
|------|------|
| 8080 | 集成服务器 |
| 8081 | A 学院服务 |
| 8082 | B 学院服务 |
| 8083 | C 学院服务 |
| 3306 | MySQL（本地） |
| 3307 | MySQL（Docker，与本地错开） |
| 1433 | SQL Server（Docker） |
| 1521 | Oracle XE（Docker） |
| 5500 | Oracle Enterprise Manager（Docker） |

---

## 5. 快速启动（纯本地模式）

此模式使用单一 MySQL 数据库存放三院数据，适合开发与调试。

### 5.1 初始化数据库

```powershell
# 确保 MySQL 服务已启动，然后执行：
mysql -uroot -p123456 < sql\all-colleges-mysql.sql
```

或在 MySQL 客户端中手动执行 `sql/all-colleges-mysql.sql` 的全部内容。

该脚本将：
- 创建数据库 `edufusion_iams`
- 创建三院所有表（StudentA/B/C、CourseA/B/C、SelectA/B/C、AdminA/B/C）
- 插入 **60 名学生/学院**、**12 门课程/学院**、**每生 5 门选课**

### 5.2 编译项目

```powershell
mvn clean compile
```

首次编译会下载 Maven 依赖，请保持网络通畅。

### 5.3 启动集成服务器

```powershell
# 方式一：直接运行 main 类（mainClass 已在 pom.xml 中配置）
mvn exec:java -pl integration-server
```

或用 IDE 打开 `integration-server` → `IntegrationServerBootstrap.java` → 运行 `main()`。

启动成功后看到日志：

```
Integration XML HTTP server started at http://localhost:8080/api/xml
```

### 5.4 启动客户端

分别为三个学院启动客户端（可同时运行，每个在一个独立终端/IDE 实例中）：

```powershell
# 终端 1：A 学院
mvn exec:java -pl client-a

# 终端 2：B 学院
mvn exec:java -pl client-b

# 终端 3：C 学院
mvn exec:java -pl client-c
```

### 5.5 登录测试

| 账号 | 密码 | 角色 | 学院 |
|------|------|------|------|
| `A001` ~ `A060` | `123456` | 学生 | A |
| `B001` ~ `B060` | `123456` | 学生 | B |
| `C001` ~ `C060` | `123456` | 学生 | C |
| `adminA` | `admin123` | 管理员 | A |
| `adminB` | `admin123` | 管理员 | B |
| `adminC` | `admin123` | 管理员 | C |

### 5.6 停止系统

```powershell
# 停止各客户端：直接关闭 Swing 窗口
# 停止集成服务器：Ctrl+C
```

---

## 6. 异构数据库部署（Docker 模式）

此模式使用 Docker 运行 SQL Server、Oracle XE、MySQL 三个数据库容器，每院连接各自异构数据库，展示真正的系统异构性。

### 6.1 启动数据库容器

```powershell
# 从项目根目录执行（不要 cd docker）
docker compose -f docker/docker-compose.yml up -d
```

该命令会依次启动：

| 容器名 | 数据库 | 端口 | 默认用户/密码 |
|--------|--------|------|-------------|
| `edufusion-sqlserver` | SQL Server 2022 | 1433 | sa / EduFusion123! |
| `edufusion-oracle` | Oracle XE 21c | 1521 | EDUFUSION_B / EduFusion123 |
| `edufusion-mysql` | MySQL 8.0 | 3307 | root / 123456 |

> **注意**：Oracle 容器首次启动需要 2-5 分钟完成初始化（创建 XE 数据库），可通过以下命令监控：
> ```powershell
> docker logs -f edufusion-oracle
> ```
> 看到 `DATABASE IS READY TO USE!` 即为就绪。

### 6.2 验证数据库就绪

```powershell
# 检查容器状态
docker ps

# 检查各数据库健康状态
curl http://localhost:8081/api/health   # 通过学院服务检查
```

各容器的初始化脚本（`docker/sqlserver/init.sql`、`docker/oracle/init.sql`、`docker/mysql/init.sql`）会在首次启动时自动建库建表。

> **SQL Server 初始化说明**：MSSQL 官方镜像不支持 `/docker-entrypoint-initdb.d/` 自动执行机制。本系统通过 `sqlserver-init` 配置器容器实现自动初始化——该容器会在 SQL Server 就绪后自动执行 `init.sql`，执行完毕后自动退出（状态 `Exited (0)` 属正常现象）。可通过以下命令确认初始化成功：
> ```powershell
> docker logs edufusion-sqlserver-init
> # 应看到 "SQL Server initialized successfully"
> ```

> **数据填充说明**：Docker 初始化脚本仅创建表结构和管理员账号。完整的学生/课程/选课数据可通过运行 SQL 脚本或通过客户端页面逐步产生。如需在 Docker 模式下一键填充全部数据，请参阅 `sql/` 目录下对应数据库的脚本。

### 6.3 在远程模式下启动系统

#### 方式 A：分别启动各服务（推荐用于调试）

```powershell
# 终端 1：A 学院服务（使用 Docker SQL Server）
mvn exec:java -pl server-a

# 终端 2：B 学院服务（使用 Docker Oracle）
mvn exec:java -pl server-b

# 终端 3：C 学院服务（使用 Docker MySQL）
mvn exec:java -pl server-c

# 终端 4：集成服务器（远程模式，mainClass 已在 pom.xml 中配置）
mvn exec:java -pl integration-server "-Dcollege.remote=true"
```

#### 方式 B：一键启动全部服务（仅限开发环境）

`IntegrationServerBootstrap` 内置了 `startLocalDevEnvironment()` 方法，会同时启动：
- 3 个学院 HTTP 服务（端口 8081/8082/8083）
- 集成服务（端口 8080）

若需使用此方式，可在 IDE 中调用 `IntegrationServerBootstrap.startLocalDevEnvironment()`，或自行添加启动入口。

每个学院服务使用的 JDBC 配置由类路径下的 properties 文件决定：

| 服务 | 本地模式配置 | Docker 模式配置 |
|------|------------|----------------|
| A (SQL Server) | `db/college-a.properties` | `db/college-a-docker.properties` |
| B (Oracle) | `db/college-b.properties` | `db/college-b-docker.properties` |
| C (MySQL) | `db/college-c.properties` | `db/college-c-docker.properties` |

通过指定 JVM 属性 `college.remote=true`，`IntegrationServerBootstrap` 会使用 `RemoteCollegeGateway` 代替本地 JDBC 直连，将请求转发至学院 HTTP 服务。

### 6.4 停止 Docker 环境

```powershell
docker compose -f docker/docker-compose.yml down          # 停止并删除容器
docker compose -f docker/docker-compose.yml down -v       # 停止并删除容器 + 数据卷（谨慎，会丢失数据）
```

---

## 7. 运行模式详解

### 7.1 本地模式（college.remote=false）

```
客户端 → 集成服务器(:8080) → Gateway 直连 → MySQL(:3306)
```

特点：
- 三院数据全部存放在同一 MySQL 数据库中
- 无需启动学院 HTTP 服务
- 集成服务器内部直接调用 `CollegeGateway` 实现
- 适合开发、调试、快速验证

### 7.2 远程模式（college.remote=true）

```
客户端 → 集成服务器(:8080) → RemoteCollegeGateway → HTTP/XML
  ├── CollegeA 服务(:8081) → JDBC → SQL Server(:1433)
  ├── CollegeB 服务(:8082) → JDBC → Oracle(:1521)
  └── CollegeC 服务(:8083) → JDBC → MySQL(:3307)
```

特点：
- 三院连接各自异构数据库
- 学院服务作为独立 HTTP 服务运行
- 集成服务器通过 `RemoteCollegeGateway` 以 XML 报文方式与学院服务通信
- 完全符合作业要求的三层架构

> **框架说明**：`RemoteCollegeGateway` 实现了 `CollegeGateway` 接口，将所有方法调用转换为 XML 请求发送至学院 HTTP 服务，再解析返回的 XML 响应。对 `IntegrationServer` 而言，使用本地网关还是远程网关完全透明。

---

## 8. 客户端操作说明

### 8.1 登录界面

启动客户端后显示登录界面：

- **用户名**：输入学生编号（如 `A001`）或管理员账号
- **密码**：默认 `123456`（学生）/ `admin123`（管理员）
- **身份**：下拉选择"学生"或"管理员"
- **学院**：固定显示当前客户端所属学院

### 8.2 主面板功能

登录成功后进入主面板，包含以下功能区域：

**顶部信息栏**：
- 学院代码、学生编号、课程编号、服务地址（自动填充）

**功能按钮**（中部）：

| 按钮 | 功能 | 操作流程 |
|------|------|---------|
| **课程查询** | 查看本院全部课程 | 直接点击，表格显示本院课程列表 |
| **共享课程选课** | 跨院选修共享课程 | ① 课程编号为空时点击 → 加载其他学院共享课<br>② 点击表格行自动填充课程编号<br>③ 再次点击按钮确认选课 |
| **我的选课** | 查看已选课程 | 直接点击，表格显示当前学生的选课记录 |
| **退课** | 退选已选课程 | ① 在表格中点击目标课程行（自动填充编号）<br>② 点击"退课"并确认 |
| **统计报表** | 查看全局统计数据 | 直接点击，展示总学生数/课程数/选课数/热门课程TOP |

**表格交互**（下方）：
- **分页**：可选择每页显示 5/10/20/50 条，支持上下翻页
- **筛选**：在筛选输入框输入关键字，表格实时过滤（按课程号、课程名、教师、学院等）
- **导出 CSV**：点击"导出当前筛选"将表格数据保存为 CSV 文件
- **点击填充**：点击表格任意行，该行课程编号自动填入上方课程编号输入框

**文本输出区**：
- 显示操作结果、课程列表详情、统计报表等文本信息

### 8.3 典型业务流

**跨院选课流程**：

```
1. 学生登录 → 点击「课程查询」查看本院课程
2. 点击「共享课程选课」（课程编号为空）→ 加载其他学院共享课程列表
3. 在表格中浏览并点击目标课程 → 课程编号自动填充
4. 再次点击「共享课程选课」→ 弹出确认对话框 → 确认完成选课
5. 系统自动刷新「我的选课」展示最新选课结果
```

**退课流程**：

```
1. 点击「我的选课」查看已选课程
2. 点击要退选的课程行 → 课程编号自动填充
3. 点击「退课」→ 确认对话框 → 确认完成退课
4. 系统自动刷新「我的选课」
```

---

## 9. 测试

### 9.1 运行全部测试

```powershell
mvn clean test
```

### 9.2 运行特定模块测试

```powershell
# 仅测试 common 模块
mvn test -pl common

# 仅测试 integration-server 模块
mvn test -pl integration-server
```

### 9.3 测试覆盖范围

| 测试类 | 模块 | 数量 | 覆盖内容 |
|--------|------|------|---------|
| `Dom4jXmlServiceTest` | common | 12 | DOM4J 创建/解析/XSD校验/XSLT转换 |
| `XmlUtilTest` | common | 2 | W3C DOM 工具兼容性 |
| `CollegeXmlHttpServerTest` | common | 9 | 学院HTTP服务 + 远程网关全链路 |
| `IntegrationServerTest` | integration-server | 7 | 集成服务路由/统计/跨院选课 |

**当前总计：30 个测试，全部通过。**

### 9.4 测试脚本

```powershell
# Windows
.\scripts\run-tests.ps1

# Linux/macOS
bash ./scripts/run-tests.sh
```

---

## 10. 代码变更后的构建清理

### 10.1 增量编译

修改代码后，仅编译变更模块：

```powershell
# 编译变更模块及其依赖
mvn compile -pl <模块名> -am

# 示例：修改 common 后重编译所有依赖它的模块
mvn compile -pl common -am
```

### 10.2 完全清理重建

```powershell
# 清除所有编译输出 + 运行全部测试
mvn clean test

# 清除 + 安装（含打包到本地仓库）
mvn clean install -DskipTests

# 彻底清理所有 target 目录
mvn clean
```

### 10.3 依赖变更后的处理

修改 `pom.xml` 后需要重新解析依赖：

```powershell
mvn clean install -DskipTests
```

### 10.4 常见构建问题

| 问题 | 原因 | 解决 |
|------|------|------|
| `Could not find artifact` | 本地仓库缺少模块包 | 执行 `mvn install -DskipTests` |
| `不再支持源选项 5` | JDK 版本过高 | 确保使用 JDK 8 或 11 |
| `Address already in use` | 端口被占用 | 检查端口占用，关闭冲突进程 |
| `Failed to parse XML` | XML 格式错误 | 检查请求报文格式 |

---

## 11. 常见问题

### 数据库连接失败

```
# 检查 MySQL 服务
mysql -uroot -p123456 -e "SELECT 1"

# 检查 JDBC 配置（每个学院独立配置）
server-a/src/main/resources/db/college-a.properties
server-b/src/main/resources/db/college-b.properties
server-c/src/main/resources/db/college-c.properties
```

### Docker 相关

```powershell
# 查看容器日志
docker logs edufusion-sqlserver
docker logs edufusion-oracle
docker logs edufusion-mysql

# 进入容器内部
docker exec -it edufusion-sqlserver bash
docker exec -it edufusion-oracle bash
docker exec -it edufusion-mysql bash

# 重启单个容器
docker restart edufusion-oracle
```

### 端口占用

```powershell
# 查看端口占用
netstat -ano | findstr "8080 8081 8082 8083"

# 释放端口（找到 PID 后终止进程）
taskkill /PID <PID> /F
```

### XSD 校验失败

所有发往集成服务器 `/api/xml` 的请求报文均需通过 `xsd/request.xsd` 校验。常见错误：

- 缺少必填元素 `<type>`
- `type` 值不在枚举列表中（`shareCourse` / `crossSelect` / `dropCourse` / `statistics` / `queryCourses` / `myCourses`）
- XML 格式错误（未闭合标签、编码问题）

### 使用 curl 测试接口

```powershell
# 查询 A 学院课程
curl -X POST http://localhost:8080/api/xml `
    -H "Content-Type: application/xml" `
    -d "<request><type>queryCourses</type><college>A</college></request>"

# 跨院选课
curl -X POST http://localhost:8080/api/xml `
    -H "Content-Type: application/xml" `
    -d "<request><type>crossSelect</type><studentId>A001</studentId><courseId>B101</courseId></request>"

# 获取统计报表
curl -X POST http://localhost:8080/api/xml `
    -H "Content-Type: application/xml" `
    -d "<request><type>statistics</type></request>"
```

---

## 附录：关键报文格式

### 请求报文

```xml
<request>
    <type>queryCourses</type>          <!-- 请求类型 -->
    <college>A</college>               <!-- 学院代码（查询时使用） -->
    <source>A</source>                 <!-- 来源学院（共享课程时使用） -->
    <studentId>A001</studentId>        <!-- 学生编号 -->
    <courseId>B101</courseId>          <!-- 课程编号 -->
</request>
```

### 响应报文（课程列表）

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

### XSLT 转换示例

学院特有格式转统一格式：

```xml
<!-- college-a-to-unified.xslt：cid→id, cname→name, room→location, shareFlag→shared -->
<xsl:template match="course">
    <course>
        <id><xsl:value-of select="cid"/></id>
        <name><xsl:value-of select="cname"/></name>
        <location><xsl:value-of select="room"/></location>
        <shared><xsl:value-of select="shareFlag"/></shared>
    </course>
</xsl:template>
```
