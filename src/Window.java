import javax.swing.*;
import java.awt.*;

public class Window {
    JFrame window = new JFrame("FTP");
    JTextArea textArea = new JTextArea("FF");

    void startWindow(){
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(600, 600);
        window.setVisible(true);
        textArea.setEnabled(true);
        textArea.setSize(500, 500);
        window.add(textArea);
    }

    void msgToTextArea( String message){
        textArea.append(message);
    }
}
