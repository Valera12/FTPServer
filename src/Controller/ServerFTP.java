package Controller;

import View.Window;
import Model.UsersDB;

import java.net.*;
import java.io.*;


public class ServerFTP {

    private final String CRLF = "\r\n";
    public static final int PORT = 8189;
    public static final int LOCALPORT = 228;
    public static final int BACKLOG = 10;

    private boolean isASCII;

    private ServerSocket dataServerSocket;
    private Socket dataSocket;
    private Socket incoming;
    private BufferedReader reader;
    private BufferedWriter writer;
    private int rowCount;
    private String dir;
    private String cwd = "D:\\PRAGRAMMAS\\SImpleServer";
    private UsersDB usersDB;
    private String lastUsername;
    private String fileName;
    private Window window;


    public ServerFTP(UsersDB usersDB, Window window) {
        this.usersDB = usersDB;
        this.window = window;
        this.fileName = null;
    }

    public void init() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
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

    public void sendWelcomeMessage() {
        sendMessage("220 HELLO");
        window.msgToTextArea("220 HELLO");
    }

    public void processRequests() {
        try {
            while (true) {
                String msgFromClient = reader.readLine();
                dispatchMessage(msgFromClient);
                window.msgToTextArea(msgFromClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatchMessage(String message) {
        System.out.println(message);
        if (message.startsWith("USER")) {
            commandUser(message);
        } else if (message.startsWith("QUIT")) {
            commandQuit(message);
        } else if (message.startsWith("PASV")) {
            commandPasv(message);
        } else if (message.startsWith("PASS")) {
            commandPass(message);
        } else if (message.startsWith("LIST")) {
            commandList(message);
        } else if (message.startsWith("CWD")) {
            commandChangeWorkDirectory(message);
        } else if (message.startsWith("TYPE")) {
            commandType(message);
        } else if (message.startsWith("PWD")) {
            printPrevWorkingDirectory(message);
        } else if (message.startsWith("STOR")) {
            commandStor(message);
        } else if (message.startsWith("RETR")) {
            commandRetr(message);
        } else if (message.startsWith("SYST")) {
            commandSyst(message);
        } else if (message.startsWith("DELE")) {
            commandDele(message);
        } else if (message.startsWith("RMD")) {
            commandRemoveDirectory(message);
        } else if (message.startsWith("MKD")) {
            commandMakeDirectory(message);
        } else if (message.startsWith("RNFR")) {
            commandRenameFrom(message);
        } else if (message.startsWith("RNTO")) {
            commandRenameTo(message);
        }
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

        return builder.toString().substring(0, builder.toString().length() > 0 ? builder.toString().length() - 1 : 0);
    }


    private void commandPasv(String message) {

            try {
                dataServerSocket = new ServerSocket(LOCALPORT, BACKLOG, Inet4Address.getLocalHost());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int firstEightSymbols;
            int lastSymbols;
            String s = Integer.toBinaryString(dataServerSocket.getLocalPort());
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < 16 - s.length(); ++index) {
                builder.append("0");
            }
            builder.append(s);

            firstEightSymbols = Integer.parseInt(builder.toString().substring(0, 8), 2);
            System.out.println(firstEightSymbols);
            lastSymbols = Integer.parseInt(builder.toString().substring(8), 2);
            System.out.println(lastSymbols);
            String ip = dataServerSocket.getInetAddress().toString();
            sendMessage("227 Entering Passive Mode (" +
                    ip.substring(ip.lastIndexOf('/') + 1).replace('.', ',') + "," +
                    firstEightSymbols + "," + lastSymbols + ")");
            window.msgToTextArea("227 Entering Passive Mode (" +
                    ip.substring(ip.lastIndexOf('/') + 1).replace('.', ',') + "," +
                    firstEightSymbols + "," + lastSymbols + ")");
    }

    private void commandUser(String message) {
        rowCount = usersDB.getRowCount();

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

    private void commandQuit(String message) {

            sendMessage("221 GOODBYE");
            window.msgToTextArea("221 GOODBYE");
            try {
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
    }

    private void commandPass(String message) {
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

    private void commandList(String message) {
            try {
                if (message.length() > 5) {
                    dir = message.substring(5);
                } else {
                    dir = cwd;
                }
                String fileInfo = listOfFiles(dir);

                sendMessage("150 Opening data chanel for directory listing of \"" + dir + "\"");
                window.msgToTextArea("150 Opening data chanel for directory listing of \"" + dir + "\"");

                dataSocket = dataServerSocket.accept();
                if (isASCII == true) {
                    PrintWriter out = new PrintWriter(dataSocket.getOutputStream(), true);
                    out.write(fileInfo + CRLF);
                    out.close();
                    sendMessage("226 Successfully transferred \"" + dir + "\"");

                } else {
                    sendMessage("500 Incorrect mode");
                }

                dataSocket.close();
                dataServerSocket.close();
                window.msgToTextArea("226 Successfully transferred \"" + dir + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void commandChangeWorkDirectory(String message) {
            try {
                dir = message.substring(4);
                cwd = cwd + '/' + dir;
                sendMessage("250 Directory changed to " + dir);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void commandStor(String message) {
            sendMessage("150 Opening data chanel");
            File file = new File(message.substring(5));
            try {
                dataSocket = dataServerSocket.accept();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                if (isASCII == false) {
                    BufferedInputStream output = new BufferedInputStream(dataSocket.getInputStream());

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = output.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    output.close();
                } else {
                    sendMessage("500 Incorrect mode");
                }
                fileOutputStream.close();
                dataSocket.close();
                dataServerSocket.close();
                sendMessage("150 Successfull transfered");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void commandRetr(String message) {
            sendMessage("150 Opening data chanel");
            File file = new File(message.substring(5));
            try {
                dataSocket = dataServerSocket.accept();
                if (isASCII == false) {
                    BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                    BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                    output.close();
                    fileInputStream.close();
                } else {
                    sendMessage("500 Incorrect mode");
                }
                dataSocket.close();
                dataServerSocket.close();
                sendMessage("150 Successfully transfered");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    private void commandType(String message) {
            if (message.substring(5).equals("A")) {
                isASCII = true;
                sendMessage("200 Go to ASCII mode");
            } else {
                isASCII = false;
                sendMessage("200 Go to I mode");
            }
    }

    private void printPrevWorkingDirectory(String message) {
            sendMessage("257 Current directory" + cwd);
    }

    private void commandSyst(String message) {
            String os = System.getProperty("os.name").toLowerCase();
            sendMessage("215 ValeraFTPWindows" + os);
    }

    private void commandDele(String message) {
            File file = new File(message.substring(5));
            if (file.delete()) {
                sendMessage("250 Successfully delete");
            } else {
                sendMessage("550 Delete error");
            }
    }


    private void commandRemoveDirectory(String message) {
            File file = new File(message.substring(4));
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    File f = new File(file, children[i]);
                    f.delete();
                }
                file.delete();
            } else file.delete();
            sendMessage("250 Successfully delete");
    }

    private void commandMakeDirectory(String message) {
            new File(cwd + '/' + message.substring(4)).mkdir();
            sendMessage("257 Created successfully");
    }


    private void commandRenameFrom(String message) {
            File file = new File(message.substring(5));
            if (file.exists()) {
                fileName = file.getName();
                sendMessage("350 File to rename: " + fileName);
            } else {
                sendMessage("550 File does not exist");
            }
    }


    private void commandRenameTo(String message) {
            File file = new File(fileName);
            file.renameTo(new File(message.substring(5)));
            sendMessage("250 New name: " + message.substring(5));
    }
}