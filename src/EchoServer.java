import java.net.*;
import java.io.*;

public class EchoServer {

    private final String CRLF = "\r\n";
    private ServerSocket serverSocket;
    private ServerSocket dataSocket;
    private Socket incoming;
    private BufferedReader reader;
    private BufferedWriter writer;
    private PrintWriter out;
    int rowCount;
    UsersDB usersDB;
    String lastUsername;
    Window window;


    public EchoServer(UsersDB usersDB, Window window) {
        this.usersDB = usersDB;
        this.window = window;
    }

    public void processRequests() {
        try {
            boolean done = true;
            while (done) {
                String msgFromClient = reader.readLine();
                dispatchMessage(msgFromClient);
                window.msgToTextArea(msgFromClient);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void init() throws IOException {
        serverSocket = new ServerSocket(8189);
        incoming = serverSocket.accept();
        reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(incoming.getOutputStream()));
        out = new PrintWriter(incoming.getOutputStream(), true);
    }

    void dispatchMessage(String message) {
        System.out.println(message);
        window.msgToTextArea(message);
        if (message.trim().equals("QUIT")) {
            sendMessage("221 GOODBYE");
            try {
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        rowCount = usersDB.getRowCount();
        if (message.startsWith("USER")) {
            boolean found = false;
            lastUsername = message.substring(5);
            for(int i = 0; i < rowCount; i++) {
                if (usersDB.getValueAt(i, 0).equals(lastUsername)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                sendMessage("331 user correct");
            } else {
                sendMessage("530 not logged in");
            }

        }

        if (message.startsWith("PASS")) {
            boolean found = false;
            for(int i = 0; i < rowCount; i++) {
                if (usersDB.getValueAt(i, 1).equals(message.substring(5)) &&
                        usersDB.getValueAt(i, 0).equals(lastUsername)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                sendMessage("230 authorized");
            } else {
                sendMessage("332 Incorrect password");
            }

        }

        if (message.startsWith("PASV")) {
            try {
                dataSocket = new ServerSocket(228, 10, Inet4Address.getLocalHost());
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMessage(dataSocket.toString());
        }
    }

    private void sendMessage(String message) {
        out.write(message + CRLF);
        out.flush();
        System.out.println(message);
    }

    void sendWelcomeMessage() {
        sendMessage("220 HELLO");
    }


}