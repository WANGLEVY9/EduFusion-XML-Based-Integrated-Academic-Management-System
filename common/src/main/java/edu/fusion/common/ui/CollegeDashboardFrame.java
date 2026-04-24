package edu.fusion.common.ui;

import edu.fusion.common.util.IntegrationXmlHttpClient;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CollegeDashboardFrame extends JFrame {

    private final String collegeCode;
    private final String serviceUrl;
    private final JTextField studentIdField = new JTextField();
    private final JTextField courseIdField = new JTextField();
    private final JTextArea outputArea = new JTextArea();
    private final DefaultTableModel courseTableModel = new DefaultTableModel(new String[]{"课程号", "课程名", "学分", "教师", "地点", "学院", "共享"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable courseTable = new JTable(courseTableModel);

    public CollegeDashboardFrame(String title, String collegeCode, String serviceUrl, String studentId) {
        this.collegeCode = collegeCode;
        this.serviceUrl = serviceUrl;
        setTitle(title);
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        studentIdField.setText(studentId == null ? "" : studentId);
        initUi();
    }

    private void initUi() {
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        topPanel.add(new JLabel("学院"));
        topPanel.add(new JLabel(collegeCode));
        topPanel.add(new JLabel("学生编号"));
        topPanel.add(studentIdField);
        topPanel.add(new JLabel("课程编号"));
        topPanel.add(courseIdField);
        topPanel.add(new JLabel("服务地址"));
        topPanel.add(new JLabel(serviceUrl));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 8, 8));
        JButton queryButton = new JButton("课程查询");
        JButton selectButton = new JButton("共享课程选课");
        JButton myCoursesButton = new JButton("我的选课");
        JButton dropButton = new JButton("退课");
        JButton statsButton = new JButton("统计报表");
        queryButton.addActionListener(e -> queryCourses());
        selectButton.addActionListener(e -> selectCourse());
        myCoursesButton.addActionListener(e -> queryMyCourses());
        dropButton.addActionListener(e -> dropCourse());
        statsButton.addActionListener(e -> queryStatistics());
        buttonPanel.add(queryButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(myCoursesButton);
        buttonPanel.add(dropButton);
        buttonPanel.add(statsButton);

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = courseTable.getSelectedRow();
                if (row < 0) {
                    return;
                }
                String courseId = String.valueOf(courseTableModel.getValueAt(row, 0));
                courseIdField.setText(courseId);
                outputArea.append("\n已选择课程: " + courseId + "，可直接执行选课或退课。\n");
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        JPanel dataPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        dataPanel.add(new JScrollPane(courseTable));
        dataPanel.add(new JScrollPane(outputArea));
        centerPanel.add(dataPanel, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.add(topPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void queryCourses() {
        String responseXml = sendRequest(buildRequest("queryCourses", "college", collegeCode));
        if (responseXml != null) {
            renderCourseList(responseXml, "本院课程");
        }
    }

    private void queryMyCourses() {
        String studentId = requireStudentId();
        if (studentId == null) {
            return;
        }
        String responseXml = sendRequest(buildRequest("myCourses", "college", collegeCode, "studentId", studentId));
        if (responseXml != null) {
            renderCourseList(responseXml, "我的选课");
        }
    }

    private void selectCourse() {
        String studentId = requireStudentId();
        if (studentId == null) {
            return;
        }

        String courseId = courseIdField.getText().trim();
        if (courseId.isEmpty()) {
            String shareXml = sendRequest(buildRequest("shareCourse", "source", collegeCode));
            if (shareXml != null) {
                renderCourseList(shareXml, "可选共享课程");
                JOptionPane.showMessageDialog(this, "已加载可选共享课程，请在课程编号栏输入课程ID后再次点击【共享课程选课】完成选课。", "操作提示", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        courseId = requireCourseId();
        if (courseId == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "确认选课？\n学生：" + studentId + "\n课程：" + courseId,
                "确认选课",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String responseXml = sendRequest(buildRequest("crossSelect", "studentId", studentId, "courseId", courseId));
        if (responseXml != null) {
            renderSimpleResult(responseXml, "选课结果");
            if (isSuccessResponse(responseXml)) {
                queryMyCourses();
            }
        }
    }

    private void dropCourse() {
        String studentId = requireStudentId();
        if (studentId == null) {
            return;
        }
        String courseId = requireCourseId();
        if (courseId == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "确认退课？\n学生：" + studentId + "\n课程：" + courseId,
                "确认退课",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String responseXml = sendRequest(buildRequest("dropCourse", "studentId", studentId, "courseId", courseId));
        if (responseXml != null) {
            renderSimpleResult(responseXml, "退课结果");
            if (isSuccessResponse(responseXml)) {
                queryMyCourses();
            }
        }
    }

    private void queryStatistics() {
        String responseXml = sendRequest(buildRequest("statistics"));
        if (responseXml != null) {
            renderStatistics(responseXml);
        }
    }

    private String sendRequest(String requestXml) {
        try {
            String responseXml = IntegrationXmlHttpClient.postXml(serviceUrl, requestXml);
            return responseXml;
        } catch (RuntimeException ex) {
            outputArea.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "请求失败", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void renderSimpleResult(String responseXml, String title) {
        clearCourseTable();
        Document document = XmlUtil.parse(responseXml);
        Element root = document.getDocumentElement();
        String success = XmlUtil.childText(root, "success");
        String message = XmlUtil.childText(root, "message");
        StringBuilder builder = new StringBuilder();
        builder.append("=== ").append(title).append(" ===\n")
                .append("状态: ").append("true".equalsIgnoreCase(success) ? "成功" : "失败").append("\n")
                .append("说明: ").append(message).append("\n");
        outputArea.setText(builder.toString());
        outputArea.setCaretPosition(0);
    }

    private void renderCourseList(String responseXml, String title) {
        Document document = XmlUtil.parse(responseXml);
        Element root = document.getDocumentElement();
        String success = XmlUtil.childText(root, "success");
        String message = XmlUtil.childText(root, "message");
        NodeList courses = root.getElementsByTagName("course");
        clearCourseTable();

        StringBuilder builder = new StringBuilder();
        builder.append("=== ").append(title).append(" ===\n")
                .append("状态: ").append("true".equalsIgnoreCase(success) ? "成功" : "失败").append("\n")
                .append("说明: ").append(message).append("\n")
                .append("数量: ").append(courses.getLength()).append("\n")
                .append("提示: 点击上方表格行可自动填充课程编号。\n\n");

        for (int i = 0; i < courses.getLength(); i++) {
            Element course = (Element) courses.item(i);
            String id = textOf(course, "id");
            String name = textOf(course, "name");
            String credit = textOf(course, "credit");
            String teacher = textOf(course, "teacher");
            String location = textOf(course, "location");
            String college = textOf(course, "college");
            String shared = textOf(course, "shared");
            courseTableModel.addRow(new Object[]{id, name, credit, teacher, location, college, shared});

            builder.append(i + 1).append(". ")
                    .append(id)
                    .append(" | ")
                    .append(name)
                    .append(" | 学分:")
                    .append(credit)
                    .append(" | 教师:")
                    .append(teacher)
                    .append(" | 地点:")
                    .append(location)
                    .append(" | 学院:")
                    .append(college)
                    .append("\n");
        }

        outputArea.setText(builder.toString());
        outputArea.setCaretPosition(0);
    }

    private void renderStatistics(String responseXml) {
        clearCourseTable();
        Document document = XmlUtil.parse(responseXml);
        Element root = document.getDocumentElement();
        String success = XmlUtil.childText(root, "success");
        String message = XmlUtil.childText(root, "message");

        NodeList statisticsList = root.getElementsByTagName("statistics");
        StringBuilder builder = new StringBuilder();
        builder.append("=== 统计报表 ===\n")
                .append("状态: ").append("true".equalsIgnoreCase(success) ? "成功" : "失败").append("\n")
                .append("说明: ").append(message).append("\n\n");

        if (statisticsList.getLength() > 0) {
            Element stats = (Element) statisticsList.item(0);
            builder.append("总学生数: ").append(textOf(stats, "totalStudents")).append("\n")
                    .append("总课程数: ").append(textOf(stats, "totalCourses")).append("\n")
                    .append("总选课数: ").append(textOf(stats, "totalSelections")).append("\n")
                    .append("共享课程数: ").append(textOf(stats, "totalSharedCourses")).append("\n\n")
                    .append("热门课程TOP:\n");

            NodeList courses = stats.getElementsByTagName("course");
            for (int i = 0; i < courses.getLength(); i++) {
                Element course = (Element) courses.item(i);
                String id = textOf(course, "id");
                String name = textOf(course, "name");
                String college = textOf(course, "college");
                String selectedCount = textOf(course, "selectedCount");
                courseTableModel.addRow(new Object[]{id, name, "", "", "", college, "TOP " + selectedCount});

                builder.append(i + 1).append(". ")
                        .append(id).append(" | ")
                        .append(name).append(" | ")
                        .append("学院:").append(college).append(" | ")
                        .append("选课数:").append(selectedCount).append("\n");
            }
        }

        outputArea.setText(builder.toString());
        outputArea.setCaretPosition(0);
    }

    private boolean isSuccessResponse(String responseXml) {
        Document document = XmlUtil.parse(responseXml);
        return "true".equalsIgnoreCase(XmlUtil.childText(document.getDocumentElement(), "success"));
    }

    private void clearCourseTable() {
        courseTableModel.setRowCount(0);
    }

    private String textOf(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return "";
        }
        return nodeList.item(0).getTextContent();
    }

    private String buildRequest(String type, String... keyValues) {
        Document document = XmlUtil.createDocument("request");
        Element root = document.getDocumentElement();
        root.appendChild(createElement(document, "type", type));
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            root.appendChild(createElement(document, keyValues[i], keyValues[i + 1]));
        }
        return XmlUtil.toString(document);
    }

    private Element createElement(Document document, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value == null ? "" : value);
        return element;
    }

    private String requireStudentId() {
        String value = studentIdField.getText().trim();
        if (value.isEmpty()) {
            value = JOptionPane.showInputDialog(this, "请输入学生编号");
            if (value != null) {
                value = value.trim();
            }
        }
        if (value == null || value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "学生编号不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        studentIdField.setText(value);
        return value;
    }

    private String requireCourseId() {
        String value = courseIdField.getText().trim();
        if (value.isEmpty()) {
            value = JOptionPane.showInputDialog(this, "请输入课程编号");
            if (value != null) {
                value = value.trim();
            }
        }
        if (value == null || value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "课程编号不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        courseIdField.setText(value);
        return value;
    }
}
