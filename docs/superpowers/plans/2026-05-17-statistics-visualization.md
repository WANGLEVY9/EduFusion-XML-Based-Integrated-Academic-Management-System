# 集成分析与可视化界面实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 EduFusion Swing 客户端添加 JFreeChart 图表可视化和多 tab 统计面板

**Architecture:** 在 common 模块中新增 StatisticsPanel、StatsCardPanel、ChartFactory 三个类，重构 CollegeDashboardFrame 加入 JTabbedPane，扩展 IntegrationServer 的 statistics XML 响应加入三学院分项数据。

**Tech Stack:** Java 8 Swing, JFreeChart 1.5.5, DOM4J

---

### Task 1: 添加 JFreeChart Maven 依赖

**Files:**
- Modify: `pom.xml` (parent)
- Modify: `common/pom.xml`

- [ ] **Step 1: 在 parent pom.xml 添加 jfreechart 版本属性和依赖管理**

在 `pom.xml` 的 `<properties>` 中添加：
```xml
<jfreechart.version>1.5.5</jfreechart.version>
```

在 `<dependencyManagement><dependencies>` 中添加：
```xml
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
    <version>${jfreechart.version}</version>
</dependency>
```

- [ ] **Step 2: 在 common/pom.xml 添加 jfreechart 依赖**

```xml
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
</dependency>
```

- [ ] **Step 3: 验证编译**

Run: `mvn clean compile -pl common`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add pom.xml common/pom.xml
git commit -m "build: add JFreeChart 1.5.5 dependency"
```

---

### Task 2: 扩展 IntegrationServer 统计 XML 响应

**Files:**
- Modify: `integration-server/src/main/java/edu/fusion/integration/service/IntegrationServer.java` (line 209-235)

- [ ] **Step 1: 在 processStats() 中添加 colleges 节点**

在 `processStats()` 方法中，`Element topCourses` 之后、`return` 之前，添加以下代码：

```java
// 添加三学院分项数据
Element collegesElement = statsElement.addElement("colleges");
for (CollegeGateway gateway : gateways.values()) {
    Element collegeElement = collegesElement.addElement("college");
    Dom4jXmlService.addTextElement(collegeElement, "code", gateway.getCollegeCode());
    Dom4jXmlService.addTextElement(collegeElement, "students", String.valueOf(gateway.countStudents()));
    Dom4jXmlService.addTextElement(collegeElement, "courses", String.valueOf(gateway.countCourses()));
    Dom4jXmlService.addTextElement(collegeElement, "selections", String.valueOf(gateway.countSelections()));
    Dom4jXmlService.addTextElement(collegeElement, "sharedCourses", String.valueOf(gateway.countSharedCourses()));
}
```

`gateways` 字段已在类中定义（`private final Map<String, CollegeGateway> gateways`），可以直接使用。

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl integration-server -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add integration-server/src/main/java/edu/fusion/integration/service/IntegrationServer.java
git commit -m "feat(server): add per-college breakdown to statistics XML response"
```

---

### Task 3: 创建 ChartFactory（JFreeChart 图表工厂）

**Files:**
- Create: `common/src/main/java/edu/fusion/common/ui/ChartFactory.java`

- [ ] **Step 1: 创建 ChartFactory 类**

```java
package edu.fusion.common.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import edu.fusion.common.model.CourseHeat;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

public final class Charts {

    private static final Color COLOR_A = new Color(0x4A, 0x90, 0xD9);
    private static final Color COLOR_B = new Color(0x50, 0xC8, 0x78);
    private static final Color COLOR_C = new Color(0xFF, 0x8C, 0x42);
    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);

    private Charts() {
    }

    /**
     * 三学院对比分组柱状图
     */
    public static JFreeChart createCollegeCompareChart(
            String[] collegeNames, int[] studentCounts, int[] courseCounts, int[] selectionCounts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < collegeNames.length; i++) {
            dataset.addValue(studentCounts[i], "学生数", collegeNames[i]);
            dataset.addValue(courseCounts[i], "课程数", collegeNames[i]);
            dataset.addValue(selectionCounts[i], "选课数", collegeNames[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "三学院数据对比", null, "数量",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, COLOR_A);
        renderer.setSeriesPaint(1, COLOR_B);
        renderer.setSeriesPaint(2, COLOR_C);
        renderer.setDrawBarOutline(false);

        return chart;
    }

    /**
     * 选课学院占比饼图
     */
    public static JFreeChart createSelectionPieChart(
            String[] collegeNames, int[] selectionCounts) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < collegeNames.length; i++) {
            dataset.setValue(collegeNames[i], selectionCounts[i]);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "选课学院分布", dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setSectionPaint(collegeNames[0], COLOR_A);
        plot.setSectionPaint(collegeNames[1], COLOR_B);
        plot.setSectionPaint(collegeNames[2], COLOR_C);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);

        return chart;
    }

    /**
     * 热门课程 TOP10 水平柱状图
     */
    public static JFreeChart createTopCoursesChart(List<CourseHeat> topCourses) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (CourseHeat heat : topCourses) {
            String label = heat.getCourseId() + " " + heat.getCourseName();
            dataset.addValue(heat.getSelectedCount(), "选课人数", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "热门课程 TOP10", null, "选课人数",
                dataset, PlotOrientation.HORIZONTAL,
                false, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, COLOR_A);
        renderer.setDrawBarOutline(false);

        return chart;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl common`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add common/src/main/java/edu/fusion/common/ui/Charts.java
