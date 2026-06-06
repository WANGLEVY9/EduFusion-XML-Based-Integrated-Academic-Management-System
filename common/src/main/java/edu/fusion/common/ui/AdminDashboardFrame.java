package edu.fusion.common.ui;

import edu.fusion.common.ui.admin.*;
import edu.fusion.common.util.ErrorLogger;
import edu.fusion.common.util.IntegrationXmlHttpClient;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDashboardFrame extends JFrame {

    private final String collegeCode;
    private final String serviceUrl;
    private final String adminUsername;
    private final Runnable logoutCallback;

    private final JLabel statusLabel = new JLabel();
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final JPanel navPanel = new JPanel();
    private final Map<String, JPanel> panels = new LinkedHashMap<>();
    private final JLabel titleLabel = new JLabel();

    private static final Color NAV_BG = new Color(0x2C, 0x3E, 0x50);
    private static final Color NAV_ACTIVE = new Color(0x34, 0x98, 0xDB);
    private static final Color NAV_HOVER = new Color(0x34, 0x49, 0x5E);
    private static final Color BG_HEADER = new Color(0xF5, 0xF7, 0xFA);

    public AdminDashboardFrame(String title, String collegeCode, String serviceUrl,
                                String adminUsername, Runnable logoutCallback) {
        this.collegeCode = collegeCode;
        this.serviceUrl = serviceUrl;
        this.adminUsername = adminUsername;
        this.logoutCallback = logoutCallback;

        setTitle(title);
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUi();
        updateStatus("就绪 - 管理员模式");
    }

    public void updateStatus(String message) {
        statusLabel.setText("学院 " + collegeCode + " | 管理员 " + adminUsername + " | " + message);
    }

    private void initUi() {
        // Header
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        titleLabel.setText("管理员端 - " + collegeCode + "学院");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "确定退出登录？",
                    "退出确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                if (logoutCallback != null) logoutCallback.run();
            }
        });
        header.add(logoutBtn, BorderLayout.EAST);

        // Navigation sidebar
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        navPanel.setPreferredSize(new Dimension(180, 0));

        addNavItem("系统概览", "dashboard");
        addNavItem("学生管理", "students");
        addNavItem("课程管理", "courses");
        addNavItem("选课管理", "selections");
        addNavItem("成绩管理", "scores");
        addNavItem("审计日志", "audit");
        addNavItem("统计报表", "statistics");
        addNavItem("跨学院查看", "cross");

        // Content area
        contentPanel.setBackground(Color.WHITE);

        // Status bar
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        statusLabel.setBackground(BG_HEADER);
        statusLabel.setOpaque(true);

        // Root layout
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.add(navPanel, BorderLayout.WEST);
        body.add(contentPanel, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(root);

        selectPanel("dashboard");
    }

    private void addNavItem(String label, String key) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(NAV_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("navKey", key);
        btn.addActionListener(e -> selectPanel(key));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!NAV_ACTIVE.equals(btn.getBackground()))
                    btn.setBackground(NAV_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!NAV_ACTIVE.equals(btn.getBackground()))
                    btn.setBackground(NAV_BG);
            }
        });
        navPanel.add(btn);
        navPanel.add(Box.createVerticalStrut(2));
    }

    void selectPanel(String key) {
        for (Component c : navPanel.getComponents()) {
            if (c instanceof JButton) {
                c.setBackground(NAV_BG);
            }
        }
        for (Component c : navPanel.getComponents()) {
            if (c instanceof JButton && key.equals(((JButton) c).getClientProperty("navKey"))) {
                c.setBackground(NAV_ACTIVE);
                break;
            }
        }
        if (!panels.containsKey(key)) {
            JPanel panel = createPanel(key);
            if (panel != null) {
                panels.put(key, panel);
                contentPanel.add(panel, key);
            }
        }
        contentLayout.show(contentPanel, key);
        updateStatus(getNavTitle(key));
    }

    private JPanel createPanel(String key) {
        switch (key) {
            case "dashboard": return new DashboardPanel(this);
            case "students": return new StudentManagementPanel(this);
            case "courses": return new CourseManagementPanel(this);
            case "selections": return new SelectionManagementPanel(this);
            case "scores": return new ScoreManagementPanel(this);
            case "audit": return new AuditLogPanel(this);
            case "statistics": return new StatisticsPanel(this::sendStatisticsRequest, true);
            case "cross": return new CrossCollegePanel(this);
            default: return null;
        }
    }

    private String getNavTitle(String key) {
        switch (key) {
            case "dashboard": return "系统概览";
            case "students": return "学生管理";
            case "courses": return "课程管理";
            case "selections": return "选课管理";
            case "scores": return "成绩管理";
            case "audit": return "审计日志";
            case "statistics": return "统计报表";
            case "cross": return "跨学院查看";
            default: return key;
        }
    }

    // ─────── HTTP helpers ───────

    public String sendRequest(String requestXml) {
        try {
            return IntegrationXmlHttpClient.postXml(serviceUrl, requestXml);
        } catch (RuntimeException ex) {
            ErrorLogger.log("admin.sendRequest", "url=" + serviceUrl, ex);
            JOptionPane.showMessageDialog(this,
                    "请求失败: " + ex.getMessage() + "\n请检查服务是否正常运行。",
                    "网络错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public String buildRequest(String type, String... keyValues) {
        Document doc = XmlUtil.createDocument("request");
        Element root = doc.getDocumentElement();
        root.appendChild(createElement(doc, "type", type));
        root.appendChild(createElement(doc, "college", collegeCode));
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            root.appendChild(createElement(doc, keyValues[i], keyValues[i + 1]));
        }
        return XmlUtil.toString(doc);
    }

    public boolean isSuccessResponse(String responseXml) {
        if (responseXml == null || responseXml.isEmpty()) return false;
        Document doc = XmlUtil.parse(responseXml);
        return "true".equalsIgnoreCase(XmlUtil.childText(doc.getDocumentElement(), "success"));
    }

    public String extractMessage(String responseXml) {
        if (responseXml == null || responseXml.isEmpty()) return "";
        Document doc = XmlUtil.parse(responseXml);
        return XmlUtil.childText(doc.getDocumentElement(), "message");
    }

    public void showResult(String title, boolean success, String message) {
        JOptionPane.showMessageDialog(this, message,
                (success ? "操作成功" : "操作失败") + " - " + title,
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    public String getCollegeCode() { return collegeCode; }
    String getServiceUrl() { return serviceUrl; }

    private Element createElement(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        el.setTextContent(value == null ? "" : value);
        return el;
    }

    public void sendStatisticsRequest() {
        String xml = buildRequest("statistics");
        String response = sendRequest(xml);
        if (response != null) {
            StatisticsPanel sp = (StatisticsPanel) panels.get("statistics");
            if (sp != null) sp.loadStatistics(response);
        }
    }
}
