package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentManagementPanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"编号", "姓名", "性别", "专业", "学院"}, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(10);
    private final JComboBox<String> sexBox = new JComboBox<>(new String[]{"M", "F"});
    private final JTextField majorField = new JTextField(10);

    public StudentManagementPanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("学生信息"));
        form.add(new JLabel("编号:")); form.add(idField);
        form.add(new JLabel("姓名:")); form.add(nameField);
        form.add(new JLabel("性别:")); form.add(sexBox);
        form.add(new JLabel("专业:")); form.add(majorField);

        JButton addBtn = new JButton("添加");
        JButton updateBtn = new JButton("修改");
        JButton deleteBtn = new JButton("删除");
        JButton refreshBtn = new JButton("刷新");

        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        refreshBtn.addActionListener(e -> loadData());

        form.add(addBtn);
        form.add(updateBtn);
        form.add(deleteBtn);
        form.add(refreshBtn);

        add(form, BorderLayout.SOUTH);

        loadData();
    }

    private void loadSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        sexBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 2)));
        majorField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    private void loadData() {
        String xml = parent.buildRequest("adminListStudents");
        String response = parent.sendRequest(xml);
        if (response == null) return;
        tableModel.setRowCount(0);
        Document doc = XmlUtil.parse(response);
        Element root = doc.getDocumentElement();
        NodeList students = root.getElementsByTagName("student");
        for (int i = 0; i < students.getLength(); i++) {
            Element s = (Element) students.item(i);
            tableModel.addRow(new Object[]{
                    XmlUtil.childText(s, "id"),
                    XmlUtil.childText(s, "name"),
                    XmlUtil.childText(s, "sex"),
                    XmlUtil.childText(s, "major"),
                    XmlUtil.childText(s, "college")
            });
        }
    }

    private void addStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "编号和姓名为必填项");
            return;
        }
        String xml = parent.buildRequest("adminAddStudent", "id", id,
                "name", name, "sex", (String) sexBox.getSelectedItem(),
                "major", majorField.getText().trim());
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("添加学生", ok, parent.extractMessage(response));
            if (ok) { loadData(); clearForm(); }
        }
    }

    private void updateStudent() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "请先选择学生"); return; }
        String xml = parent.buildRequest("adminUpdateStudent", "id", id,
                "name", nameField.getText().trim(), "sex", (String) sexBox.getSelectedItem(),
                "major", majorField.getText().trim());
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("修改学生", ok, parent.extractMessage(response));
            if (ok) loadData();
        }
    }

    private void deleteStudent() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "请先选择学生"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除学生 " + id + "？\n将同时删除其选课记录。",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        String xml = parent.buildRequest("adminDeleteStudent", "id", id);
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("删除学生", ok, parent.extractMessage(response));
            if (ok) { loadData(); clearForm(); }
        }
    }

    private void clearForm() {
        idField.setText(""); nameField.setText(""); sexBox.setSelectedIndex(0); majorField.setText("");
    }
}
