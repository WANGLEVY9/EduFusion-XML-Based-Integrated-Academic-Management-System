package edu.fusion.common.ui;

import edu.fusion.common.util.ErrorLogger;
import edu.fusion.common.util.IntegrationXmlHttpClient;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollegeDashboardFrame extends JFrame {

    private final String collegeCode;
    private final String serviceUrl;
    private final JTextField studentIdField = new JTextField();
    private final JTextField courseIdField = new JTextField();
    private final JTextArea outputArea = new JTextArea();
    private final JTextField filterField = new JTextField(22);
    private final JComboBox<String> pageSizeBox = new JComboBox<>(new String[]{"5", "10", "20", "50"});
    private final JLabel pageInfoLabel = new JLabel("第 1 / 1 页");
    private final JButton prevPageButton = new JButton("上一页");
    private final JButton nextPageButton = new JButton("下一页");
    private final JButton exportButton = new JButton("导出CSV");
    private final JLabel statusLabel = new JLabel();
    private final List<Object[]> allRows = new ArrayList<>();
    private final List<Object[]> filteredRows = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = 10;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final StatisticsPanel statisticsPanel = new StatisticsPanel();
    private final DefaultTableModel courseTableModel = new DefaultTableModel(
            new String[]{"课程号", "课程名", "学分", "教师", "地点", "学院", "共享"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable courseTable = new JTable(courseTableModel);

    // Colors
    private static final Color BG_HEADER = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_SUCCESS = new Color(0x27, 0xAE, 0x60);
    private static final Color COLOR_ERROR = new Color(0xE7, 0x4C, 0x3C);
    private static final Color COLOR_INFO = new Color(0x29, 0x80, 0xB9);

    public CollegeDashboardFrame(String title, String collegeCode, String serviceUrl, String studentId) {
        this.collegeCode = collegeCode;
        this.serviceUrl = serviceUrl;
        setTitle(title);
        setSize(950, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        studentIdField.setText(studentId == null ? "" : studentId);
        initUi();
        updateStatus("就绪 - 已连接到集成服务");
    }

    private void updateStatus(String message) {
        statusLabel.setText("学院 " + collegeCode + " | 用户 " + studentIdField.getText().trim()
                + " | " + message);
    }

    // ─────── UI Layout ───────

    private void initUi() {
        // Top: input fields
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 4, 6));
        topPanel.setBackground(BG_HEADER);
        topPanel.add(new JLabel("学院"));
        topPanel.add(new JLabel(collegeCode));
        topPanel.add(new JLabel("学生编号"));
        topPanel.add(studentIdField);
        topPanel.add(new JLabel("课程编号"));
        topPanel.add(courseIdField);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel(""));

        // Enter key in courseIdField → focus studentId for crossSelect
        courseIdField.addActionListener(e -> selectCourse());

        // Buttons: grouped logically
        JPanel queryGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton queryButton = new JButton("课程查询");
        JButton sharedViewButton = new JButton("查看共享课程");
        JButton myCoursesButton = new JButton("我的选课");
        queryButton.addActionListener(e -> queryCourses());
        sharedViewButton.addActionListener(e -> querySharedCourses());
        myCoursesButton.addActionListener(e -> queryMyCourses());
        queryGroup.add(queryButton);
        queryGroup.add(sharedViewButton);
        queryGroup.add(myCoursesButton);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton crossSelectButton = new JButton("跨院选课");
        JButton dropButton = new JButton("退课");
        crossSelectButton.addActionListener(e -> selectCourse());
        dropButton.addActionListener(e -> dropCourse());
        actionGroup.add(crossSelectButton);
        actionGroup.add(dropButton);

        JPanel statsGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton statsButton = new JButton("统计报表");
        statsButton.addActionListener(e -> {
            queryStatistics();
            tabbedPane.setSelectedIndex(1);
        });
        statsGroup.add(statsButton);

        JPanel buttonPanel = new JPanel(new BorderLayout(16, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        buttonPanel.add(queryGroup, BorderLayout.WEST);
        buttonPanel.add(actionGroup, BorderLayout.CENTER);
        buttonPanel.add(statsGroup, BorderLayout.EAST);

        // Table control bar
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        tableControlPanel.add(new JLabel("筛选："));
        tableControlPanel.add(filterField);
        tableControlPanel.add(new JLabel("每页："));
        tableControlPanel.add(pageSizeBox);
        tableControlPanel.add(prevPageButton);
        tableControlPanel.add(nextPageButton);
        tableControlPanel.add(pageInfoLabel);
        tableControlPanel.add(exportButton);

        pageSizeBox.setSelectedItem("10");
        prevPageButton.setEnabled(false);
        nextPageButton.setEnabled(false);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilterAndRefresh(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilterAndRefresh(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilterAndRefresh(); }
        });

        pageSizeBox.addActionListener(e -> {
            Object selected = pageSizeBox.getSelectedItem();
            if (selected != null) {
                pageSize = Integer.parseInt(String.valueOf(selected));
                currentPage = 1;
                refreshTablePage();
            }
        });

        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; refreshTablePage(); }
        });

        nextPageButton.addActionListener(e -> {
            if (currentPage < computeTotalPages()) { currentPage++; refreshTablePage(); }
        });

        exportButton.addActionListener(e -> exportFilteredRows());

        // Output area
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Course table
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setRowHeight(24);
        courseTable.setAutoCreateRowSorter(true);
        courseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        courseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = courseTable.getSelectedRow();
                if (row < 0) return;
                int modelRow = courseTable.convertRowIndexToModel(row);
                String courseId = String.valueOf(courseTableModel.getValueAt(modelRow, 0));
                courseIdField.setText(courseId);
                appendOutput("已选择课程: " + courseId, COLOR_INFO);
            }
        });

        // Tab 1: Course operations
        JPanel coursePanel = new JPanel(new BorderLayout(8, 8));
        JPanel northControl = new JPanel(new BorderLayout(8, 2));
        northControl.add(buttonPanel, BorderLayout.NORTH);
        northControl.add(tableControlPanel, BorderLayout.SOUTH);
        coursePanel.add(northControl, BorderLayout.NORTH);

        JPanel dataPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        dataPanel.add(new JScrollPane(courseTable));
        dataPanel.add(new JScrollPane(outputArea));
        coursePanel.add(dataPanel, BorderLayout.CENTER);

        // Tab 2: Statistics
        tabbedPane.addTab("课程操作", coursePanel);
        tabbedPane.addTab("数据统计", statisticsPanel);

        // Status bar
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        statusLabel.setBackground(BG_HEADER);
        statusLabel.setOpaque(true);

        // Root layout
        JPanel root = new JPanel(new BorderLayout(8, 4));
        root.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        root.add(topPanel, BorderLayout.NORTH);
        root.add(tabbedPane, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ─────── Business operations ───────

    private void queryCourses() {
        String responseXml = sendRequest(buildRequest("queryCourses", "college", collegeCode));
        if (responseXml != null) {
            renderCourseList(responseXml, "本院课程");
            updateStatus("已加载本院课程列表");
        }
    }

    private void queryMyCourses() {
        String studentId = requireStudentId();
        if (studentId == null) return;
        String responseXml = sendRequest(
                buildRequest("myCourses", "college", collegeCode, "studentId", studentId));
        if (responseXml != null) {
            renderCourseList(responseXml, "我的选课");
            updateStatus("已加载 " + studentId + " 的选课列表");
        }
    }

    private void querySharedCourses() {
        String shareXml = sendRequest(buildRequest("shareCourse", "source", collegeCode));
        if (shareXml != null) {
            renderCourseList(shareXml, "可选共享课程");
            updateStatus("已加载其他学院共享课程");
        }
    }

    private void selectCourse() {
        String studentId = requireStudentId();
        if (studentId == null) return;

        String courseId = requireCourseId();
        if (courseId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认选课？\n学生：" + studentId + "\n课程：" + courseId,
                "确认跨院选课",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String responseXml = sendRequest(
                buildRequest("crossSelect", "studentId", studentId, "courseId", courseId));
        if (responseXml != null) {
            boolean ok = isSuccessResponse(responseXml);
            String msg = extractMessage(responseXml);
            showResult("跨院选课结果", ok, msg);
            if (ok) {
                courseIdField.setText("");
                updateStatus(studentId + " 选课 " + courseId + " 成功");
                queryMyCourses();
            } else {
                updateStatus("选课失败: " + msg);
            }
        }
    }

    private void dropCourse() {
        String studentId = requireStudentId();
        if (studentId == null) return;
        String courseId = requireCourseId();
        if (courseId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认退课？\n学生：" + studentId + "\n课程：" + courseId,
                "确认退课",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String responseXml = sendRequest(
                buildRequest("dropCourse", "studentId", studentId, "courseId", courseId));
        if (responseXml != null) {
            boolean ok = isSuccessResponse(responseXml);
            String msg = extractMessage(responseXml);
            showResult("退课结果", ok, msg);
            if (ok) {
                courseIdField.setText("");
                updateStatus(studentId + " 退课 " + courseId + " 成功");
                queryMyCourses();
            } else {
                updateStatus("退课失败: " + msg);
            }
        }
    }

    private void queryStatistics() {
        String responseXml = sendRequest(buildRequest("statistics"));
        if (responseXml != null) {
            statisticsPanel.loadStatistics(responseXml);
            updateStatus("统计报表已加载");
        }
    }

    // ─────── HTTP and XML helpers ───────

    private String sendRequest(String requestXml) {
        try {
            return IntegrationXmlHttpClient.postXml(serviceUrl, requestXml);
        } catch (RuntimeException ex) {
            ErrorLogger.log("client.sendRequest", "serviceUrl=" + serviceUrl, ex);
            outputArea.setText("请求失败: " + ex.getMessage() + "\n请检查服务是否正常运行。");
            JOptionPane.showMessageDialog(this,
                    "请求失败: " + ex.getMessage() + "\n请检查服务是否正常运行。",
                    "网络错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private boolean isSuccessResponse(String responseXml) {
        Document doc = XmlUtil.parse(responseXml);
        return "true".equalsIgnoreCase(XmlUtil.childText(doc.getDocumentElement(), "success"));
    }

    private String extractMessage(String responseXml) {
        Document doc = XmlUtil.parse(responseXml);
        return XmlUtil.childText(doc.getDocumentElement(), "message");
    }

    private void showResult(String title, boolean success, String message) {
        outputArea.setText("");
        appendOutput("=== " + title + " ===", success ? COLOR_SUCCESS : COLOR_ERROR);
        appendOutput("状态: " + (success ? "成功" : "失败"), success ? COLOR_SUCCESS : COLOR_ERROR);
        appendOutput("说明: " + message, success ? COLOR_SUCCESS : COLOR_ERROR);
    }

    private void appendOutput(String text, Color color) {
        javax.swing.text.StyledDocument doc = null;
        if (outputArea.getDocument() instanceof javax.swing.text.StyledDocument) {
            doc = (javax.swing.text.StyledDocument) outputArea.getDocument();
        }
        // Fallback: plain text
        String prefix = outputArea.getText().isEmpty() ? "" : "\n";
        outputArea.append(prefix + text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    // ─────── Table rendering ───────

    private void renderCourseList(String responseXml, String title) {
        Document document = XmlUtil.parse(responseXml);
        Element root = document.getDocumentElement();
        String message = XmlUtil.childText(root, "message");
        NodeList courses = root.getElementsByTagName("course");
        resetRowCache();

        StringBuilder builder = new StringBuilder();
        builder.append("=== ").append(title).append(" ===\n")
                .append(message).append("\n")
                .append("课程数量: ").append(courses.getLength()).append("\n")
                .append("提示: 点击表格行可自动填充课程编号到输入框\n\n");

        for (int i = 0; i < courses.getLength(); i++) {
            Element course = (Element) courses.item(i);
            String id = textOf(course, "id");
            String name = textOf(course, "name");
            String credit = textOf(course, "credit");
            String teacher = textOf(course, "teacher");
            String location = textOf(course, "location");
            String college = textOf(course, "college");
            String shared = textOf(course, "shared");
            allRows.add(new Object[]{id, name, credit, teacher, location, college, shared});

            builder.append(i + 1).append(". ")
                    .append(id).append(" | ").append(name)
                    .append(" | ").append(credit).append("学分")
                    .append(" | ").append(teacher)
                    .append(" | ").append(location)
                    .append(" | ").append(college)
                    .append(" | ").append("true".equals(shared) ? "共享" : "非共享")
                    .append("\n");
        }

        outputArea.setText(builder.toString());
        outputArea.setCaretPosition(0);
        applyFilterAndRefresh();
    }

    // ─────── Pagination & export ───────

    private void clearCourseTable() {
        courseTableModel.setRowCount(0);
    }

    private void resetRowCache() {
        allRows.clear();
        filteredRows.clear();
        clearCourseTable();
        currentPage = 1;
    }

    private void applyFilterAndRefresh() {
        filteredRows.clear();
        String keyword = filterField.getText() == null ? ""
                : filterField.getText().trim().toLowerCase(Locale.ENGLISH);
        for (Object[] row : allRows) {
            if (keyword.isEmpty() || rowMatchKeyword(row, keyword)) {
                filteredRows.add(row);
            }
        }
        currentPage = 1;
        refreshTablePage();
    }

    private boolean rowMatchKeyword(Object[] row, String keyword) {
        for (Object cell : row) {
            if (cell != null && String.valueOf(cell).toLowerCase(Locale.ENGLISH).contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void refreshTablePage() {
        clearCourseTable();
        int total = filteredRows.size();
        int totalPages = computeTotalPages();
        if (currentPage > totalPages) currentPage = totalPages;
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        for (int i = start; i < end; i++) {
            courseTableModel.addRow(filteredRows.get(i));
        }
        pageInfoLabel.setText("第 " + currentPage + " / " + totalPages + " 页（共 " + total + " 条）");
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < totalPages);
    }

    private int computeTotalPages() {
        if (pageSize <= 0) return 1;
        return Math.max(1, (filteredRows.size() + pageSize - 1) / pageSize);
    }

    private void exportFilteredRows() {
        if (filteredRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前无可导出的行，请先查询或调整筛选条件。",
                    "导出提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出CSV");
        String timeTag = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(new Date());
        chooser.setSelectedFile(new File("courses-" + collegeCode + "-" + timeTag + ".csv"));
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,name,credit,teacher,location,college,shared");
            writer.newLine();
            for (Object[] row : filteredRows) {
                writer.write(csvCell(row[0]) + "," + csvCell(row[1]) + ","
                        + csvCell(row[2]) + "," + csvCell(row[3]) + ","
                        + csvCell(row[4]) + "," + csvCell(row[5]) + ","
                        + csvCell(row[6]));
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "导出成功：" + file.getAbsolutePath(),
                    "导出完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ErrorLogger.log("client.exportCsv", "file=" + file.getAbsolutePath(), ex);
            JOptionPane.showMessageDialog(this, "导出失败：" + ex.getMessage(),
                    "导出失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csvCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    // ─────── XML / DOM helpers ───────

    private String textOf(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        return nodeList.getLength() == 0 ? "" : nodeList.item(0).getTextContent();
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

    // ─────── Input validation ───────

    private String requireStudentId() {
        String value = studentIdField.getText().trim();
        if (value.isEmpty()) {
            value = JOptionPane.showInputDialog(this, "请输入学生编号");
            if (value != null) value = value.trim();
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
            if (value != null) value = value.trim();
        }
        if (value == null || value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "课程编号不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        courseIdField.setText(value);
        return value;
    }
}
