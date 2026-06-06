# 管理员模式全面升级设计方案

## 1. 概述

### 1.1 目标
对 EduFusion 系统的管理员模式进行全面升级，使其与普通学生用户的权限和视图有明显区分，并集成跨学院数据库 CRUD 操作能力。

### 1.2 设计原则
- **本院管理 + 全局查看** — 管理员只能修改本院数据，但可查看所有学院的统计信息
- **避免过度设计** — 最小可行增量，优先实现核心 CRUD 功能
- **复用现有模式** — 沿用 XML-over-HTTP、CollegeGateway 接口模式、表格/分页等 UI 组件

### 1.3 范围
涉及 8 个功能模块：
1. 系统概览面板
2. 学生管理 (CRUD)
3. 课程管理 (CRUD)
4. 选课管理
5. 成绩管理
6. 审计日志
7. 统计报表（复用现有 StatisticsPanel）
8. 跨学院数据查看

---

## 2. 架构

### 2.1 组件关系

```
┌──────────────────────┐     XML/HTTP      ┌──────────────────────┐
│  AdminDashboardFrame │ ◄──────────────►  │  IntegrationServer   │
│  (Swing Client)      │                    │  (路由所有请求)       │
│  ┌─────────────────┐ │                    │  ┌────────────────┐  │
│  │ 系统概览面板      │ │                    │  │ queryCourses   │  │
│  │ 学生管理面板      │ │                    │  │ myCourses      │  │
│  │ 课程管理面板      │ │                    │  │ statistics     │  │
│  │ 选课管理面板      │ │                    │  │ admin*         │──┼──► CollegeGateway
│  │ 成绩管理面板      │ │                    │  │ auditLog       │  │    (A/B/C)
│  │ 审计日志面板      │ │                    │  └────────────────┘  │
│  │ 统计报表面板      │ │                    └──────────────────────┘
│  └─────────────────┘ │
└──────────────────────┘
```

### 2.2 管理员认证流程

```
LoginFrame (选择"管理员"角色)
  → AuthService.authenticate(username, password, ADMIN)
    → CollegeGateway.authenticateAdmin(username, password)
      → adminPasswords map (InMemory) / admin 表 (JDBC)
  → 成功后打开 AdminDashboardFrame 而非 CollegeDashboardFrame
```

---

## 3. 后端 API 设计

### 3.1 CollegeGateway 接口新增方法

```java
// ===== 学生管理 CRUD =====
boolean addStudent(Student student);
boolean updateStudent(Student student);
boolean deleteStudent(String studentId);

// ===== 课程管理 CRUD =====
boolean addCourse(Course course);
boolean updateCourse(Course course);
boolean deleteCourse(String courseId);

// ===== 成绩管理 =====
boolean updateScore(String studentId, String courseId, int score);

// ===== 审计日志 =====
List<String> getAuditLogs(int limit);
```

### 3.2 IntegrationServer 新增路由

在 `processRequestXml()` 的 switch 中新增：

| 请求类型 | 功能 | 权限校验 |
|---------|------|---------|
| `adminAddStudent` | 添加学生 | 校验 college 匹配 |
| `adminUpdateStudent` | 修改学生信息 | 同上 |
| `adminDeleteStudent` | 删除学生 | 同上 |
| `adminAddCourse` | 添加课程 | 同上 |
| `adminUpdateCourse` | 修改课程 | 同上 |
| `adminDeleteCourse` | 删除课程 | 同上 |
| `adminUpdateScore` | 录入/修改成绩 | 同上 |
| `adminListStudents` | 列出本院所有学生 | 仅本院 |
| `adminListSelections` | 列出本院所有选课 | 仅本院 |
| `adminAuditLog` | 获取审计日志 | 管理员专属 |

### 3.3 XML 请求/响应格式

所有请求沿用现有 XML 格式：

```xml
<!-- 添加学生 -->
<request>
  <type>adminAddStudent</type>
  <college>A</college>
  <id>A101</id>
  <name>张三</name>
  <sex>M</sex>
  <major>Major1</major>
</request>

<!-- 响应 -->
<response>
  <success>true</success>
  <message>Student added successfully</message>
</response>
```

```xml
<!-- 修改成绩 -->
<request>
  <type>adminUpdateScore</type>
  <college>A</college>
  <studentId>A001</studentId>
  <courseId>AC01</courseId>
  <score>85</score>
</request>
```

```xml
<!-- 审计日志 -->
<request>
  <type>adminAuditLog</type>
  <college>A</college>
  <limit>50</limit>
</request>

<response>
  <success>true</success>
  <logs>
    <log>action=addStudent actor=A_ADMIN target=A101 success=true message=...</log>
    <log>action=updateScore actor=A_ADMIN target=A001/AC01 success=true ...</log>
  </logs>
</response>
```

