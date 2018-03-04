import com.sun.deploy.panel.JreTableModel;

import javax.swing.*;
import java.awt.*;
import javax.swing.JTable;

public class Window  {
    private JFrame window = new JFrame("FTP");
    private JTextArea textArea = new JTextArea("FF");

    UsersDB usersDB = new UsersDB();
    JTable userTable = new JTable(usersDB);
    JScrollPane scrollPane = new JScrollPane(userTable);

    void startWindow(){
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setVisible(true);
        window.setLayout(new GridBagLayout());
        textArea.setEnabled(true);
        textArea.setSize(150, 150);
        userTable.setVisible(true);
        userTable.setSize(50,50);
        window.getContentPane().add(scrollPane);
        window.add(textArea);

    }

    void msgToTextArea( String message){
        textArea.append(message + "\n");

    }

    public static void main(String[] args) {
        Window window = new Window();
        EchoServer server = new EchoServer(window.usersDB, window);
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
