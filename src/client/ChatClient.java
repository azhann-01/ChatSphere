package client;

import gui.LoginFrame;
import java.awt.EventQueue;

public class ChatClient {

    public static void main(String[] args) {
        EventQueue.invokeLater(LoginFrame::new);
    }
}
