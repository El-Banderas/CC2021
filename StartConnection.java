import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Here, we start the connection with another computer, and receive messages from other computers.
 * While we don't connect with the IP address given by the user, we send a SYN message to start connection.
 * Now, we will describe the three types of message we can receive:
 * > SYN - Some computer wants to start a connection with this computer.
 * > getLS - Some computer wants to get the list of files in a given directory.
 * > getFile - Some computer wants to get a file.
 *
 * Also, when this computer receives a message like the ones described above, we check for the authenticity of the sender.
 * That part is described in the report.
 */
public class StartConnection {
    private LogInformation log;

    /**
     * We use the class LogInformation to store the information abouts the connections.
     * Also, this log class is used to access shared variables, like number of threads.
     * @param log
     */
    public StartConnection(LogInformation log) {
        this.log = log;   }

    /**
     * Compares the first part of the message with the Key of Constants class.
     * This way, we check if the sender of this message is trustworthy.
     * @param fullMessage Message received
     * @return Authenticity of the sender of the message
     */
    private boolean trustworthy(byte[] fullMessage){
        byte[] key = new byte[Constantes.startPositionGET];

        System.arraycopy(fullMessage,  0, key, 0, Constantes.startPositionGET);
        String keyTest = new String(key);
        //return false;
        boolean res = Arrays.equals(key, Constantes.keyWordB);
        if (res) System.out.println("[StartConnection] A Mensagem é confiável ");
        else {
            System.out.println("[StartConnection] A Mensagem NÃO é confiável. Descartada ");
            System.out.println("[StartConnection] Recebeu "+keyTest);
        }

        return (res);

    }

    /**
     * This function receives all messages in the port 80, but this can be easily changed in the Constantes class.
     * At start, it tries to connect with the given IP, in Main.
     * When receives a normal SYN, from other PC, it creates a AfterConnection object in a new thread, to handle synchronization.
     * But when it receives a getFile, or getLS, it creates a new thread to handle that request.
     * @param destIP IP given by the user to start synchronization.
     * @param flagPort
     */
    public void startConnection(InetAddress destIP, Constantes flagPort, String folder){
        //connected is used to connect to the ip given in argument.
        //while there is no connection, it sends HelloPackets.
        boolean connected = false;
        log.beginningSide(destIP);
        try {
            //Construction os SYN message with the IP given by the user.
            byte[] tryConnection = new byte[1+Constantes.startPositionGET];
            tryConnection[Constantes.startPositionGET] = (byte) Constantes.SYN;
            System.arraycopy(Constantes.keyWordB,  0, tryConnection, 0, Constantes.startPositionGET);

            //Packet to receive messages
            byte[] receiveFile = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);

            DatagramSocket sendSocket = new DatagramSocket();
            DatagramSocket receiveSocket = new DatagramSocket(flagPort.WhatPort());
            receiveSocket.setSoTimeout(Constantes.timeBetweenHello);
            DatagramPacket helloPacket = new DatagramPacket(tryConnection, tryConnection.length,
                    destIP, flagPort.WhatPortToSend());

        while (true) {
            //While this socket doesn't receive a SYN message, it sends to the givenIP a Syn message.
            if (!connected) {
                sendSocket.send(helloPacket);
                System.out.println("[StartConnection]: Envia Hello");
            }
            //Tries to receive something
            try {
                receiveSocket.receive(receive);
                if (trustworthy(receiveFile)) {
                    if (!connected) sendSocket.send(helloPacket);
                    if (receiveFile[Constantes.startPositionGET] == (byte) Constantes.SynACK || receiveFile[Constantes.startPositionGET] == (byte) Constantes.SYN) {
                        if (!connected) {
                            connected = true; // a packet has been received : stop sending
                            System.out.println("[StartConnection]: Houve conexão");
                            System.out.println("[StartConnection]: Information about other side:");
                            System.out.println("[StartConnection]: " + receive.getPort() + " origem");
                            MainConnection connectedMaind = new MainConnection(receiveSocket, receive, sendSocket, helloPacket, false);
                            log.writeSYNreceived(receive.getPort(), receive.getAddress());
                            AfterConnection connection = new AfterConnection(connectedMaind, log, receive.getAddress(), flagPort, folder);
                            new Thread(connection).start();
                        }

                    } else {
                        //Receive a get File
                        if (receiveFile[Constantes.startPositionGET] == (byte) Constantes.SYNFile) {
                            System.out.println("[StartConnection]: Houve conexão, é um GET");
                            System.out.println("[StartConnection]: Information about other side:");
                            System.out.println("[StartConnection]: " + receive.getPort() + " origem");
                            SendFile.getReceived(receiveFile, receive.getAddress(), receive.getPort(), log);// new MainConnection(receiveSocket, receive, sendSocket, helloPacket, false);
                        } else {
                            //Receive a get LS
                            if (receiveFile[Constantes.startPositionGET] == (byte) Constantes.SYNLs) {
                                System.out.println("[StartConnection]: Houve conexão, é um GET LS");

                                SendLS toSend = new SendLS(receive.getAddress(), receiveFile, flagPort, receive.getPort(), log);
                                toSend.sendLSmain();
                                System.out.println("[StartConnection]: Houve conexão, é um LS");
                            } else {
                                System.out.println("[StartConnection]: RECEIVE, but not SYN or SynACK");
                            }
                        }
                    }
                }
                else {
                    System.out.println("[StartConnection]: Uma mensagem foi descartada");
                }
            }
            //When the socket wait too long to receive something
            catch(SocketTimeoutException e){
                    System.out.println("[StartConnection]: Não houve conexão");
                }

            }

        } catch (SocketException socketException) {
            socketException.printStackTrace();
            System.out.println("[StartConnection]: Erro no socket");

        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("[StartConnection]: Erro no socket");
        }
    }



}
