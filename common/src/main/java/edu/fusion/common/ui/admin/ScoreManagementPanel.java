package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ScoreManagementPanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"学生编号", "课程编号", "所属学院", "成绩"}, 0) {
        public boolean isCellEditable(int row, int col) { return col == 3; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField scoreField = new JTextField(5);

    public ScoreManagementPanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedScore();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setBorder(BorderFactory.createTitledBorder("成绩录入"));
        form.add(new JLabel("成绩:"));
        form.add(scoreField);
        JButton setBtn = new JButton("录入/修改");
        setBtn.addActionListener(e -> updateScore());
        form.add(setBtn);
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadData());
        form.add(refreshBtn);

        add(form, BorderLayout.SOUTH);

        loadData();
    }

    private void loadSelectedScore() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String score = String.valueOf(tableModel.getValueAt(row, 3));
        if (!"-".equals(score)) scoreField.setText(score);
    }

    private void loadData() {
        String xml = parent.buildRequest("adminListSelections");
        String response = parent.sendRequest(xml);
        if (response == null) return;
        tableModel.setRowCount(0);
        Document doc = XmlUtil.parse(response);
        Element root = doc.getDocumentElement();
        NodeList selections = root.getElementsByTagName("selection");
        for (int i = 0; i < selections.getLength(); i++) {
            Element s = (Element) selections.item(i);
            String scoreStr = XmlUtil.childText(s, "score");
            tableModel.addRow(new Object[]{
                    XmlUtil.childText(s, "studentId"),
                    XmlUtil.childText(s, "courseId"),
                    XmlUtil.childText(s, "college"),
                    scoreStr.isEmpty() ? "-" : scoreStr
            });
        }
    }

    private void updateScore() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "请先选择一条记录"); return; }
        String sid = String.valueOf(tableModel.getValueAt(row, 0));
        String cid = String.valueOf(tableModel.getValueAt(row, 1));
        String scoreText = scoreField.getText().trim();
        if (scoreText.isEmpty()) { JOptionPane.showMessageDialog(this, "请输入成绩"); return; }
        int score;
        try {
            score = Integer.parseInt(scoreText);
            if (score < 0 || score > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "成绩必须是0-100的整数");
            return;
        }
        String xml = parent.buildRequest("adminUpdateScore",
                "studentId", sid, "courseId", cid, "score", String.valueOf(score));
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("成绩录入", ok, parent.extractMessage(response));
            if (ok) { loadData(); scoreField.setText(""); }
        }
    }
}
