import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;

public class StartConnection {

    public DatagramSocket startConnection(InetAddress destIP, DatagramSocket sendSocket, Constantes flagPort){
        boolean connected = false;
        try {
            int timeBetweenHellos = 1000;
            //Bytes para enviar
            byte[] tryConnection = new byte[1];
            tryConnection[0] = (byte) Constantes.SYN;

            //Bytes para receber
            byte[] receiveFile = new byte[10]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);

            //Define qual a porta em que recebe coisas
            DatagramSocket receiveSocket = new DatagramSocket(flagPort.WhatPort());
            receiveSocket.setSoTimeout(timeBetweenHellos);
            DatagramPacket receivePacket = new DatagramPacket(receiveFile, receiveFile.length);

        //Enquanto não estiver connectado
        while (!connected){

            //Envia uma tentativa de conexão
            DatagramPacket helloPacket = new DatagramPacket(tryConnection, tryConnection.length,
                                                            destIP, flagPort.WhatPortToSend());
            sendSocket.send(helloPacket);
            System.out.println("[StartConnection]: Envia Hello");
            //Tenta receber qualquer coisa
            try{
                receiveSocket.receive(receive);
                sendSocket.send(helloPacket);
                if (receiveFile[0] == (byte) Constantes.SynACK || receiveFile[0] == (byte) Constantes.SYN) {
                    connected = true; // a packet has been received : stop sending
                    System.out.println("[StartConnection]: Houve conexão");
                //    System.out.println("[StartConnection]: Information about other side:");
                //    System.out.println("[StartConnection]: "+receive.getPort()+" origem");

                    return receiveSocket;
                }
                else
                {
                    System.out.println("[StartConnection]: RECEIVE, but not SYN or SynACK");
                    return null;
                }
            }
            catch (SocketTimeoutException e) {
                System.out.println("[StartConnection]: Não houve conexão");
            }

            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            System.out.println("[StartConnection]: Erro no socket");

            return null;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("[StartConnection]: Erro no socket");
            return null;
        }
        return null;
    }
}
