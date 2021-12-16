import java.io.*;
import java.net.*;

public class TCP {
    public static void main(String[] args) {
        ServerSocket servidor = null;

        try {
            servidor = new ServerSocket(Constantes.OfficialPort);
            servidor.setReuseAddress(true);

            while (true) {

                Socket cliente = servidor.accept();

                System.out.println("Novo cliente connectado : " + cliente.getInetAddress().getHostAddress());

                ClientHandler clienteSocket = new ClientHandler(cliente);

                new Thread(clienteSocket).start();

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (servidor != null) {
                try {
                    servidor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket client;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;

            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                while((line = in.readLine()) != null) {

                    System.out.println(line);

                    // Step 1 : Have the StringBuilder obtain the IP address given as argument.
                    StringBuilder HTML = null;
                    HTML.append(line);

                    // Step 2 : Get directory given from the IP and obtain all the files in it through InfoLs, an array of Strings. While I'm at it,clean the StringBuilder.
                    File dir = new File(HTML.toString());
                    String[] InfoLs = dir.list();
                    HTML.delete(0,HTML.length());

                    //Step 3 : Create the start of the HTML file.

                    String startHTML = new String("<html>\n <head>\n </head>\n <body>\n");
                    HTML.append(startHTML);

                    //Step 4 : Create the body of the HTML file( AKA, the files from the IP address )

                    for (int i = 0; i <= InfoLs.length; i++) {

                    }

                    // start of html file.
                    // body of html file.
                    // Pires DLC.
                    // end of html file.

                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        client.close();
                    }
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}