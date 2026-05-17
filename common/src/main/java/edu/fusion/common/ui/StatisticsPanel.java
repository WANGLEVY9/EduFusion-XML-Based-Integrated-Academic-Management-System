package edu.fusion.common.ui;

import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.util.XmlUtil;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.BorderFactory;
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

    /**
     * Parse statistics XML response and populate all chart/card/detail components.
     */
    public void loadStatistics(String responseXml) {
        Document doc = XmlUtil.parse(responseXml);
        Element root = doc.getDocumentElement();

        if (!isSuccess(root)) {
            showError(extractMessage(root));
            return;
        }

        Element stats = findStats(root);
        if (stats == null) {
            showEmptyState();
            return;
        }

        int[] totals = extractTotals(stats);
        cardPanel.setData(totals[0], totals[1], totals[2], totals[3]);

        List<CollegeData> colleges = extractCollegeData(stats);
        List<CourseHeat> topCourses = extractTopCourses(stats);

        buildChartTabs(colleges, topCourses);
        buildDetailText(totals, colleges, topCourses);
    }

    // ─── XML helpers ───

    private static boolean isSuccess(Element root) {
        return "true".equalsIgnoreCase(XmlUtil.childText(root, "success"));
    }

    private static String extractMessage(Element root) {
        return XmlUtil.childText(root, "message");
    }

    private static Element findStats(Element root) {
        NodeList list = root.getElementsByTagName("statistics");
        return list.getLength() > 0 ? (Element) list.item(0) : null;
    }

    private static int[] extractTotals(Element stats) {
        return new int[]{
                parseInt(XmlUtil.childText(stats, "totalStudents")),
                parseInt(XmlUtil.childText(stats, "totalCourses")),
                parseInt(XmlUtil.childText(stats, "totalSelections")),
                parseInt(XmlUtil.childText(stats, "totalSharedCourses"))
        };
    }

    private static List<CollegeData> extractCollegeData(Element stats) {
        List<CollegeData> result = new ArrayList<>();
        NodeList collegesNodes = stats.getElementsByTagName("colleges");
        if (collegesNodes.getLength() == 0) return result;

        NodeList collegeList = ((Element) collegesNodes.item(0)).getElementsByTagName("college");
        for (int i = 0; i < collegeList.getLength(); i++) {
            Element c = (Element) collegeList.item(i);
            result.add(new CollegeData(
                    XmlUtil.childText(c, "code") + "学院",
                    parseInt(XmlUtil.childText(c, "students")),
                    parseInt(XmlUtil.childText(c, "courses")),
                    parseInt(XmlUtil.childText(c, "selections"))
            ));
        }
        return result;
    }

    private static List<CourseHeat> extractTopCourses(Element stats) {
        List<CourseHeat> result = new ArrayList<>();
        NodeList topNodes = stats.getElementsByTagName("topCourses");
        if (topNodes.getLength() == 0) return result;

        NodeList courseList = ((Element) topNodes.item(0)).getElementsByTagName("course");
        for (int i = 0; i < courseList.getLength(); i++) {
            Element c = (Element) courseList.item(i);
            result.add(new CourseHeat(
                    XmlUtil.childText(c, "id"),
                    XmlUtil.childText(c, "name"),
                    XmlUtil.childText(c, "college"),
                    parseInt(XmlUtil.childText(c, "selectedCount"))
            ));
        }
        return result;
    }

    // ─── UI building ───

    private void buildChartTabs(List<CollegeData> colleges, List<CourseHeat> topCourses) {
        chartContainer.removeAll();
        chartTabPane.removeAll();

        if (colleges.size() >= 2) {
            JPanel comparePanel = new JPanel(new GridLayout(2, 1, 4, 4));
            comparePanel.setBackground(BG_COLOR);

            String[] names = colleges.stream().map(c -> c.name).toArray(String[]::new);
            int[] students = colleges.stream().mapToInt(c -> c.students).toArray();
            int[] courses = colleges.stream().mapToInt(c -> c.courses).toArray();
            int[] selections = colleges.stream().mapToInt(c -> c.selections).toArray();

            JFreeChart barChart = Charts.createCollegeCompareChart(names, students, courses, selections);
            ChartPanel barChartPanel = new ChartPanel(barChart);
            barChartPanel.setPreferredSize(new Dimension(600, 200));
            comparePanel.add(barChartPanel);

            JFreeChart pieChart = Charts.createSelectionPieChart(names, selections);
            ChartPanel pieChartPanel = new ChartPanel(pieChart);
            pieChartPanel.setPreferredSize(new Dimension(600, 200));
            comparePanel.add(pieChartPanel);

            chartTabPane.addTab("学院对比", comparePanel);
        }

        if (!topCourses.isEmpty()) {
            JPanel topPanel = new JPanel(new BorderLayout(4, 4));
            topPanel.setBackground(BG_COLOR);

            JFreeChart topChart = Charts.createTopCoursesChart(topCourses);
            ChartPanel topChartPanel = new ChartPanel(topChart);
            topChartPanel.setPreferredSize(new Dimension(600, 260));
            topPanel.add(topChartPanel, BorderLayout.CENTER);

            chartTabPane.addTab("课程热度 TOP10", topPanel);
        }

        if (chartTabPane.getTabCount() > 0) {
            chartContainer.add(chartTabPane, BorderLayout.CENTER);
        }

        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void buildDetailText(int[] totals, List<CollegeData> colleges, List<CourseHeat> topCourses) {
        StringBuilder builder = new StringBuilder();
        builder.append("=== 统计报表 ===\n")
                .append("总学生数: ").append(totals[0]).append("\n")
                .append("总课程数: ").append(totals[1]).append("\n")
                .append("总选课数: ").append(totals[2]).append("\n")
                .append("共享课程数: ").append(totals[3]).append("\n\n")
                .append("--- 学院详情 ---\n");

        for (CollegeData c : colleges) {
            builder.append(c.name)
                    .append(": 学生 ").append(c.students)
                    .append(", 课程 ").append(c.courses)
                    .append(", 选课 ").append(c.selections).append("\n");
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
    }

    // ─── Error / empty states ───

    private void showError(String message) {
        cardPanel.reset();
        chartContainer.removeAll();
        chartContainer.add(new JLabel("统计请求失败：" + message, SwingConstants.CENTER), BorderLayout.CENTER);
        detailArea.setText("统计请求失败：" + message + "\n");
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void showEmptyState() {
        cardPanel.reset();
        chartContainer.removeAll();
        chartContainer.add(new JLabel("没有统计数据", SwingConstants.CENTER), BorderLayout.CENTER);
        detailArea.setText("没有统计数据\n");
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

    // ─── Internal model ───

    private static class CollegeData {
        final String name;
        final int students;
        final int courses;
        final int selections;

        CollegeData(String name, int students, int courses, int selections) {
            this.name = name;
            this.students = students;
            this.courses = courses;
            this.selections = selections;
        }
    }
}
