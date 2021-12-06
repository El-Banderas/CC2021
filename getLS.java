import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Send a get to the main door of the other IP. Then, it receives the ls information and returns to the AfterConnection.
 * Also, it sends an ACK message to the other side, confirming it received the information.
 * Falta fazer isso
 */
public class getLS {
    private static DatagramPacket packetOfSynLs(int localPortToReceive, InetAddress destIP, Constantes flagPort){
        //Bytes para enviar
        int currentByteTry = 0;
        int sizePacket = 1+ Integer.SIZE;
        byte[] tryConnection = new byte[sizePacket];
        byte[] portToReceive = ByteBuffer.allocate(Integer.SIZE).putInt(localPortToReceive).array();
        System.arraycopy(portToReceive, 0, tryConnection, 1, Integer.SIZE);

         tryConnection[0] = (byte) Constantes.SYNLs;
        DatagramPacket synLS = new DatagramPacket(tryConnection, sizePacket,
                destIP, flagPort.WhatPortToSend());
        return synLS;
    }


    public static byte[] getLS(InetAddress destIP, Constantes flagPort) throws NotConnectedExcpetion {
        try {
            System.out.println("[getLS]: Incia pedido de get");

            DatagramSocket sendSocket = new DatagramSocket();
            System.out.println("[getLS]: Informações do socket");
            System.out.println("[getLS]: esta porta "+sendSocket.getLocalPort());

          //  DatagramSocket receiveSocket = new DatagramSocket();
            DatagramPacket synLS = packetOfSynLs(sendSocket.getLocalPort(), destIP, flagPort);
            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name


            byte[] sendFin = new byte[1]; // Where we store the data of datagram of the name
            sendFin[0] = (byte) Constantes.FynLS; // Where we store the data of datagram of the name


            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);
            sendSocket.setSoTimeout(Constantes.timeBetweenHello);

          //  sendSocket.send(synLS);

            boolean connected = false;
            while (!connected) {
                sendSocket.send(synLS);
                System.out.println("[getLS]: Envia GetLS");
                try {
                    sendSocket.receive(receive);
                 //   sendSocket.send(synLS);
                    if (receiveFileInfo[0] == (byte) Constantes.ACKLs) {
                        connected = true; // a packet has been received : stop sending
                        int portDest = SendLS.getPortFromSynLS(receiveFileInfo);
                        //portDest++;

                        DatagramPacket finLS = new DatagramPacket(sendFin, sendFin.length,
                                destIP, portDest);

                        System.out.println("[getLS]: Houve conexão");
                        System.out.println("[getLS] Send Fin to:");
                        System.out.println("[getLS] " + portDest+ " -1");

                        sendSocket.send(finLS);
                        endConnection(sendSocket, sendSocket, destIP, portDest, finLS);

                        return receiveFileInfo;
                    } else {
                        System.out.println("[getLS]: RECEIVE, but not SYN or SynACK");
                        return null;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("[getLS]: Não houve conexão");
                }


            }
        } catch (SocketException e) {
            e.printStackTrace();

            System.out.println("[getLS]: Erro ao criar socket");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[getLS]: Erro ao enviar pacote.");
        }
        throw new NotConnectedExcpetion("[getLS] Não sei o que se passou");
    }

    /**
     * Tenta receber o fin do outro lado.
     * Se tentar 10 vezes e não conseguir, desiste. Porque já tem o que precisa.
     * @param sendSocket
     * @param receiveSocket
     * @param destIP
     * @param flagPort
     * @param finLS
     */
    private static void endConnection(DatagramSocket sendSocket, DatagramSocket receiveSocket,
                                      InetAddress destIP, int portDest, DatagramPacket finLS) {
        try {
            int attempts = 2;
            boolean finACK = false;
            byte[] lastFinB = new byte[1];
            lastFinB[0] = (byte) Constantes.FynAck;
            DatagramPacket ackPacket = new DatagramPacket(lastFinB, lastFinB.length, destIP, portDest);

            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);

            sendSocket.setSoTimeout(Constantes.timeBetweenHello);


        System.out.println("[getLS]: Start End of connection");
        while (!finACK && attempts > 0) {
            try {

              //  sendSocket.send(finLS);
                sendSocket.receive(receive);
                if (receiveFileInfo[0] == (byte) Constantes.FynLS ) {
                    finACK = true; // a packet has been received : stop sending
                    System.out.println("[getLS]: Recebeu o fin do outro lado");
                    System.out.println("[getLS]: Fecha");

                } else {
                    System.out.println("[getLS]: RECEIVE, but not SYN or SynACK");
                    System.out.println("[getLS]: Vai em " + attempts + " tentativas");
                    sendSocket.send(finLS);
                    attempts--;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("[getLS]: Volta a enviar FIN LS");
                System.out.println("[getLS]: Vai em " + attempts + "tentativas");
                sendSocket.send(finLS);
                attempts--;
            }
        }
            System.out.println("[getLS]: Vai enviar último ACK");
            sendSocket.send(ackPacket);
            sendSocket.close();
            receiveSocket.close();
        }
        catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Já recebeu o fin

    }


}



