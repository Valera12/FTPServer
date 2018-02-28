import javax.swing.*;
import java.awt.*;

public class Window {
    void startWindow(){
        JFrame window = new JFrame("FTP");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(600, 600);
        window.setVisible(true);
        JTextArea textArea = new JTextArea("FFfvbfkvnfdlnvlsdnbsfdnblgbnvckbglbnsgbnsgfnblfsn");
        textArea.setEnabled(true);
        textArea.setSize(500, 500);

        window.add(textArea);
    }
}
