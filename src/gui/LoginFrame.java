package gui;

import database.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class LoginFrame {

    public LoginFrame() {
        Frame frame = new Frame("ChatApp Login");
        frame.setLayout(null);
        frame.setSize(340, 290);
        frame.setResizable(false);
        centerFrame(frame);

        Label titleLabel = new Label("Private Chat Login", Label.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBounds(20, 35, 300, 30);

        Label userLabel = new Label("Username:");
        userLabel.setBounds(30, 85, 120, 24);

        TextField usernameField = new TextField();
        usernameField.setBounds(30, 110, 280, 28);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setBounds(30, 145, 120, 24);

        TextField passwordField = new TextField();
        passwordField.setEchoChar('*');
        passwordField.setBounds(30, 170, 280, 28);

        Button loginButton = new Button("Login");
        loginButton.setBounds(30, 210, 90, 30);

        Label messageLabel = new Label("");
        messageLabel.setBounds(130, 210, 180, 30);
        messageLabel.setForeground(Color.RED);

        ActionListener loginAction = (ActionEvent event) -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

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
                    EventQueue.invokeLater(() -> new ChatFrame(username));
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Invalid username or password.");
                }
            } catch (IllegalStateException | SQLException e) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Check database setup.");
                e.printStackTrace();
            }
        };

        loginButton.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        frame.add(titleLabel);
        frame.add(userLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(messageLabel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    private void centerFrame(Frame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(Math.max(x, 0), Math.max(y, 0));
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
