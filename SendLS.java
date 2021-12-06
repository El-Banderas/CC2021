import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class SendLS implements Runnable{
    private InetAddress destIP;
    private byte[] message;
    private Constantes flagPort;

    static public int getPortFromSynLS(byte[] message){
        byte[] destPortByte = new byte[Integer.SIZE];
        System.arraycopy(message,  1, destPortByte, 0, Integer.SIZE);
        return ByteBuffer.wrap(destPortByte).getInt();

    }

    public SendLS(InetAddress destIP, byte[] message, Constantes flagPort) {
        this.destIP = destIP;
        this.message = message;
        this.flagPort = flagPort;
    }

    /**
     * Gets the ip to send the information, and the message of get to see the port to send.
     */
    public void run() {
        int portOtherSide = getPortFromSynLS(this.message);
        try {
            System.out.println("[sendLS]: manda o ls");

            DatagramSocket sendSocket = new DatagramSocket();
            DatagramSocket receiveSocket = new DatagramSocket();
            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);

            byte[] toSend = AfterConnection.currentLS(receiveSocket.getLocalPort());
            DatagramPacket sendLS = new DatagramPacket(toSend, toSend.length,
                    this.destIP, portOtherSide);

          //  sendSocket.send(sendLS);
            receiveSocket.setSoTimeout(Constantes.timeBetweenHello);

            boolean connected = false;
            //Enquanto não receber um ACK do ls que mandou, continua a enviar.
            while (!connected) {
                sendSocket.send(sendLS);
                System.out.println("[sendLS]: Envia ls");
                System.out.println("[sendLS]: Recebe nesta porta "+receiveSocket.getLocalPort());

                try {
                    receiveSocket.receive(receive);
                    //sendSocket.send(sendLS);
                    if (receiveFileInfo[0] == (byte) Constantes.FynLS) {
                        connected = true; // a packet has been received : stop sending
                        System.out.println("[sendLS]: Receive Fin LS");
                        endConnection(sendSocket, receiveSocket, this.destIP, this.flagPort);
                        return;
                    } else {
                        System.out.println("[sendLS]: RECEIVE, but not SYN or SynACK");
                        return;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("[sendLS]: Não houve conexão");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();

            System.out.println("[sendLS]: Erro ao criar socket");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[sendLS]: Erro ao enviar pacote.");
        }
    }

    /**
     * Envia para o outro lado o fin. Envia 10 vezes para o caso de não dar  o primeiro.
     * Este fim de conexão não é importante, porque o outro lado já tem o que precisa.
     * @param sendSocket
     * @param receiveSocket
     * @param destIP
     * @param flagPort
     * @throws IOException
     */
    private static void endConnection(DatagramSocket sendSocket, DatagramSocket receiveSocket, InetAddress destIP, Constantes flagPort) throws IOException {
       int attempts = 10;
        boolean receiveFin = false;
        byte[] finACKByte = new byte[1];
        finACKByte[0] = (byte) Constantes.FynLS;
        //byte[] lastFinB = new byte[1];
        DatagramPacket finPacket = new DatagramPacket(finACKByte, finACKByte.length,destIP, flagPort.WhatPortToSend());

        byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
        DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);

        for (int i = 0; i < attempts; i++ )
        sendSocket.send(finPacket);

        System.out.println("[sendLS]: Send 10 fin packets");
        /*
        while (!receiveFin) {
            try {
                receiveSocket.receive(receive);

                if (receiveFileInfo[0] == (byte) Constantes.FynLS) {
                    receiveFin = true; // a packet has been received : stop sending
                    System.out.println("[sendLS]: Recebeu o fin do outro lado");
                } else {
                    System.out.println("[sendLS]: RECEIVE, but not SYN or SynACK");
                }
            }
            catch (SocketTimeoutException e) {
                System.out.println("[sendLS]: Não recebeu FinLS");
                sendSocket.send(finPacket);

            }
        }
        //Já recebeu o fin
        System.out.println("[sendLS]: Vai enviar último ACK");
        sendSocket.send(ackPacket);
        */
        sendSocket.close();
        receiveSocket.close();
    }

}

