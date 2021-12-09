import java.io.*;
import java.net.*;

public class TCP {
    public static void main(String[] args) {

        ServerSocket servidor = null;

        try {

            servidor = new ServerSocket(8888);servidor.setReuseAddress(true);

            while (true) {

                Socket cliente = servidor.accept();

                System.out.println("Novo cliente connetado " + cliente.getInetAddress().getHostAddress());

                ClientHandler clienteSocket = new ClientHandler(cliente);

                new Thread(clienteSocket).start();

            }
        }
        catch (IOException except) {
            except.printStackTrace();
        }
        finally {
            if (servidor != null) {
                try {
                    servidor.close();
                }
                catch (IOException except) {
                    except.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clienteSocket;

        public ClientHandler(Socket socket) {
            this.clienteSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clienteSocket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));

                String linha;
                while (linha = in.readLine() != null) {

                    System.out.printf(" Sent from the client: %s\n",line);
                    out.println(linha);

                }
            }
            catch (IOException expect) {
                except.printStackTrace();
            }
            finally {

                try {
                    if (out != null) {out.close();}
                    if (in != null) {in.close();clienteSocket.close();}
                }
                catch (IOException expect) {
                    except.printStackTrace();
                }

            }
        }
    }
}