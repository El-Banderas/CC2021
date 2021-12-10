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
                }

                catch (IOException ex) {
                    ex.printStackTrace();
                }

                System.out.println("Novo cliente conectado " + cliente.getInetAddress().getHostAddress());

                try {

                    out = new PrintWriter(cliente.getOutputStream(),true);
                    in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                    String input;

                    while ((input = in.readLine()) != null) {

                        String link = input.substring(5);

                        try {

                            URL url = new URL(link);
                            BufferedReader readURL = new BufferedReader(new InputStreamReader(url.openStream()));
                            BufferedWriter writeURL = new BufferedWriter(new FileWriter("")); // precisa da pasta pedida como o argumento 1 de FFSync.
                            String linha;

                            while ((linha = readURL.readLine()) != null) {
                                writeURL.write(linha);
                            }

                            readURL.close();
                            writeURL.close();

                        }
                        catch (MalformedURLException exception) {
                            System.out.println("Malformed URL Exception raised");
                        }
                        catch (IOException i) {
                            System.out.println("IOException raised");
                        }

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