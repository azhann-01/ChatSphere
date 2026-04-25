package server;

import config.AppConfig;
import database.DBConnection;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = AppConfig.getInt("server.port", "CHATAPP_SERVER_PORT", 5000);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat server started on port " + port + ".");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {

    private final Socket socket;
    private PrintWriter out;
    private String username;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket clientSocket = socket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            out = writer;

            username = reader.readLine();
            if (username == null || username.trim().isEmpty()) {
                send("ERROR|Username was not provided.");
                return;
            }

            if (ChatServer.clients.putIfAbsent(username, this) != null) {
                send("ERROR|This account is already connected.");
                return;
            }

            System.out.println(username + " connected.");
            send("INFO|Connected to chat server.");

            String message;
            while ((message = reader.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            if (username != null) {
                System.out.println(username + " disconnected.");
            }
        } finally {
            if (username != null) {
                ChatServer.clients.remove(username, this);
            }
        }
    }

    private void handleMessage(String message) {
        if (!message.startsWith("SEND|")) {
            return;
        }

        String[] parts = message.split("\\|", 3);
        if (parts.length < 3) {
            send("ERROR|Invalid message format.");
            return;
        }

        String receiver = parts[1].trim();
        String text = parts[2].trim();

        if (receiver.isEmpty() || text.isEmpty()) {
            return;
        }

        try {
            saveMessage(receiver, text);
        } catch (SQLException | IllegalStateException e) {
            send("ERROR|Unable to save the message.");
            e.printStackTrace();
            return;
        }

        String payload = "MESSAGE|" + username + "|" + receiver + "|" + text;
        send(payload);

        ClientHandler receiverHandler = ChatServer.clients.get(receiver);
        if (receiverHandler != null) {
            receiverHandler.send(payload);
        }
    }

    private void saveMessage(String receiver, String message) throws SQLException {
        String query = "INSERT INTO messages (sender, receiver, message) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, receiver);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    void send(String message) {
        synchronized (this) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
