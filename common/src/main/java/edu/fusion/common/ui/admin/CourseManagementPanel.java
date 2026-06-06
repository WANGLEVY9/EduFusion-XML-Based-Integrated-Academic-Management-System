package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CourseManagementPanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"课程号", "课程名", "学分", "教师", "地点", "共享"}, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(10);
    private final JTextField creditField = new JTextField(5);
    private final JTextField teacherField = new JTextField(10);
    private final JTextField locationField = new JTextField(10);
    private final JCheckBox sharedBox = new JCheckBox("共享");

    public CourseManagementPanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("课程信息"));
        form.add(new JLabel("编号:")); form.add(idField);
        form.add(new JLabel("名称:")); form.add(nameField);
        form.add(new JLabel("学分:")); form.add(creditField);
        form.add(new JLabel("教师:")); form.add(teacherField);
        form.add(new JLabel("地点:")); form.add(locationField);
        form.add(sharedBox);

        JButton addBtn = new JButton("添加");
        JButton updateBtn = new JButton("修改");
        JButton deleteBtn = new JButton("删除");
        JButton refreshBtn = new JButton("刷新");

        addBtn.addActionListener(e -> addCourse());
        updateBtn.addActionListener(e -> updateCourse());
        deleteBtn.addActionListener(e -> deleteCourse());
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
        creditField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        teacherField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        locationField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        sharedBox.setSelected("true".equals(String.valueOf(tableModel.getValueAt(row, 5))));
    }

    private void loadData() {
        String xml = parent.buildRequest("queryCourses");
        String response = parent.sendRequest(xml);
        if (response == null) return;
        tableModel.setRowCount(0);
        Document doc = XmlUtil.parse(response);
        Element root = doc.getDocumentElement();
        NodeList courses = root.getElementsByTagName("course");
        for (int i = 0; i < courses.getLength(); i++) {
            Element c = (Element) courses.item(i);
            tableModel.addRow(new Object[]{
                    XmlUtil.childText(c, "id"),
                    XmlUtil.childText(c, "name"),
                    XmlUtil.childText(c, "credit"),
                    XmlUtil.childText(c, "teacher"),
                    XmlUtil.childText(c, "location"),
                    XmlUtil.childText(c, "shared")
            });
        }
    }

    private void addCourse() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "编号和名称为必填项");
            return;
        }
        String xml = parent.buildRequest("adminAddCourse",
                "id", id, "name", name,
                "credit", creditField.getText().trim(),
                "teacher", teacherField.getText().trim(),
                "location", locationField.getText().trim(),
                "shared", String.valueOf(sharedBox.isSelected()));
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("添加课程", ok, parent.extractMessage(response));
            if (ok) { loadData(); clearForm(); }
        }
    }

    private void updateCourse() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "请先选择课程"); return; }
        String xml = parent.buildRequest("adminUpdateCourse",
                "id", id, "name", nameField.getText().trim(),
                "credit", creditField.getText().trim(),
                "teacher", teacherField.getText().trim(),
                "location", locationField.getText().trim(),
                "shared", String.valueOf(sharedBox.isSelected()));
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("修改课程", ok, parent.extractMessage(response));
            if (ok) loadData();
        }
    }

    private void deleteCourse() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "请先选择课程"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除课程 " + id + "？\n如已有学生选课则无法删除。",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        String xml = parent.buildRequest("adminDeleteCourse", "id", id);
        String response = parent.sendRequest(xml);
        if (response != null) {
            boolean ok = parent.isSuccessResponse(response);
            parent.showResult("删除课程", ok, parent.extractMessage(response));
            if (ok) { loadData(); clearForm(); }
        }
    }

    private void clearForm() {
        idField.setText(""); nameField.setText(""); creditField.setText("");
        teacherField.setText(""); locationField.setText(""); sharedBox.setSelected(false);
    }
}
