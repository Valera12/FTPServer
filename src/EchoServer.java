import javax.swing.*;
import java.net.*;
import java.io.*;

public class EchoServer {

    private final String CRLF = "\r\n";
    private ServerSocket serverSocket;
    private Socket incoming;
    private BufferedReader reader;
    private BufferedWriter writer;
    private PrintWriter out;

    private void init() throws IOException {
        serverSocket = new ServerSocket(8189);
        incoming =  serverSocket.accept();
        reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(incoming.getOutputStream()));
        out = new PrintWriter(incoming.getOutputStream(), true);
    }

    private void sendMessage(String message) {
        out.write(message + CRLF);
        out.flush();
        System.out.println(message);
    }

    void sendWelcomeMessage() {
        sendMessage("220 HELLO");
    }

    public static void main(String[] args) {
        EchoServer server = new EchoServer();

        try {
            server.init();
            server.sendWelcomeMessage();

            boolean done = true;
            while (done) {
                String msgFromClient = server.reader.readLine();

                System.out.println(msgFromClient);

                if (msgFromClient.trim().equals("QUIT"))
                    done = false;

                if(msgFromClient.startsWith("USER")) {
                    server.sendMessage("331 user correct");
                }
                if(msgFromClient.startsWith("PASS")) {
                    server.sendMessage("230 authorized");
                }

            }
            server.incoming.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
