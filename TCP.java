import java.io.*;
import java.net.*;

public class TCP implements Runnable {

    @Override
    public void run() {
        try {

            ServerSocket servidor = null;
            try {
                servidor = new ServerSocket(Constantes.OfficialPort);
                servidor.setReuseAddress(true);
            } catch (IOException e) {
                e.printStackTrace();

                while (true) {

                    Socket cliente = null;
                    try {
                        cliente = servidor.accept();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("Novo cliente conectado " + cliente.getInetAddress().getHostAddress());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}