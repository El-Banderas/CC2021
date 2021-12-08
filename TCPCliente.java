import java.io.*;
import java.net.*;
import java.util.*;

public class TCPCliente {
    public static void main(String[] args){

        try (Socket socket = new Socket("TCPHost",8888)) {

            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Scanner scan = new Scanner(System.in);
            String line = null;

            while(!(line.equals("exit"))) {

                line = scan.nextLine();

                out.println(line);out.flush();

                System.out.println("Server replied " + in.readLine());

            }

            scan.close();

        }
        catch (IOException except) {
            except.printStackTrace();
        }
    }
}