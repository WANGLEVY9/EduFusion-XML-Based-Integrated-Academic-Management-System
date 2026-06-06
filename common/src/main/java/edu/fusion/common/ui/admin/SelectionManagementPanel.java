package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SelectionManagementPanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"学生编号", "课程编号", "所属学院", "成绩"}, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField filterField = new JTextField(15);

    public SelectionManagementPanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.add(new JLabel("筛选:"));
        topBar.add(filterField);
        JButton filterBtn = new JButton("查询");
        filterBtn.addActionListener(e -> loadData());
        topBar.add(filterBtn);
        JButton refreshBtn = new JButton("全部");
        refreshBtn.addActionListener(e -> { filterField.setText(""); loadData(); });
        topBar.add(refreshBtn);

        add(topBar, BorderLayout.NORTH);

        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        String xml = parent.buildRequest("adminListSelections");
        String response = parent.sendRequest(xml);
        if (response == null) return;
        tableModel.setRowCount(0);
        Document doc = XmlUtil.parse(response);
        Element root = doc.getDocumentElement();
        NodeList selections = root.getElementsByTagName("selection");
        String keyword = filterField.getText().trim().toLowerCase();
        for (int i = 0; i < selections.getLength(); i++) {
            Element s = (Element) selections.item(i);
            String sid = XmlUtil.childText(s, "studentId");
            String cid = XmlUtil.childText(s, "courseId");
            String college = XmlUtil.childText(s, "college");
            String score = XmlUtil.childText(s, "score");
            if (!keyword.isEmpty() && !sid.toLowerCase().contains(keyword)
                    && !cid.toLowerCase().contains(keyword)) continue;
            tableModel.addRow(new Object[]{sid, cid, college, score.isEmpty() ? "-" : score});
        }
    }
}
