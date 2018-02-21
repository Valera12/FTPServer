import java.net.*;
import java.io.*;

public class EchoServer {

    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(8189);

            Socket incoming = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);
            out.println("Hello! Enter BYE to exit.");

            boolean done = false;
            while (!done){
             String line = in.readLine();
             if(line == null)
                 done = true;
             else{
                 out.println("Echo: " + line);
                 if(line.trim().equals("BYE"))
                     done = true;
             }
            }
            incoming.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
