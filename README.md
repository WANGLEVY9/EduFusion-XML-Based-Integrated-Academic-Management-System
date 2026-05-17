# EduFusion XML Based Integrated Academic Management System

## 1. 项目概述

EduFusion 是一个基于 XML 数据交换的跨学院教务集成系统，面向三学院课程资源统一编排，支持：

- 本院课程查询
- 共享课程查询与跨院选课
- 退课
- 我的选课查询
- 全局统计报表

当前版本已完成真实 JDBC 数据访问与 XML over HTTP 通信，并提供本地模式（单一 MySQL）与异构模式（SQL Server/Oracle/MySQL）两种运行方式。

## 2. 架构说明

系统为 Maven 多模块结构：

- common：公共模型、网关抽象、JDBC 通用组件、XML 工具、学院 HTTP 服务器、客户端通用面板
- server-a：A 学院网关与认证实现
- server-b：B 学院网关与认证实现
- server-c：C 学院网关与认证实现
- integration-server：集成服务编排与 HTTP+XML 接口
- client-a / client-b / client-c：三学院登录入口
- sql：建库建表与模拟数据脚本
- xml / xsd / xslt：XML 示例、校验规则、转换模板
- docker：异构数据库容器编排与初始化脚本

两种运行模式：

- 本地模式（默认）：客户端 -> 集成服务 -> JDBC 直连单一 MySQL
- 远程模式：客户端 -> 集成服务 -> HTTP/XML -> 学院服务 -> JDBC -> 异构数据库

## 3. 已实现能力

- 真实 JDBC DAO 替换 InMemory
- 统一 XML over HTTP 服务（/api/xml、/api/health）
- 多请求类型支持：queryCourses、shareCourse、myCourses、crossSelect、dropCourse、statistics
- XSD 校验与 XSLT 转换模板
- 客户端表格化交互（点击课程行自动填充课程编号）
- 客户端分页、筛选与 CSV 导出能力
- 统计报表图形化展示（JFreeChart 多 Tab 面板）
- 操作审计日志与异常追踪（logs/audit.log、logs/error.log）
- 自动化测试与 GitHub Actions CI

## 4. 运行环境要求

- Windows/macOS/Linux
- JDK 8 及以上
- Maven 3.8+
- MySQL 8.0+（本地模式）
- Docker Desktop（异构模式可选）

## 5. 本地模式快速启动（单一 MySQL）

### 5.1 初始化数据库

执行脚本：

[sql/all-colleges-mysql.sql](sql/all-colleges-mysql.sql)

脚本内容包含：

- 三学院学生/课程/选课/管理员表
- 60 名学生/学院
- 12 门课程/学院
- 每生 5 门选课

命令行方式：

```powershell
mysql -uroot -p123456 -e "source sql/all-colleges-mysql.sql"
```

### 5.2 编译与启动

```powershell
mvn clean compile
mvn exec:java -pl integration-server
```

启动成功日志包含：

Integration XML HTTP server started at http://localhost:8080/api/xml

### 5.3 启动客户端

```powershell
mvn exec:java -pl client-a
mvn exec:java -pl client-b
mvn exec:java -pl client-c
```

## 6. 异构数据库模式（Docker）

### 6.1 启动数据库容器

```powershell
docker compose -f docker/docker-compose.yml up -d
```

容器与端口：

- SQL Server 2022：1433（账号 sa / EduFusion123!）
- Oracle XE 21c：1521（账号 edufusion_b / EduFusion123）
- MySQL 8.0：3307（账号 root / 123456）

### 6.2 启动学院服务与集成服务

```powershell
mvn exec:java -pl server-a
mvn exec:java -pl server-b
mvn exec:java -pl server-c
mvn exec:java -pl integration-server "-Dcollege.remote=true"
```

### 6.3 JDBC 配置切换说明

学院服务默认读取类路径下的 properties 文件：

- A：server-a/src/main/resources/db/college-a.properties
- B：server-b/src/main/resources/db/college-b.properties
- C：server-c/src/main/resources/db/college-c.properties

Docker 模式对应配置：

- A：server-a/src/main/resources/db/college-a-docker.properties
- B：server-b/src/main/resources/db/college-b-docker.properties
- C：server-c/src/main/resources/db/college-c-docker.properties

## 7. 启动入口类

- IntegrationServerBootstrap（integration-server，8080）
- CollegeAServerBootstrap（server-a，8081）
- CollegeBServerBootstrap（server-b，8082）
- CollegeCServerBootstrap（server-c，8083）
- ClientAApp / ClientBApp / ClientCApp（客户端）

## 8. 客户端操作流程

登录后统一进入通用面板，包含：

- 顶部输入区：学院、学生编号、课程编号、服务地址
- 中部按钮区：课程查询、共享课程选课、我的选课、退课、统计报表
- 下方表格区：展示课程结果，点击行自动填充课程编号
- 文本区：显示操作说明与统计详情

关键流程：

1. 点击课程查询：加载本院课程
2. 点击共享课程选课（课程编号为空）：先加载共享课
3. 点击表格目标行：自动回填课程编号
4. 再次点击共享课程选课：确认后执行选课
5. 成功后自动刷新我的选课
6. 退课流程同样支持确认+自动刷新

表格增强交互：

- 分页：支持每页 5/10/20/50 条，上一页/下一页切换
- 筛选：按课程号、课程名、教师、学院等关键字实时筛选
- 导出：一键导出当前筛选结果为 CSV 文件

## 9. 默认账号

学生账号（示例）：

- A001 / 123456
- B001 / 123456
- C001 / 123456

管理员账号：

- adminA / admin123
- adminB / admin123
- adminC / admin123

## 10. XML 接口示例

请求 URL：

http://localhost:8080/api/xml

示例：查询 A 学院课程

```xml
<request>
  <type>queryCourses</type>
  <college>A</college>
</request>
```

示例：跨院选课

```xml
<request>
  <type>crossSelect</type>
  <studentId>A001</studentId>
  <courseId>B101</courseId>
</request>
```

## 11. 自动化测试

当前已包含：

- 公共工具单元测试：common/src/test/java/edu/fusion/common/util/XmlUtilTest.java
- 集成服务业务测试：integration-server/src/test/java/edu/fusion/integration/service/IntegrationServerTest.java

执行全部测试：

```powershell
mvn -B clean test
```

执行快速测试脚本：

```powershell
powershell ./scripts/run-tests.ps1
```

Linux/macOS:

```bash
bash ./scripts/run-tests.sh
```

## 12. CI 脚本

GitHub Actions 工作流：

- .github/workflows/ci.yml

CI 内容：

- JDK 8 环境初始化
- Maven 缓存
- 执行 mvn -B clean test
- 上传 surefire 测试报告

## 13. 常见问题

1. 端口占用

- 现象：服务启动报 Address already in use
- 处理：释放 8080/8081/8082/8083 或修改启动端口

2. 数据库连接失败

- 检查数据库服务是否启动
- 检查 JDBC properties 中 url/username/password

3. Maven settings 警告

- settings.xml 中 proxy 告警通常不影响本项目编译

## 14. 后续规划

- 补充权限分级与更细粒度的操作审计检索
- 增加课程共享审批与撤销机制
- 增强客户端异常恢复与离线提示
