# EduFusion XML Based Integrated Academic Management System

## 1. 项目概述

EduFusion 是一个基于 XML 数据交换的跨学院教务集成系统，围绕三个学院的课程资源进行统一编排，支持：

- 本院课程查询
- 共享课程查询与跨院选课
- 退课
- 我的选课查询
- 全局统计报表

当前版本已完成真实 JDBC 数据访问，默认落地在 MySQL（数据库名：edufusion_iams），并提供可直接运行的模拟数据脚本。

## 2. 架构说明

系统为 Maven 多模块结构：

- common：公共模型、网关抽象、JDBC 通用组件、XML 工具、客户端通用面板
- server-a：A 学院网关与认证实现
- server-b：B 学院网关与认证实现
- server-c：C 学院网关与认证实现
- integration-server：集成服务编排与 HTTP+XML 接口
- client-a / client-b / client-c：三学院登录入口
- sql：建库建表与模拟数据脚本
- xml / xsd / xslt：XML 示例、校验规则、转换模板

调用链路：

客户端 Swing -> HTTP POST XML -> 集成服务 -> 学院网关 JDBC -> MySQL

## 3. 已实现能力

- 真实 JDBC DAO 替换 InMemory
- 统一 XML over HTTP 服务（接口：/api/xml）
- 多请求类型支持：queryCourses、shareCourse、myCourses、crossSelect、dropCourse、statistics
- 客户端表格化交互（点击课程行自动填充课程编号）

## 4. 运行环境要求

- Windows/macOS/Linux
- JDK 8 及以上
- Maven 3.8+
- MySQL 8.0+

## 5. 数据库初始化

### 5.1 在 VSCode 中已连接数据库的前提

你当前连接名为 EduFusion，数据库为 edufusion_iams。可直接执行脚本：

[sql/all-colleges-mysql.sql](sql/all-colleges-mysql.sql)

脚本内容包含：

- 三学院学生/课程/选课/管理员表
- 10 名学生/学院
- 6 门课程/学院
- 初始选课数据
- 管理员账号初始化

### 5.2 命令行执行方式（可选）

在项目根目录：

```powershell
mysql -uroot -p123456 -e "source sql/all-colleges-mysql.sql"
```

## 6. JDBC 配置说明

当前默认全部指向 MySQL（便于本地联调）：

- [server-a/src/main/resources/db/college-a.properties](server-a/src/main/resources/db/college-a.properties)
- [server-b/src/main/resources/db/college-b.properties](server-b/src/main/resources/db/college-b.properties)
- [server-c/src/main/resources/db/college-c.properties](server-c/src/main/resources/db/college-c.properties)

默认连接：

- url: jdbc:mysql://localhost:3306/edufusion_iams...
- username: root
- password: 123456

## 7. 编译与启动

### 7.1 编译

```powershell
mvn clean compile
```

### 7.2 启动集成服务

推荐先确保 8080 端口未被占用，然后启动：

主类：

edu.fusion.integration.IntegrationServerBootstrap

启动成功日志包含：

Integration XML HTTP server started at [http://localhost:8080/api/xml](http://localhost:8080/api/xml)

### 7.3 启动客户端

分别运行：

- edu.fusion.clienta.ClientAApp
- edu.fusion.clientb.ClientBApp
- edu.fusion.clientc.ClientCApp

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

## 9. 默认账号

学生账号：

- A001 / 123456
- B001 / 123456
- C001 / 123456

管理员账号：

- adminA / admin123
- adminB / admin123
- adminC / admin123

## 10. XML 接口示例

请求 URL：

[http://localhost:8080/api/xml](http://localhost:8080/api/xml)

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

## 11. 常见问题

1. 端口占用

- 现象：服务启动报 Address already in use
- 处理：释放 8080 或修改启动端口

1. 数据库连接失败

- 检查 MySQL 服务是否启动
- 检查 JDBC properties 中 url/username/password

1. Maven settings 警告

- 当前若看到 settings.xml 中 proxy 告警，通常不影响本项目编译

## 12. 后续规划建议

- 将 A/B/C 分别切回 SQL Server / Oracle / MySQL 真正异构部署
- 增加操作审计日志与异常追踪
- 客户端增加分页、筛选和导出
- 新增自动化测试与 CI 脚本