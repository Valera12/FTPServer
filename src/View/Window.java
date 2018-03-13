package View;

import javax.swing.*;
import java.awt.*;
import Model.UsersDB;
import Controller.ServerFTP;

public class Window  {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    private JFrame window = new JFrame("FTP");
    private JTextArea textArea = new JTextArea(" ");

    private UsersDB usersDB = new UsersDB();
    private JTable userTable = new JTable(usersDB);

    private void startWindow(){
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(WIDTH, HEIGHT);
        window.setVisible(true);
        window.setLayout(new GridBagLayout());
        textArea.setEnabled(true);
        textArea.setSize(WIDTH, HEIGHT);
        userTable.setVisible(true);
        window.add(textArea);

    }

    public void msgToTextArea(String message){
        textArea.append(message + "\n");

    }

    public static void main(String[] args) {
        Window window = new Window();
        ServerFTP server = new ServerFTP(window.usersDB, window);
        window.startWindow();
        try {
            server.init();
            server.sendWelcomeMessage();
            server.processRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
