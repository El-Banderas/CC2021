import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;

public class StartConnection {
    public StartConnection() {
    }



    public void startConnection(InetAddress destIP, DatagramSocket sendSocket){
        System.out.println("[Start Connection]: Datagram send: ");
        System.out.println("From: "+sendSocket.getLocalPort() + " to: "+sendSocket.getPort());
        boolean connected = false;
        try {
            int timeBetweenHellos = 1000;
            //Bytes para enviar
            byte[] tryConnection = new byte[1];
            tryConnection[0] = (byte) Constantes.SYN;

            //Bytes para receber
            byte[] receiveFile = new byte[10]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);


            DatagramSocket receiveSocket = new DatagramSocket(Constantes.OfficialPort);
            receiveSocket.setSoTimeout(timeBetweenHellos);
            DatagramPacket receivePacket = new DatagramPacket(receiveFile, receiveFile.length);

        //Enquanto não estiver connectado
        while (!connected){
            //Tenta receber qualquer coisa
            try{
                receiveSocket.receive(receive);
                connected = true; // a packet has been received : stop sending
                System.out.println("[StartConnection]: Houve conexão");
            }
            catch (SocketTimeoutException e) {
                System.out.println("[Start Connection]: Não houve conexão");
            }

            //Envia uma tentativa de conexão
            DatagramPacket helloPacket = new DatagramPacket(tryConnection, tryConnection.length,
                                                            destIP, Constantes.OfficialPort);
            sendSocket.send(helloPacket);
            System.out.println("[StartConnection]: Envia Hello");

            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
}
