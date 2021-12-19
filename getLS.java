import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Send a get to the main door of the other IP. Then, it receives the ls information and returns to the AfterConnection.
 * Also, it sends an ACK message to the other side, confirming it received the information.
 * Falta fazer isso
 */
public class getLS {

    static public int getPortFromBytes(byte[] message){
        byte[] destPortByte = new byte[Integer.SIZE];
        System.arraycopy(message,  1, destPortByte, 0, Integer.SIZE);
        return ByteBuffer.wrap(destPortByte).getInt();

    }

    private static int handleACK(byte[] messageACK){
        byte[] arrayBytes = new byte[Integer.SIZE];
        System.arraycopy(messageACK,  1, arrayBytes, 0, Integer.SIZE);
       int numberOfBytes = ByteBuffer.wrap(arrayBytes).getInt();

       int numberPackets = Constantes.calculaNumPacotes(numberOfBytes);
        //System.arraycopy(messageACK,  1+Integer.SIZE+Long.SIZE, arrayBytes, 0, Integer.SIZE);
        //this.portOtherSide = ByteBuffer.wrap(arrayBytes).getInt();

        System.out.println("[Files.GetFile] Read this information from Sender");
        System.out.println("[Files.GetFile] num pack: " + numberPackets);
        return numberOfBytes;
    }

    /**
     * Main function that connects to the other computer and returns the files in the given folder.
     * The description of this function is in the report.
     * @param destIP
     * @param flagPort
     * @param folder
     * @return
     * @throws NotConnectedExcpetion
     */
    public static byte[] getLS(InetAddress destIP, Constantes flagPort, String folder, LogInformation log) throws NotConnectedExcpetion {
        boolean connected = false;
        try {
            int timeBetweenHellos = 1000;
            //Bytes para receber

            //Define qual a porta em que recebe coisas
// Talvez seja preciso meter um número no receive
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramPacket synLs = GetArray.packetOfSYN(false, folder, destIP, flagPort);
            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);


            sendSocket.setSoTimeout(timeBetweenHellos);
            //  DatagramPacket receivePacket = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);
            //sendSocket.send(synFile);
            //Enquanto não estiver connectado
            while (!connected){
                System.out.println("[getLS] O outro lado deve mostrar:");
                System.out.println("[getLS] porta deste lado " + sendSocket.getLocalPort());
                System.out.println("[getLS] path " + folder);
                //Envia uma tentativa de conexão
                sendSocket.send(synLs);
                System.out.println("[getLS]: Envia Get");
                //Tenta receber qualquer coisa
                try{
                    sendSocket.receive(receive);
                    //   sendSocket.send(synFile);

                    if (receiveFileInfo[0] == (byte) Constantes.ACKLs) {
                        //Envia ACK de que recebeu tamanho do ficheiro

                        connected = true; // a packet has been received : stop sending
                        System.out.println("[getLS]: Houve conexão");
                        System.out.println("[getLS]: Information about other side:");
                        System.out.println("[getLS]: "+receive.getPort()+" origem");
                        int sizeMessage = handleACK(receiveFileInfo);
                        int portOtherSide = receive.getPort();
                        //O MainConnection receberá  o Path

                        //  sendSocket.send(ackFile);
                        //  sendSocket.send(ackFile);
                        //  sendSocket.send(ackFile);
                       // GetArray toGet = new GetArray(sendSocket, destIP, Constantes.calculaNumPacotes(sizeMessage), sizeMessage, portOtherSide, log);
                        MainConnection connection = new MainConnection(sendSocket, receive, sendSocket, synLs, false);
                        GetArray toGet = new GetArray(connection, log, Constantes.calculaNumPacotes(sizeMessage), sizeMessage);

                        return toGet.receiveFile(false);
                       // return receiveFile(new MainConnection(sendSocket, receive, sendSocket, synLs, true));
                    }
                    else
                    {
                        System.out.println("[getLS]: RECEIVE, but not SYN or SynACK");
                        return null;
                    }
                }
                catch (SocketTimeoutException e) {
                    System.out.println("[getLS]: Não houve conexão");
                }

            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            System.out.println("[getLS]: Erro no socket");

            return null;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("[getLS]: Erro no socket");
            return null;
        }
        return null;
    }


    /**
     * Tenta receber o fin do outro lado.
     * Se tentar 10 vezes e não conseguir, desiste. Porque já tem o que precisa.
     * @param sendSocket
     * @param receiveSocket
     * @param destIP
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



    private static DatagramPacket packetOfSynLs(int localPortToReceive, InetAddress destIP, Constantes flagPort, String folder){
        //Bytes para enviar
        //   byte[] tryConnection = new byte[1+Constantes.startPositionGET];
        //   tryConnection[Constantes.startPositionGET] = (byte) Constantes.SYN;


        int currentByteTry = 0;
        int sizePacket = 1+ Integer.SIZE + Constantes.startPositionGET;

        byte[] tryConnection = new byte[sizePacket];
        byte[] portToReceive = ByteBuffer.allocate(Integer.SIZE).putInt(localPortToReceive).array();

        System.arraycopy(Constantes.keyWordB,  0, tryConnection, 0, Constantes.startPositionGET);
        tryConnection[Constantes.startPositionGET] = (byte) Constantes.SYNLs;
        System.arraycopy(portToReceive, 0, tryConnection, 1+Constantes.startPositionGET, Integer.SIZE);

        DatagramPacket synLS = new DatagramPacket(tryConnection, sizePacket,
                destIP, flagPort.WhatPortToSend());
        return synLS;
    }

}



