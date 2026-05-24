package gui;

import config.AppConfig;
import database.DBConnection;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatFrame {

    private static final Color BACKGROUND_COLOR = new Color(243, 246, 249);
    private static final Color CHAT_BACKGROUND = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(33, 115, 70);
    private static final Color ERROR_COLOR = new Color(179, 38, 30);

    private final String username;
    private final Map<String, StringBuffer> chats = new ConcurrentHashMap<>();

    private Frame frame;
    private TextArea textArea;
    private java.awt.List userList;
    private TextField messageField;
    private Button sendButton;
    private Label statusLabel;
    private volatile boolean closing;

    private volatile String selectedUser;
    private volatile Socket socket;
    private volatile PrintWriter out;
    private volatile BufferedReader in;

    public ChatFrame(String username) {
        this.username = username;

        buildUi();
        connectToServer();
        loadUsers();

        frame.setVisible(true);
    }

    private void buildUi() {
        Font chatFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 14);

        frame = new Frame("ChatApp - " + username);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setBackground(BACKGROUND_COLOR);
        frame.setSize(760, 520);
        centerFrame(frame);

        Panel topPanel = new Panel(new BorderLayout(10, 0));
        topPanel.setBackground(BACKGROUND_COLOR);

        Label welcomeLabel = new Label("Logged in as: " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        Button refreshButton = new Button("Refresh");
        refreshButton.setFont(boldFont);
        refreshButton.addActionListener(event -> {
            if (!isConnected()) {
                connectToServer();
            }
            loadUsers();
        });

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(chatFont);
        textArea.setBackground(CHAT_BACKGROUND);

        userList = new java.awt.List();
        userList.setFont(chatFont);
        userList.setBackground(CHAT_BACKGROUND);
        userList.setPreferredSize(new Dimension(180, 400));
        userList.addItemListener(event -> {
            selectedUser = userList.getSelectedItem();
            loadChatHistory();
            updateSendState();
        });

        Panel userPanel = new Panel(new BorderLayout(0, 6));
        userPanel.setBackground(BACKGROUND_COLOR);

        Label userLabel = new Label("Users");
        userLabel.setFont(boldFont);
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(userList, BorderLayout.CENTER);

        Panel centerPanel = new Panel(new BorderLayout(10, 0));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(userPanel, BorderLayout.WEST);
        centerPanel.add(textArea, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        statusLabel = new Label("Connect to the server and select a user to begin.");
        statusLabel.setFont(chatFont);
        statusLabel.setForeground(new Color(60, 60, 60));

        messageField = new TextField();
        messageField.setFont(chatFont);

        sendButton = new Button("Send");
        sendButton.setFont(boldFont);
        sendButton.setBackground(ACCENT_COLOR);
        sendButton.setForeground(Color.WHITE);

        ActionListener sendAction = (ActionEvent event) -> sendCurrentMessage();
        sendButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction);

        Panel inputPanel = new Panel(new BorderLayout(8, 0));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        Panel bottomPanel = new Panel(new BorderLayout(0, 8));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                closeApplication();
            }
        });

        updateSendState();
    }

    private void connectToServer() {
        if (isConnected()) {
            return;
        }

        String host = AppConfig.getString("server.host", "CHATAPP_SERVER_HOST", "localhost");
        int port = AppConfig.getInt("server.port", "CHATAPP_SERVER_PORT", 5000);

        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(username);

            setStatus("Connected to chat server.", ACCENT_COLOR);
            startReaderThread();
        } catch (IOException | IllegalStateException e) {
            disconnectFromServer("Server offline. Start ChatServer for live messaging.");
        }

        updateSendState();
    }

    private void startReaderThread() {
        Thread readerThread = new Thread(() -> {
            try {
                String serverMessage;

                while ((serverMessage = in.readLine()) != null) {
                    handleServerMessage(serverMessage);
                }
            } catch (IOException e) {
                if (isConnected()) {
                    e.printStackTrace();
                }
            } finally {
                disconnectFromServer("Connection closed.");
            }
        }, "chat-reader-" + username);

        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void handleServerMessage(String serverMessage) {
        if (serverMessage.startsWith("MESSAGE|")) {
            String[] parts = serverMessage.split("\\|", 4);
            if (parts.length < 4) {
                return;
            }

            String sender = parts[1];
            String receiver = parts[2];
            String message = parts[3];
            String chatPartner = sender.equals(username) ? receiver : sender;
            String label = sender.equals(username) ? "You" : sender;

            StringBuffer chat = chats.computeIfAbsent(chatPartner, key -> new StringBuffer());
            chat.append(label).append(" : ").append(message).append("\n");

            if (chatPartner.equals(selectedUser)) {
                renderChat(chatPartner);
            }

            setStatus("Message saved.", ACCENT_COLOR);
            return;
        }

        if (serverMessage.startsWith("INFO|")) {
            String[] parts = serverMessage.split("\\|", 2);
            String message = parts.length == 2 ? parts[1] : "Connected.";
            setStatus(message, ACCENT_COLOR);
            return;
        }

        if (serverMessage.startsWith("ERROR|")) {
            String[] parts = serverMessage.split("\\|", 2);
            String message = parts.length == 2 ? parts[1] : "Server error.";
            setStatus(message, ERROR_COLOR);
            disconnectFromServer(message);
        }
    }

    private void loadUsers() {
        List<String> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT username FROM users WHERE username <> ? ORDER BY username")) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(rs.getString("username"));
                }
            }
        } catch (SQLException | IllegalStateException e) {
            setStatus("Unable to load users. Check the database setup.", ERROR_COLOR);
            textArea.setText("The user list could not be loaded.");
            updateSendState();
            e.printStackTrace();
            return;
        }

        userList.removeAll();
        for (String user : users) {
            userList.add(user);
        }

        if (users.isEmpty()) {
            selectedUser = null;
            textArea.setText("No other users are available. Run database/schema.sql or add another user.");
            setStatus("No chat partner available.", ERROR_COLOR);
            updateSendState();
            return;
        }

        if (selectedUser == null || !users.contains(selectedUser)) {
            selectedUser = users.get(0);
        }

        userList.select(users.indexOf(selectedUser));
        loadChatHistory();

        if (isConnected()) {
            setStatus("Select a user and start chatting.", ACCENT_COLOR);
        } else {
            setStatus("Saved chats are available, but the live server is offline.", ERROR_COLOR);
        }
    }

    private void loadChatHistory() {
        if (selectedUser == null) {
            textArea.setText("Select a user to view messages.");
            updateSendState();
            return;
        }

        StringBuffer chat = new StringBuffer();
        String query = "SELECT sender, message FROM messages "
                + "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, selectedUser);
            ps.setString(3, selectedUser);
            ps.setString(4, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sender = rs.getString("sender");
                    String message = rs.getString("message");
                    String label = sender.equals(username) ? "You" : sender;

                    chat.append(label).append(" : ").append(message).append("\n");
                }
            }
        } catch (SQLException | IllegalStateException e) {
            textArea.setText("Unable to load chat history.");
            setStatus("Could not load messages from the database.", ERROR_COLOR);
            e.printStackTrace();
            updateSendState();
            return;
        }

        chats.put(selectedUser, chat);
        renderChat(selectedUser);
        updateSendState();
    }

    private void renderChat(String chatPartner) {
        StringBuffer chat = chats.get(chatPartner);
        String chatText = (chat == null || chat.length() == 0)
                ? "No messages yet with " + chatPartner + "."
                : chat.toString();

        EventQueue.invokeLater(() -> {
            textArea.setText(chatText);
            textArea.setCaretPosition(textArea.getText().length());
        });
    }

    private void sendCurrentMessage() {
        String receiver = selectedUser;
        String message = messageField.getText().trim();

        if (receiver == null) {
            setStatus("Select a user first.", ERROR_COLOR);
            return;
        }

        if (message.isEmpty()) {
            return;
        }

        if (out == null) {
            setStatus("Server offline. Start ChatServer and press Refresh.", ERROR_COLOR);
            updateSendState();
            return;
        }

        out.println("SEND|" + receiver + "|" + message);
        if (out.checkError()) {
            disconnectFromServer("Message could not be sent. Reconnect to the server.");
            return;
        }

        messageField.setText("");
        setStatus("Sending message...", ACCENT_COLOR);
    }

    private void updateSendState() {
        boolean canSend = isConnected() && selectedUser != null;
        sendButton.setEnabled(canSend);
        messageField.setEnabled(canSend);
    }

    private boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && out != null && in != null;
    }

    private void disconnectFromServer(String statusMessage) {
        BufferedReader currentIn = in;
        PrintWriter currentOut = out;
        Socket currentSocket = socket;

        in = null;
        out = null;
        socket = null;

        try {
            if (currentSocket != null && !currentSocket.isClosed()) {
                currentSocket.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (currentIn != null) {
                currentIn.close();
            }
        } catch (IOException ignored) {
        }

        if (currentOut != null) {
            currentOut.close();
        }

        if (!closing && statusMessage != null) {
            setStatus(statusMessage, ERROR_COLOR);
            updateSendState();
        }
    }

    private void setStatus(String message, Color color) {
        EventQueue.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        });
    }

    private void closeApplication() {
        if (closing) {
            return;
        }

        closing = true;
        disconnectFromServer(null);
        frame.setVisible(false);
        frame.dispose();
        System.exit(0);
    }

    private void centerFrame(Frame currentFrame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - currentFrame.getWidth()) / 2;
        int y = (screenSize.height - currentFrame.getHeight()) / 2;
        currentFrame.setLocation(Math.max(x, 0), Math.max(y, 0));
    }
}
