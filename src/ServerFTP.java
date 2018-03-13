import java.net.*;
import java.io.*;
import java.io.File;


class ServerFTP {

    private final String CRLF = "\r\n";
    private ServerSocket dataServerSocket;
    private Socket dataSocket;
    private Socket incoming;
    private BufferedReader reader;
    private BufferedWriter writer;
    private int rowCount;
    private String dir;
    private String cwd;
    private UsersDB usersDB;
    private String lastUsername;
    private String fileName;
    private Window window;


    ServerFTP(UsersDB usersDB, Window window) {
        this.usersDB = usersDB;
        this.window = window;
        this.cwd = "D:\\PRAGRAMMAS\\SImpleServer";
        this.fileName = null;
    }

    void init() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8189);
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

    void processRequests() {
        try {
            //boolean done = true;
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

        commandUser(message);
        commandQuit(message);
        commandPasv(message);
        commandPass(message);
        commandList(message);
        commandChangeWorkDirectory(message);
        commandType(message);
        printPrevWorkingDirectory(message);
        commandStor(message);
        commandRetr(message);
        commandSyst(message);
        commandDele(message);
        commandRemoveDirectory(message);
        commandMakeDirectory(message);
        commandRenameFrom(message);
        commandRenameTo(message);
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
        if (message.startsWith("PASV")) {
            try {
                dataServerSocket = new ServerSocket(228, 10, Inet4Address.getLocalHost());
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
    }

    private void commandUser(String message) {
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

    private void commandQuit(String message) {
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

    }

    private void commandPass(String message) {
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
                } else {
                    dir = cwd;
                }
                String fileInfo = listOfFiles(dir);

                sendMessage("150 Opening data chanel for directory listing of \"" + dir + "\"");
                window.msgToTextArea("150 Opening data chanel for directory listing of \"" + dir + "\"");

                dataSocket = dataServerSocket.accept();

                PrintWriter out = new PrintWriter(dataSocket.getOutputStream(), true);
                out.write(fileInfo + CRLF);
                out.close();
                dataSocket.close();
                dataServerSocket.close();

                sendMessage("226 Successfully transferred \"" + dir + "\"");
                window.msgToTextArea("226 Successfully transferred \"" + dir + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void commandChangeWorkDirectory(String message) {
        if (message.startsWith("CWD")) {
            try {
                dir = message.substring(4);
                sendMessage("250 Directory changed to " + dir);
                cwd = cwd + '/' + dir;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void commandStor(String message) {
        if (message.startsWith("STOR")) {
            sendMessage("150 Opening data chanel");
            File file = new File(message.substring(5));
            try {
                dataSocket = dataServerSocket.accept();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedInputStream output = new BufferedInputStream(dataSocket.getInputStream());
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = output.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                output.close();
                fileOutputStream.close();
                dataSocket.close();
                dataServerSocket.close();
                sendMessage("150 Successfull transfered");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void commandRetr(String message) {
        if (message.startsWith("RETR")) {
            sendMessage("150 Opening data chanel");
            File file = new File(message.substring(5));
            try {
                dataSocket = dataServerSocket.accept();
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
                dataSocket.close();
                dataServerSocket.close();
                sendMessage("150 Successfull transfered");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //TODO: сделать нормальную реализвцию комманды TYPE
    private void commandType(String message) {
        if (message.startsWith("TYPE")) {
            sendMessage("200");
        }
    }

    private void printPrevWorkingDirectory(String message) {
        if (message.startsWith("PWD"))
            sendMessage("257 Current directory" + cwd);
    }

    private void commandSyst(String message) {
        if (message.startsWith("SYST")) {
            String os = System.getProperty("os.name").toLowerCase();
            sendMessage("215 ValeraFTPWindows" + os);
        }
    }

    private void commandDele(String message) {
        if (message.startsWith("DELE")) {
            File file = new File(message.substring(5));
            if (file.delete()) {
                sendMessage("250 Successfully delete");
            } else {
                sendMessage("550 Delete error");
            }
        }
    }


    private void commandRemoveDirectory(String message) {
        if (message.startsWith("RMD")) {
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
    }

    private void commandMakeDirectory(String message) {
        if (message.startsWith("MKD")) {
            new File(cwd + '/' + message.substring(4)).mkdir();
            sendMessage("257 Created successfully");
        }
    }


    private void commandRenameFrom(String message) {
        if (message.startsWith("RNFR")) {
            File file = new File(message.substring(5));
            if (file.exists()) {
                fileName = file.getName();
                sendMessage("350 File to rename: " + fileName);
            }else{
                sendMessage("550 File does not exist");
            }
        }
    }


    private void commandRenameTo(String message) {
        if (message.startsWith("RNTO")) {
            File file = new File(fileName);
            file.renameTo(new File(message.substring(5)));
            sendMessage("250 New name: " + message.substring(5));
        }
    }
}