### 3.4 JdbcCollegeRepository 实现要点

- `addStudent`: INSERT INTO student_table
- `updateStudent`: UPDATE student_table SET name=?, sex=?, major=? WHERE id=?
- `deleteStudent`: DELETE FROM student_table WHERE id=? (附带清理选课记录)
- `addCourse`: INSERT INTO course_table
- `updateCourse`: UPDATE course_table SET ... WHERE id=?
- `deleteCourse`: DELETE FROM course_table WHERE id=?
- `updateScore`: UPDATE selection_table SET score=? WHERE sid=? AND cid=?
- `getAuditLogs`: 从 audit_log 表查询，若表不存在则返回空列表

### 3.5 InMemoryCollegeStore 实现要点

- 在内存 List 中直接执行 add/remove/update 操作
- 维护 id 的唯一性约束（add 时检查）
- deleteStudent 同时清理相关选课记录

---

## 4. 前端设计

### 4.1 AdminDashboardFrame

```
┌──────────────────────────────────────────────────────────────┐
│  管理员端 - A学院                          用户: A_ADMIN    │
├──────────┬───────────────────────────────────────────────────┤
│          │  [系统概览]  [学生管理]  [课程管理]               │
│ 系统概览  │  [选课管理]  [成绩管理]  [审计日志]               │
│ 学生管理  │  [统计报表]  [跨学院查看]                         │
│ 课程管理  │                                                  │
│ 选课管理  │  ┌─────────────────────────────────────────┐     │
│ 成绩管理  │  │  内容区域（根据选中的功能切换）           │     │
│ 审计日志  │  │                                         │     │
│ 统计报表  │  │                                         │     │
│ 跨学院查看 │  │                                         │     │
│          │  └─────────────────────────────────────────┘     │
│          │                                                  │
│          │  状态栏: 学院 A | 用户 A_ADMIN | 就绪           │
├──────────┴───────────────────────────────────────────────────┤
│  管理员版 v1.0                                               │
└──────────────────────────────────────────────────────────────┘
```

- **布局**: 左侧导航栏 + 右侧内容区（CardLayout 切换）
- **导航**: 点击导航项切换右侧面板
- **顶部栏**: 显示学院标识和当前用户
- **状态栏**: 与现有风格一致

### 4.2 模块面板详情

#### 4.2.1 系统概览 (DashboardPanel)
- 顶部：四个指标卡片（总学生数、总课程数、总选课数、共享课程数），复用 StatsCardPanel
- 中部：近期操作列表（从审计日志中取最近记录）
- 底部：各学院信息卡片（只读方式展示三学院基本数据）

#### 4.2.2 学生管理 (StudentManagementPanel)
- 功能：查看本学院学生列表、添加学生、修改学生信息、删除学生
- 表格列：编号、姓名、性别、专业
- 操作：下方表单（新增/编辑）+ 表格行选中后修改/删除
- 确认对话框：删除前二次确认
- 复用 CollegeDashboardFrame 中的分页/筛选/导出组件

#### 4.2.3 课程管理 (CourseManagementPanel)
- 功能：查看本院课程、添加课程、修改课程信息、删除课程、切换共享状态
- 表格列：课程号、课程名、学分、教师、地点、共享状态
- 操作：表单 + 表格行操作的 CRUD 模式
- 删除限制：已有学生选课的课程不能直接删除（需提示）

#### 4.2.4 选课管理 (SelectionManagementPanel)
- 功能：查看本院所有选课记录、按学生/课程筛选、退课操作
- 表格列：学生编号、课程编号、课程名称、学生姓名、所属学院
- 筛选：按学生编号或课程编号筛选
- 操作：选中记录后可退课（确认对话框）

#### 4.2.5 成绩管理 (ScoreManagementPanel)
- 功能：查看选课记录列表、录入/修改成绩
- 表格列：学生编号、学生姓名、课程编号、课程名称、成绩
- 操作：双击成绩单元格或选中后点击"修改成绩"
- 成绩输入框：0-100 整数校验

#### 4.2.6 审计日志 (AuditLogPanel)
- 功能：查看所有操作日志
- 表格列：时间、操作类型、操作用户、目标、结果、描述
- 筛选条件：按操作类型、时间范围筛选
- 日志来源：audit.log 文件或 AuditLogger 缓冲区

#### 4.2.7 统计报表 (StatisticsPanel)
- 直接复用现有的 StatisticsPanel 组件
- 管理员模式下已有额外 3 个管理员专属图表

#### 4.2.8 跨学院查看 (CrossCollegePanel)
- 以只读方式展示其他学院的学生、课程数据
- 下拉选择目标学院
- 只读表格，不提供任何修改操作

### 4.3 登录逻辑修改

各客户端的 LoginFrame 登录成功后：

