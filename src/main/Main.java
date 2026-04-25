package main;

import gui.LoginFrame;
import java.awt.EventQueue;

public class Main {

    public static void main(String[] args) {
        EventQueue.invokeLater(LoginFrame::new);
    }
}
