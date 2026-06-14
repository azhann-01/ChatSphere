package gui;

import database.DBConnection;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class LoginFrame {

    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginFrame() {

        frame = new JFrame("ChatApp Login");
        frame.setSize(340, 290);
        frame.setLayout(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Private Chat Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBounds(20, 35, 300, 30);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30, 85, 120, 24);

        usernameField = new JTextField();
        usernameField.setBounds(30, 110, 280, 30);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 145, 120, 24);

        passwordField = new JPasswordField();
        passwordField.setBounds(30, 170, 280, 30);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(30, 210, 90, 30);

        messageLabel = new JLabel("");
        messageLabel.setBounds(130, 210, 180, 30);
        messageLabel.setForeground(Color.RED);

        loginButton.addActionListener(e -> login());

        usernameField.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        frame.add(titleLabel);
        frame.add(userLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(messageLabel);

        frame.setVisible(true);
    }

    private void login() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Both fields are required.");
            return;
        }

        try {

            boolean isValid = DBConnection.checkLogin(username, password);

            if (isValid) {

                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setText("Login successful.");

                frame.dispose();

                SwingUtilities.invokeLater(() ->
                        new ChatFrame(username));

            } else {

                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Invalid username or password.");
            }

        } catch (IllegalStateException | SQLException ex) {

            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Check database setup.");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(LoginFrame::new);
    }
}