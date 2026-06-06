package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CrossCollegePanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final JComboBox<String> collegeBox = new JComboBox<>(new String[]{"A", "B", "C"});
    private final DefaultTableModel studentModel = new DefaultTableModel(
            new String[]{"编号", "姓名", "性别", "专业"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel courseModel = new DefaultTableModel(
            new String[]{"课程号", "课程名", "学分", "教师", "地点", "共享"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public CrossCollegePanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.add(new JLabel("选择学院:"));
        topBar.add(collegeBox);
        JButton queryBtn = new JButton("查看");
        queryBtn.addActionListener(e -> loadData());
        topBar.add(queryBtn);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        JPanel studentPanel = new JPanel(new BorderLayout());
        JTable studentTable = new JTable(studentModel);
        studentTable.setRowHeight(24);
        studentPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        tabs.addTab("学生列表", studentPanel);

        JPanel coursePanel = new JPanel(new BorderLayout());
        JTable courseTable = new JTable(courseModel);
        courseTable.setRowHeight(24);
        coursePanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        tabs.addTab("课程列表", coursePanel);

        add(tabs, BorderLayout.CENTER);
    }

    private void loadData() {
        String target = (String) collegeBox.getSelectedItem();
        loadStudents(target);
        loadCourses(target);
    }

    private void loadStudents(String target) {
        studentModel.setRowCount(0);
        String xml = "<request><type>adminListStudents</type><college>" + target + "</college></request>";
        String response = parent.sendRequest(xml);
        if (response == null) return;
        Document doc = XmlUtil.parse(response);
        Element root = doc.getDocumentElement();
        NodeList students = root.getElementsByTagName("student");
        for (int i = 0; i < students.getLength(); i++) {
            Element s = (Element) students.item(i);
            studentModel.addRow(new Object[]{
                    XmlUtil.childText(s, "id"),
                    XmlUtil.childText(s, "name"),
                    XmlUtil.childText(s, "sex"),
                    XmlUtil.childText(s, "major")
            });
        }
    }

    private void loadCourses(String target) {
        courseModel.setRowCount(0);
        String xml = parent.buildRequest("queryCourses", "college", target);
        String response = parent.sendRequest(xml);
        if (response == null) return;
        Document doc = XmlUtil.parse(response);
        Element root = doc.getDocumentElement();
        NodeList courses = root.getElementsByTagName("course");
        for (int i = 0; i < courses.getLength(); i++) {
            Element c = (Element) courses.item(i);
            courseModel.addRow(new Object[]{
                    XmlUtil.childText(c, "id"),
                    XmlUtil.childText(c, "name"),
                    XmlUtil.childText(c, "credit"),
                    XmlUtil.childText(c, "teacher"),
                    XmlUtil.childText(c, "location"),
                    XmlUtil.childText(c, "shared")
            });
        }
    }
}
