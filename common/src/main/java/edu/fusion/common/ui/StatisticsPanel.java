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
        String success = XmlUtil.childText(root, "success");

        if (!"true".equalsIgnoreCase(success)) {
            cardPanel.reset();
            chartContainer.removeAll();
            chartContainer.add(new JLabel(
                    "统计请求失败：" + XmlUtil.childText(root, "message"), SwingConstants.CENTER),
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

        // Parse college-level breakdown
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

        // Parse top courses
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

        // Build chart tab pane
        chartContainer.removeAll();
        chartTabPane.removeAll();

        if (collegeNames.size() == 3) {
            JPanel comparePanel = new JPanel(new GridLayout(2, 1, 4, 4));
            comparePanel.setBackground(BG_COLOR);

            String[] names = collegeNames.toArray(new String[0]);
            int[] students = collegeStudents.stream().mapToInt(i -> i).toArray();
            int[] courses = collegeCourses.stream().mapToInt(i -> i).toArray();
            int[] selections = collegeSelections.stream().mapToInt(i -> i).toArray();

            JFreeChart barChart = Charts.createCollegeCompareChart(
                    names, students, courses, selections);
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

        // Detail text output
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
