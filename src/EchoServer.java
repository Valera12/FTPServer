import java.net.*;
import java.io.*;
import java.io.File;

public class EchoServer {

    private final String CRLF = "\r\n";
    private ServerSocket serverSocket;
    private ServerSocket dataServerSocket;
    private Socket dataSocket;
    private Socket incoming;
    private BufferedReader reader;
    private BufferedWriter writer;
    private PrintWriter out;
    private int rowCount;
    private String dir;
    private UsersDB usersDB;
    private String lastUsername;
    private Window window;


    public EchoServer(UsersDB usersDB, Window window) {
        this.usersDB = usersDB;
        this.window = window;
    }

    public void init() throws IOException {
        serverSocket = new ServerSocket(8189);
        incoming = serverSocket.accept();
        reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(incoming.getOutputStream()));
    }

    private void sendMessage(String message) {
        try {
            writer.write(message + CRLF);
            writer.flush();
            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendWelcomeMessage() {
        sendMessage("220 HELLO");
        window.msgToTextArea("220 HELLO");
    }

    public void processRequests() {
        try {
            boolean done = true;
            while (done) {
                String msgFromClient = reader.readLine();
                dispatchMessage(msgFromClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void dispatchMessage(String message) {
        System.out.println(message);
        //window.msgToTextArea(message);

        commandQuit(message);
        commandPasv(message);
        commandPass(message);
        commandList(message);
        commandChangeWorkDirectory(message);
    }

    private String listOfFiles(String dir) {
        File[] filesList;
        File dirDescr = new File(dir);

        filesList = dirDescr.listFiles();
        StringBuilder builder = new StringBuilder();

        if (filesList != null) {
            for (File file : filesList) {
                builder.append(file.length()).append(" ").append(file.getName()).append("\n");
            }
        }

        return builder.toString();
    }


    void commandPasv(String message) {
        if (message.startsWith("PASV")) {
            try {
                dataServerSocket = new ServerSocket(228, 10, Inet4Address.getLocalHost());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i;
            int j;
            String s = Integer.toBinaryString(dataServerSocket.getLocalPort());
            //System.out.println(s);
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < 16 - s.length(); ++index) {
                builder.append("0");
            }
            builder.append(s);

            i = Integer.parseInt(builder.toString().substring(0, 8), 2);
            System.out.println(i);
            j = Integer.parseInt(builder.toString().substring(8), 2);
            System.out.println(j);
            sendMessage("227 Entering Passive Mode (192,168,0,103," + i + "," + j + ")");
            window.msgToTextArea("227 Entering Passive Mode (192,168,0,103," + i + "," + j + ")");
        }
    }

    void commandUser(String message) {
        rowCount = usersDB.getRowCount();
        if (message.startsWith("USER")) {
            boolean found = false;
            lastUsername = message.substring(5);
            for (int i = 0; i < rowCount; i++) {
                if (usersDB.getValueAt(i, 0).equals(lastUsername)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                sendMessage("331 Password required for " + lastUsername);
                window.msgToTextArea("331 Password required for " + lastUsername);
            } else {
                sendMessage("530 not logged in");
                window.msgToTextArea("530 not logged in");
            }

        }
    }

    void commandQuit(String message) {
        if (message.trim().equals("QUIT")) {
            sendMessage("221 GOODBYE");
            window.msgToTextArea("221 GOODBYE");
            try {
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        commandUser(message);
    }

    void commandPass(String message) {
        if (message.startsWith("PASS")) {
            boolean found = false;
            for (int i = 0; i < rowCount; i++) {
                if (usersDB.getValueAt(i, 1).equals(message.substring(5)) &&
                        usersDB.getValueAt(i, 0).equals(lastUsername)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                sendMessage("230 Logged on");
                window.msgToTextArea("230 Logged on");
            } else {
                sendMessage("332 Incorrect password");
                window.msgToTextArea("332 Incorrect password");
            }
        }
    }

    void commandList(String message) {
        if (message.startsWith("LIST")) {
            try {

                if (message.length() > 5) {
                    dir = message.substring(5);
                    String fileInfo = listOfFiles(dir);
                    sendMessage("150 Opening data chanel for directory listing of \"" + dir + "\"");
                    window.msgToTextArea("150 Opening data chanel for directory listing of \"" + dir + "\"");
                    dataSocket = dataServerSocket.accept();
                    out = new PrintWriter(dataSocket.getOutputStream(), true);
                    /*BufferedOutputStream out2 = new BufferedOutputStream(dataSocket.getOutputStream());
                    out2.write(fileInfo.getBytes());
                    out2.flush();
                    out2.close();*/
                    out.write(fileInfo + CRLF);
                    out.close();
                    sendMessage("226 Successfully transferred \"" + dir + "\"");
                    window.msgToTextArea("226 Successfully transferred \"" + dir + "\"");
                } else {
                    dir = "D:\\";
                    String fileInfo = listOfFiles(dir);
                    sendMessage("150 Opening data chanel for directory listing of \"" + dir + "\"");
                    window.msgToTextArea("150 Opening data chanel for directory listing of \"" + dir + "\"");
                    dataSocket = dataServerSocket.accept();
                    out = new PrintWriter(dataSocket.getOutputStream(), true);
                    //out.print(fileInfo + CRLF);
                    out.write(fileInfo + CRLF);
                    out.close();
                    sendMessage("226 Successfully transferred \"" + dir + "\"");
                    window.msgToTextArea("226 Successfully transferred \"" + dir + "\"");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void commandChangeWorkDirectory(String message) {
        if (message.startsWith("CWD")) {
            try {
                dir = message.substring(4);
                sendMessage("ZXC Directory changed to " + dir);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //TODO: доделать команду отправки файли клиенту.
    void commandStor(String message) {
        if (message.startsWith("STOR")) {
            try {
                dataServerSocket = new ServerSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String filenameToStor;
            filenameToStor = message.substring(5);
            listOfFiles(filenameToStor);
        }
    }
}