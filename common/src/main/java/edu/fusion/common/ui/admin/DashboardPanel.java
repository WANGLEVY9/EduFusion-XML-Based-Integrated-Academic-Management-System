package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;
import edu.fusion.common.ui.StatsCardPanel;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final StatsCardPanel cardPanel = new StatsCardPanel();

    public DashboardPanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(cardPanel, BorderLayout.NORTH);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoArea.setText("欢迎使用教务管理系统\n"
                + "学院: " + parent.getCollegeCode() + "\n\n"
                + "功能说明:\n"
                + "  学生管理 - 添加/修改/删除学生信息\n"
                + "  课程管理 - 添加/修改/删除课程\n"
                + "  选课管理 - 查看选课记录和执行退课\n"
                + "  成绩管理 - 录入/修改学生成绩\n"
                + "  审计日志 - 查看操作日志\n"
                + "  统计报表 - 查看数据统计图表\n"
                + "  跨学院查看 - 查看其他学院数据");
        topSection.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        add(topSection, BorderLayout.CENTER);
        loadSummary();
    }

    private void loadSummary() {
        String xml = parent.buildRequest("statistics");
        String response = parent.sendRequest(xml);
        if (response != null) {
            Document doc = XmlUtil.parse(response);
            Element root = doc.getDocumentElement();
            if (!"true".equalsIgnoreCase(XmlUtil.childText(root, "success"))) return;
            NodeList statsNodes = root.getElementsByTagName("statistics");
            if (statsNodes.getLength() == 0) return;
            Element stats = (Element) statsNodes.item(0);
            int students = parseInt(XmlUtil.childText(stats, "totalStudents"));
            int courses = parseInt(XmlUtil.childText(stats, "totalCourses"));
            int selections = parseInt(XmlUtil.childText(stats, "totalSelections"));
            int shared = parseInt(XmlUtil.childText(stats, "totalSharedCourses"));
            cardPanel.setData(students, courses, selections, shared);
        }
    }

    private int parseInt(String v) {
        if (v == null || v.trim().isEmpty()) return 0;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return 0; }
    }
}
