import javax.swing.*;
import java.awt.*;
import javax.swing.JTable;

public class Window  {
    private JFrame window = new JFrame("FTP");
    private JTextArea textArea = new JTextArea(" ");

    private UsersDB usersDB = new UsersDB();
    private JTable userTable = new JTable(usersDB);

    private void startWindow(){
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setVisible(true);
        window.setLayout(new GridBagLayout());
        textArea.setEnabled(true);
        textArea.setSize(800, 800);
        userTable.setVisible(true);
        userTable.setSize(50,50);
        window.add(textArea);

    }

    void msgToTextArea( String message){
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