git commit -m "feat(ui): add Charts factory with JFreeChart bar/pie chart methods"
```

---

### Task 4: 创建 StatsCardPanel（四张数字卡片）

**Files:**
- Create: `common/src/main/java/edu/fusion/common/ui/StatsCardPanel.java`

- [ ] **Step 1: 创建 StatsCardPanel 类**

```java
package edu.fusion.common.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class StatsCardPanel extends JPanel {

    private final Card[] cards = new Card[4];

    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);
    private static final Color[] ACCENT_COLORS = {
        new Color(0x4A, 0x90, 0xD9),
        new Color(0x50, 0xC8, 0x78),
        new Color(0xFF, 0x8C, 0x42),
        new Color(0x9B, 0x59, 0xB6)
    };
    private static final String[] LABELS = {"总学生数", "总课程数", "总选课数", "共享课程数"};

    public StatsCardPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 16, 12));
        setBackground(BG_COLOR);
        for (int i = 0; i < 4; i++) {
            cards[i] = new Card(LABELS[i], ACCENT_COLORS[i]);
            add(cards[i]);
        }
    }

    public void setData(int students, int courses, int selections, int shared) {
        cards[0].setValue(students);
        cards[1].setValue(courses);
        cards[2].setValue(selections);
        cards[3].setValue(shared);
    }

    public void reset() {
        for (Card card : cards) {
            card.setValue(0);
        }
    }

    private static class Card extends JPanel {
        private final JLabel valueLabel;

        Card(String title, Color accentColor) {
            setPreferredSize(new Dimension(165, 85));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            setLayout(new BorderLayout(10, 4));

            JPanel accent = new JPanel();
            accent.setPreferredSize(new Dimension(6, 65));
            accent.setBackground(accentColor);
            add(accent, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            titleLabel.setForeground(new Color(0x88, 0x88, 0x88));
            textPanel.add(titleLabel);

            valueLabel = new JLabel("--");
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            valueLabel.setForeground(new Color(0x33, 0x33, 0x33));
            textPanel.add(valueLabel);

            add(textPanel, BorderLayout.CENTER);
        }

        void setValue(int value) {
            valueLabel.setText(String.valueOf(value));
        }
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl common`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add common/src/main/java/edu/fusion/common/ui/StatsCardPanel.java
git commit -m "feat(ui): add StatsCardPanel with 4 summary cards"
```

---

### Task 5: 创建 StatisticsPanel（统计数据主面板）

**Files:**
- Create: `common/src/main/java/edu/fusion/common/ui/StatisticsPanel.java`

- [ ] **Step 1: 创建 StatisticsPanel 类**

```java
package edu.fusion.common.ui;

import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.util.XmlUtil;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class StatisticsPanel extends JPanel {

    private final StatsCardPanel cardPanel = new StatsCardPanel();
    private final JTabbedPane chartTabPane = new JTabbedPane();
    private final JTextArea detailArea = new JTextArea();
    private final JPanel chartContainer = new JPanel(new BorderLayout());

    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);

    public StatisticsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(cardPanel, BorderLayout.NORTH);

        chartContainer.setBackground(BG_COLOR);
        JLabel placeholder = new JLabel("请点击「统计报表」按钮加载数据", SwingConstants.CENTER);
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 16));
        placeholder.setForeground(new Color(0x99, 0x99, 0x99));
        chartContainer.add(placeholder, BorderLayout.CENTER);
        add(chartContainer, BorderLayout.CENTER);

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setRows(6);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setPreferredSize(new Dimension(800, 130));
        add(scrollPane, BorderLayout.SOUTH);
    }

    public void loadStatistics(String responseXml) {
        Document doc = XmlUtil.parse(responseXml);
        Element root = doc.getDocumentElement();
        String success = XmlUtil.childText(root, "success");

        if (!"true".equalsIgnoreCase(success)) {
            cardPanel.reset();
            chartContainer.removeAll();
            chartContainer.add(new JLabel("统计请求失败：" + XmlUtil.childText(root, "message"), SwingConstants.CENTER),
                    BorderLayout.CENTER);
            detailArea.setText("统计请求失败\n");
            chartContainer.revalidate();
            chartContainer.repaint();
            return;
        }

        NodeList statsList = root.getElementsByTagName("statistics");
        if (statsList.getLength() == 0) return;
        Element stats = (Element) statsList.item(0);

        int totalStudents = parseInt(XmlUtil.childText(stats, "totalStudents"));
        int totalCourses = parseInt(XmlUtil.childText(stats, "totalCourses"));
        int totalSelections = parseInt(XmlUtil.childText(stats, "totalSelections"));
        int totalSharedCourses = parseInt(XmlUtil.childText(stats, "totalSharedCourses"));

        cardPanel.setData(totalStudents, totalCourses, totalSelections, totalSharedCourses);

        // 解析学院分项数据
        List<String> collegeNames = new ArrayList<>();
        List<Integer> collegeStudents = new ArrayList<>();
        List<Integer> collegeCourses = new ArrayList<>();
        List<Integer> collegeSelections = new ArrayList<>();

        NodeList collegesNodes = stats.getElementsByTagName("colleges");
        if (collegesNodes.getLength() > 0) {
            NodeList collegeList = ((Element) collegesNodes.item(0)).getElementsByTagName("college");
            for (int i = 0; i < collegeList.getLength(); i++) {
                Element c = (Element) collegeList.item(i);
                collegeNames.add(XmlUtil.childText(c, "code") + "学院");
                collegeStudents.add(parseInt(XmlUtil.childText(c, "students")));
                collegeCourses.add(parseInt(XmlUtil.childText(c, "courses")));
                collegeSelections.add(parseInt(XmlUtil.childText(c, "selections")));
            }
        }

        // 解析热门课程
        List<CourseHeat> topCourses = new ArrayList<>();
        NodeList topNodes = stats.getElementsByTagName("topCourses");
        if (topNodes.getLength() > 0) {
            NodeList courseList = ((Element) topNodes.item(0)).getElementsByTagName("course");
            for (int i = 0; i < courseList.getLength(); i++) {
                Element c = (Element) courseList.item(i);
                topCourses.add(new CourseHeat(
                        XmlUtil.childText(c, "id"),
                        XmlUtil.childText(c, "name"),
                        XmlUtil.childText(c, "college"),
                        parseInt(XmlUtil.childText(c, "selectedCount"))
                ));
            }
        }

        // 构建图表面板
        chartContainer.removeAll();
        chartTabPane.removeAll();

        if (collegeNames.size() == 3) {
            JPanel comparePanel = new JPanel(new GridLayout(2, 1, 4, 4));
            comparePanel.setBackground(BG_COLOR);

            String[] names = collegeNames.toArray(new String[0]);
            int[] students = collegeStudents.stream().mapToInt(i -> i).toArray();
            int[] courses = collegeCourses.stream().mapToInt(i -> i).toArray();
            int[] selections = collegeSelections.stream().mapToInt(i -> i).toArray();

            JFreeChart barChart = edu.fusion.common.ui.Charts.createCollegeCompareChart(
                    names, students, courses, selections);
            ChartPanel barChartPanel = new ChartPanel(barChart);
            barChartPanel.setPreferredSize(new Dimension(600, 200));
            comparePanel.add(barChartPanel);

            JFreeChart pieChart = edu.fusion.common.ui.Charts.createSelectionPieChart(names, selections);
            ChartPanel pieChartPanel = new ChartPanel(pieChart);
            pieChartPanel.setPreferredSize(new Dimension(600, 200));
            comparePanel.add(pieChartPanel);

            chartTabPane.addTab("学院对比", comparePanel);
        }

        if (!topCourses.isEmpty()) {
            JPanel topPanel = new JPanel(new BorderLayout(4, 4));
            topPanel.setBackground(BG_COLOR);

            JFreeChart topChart = edu.fusion.common.ui.Charts.createTopCoursesChart(topCourses);
            ChartPanel topChartPanel = new ChartPanel(topChart);
            topChartPanel.setPreferredSize(new Dimension(600, 260));
            topPanel.add(topChartPanel, BorderLayout.CENTER);

            chartTabPane.addTab("课程热度 TOP10", topPanel);
        }

        if (chartTabPane.getTabCount() > 0) {
            chartContainer.add(chartTabPane, BorderLayout.CENTER);
        }

        // 详情文本
        StringBuilder builder = new StringBuilder();
        builder.append("=== 统计报表 ===\n")
                .append("总学生数: ").append(totalStudents).append("\n")
                .append("总课程数: ").append(totalCourses).append("\n")
                .append("总选课数: ").append(totalSelections).append("\n")
                .append("共享课程数: ").append(totalSharedCourses).append("\n\n")
                .append("--- 学院详情 ---\n");

        for (int i = 0; i < collegeNames.size(); i++) {
            builder.append(collegeNames.get(i))
                    .append(": 学生 ").append(collegeStudents.get(i))
                    .append(", 课程 ").append(collegeCourses.get(i))
                    .append(", 选课 ").append(collegeSelections.get(i)).append("\n");
        }

        builder.append("\n--- 热门课程 TOP10 ---\n");
        for (int i = 0; i < topCourses.size(); i++) {
            CourseHeat h = topCourses.get(i);
            builder.append(i + 1).append(". ")
                    .append(h.getCourseId()).append(" ")
                    .append(h.getCourseName())
                    .append(" (").append(h.getCollege()).append("学院)")
                    .append(" - ").append(h.getSelectedCount()).append("人选修\n");
        }

        detailArea.setText(builder.toString());
        detailArea.setCaretPosition(0);

        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private static int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl common`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add common/src/main/java/edu/fusion/common/ui/StatisticsPanel.java
git commit -m "feat(ui): add StatisticsPanel with chart tabs and detail area"
```

---

### Task 6: 重构 CollegeDashboardFrame（添加 JTabbedPane）

**Files:**
- Modify: `common/src/main/java/edu/fusion/common/ui/CollegeDashboardFrame.java`

- [ ] **Step 1: 修改 initUi() 方法，引入 JTabbedPane 和 StatisticsPanel**

需要做的修改：
1. 在类中添加两个新字段：
   ```java
   private final JTabbedPane tabbedPane = new JTabbedPane();
   private final StatisticsPanel statisticsPanel = new StatisticsPanel();
   ```

2. 将 `initUi()` 中的中心面板构建部分拆分：
   - 原有内容放入 "课程操作" Tab
   - StatisticsPanel 放入 "数据统计" Tab

3. 修改 `statsButton` 的 ActionListener：
   ```java
   statsButton.addActionListener(e -> {
       queryStatistics();
       tabbedPane.setSelectedIndex(1);
   });
   ```

4. 修改 `queryStatistics()` 方法：
   ```java
   private void queryStatistics() {
       String responseXml = sendRequest(buildRequest("statistics"));
       if (responseXml != null) {
           statisticsPanel.loadStatistics(responseXml);
       }
   }
   ```

具体修改如下：

```java
// === In class fields (after pageSize declaration) ===
private final JTabbedPane tabbedPane = new JTabbedPane();
private final StatisticsPanel statisticsPanel = new StatisticsPanel();
```

修改 `initUi()` 中 buttonPanel 之后、tableControlPanel 之前的代码。将以下原有结构：

```java
JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
JPanel northControl = new JPanel(new BorderLayout(8, 8));
northControl.add(buttonPanel, BorderLayout.NORTH);
northControl.add(tableControlPanel, BorderLayout.SOUTH);
centerPanel.add(northControl, BorderLayout.NORTH);

JPanel dataPanel = new JPanel(new GridLayout(2, 1, 8, 8));
dataPanel.add(new JScrollPane(courseTable));
dataPanel.add(new JScrollPane(outputArea));
centerPanel.add(dataPanel, BorderLayout.CENTER);

JPanel root = new JPanel(new BorderLayout(8, 8));
root.add(topPanel, BorderLayout.NORTH);
root.add(centerPanel, BorderLayout.CENTER);
setContentPane(root);
```

替换为：

```java
// Tab 1: 课程操作面板（原有全部内容）
JPanel coursePanel = new JPanel(new BorderLayout(8, 8));
JPanel northControl = new JPanel(new BorderLayout(8, 8));
northControl.add(buttonPanel, BorderLayout.NORTH);
northControl.add(tableControlPanel, BorderLayout.SOUTH);
coursePanel.add(northControl, BorderLayout.NORTH);

JPanel dataPanel = new JPanel(new GridLayout(2, 1, 8, 8));
dataPanel.add(new JScrollPane(courseTable));
dataPanel.add(new JScrollPane(outputArea));
coursePanel.add(dataPanel, BorderLayout.CENTER);

// Tab 2: 数据统计面板（新）
tabbedPane.addTab("课程操作", coursePanel);
tabbedPane.addTab("数据统计", statisticsPanel);

JPanel root = new JPanel(new BorderLayout(8, 8));
root.add(topPanel, BorderLayout.NORTH);
root.add(tabbedPane, BorderLayout.CENTER);
setContentPane(root);
```

同时修改 `statsButton` 的 ActionListener：

```java
statsButton.addActionListener(e -> {
    queryStatistics();
    tabbedPane.setSelectedIndex(1);
});
```

替换原有的：
```java
statsButton.addActionListener(e -> queryStatistics());
```

修改 `queryStatistics()` 方法，替换原有实现：

```java
private void queryStatistics() {
    String responseXml = sendRequest(buildRequest("statistics"));
    if (responseXml != null) {
        statisticsPanel.loadStatistics(responseXml);
    }
}
```

删除原有的 `renderStatistics()` 方法（已由 `StatisticsPanel.loadStatistics()` 替代）。

需要新增的 import：
```java
import javax.swing.JTabbedPane;
```

`JTabbedPane` 在 `javax.swing` 包中，和 `JButton`、`JPanel` 等在同一个包下。

- [ ] **Step 2: 编译验证**

Run: `mvn compile -pl common`
Expected: BUILD SUCCESS

- [ ] **Step 3: 完整编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add common/src/main/java/edu/fusion/common/ui/CollegeDashboardFrame.java
git commit -m "feat(ui): refactor dashboard with JTabbedPane and statistics tab"
```

---

### Task 7: 更新测试

**Files:**
- Modify: `integration-server/src/test/java/edu/fusion/integration/service/IntegrationServerTest.java`

- [ ] **Step 1: 读取现有测试文件**

Run: `cat integration-server/src/test/java/edu/fusion/integration/service/IntegrationServerTest.java`

确认现有的 statistics 测试内容。

- [ ] **Step 2: 扩展 statistics 测试，验证 colleges 节点**

在现有的 statistics 测试方法中，添加对 `<colleges>` 节点的断言：

```java
@Test
public void testStatisticsContainsCollegeBreakdown() {
    String requestXml = "<request><type>statistics</type></request>";
    Result<Document> result = integrationServer.processRequestXml(requestXml);
    assertTrue(result.isSuccess());
    Document doc = result.getData();
    Element root = doc.getRootElement();
    Element stats = (Element) root.getElementsByTagName("statistics").item(0);
    
    // Verify colleges node exists and has 3 entries
    NodeList collegesList = stats.getElementsByTagName("colleges");
    assertEquals(1, collegesList.getLength());
    Element colleges = (Element) collegesList.item(0);
    NodeList collegeEntries = colleges.getElementsByTagName("college");
    assertEquals(3, collegeEntries.getLength());
    
    // Verify each college has required fields
    for (int i = 0; i < collegeEntries.getLength(); i++) {
        Element c = (Element) collegeEntries.item(i);
        assertNotNull(c.getElementsByTagName("code").item(0));
        assertNotNull(c.getElementsByTagName("students").item(0));
        assertNotNull(c.getElementsByTagName("courses").item(0));
        assertNotNull(c.getElementsByTagName("selections").item(0));
        assertNotNull(c.getElementsByTagName("sharedCourses").item(0));
    }
}
```

需要新增的 import：
```java
import org.w3c.dom.NodeList;
```

- [ ] **Step 3: 运行测试验证**

Run: `mvn test -pl integration-server -am`
Expected: 现有测试全部通过 + 新测试通过

- [ ] **Step 4: 运行全部测试确认无回归**

Run: `mvn clean test`
Expected: 31 tests passed (原有 30 + 新增 1)

- [ ] **Step 5: 提交**

```bash
git add integration-server/src/test/java/edu/fusion/integration/service/IntegrationServerTest.java
git commit -m "test: verify statistics XML response includes college breakdown"
```

---

### 执行摘要

| 步骤 | 文件 | 操作 |
|------|------|------|
| Task 1 | pom.xml, common/pom.xml | 添加 JFreeChart 依赖 |
| Task 2 | IntegrationServer.java | 添加 `<colleges>` 节点 |
| Task 3 | Charts.java | 新建 - 三种图表工厂 |
| Task 4 | StatsCardPanel.java | 新建 - 四张数字卡片 |
| Task 5 | StatisticsPanel.java | 新建 - 统计主面板 |
| Task 6 | CollegeDashboardFrame.java | 重构 - 引入 JTabbedPane |
| Task 7 | IntegrationServerTest.java | 扩展 - 验证 colleges 节点 |
