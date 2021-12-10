import java.io.*;
import java.net.*;

public class TCP implements Runnable {

    @Override
    public void run() {

        BufferedReader in = null;
        PrintWriter out = null;

        try {

            ServerSocket servidor = null;
            try {
                servidor = new ServerSocket(Constantes.OfficialPort);
                servidor.setReuseAddress(true);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

                while (true) {

                    Socket cliente = null;
                    try {
                        cliente = servidor.accept();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("Novo cliente conectado " + cliente.getInetAddress().getHostAddress());

                    try {

                        out = new PrintWriter(cliente.getOutputStream(),true);
                        in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

                        String input;
                        while ((input = in.readLine()) != null) {

                            // Função que dá print do HTML.



                        }

                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        try {
                            if (out != null) out.close();
                            if (in != null) {
                                in.close();
                                cliente.close();
                            }
                        }
                        catch (IOException exc) {
                            exc.printStackTrace();
                        }
                    }

                    new Thread().start();

                }
            }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}