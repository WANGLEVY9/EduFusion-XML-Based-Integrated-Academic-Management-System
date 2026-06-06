package edu.fusion.common.ui.admin;

import edu.fusion.common.ui.AdminDashboardFrame;

import javax.swing.*;
import java.awt.*;

public class AuditLogPanel extends JPanel {

    private final AdminDashboardFrame parent;
    private final JTextArea logArea = new JTextArea();

    public AuditLogPanel(AdminDashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton refreshBtn = new JButton("刷新日志");
        refreshBtn.addActionListener(e -> loadLogs());
        topBar.add(refreshBtn);

        add(topBar, BorderLayout.NORTH);

        loadLogs();
    }

    private void loadLogs() {
        String xml = parent.buildRequest("adminAuditLog", "limit", "100");
        String response = parent.sendRequest(xml);
        if (response == null) return;
        logArea.setText("");
        if (response.contains("<logs>")) {
            int start = response.indexOf("<logs>") + 6;
            int end = response.indexOf("</logs>");
            if (start < end) {
                String logs = response.substring(start, end).trim();
                logArea.setText(logs.isEmpty() ? "暂无审计日志" : logs);
                logArea.setCaretPosition(0);
            }
        } else {
            logArea.setText("暂无审计日志");
        }
    }
}
