package com.example.weighing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {
        setUndecorated(true); // 去除原始标题栏
        setSize(1200, 700); // 更大尺寸
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        // 顶部标题栏
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(1200, 40));
        topPanel.setBackground(new Color(240, 240, 240));

        JButton closeButton = new JButton("X");
        closeButton.setBounds(1160, 5, 30, 30);
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(Color.RED);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(e -> System.exit(0));
        topPanel.add(closeButton);

        // 拖动窗口功能
        Point mouseClickPoint = new Point();
        topPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseClickPoint.setLocation(e.getPoint());
            }
        });
        topPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point location = getLocation();
                setLocation(location.x + e.getX() - mouseClickPoint.x,
                        location.y + e.getY() - mouseClickPoint.y);
            }
        });

        // 主内容区（左右布局）
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        // 左侧公司介绍（2/3）
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(800, 660));
        leftPanel.setBackground(new Color(230, 240, 255));

        JLabel companyLabel = new JLabel("公司简介（可替换为图片）", SwingConstants.CENTER);
        companyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 28));
        leftPanel.add(companyLabel, BorderLayout.CENTER);

        // 右侧登录面板（1/3）
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(400, 660));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(100, 40, 40, 40));

        JLabel titleLabel = new JLabel("用户登录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(66, 133, 244));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 悬停颜色变化
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(48, 117, 230));
            }

            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(66, 133, 244));
            }
        });

        // 登录逻辑绑定
        loginButton.addActionListener(this::handleLogin);

        // 回车键触发登录
        KeyAdapter enterKeyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // 组装右侧表单区域
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(25));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(40));
        rightPanel.add(loginButton);

        // 合并左右面板
        contentPanel.add(leftPanel);
        contentPanel.add(rightPanel);

        // 组装到根容器
        rootPanel.add(topPanel, BorderLayout.NORTH);
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    /**
     * 登录验证逻辑
     */
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if ("admin".equals(username) && "123456".equals(password)) {
            SwingUtilities.invokeLater(() -> new WeighingApp().setVisible(true));
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "用户名或密码错误！", "登录失败", JOptionPane.ERROR_MESSAGE);
            shakeWindow();
        }
    }

    /**
     * 登录失败抖动动画
     */
    private void shakeWindow() {
        Point originalLocation = getLocation();
        int shakeDistance = 10;
        int shakeTimes = 8;
        int delay = 25;

        Timer timer = new Timer(delay, null);
        final int[] count = {0};
        timer.addActionListener(e -> {
            int dx = (count[0] % 2 == 0) ? shakeDistance : -shakeDistance;
            setLocation(originalLocation.x + dx, originalLocation.y);
            count[0]++;
            if (count[0] >= shakeTimes) {
                timer.stop();
                setLocation(originalLocation);
            }
        });
        timer.start();
    }
}
