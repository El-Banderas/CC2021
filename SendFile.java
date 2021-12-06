import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SendFile implements Runnable{
    private DatagramSocket here;
    private MainConnection otherSide;
    private InetAddress destIP;

    /**
     * Depois é preciso tirar o x
     * @param mainConnection
     * @param destIP
     */
    public SendFile(MainConnection mainConnection, InetAddress destIP) {
        System.out.println("[SendFile]: "+mainConnection.here.getLocalPort() + " e envia para "+ mainConnection.otherSide.getPort());

        try {
            this.here = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("[SendFile] Erro ao criar socket");
        }
        this.otherSide = mainConnection;
        this.destIP = destIP;
    }





    /**
     * Coisas para fazer
     * Verificar a utilidade de cada porta, em princípio está certo
     * Sliding window
     */

    public void run() {
        byte[] data = new byte[0];


    }
}
