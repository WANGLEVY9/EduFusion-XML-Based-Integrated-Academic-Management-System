# EduFusion 集成分析与可视化界面设计

## 概述

在现有 EduFusion Swing 客户端基础上，为统计报表功能增加 JFreeChart 图表可视化和多 tab 分析面板，形成完整的集成数据可视化界面。

## 架构变更

### 布局重构

`CollegeDashboardFrame` 从单一面板改为 `JTabbedPane` 布局：

- **Tab 1 "课程操作"**：保留现有全部功能（课程查询、选课、退课、表格、分页、筛选、导出），界面完全不变
- **Tab 2 "数据统计"**：新增的可视化统计面板

### 新增文件

| 包路径 | 类名 | 职责 |
|--------|------|------|
| `edu.fusion.common.ui` | `StatisticsPanel` | 统计面板主容器，持有卡片和图表 |
| `edu.fusion.common.ui` | `StatsCardPanel` | 四张数字卡片（总学生/课程/选课/共享） |
| `edu.fusion.common.ui` | `ChartFactory` | JFreeChart 图表工厂（三种图表） |

### 修改文件

| 文件 | 变更 |
|------|------|
| `CollegeDashboardFrame.java` | 引入 JTabbedPane，按钮触发统计后切换 tab |
| `IntegrationServer.java` | `processStats()` 的 XML 响应中追加 `<colleges>` 节点 |
| `pom.xml` (parent) | 添加 jfreechart 1.5.5 依赖管理 |
| `common/pom.xml` | 添加 jfreechart 依赖声明 |

## 数据流

```
用户点击 [统计报表]
  → buildRequest("statistics") → XML POST
  → IntegrationXmlHttpClient.postXml() → 集成服务器
  ← XML 响应（含总览 + 三院分项 + TOP10）
  → StatisticsPanel.loadStatistics(responseXml)
    → StatsCardPanel.setData(总学生数, 总课程数, 总选课数, 共享课程数)
    → ChartFactory.createCollegeCompareChart() → ChartPanel
    → ChartFactory.createSelectionPieChart()   → ChartPanel
    → ChartFactory.createTopCoursesChart()     → ChartPanel
    → detailArea.append(统计详情文本)
  → tabbedPane.setSelectedIndex(1)  // 切换到数据统计 tab
```

## 后端扩展

`IntegrationServer.processStats()` 的 XML 响应新增 `<colleges>` 节点：

```xml
<statistics>
  <totalStudents>180</totalStudents>
  <totalCourses>30</totalCourses>
  <totalSelections>900</totalSelections>
  <totalSharedCourses>45</totalSharedCourses>
  <colleges>
    <college><code>A</code><students>60</students><courses>10</courses><selections>300</selections><sharedCourses>15</sharedCourses></college>
    <college><code>B</code><students>60</students><courses>10</courses><selections>300</selections><sharedCourses>15</sharedCourses></college>
    <college><code>C</code><students>60</students><courses>10</courses><selections>300</selections><sharedCourses>15</sharedCourses></college>
  </colleges>
  <topCourses>
    <course><id>A101</id><name>高等数学</name><college>A</college><selectedCount>45</selectedCount></course>
    ...
  </topCourses>
</statistics>
```

## 界面布局

### 统计面板

```
┌─────────────────────────────────────────────────────────────┐
│ [StatsCardPanel - 四张卡片]                                │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                     │
│  │180 总│ │30 总 │ │900 总│ │45 共 │                     │
│  │学生数│ │课程数│ │选课数│ │享课程│                     │
│  └──────┘ └──────┘ └──────┘ └──────┘                     │
├────┬────────────────────────────────────────────────────────┤
│ 学 │ 对比 │  课程热度 TOP10 │                              │
│ 院 │ [分组柱状图] │ [水平柱状图]      │                    │
│ 对 │             │                    │                    │
│ 比 │ [饼图]      │ [排名表格]         │                    │
├────┴────────────────────────────────────────────────────────┤
│ 文本详情输出区                                              │
└─────────────────────────────────────────────────────────────┘
```

### 数字卡片

每张卡片 = 白色圆角背景 + 左侧彩色竖条 + 标题 + 大号数字（48px 粗体）。

### 图表规格

| 图表 | 类型 | X轴 | Y轴 | 系列 |
|------|------|-----|-----|------|
| 学院对比 | 分组柱状图 (GroupedBarChart) | 学院 A/B/C | 数值 | 学生数、课程数、选课数 |
| 选课占比 | 饼图 (PieChart) | — | — | A/B/C 三色扇形 |
| 课程热度 | 水平柱状图 (HorizontalBarChart) | 课程名 | 选课数 | 按学院着色 |

### 配色

- A: #4A90D9, B: #50C878, C: #FF8C42
- 面板底色: #F0F2F5, 卡片白底, 文字 #333333

## 异常处理

- 统计失败：卡片显示 `--`，图表区域提示错误
- 缺少 `colleges` 节点：兼容处理，不崩溃
- TOP10 不足 10 条：有多少显示多少
- 初次加载：显示引导提示文字

## 依赖

- JFreeChart 1.5.5（兼容 JDK 8+）

## 测试计划

1. 后端：`IntegrationServerTest` 扩展，验证 `<colleges>` 节点
2. 前端：手动验证（数字、柱状图、饼图、TOP10）
3. 回归：执行 `mvn clean test`，30 个测试全部通过
