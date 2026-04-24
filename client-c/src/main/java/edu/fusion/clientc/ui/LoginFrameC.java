package edu.fusion.clientc.ui;

import edu.fusion.common.model.Role;
import edu.fusion.common.ui.CollegeDashboardFrame;
import edu.fusion.serverc.service.AuthServiceC;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class LoginFrameC extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"学生", "管理员"});
    private final AuthServiceC authService = new AuthServiceC();

    public LoginFrameC() {
        setTitle("C学院教务系统登录");
        setSize(360, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.add(new JLabel("用户名"));
        panel.add(usernameField);
        panel.add(new JLabel("密码"));
        panel.add(passwordField);
        panel.add(new JLabel("身份"));
        panel.add(roleBox);
        panel.add(new JLabel("学院"));
        panel.add(new JLabel("C"));

        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(e -> login());
        panel.add(new JLabel(""));
        panel.add(loginButton);

        setContentPane(panel);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        Role role = "管理员".equals(roleBox.getSelectedItem()) ? Role.ADMIN : Role.STUDENT;

        boolean ok = authService.authenticate(username, password, role);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CollegeDashboardFrame dashboard = new CollegeDashboardFrame(
                role == Role.ADMIN ? "C学院管理员端" : "C学院学生端",
                "C",
                "http://localhost:8080/api/xml",
                username);
        dashboard.setVisible(true);
        dispose();
    }
}