```java
// 在 login() 方法中：
if (role == Role.ADMIN) {
    AdminDashboardFrame dashboard = new AdminDashboardFrame(
        collegeCode + "学院管理员端",
        collegeCode,
        serviceUrl,
        username,
        () -> { /* logout callback */ });
    dashboard.setVisible(true);
} else {
    // 现有逻辑：打开 CollegeDashboardFrame
}
```

---

## 5. 数据流

### 5.1 管理员 CRUD 操作流程

```
1. 管理员在 AdminDashboardFrame 中操作 UI
2. 面板构建 XML 请求 → AdminDashboardFrame.sendRequest()
3. POST 到 IntegrationServer (http://localhost:8080/api/xml)
4. IntegrationServer 解析 type → 调用对应方法
5. 校验 college 与管理员身份匹配
6. 调用 CollegeGateway 对应方法
7. JdbcCollegeRepository/InMemoryCollegeStore 执行数据操作
8. 返回 XML 响应 → 客户端解析 → UI 刷新
```

### 5.2 查询操作流程

```
1. 管理员点击查询 → 构建 XML
2. POST 到 IntegrationServer
3. IntegrationServer 调用对应 gateway 方法
4. 返回 XML 数据 → 渲染表格
```

---

## 6. 实现顺序

建议按以下顺序分阶段实现：

### Phase 1: 基础设施
1. 完善已有的 listAllStudents / listAllSelections （当前未提交的代码编译通过）
2. 在 CollegeGateway 中添加剩余的 CRUD 方法定义
3. 在 InMemoryCollegeStore 中实现 CRUD 方法
4. 在 JdbcCollegeRepository 中实现 CRUD 方法
5. 在 IntegrationServer 中添加所有 admin* 路由
6. 在 CollegeXmlHttpServer 中添加 admin* XML 处理

### Phase 2: AdminDashboardFrame
7. 创建 AdminDashboardFrame（侧边栏导航 + CardLayout 内容区）
8. 实现 DashboardPanel（系统概览）
9. 实现 StudentManagementPanel
10. 实现 CourseManagementPanel
11. 实现 SelectionManagementPanel

### Phase 3: 完善
12. 实现 ScoreManagementPanel
13. 实现 AuditLogPanel
14. 实现 CrossCollegePanel
15. 修改 LoginFrame 登录逻辑
16. 编译验证 + 功能测试

---

## 7. 文件变更清单

### 修改的文件
| 文件 | 变更内容 |
|------|---------|
| `common/.../service/CollegeGateway.java` | 新增 CRUD 接口方法 |
| `common/.../service/InMemoryCollegeStore.java` | 实现 CRUD 方法 |
| `common/.../service/JdbcCollegeRepository.java` | 实现 CRUD SQL 方法 + getAuditLogs |
| `common/.../service/RemoteCollegeGateway.java` | 添加远程 CRUD 调用 + XML 解析 |
| `common/.../server/CollegeXmlHttpServer.java` | 添加 admin* XML 处理 |
| `integration/.../service/IntegrationServer.java` | 添加 admin* 路由方法 |
| `server-a/.../service/CollegeAGateway.java` | 委托 CRUD 方法到 Repository |
| `server-b/.../service/CollegeBGateway.java` | 同上 |
| `server-c/.../service/CollegeCGateway.java` | 同上 |
| `client-a/.../LoginFrameA.java` | 登录后根据角色打开不同 Dashboard |
| `client-b/.../LoginFrameB.java` | 同上 |
| `client-c/.../LoginFrameC.java` | 同上 |

### 新增的文件
| 文件 | 说明 |
|------|------|
| `common/.../ui/AdminDashboardFrame.java` | 管理员主窗口 |
| `common/.../ui/admin/DashboardPanel.java` | 系统概览面板 |
| `common/.../ui/admin/StudentManagementPanel.java` | 学生管理面板 |
| `common/.../ui/admin/CourseManagementPanel.java` | 课程管理面板 |
| `common/.../ui/admin/SelectionManagementPanel.java` | 选课管理面板 |
| `common/.../ui/admin/ScoreManagementPanel.java` | 成绩管理面板 |
| `common/.../ui/admin/AuditLogPanel.java` | 审计日志面板 |
| `common/.../ui/admin/CrossCollegePanel.java` | 跨学院查看面板 |

---

## 8. 测试策略

- **CollegeGateway 接口变更**: 新增方法在 JdbcCollegeRepository 和 InMemoryCollegeStore 中都有实现
- **IntegrationServer 路由**: 新增各 admin* 路由的单元测试
- **AdminDashboardFrame**: 手工测试各功能面板 CRUD 操作
- **权限隔离**: 验证一个学院的管理员无法操作其他学院数据
- **审计日志**: 验证 admin 操作被正确记录
