import javax.swing.*;
import java.net.*;
import java.io.*;

public class EchoServer {

    private final String CRLF = "\r\n";
    private ServerSocket serverSocket;
    public ServerSocket dataSocket;
    private Socket incoming;
    private BufferedReader reader;
    private BufferedWriter writer;
    private PrintWriter out;
/*
    EchoServer(){
    }
*/

    private void init() throws IOException {
        serverSocket = new ServerSocket(21);
        incoming =  serverSocket.accept();
        reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(incoming.getOutputStream()));
        out = new PrintWriter(incoming.getOutputStream(), true);
    }

    void dispatchMessage(String message){
        System.out.println(message);
        if (message.trim().equals("QUIT")){
            sendMessage("221 GOODBYE");
            try {
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        if(message.startsWith("USER")) {
            sendMessage("331 user correct");
        }
        if(message.startsWith("PASS")) {
            sendMessage("230 authorized");
        }

        if(message.startsWith("PASV")){
            try {
                dataSocket = new ServerSocket(228,10, Inet4Address.getLocalHost() );
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

    public static void main(String[] args) {
        EchoServer server = new EchoServer();
        Window window = new Window();
        //window.startWindow();

        try {
            server.init();
            server.sendWelcomeMessage();


            boolean done = true;
            while (done) {
                String msgFromClient = server.reader.readLine();
                server.dispatchMessage(msgFromClient);
            }
            server.incoming.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}