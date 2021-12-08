import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StartConnection {

    /**
     * 1:36 - This function receives everything. But, at start, it tries to connect with a given destIP. AfterConnection takes care of share folders.
     * If there is a connection, it creates a new thread and keeps receiving packets.
     * For each new connection/request, it creates a new thread.
     *
     *Falta ler ls's
     *
     * This functions returns information about connections to the main port.
     * When receive a normal SYN, from other PC, it creates a general MainConnection.
     * But when it get a getFile, it creates a seperate MainConnection.
     * @param destIP
     * @param flagPort
     * @return
     */
    public MainConnection startConnection(InetAddress destIP, Constantes flagPort){
        //connected is used to connect to the ip given in argument.
        //while there is no connection, it sends HelloPackets.
        boolean connected = false;
        try {
            //Bytes para enviar
            byte[] tryConnection = new byte[1];
            tryConnection[0] = (byte) Constantes.SYN;

            //Bytes para receber
            byte[] receiveFile = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);

            //Define qual a porta em que recebe coisas
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramSocket receiveSocket = new DatagramSocket(flagPort.WhatPort());
            receiveSocket.setSoTimeout(Constantes.timeBetweenHello);
            DatagramPacket helloPacket = new DatagramPacket(tryConnection, tryConnection.length,
                    destIP, flagPort.WhatPortToSend());

            //Enquanto não estiver connectado
        while (true){
            //Envia uma tentativa de conexão
            if (!connected) {
                sendSocket.send(helloPacket);
                System.out.println("[StartConnection]: Envia Hello");
            }
            //Tenta receber qualquer coisa
            try{
                receiveSocket.receive(receive);
                if (!connected) sendSocket.send(helloPacket);
                if (receiveFile[0] == (byte) Constantes.SynACK || receiveFile[0] == (byte) Constantes.SYN) {
                    if (!connected) {
                        connected = true; // a packet has been received : stop sending
                        System.out.println("[StartConnection]: Houve conexão");
                        System.out.println("[StartConnection]: Information about other side:");
                        System.out.println("[StartConnection]: " + receive.getPort() + " origem");
                        MainConnection connectedMaind = new MainConnection(receiveSocket, receive, sendSocket, helloPacket, false);
                        AfterConnection connection = new AfterConnection(connectedMaind, destIP, flagPort);
                        new Thread(connection).start();
                    }

                }
                else
                {
                    if (receiveFile[0] == (byte) Constantes.SYNFile) {
                        connected = true; // a packet has been received : stop sending
                        System.out.println("[StartConnection]: Houve conexão, é um GET");
                        System.out.println("[StartConnection]: Information about other side:");
                        System.out.println("[StartConnection]: "+receive.getPort()+" origem");

                        getReceived(receiveFile, destIP);// new MainConnection(receiveSocket, receive, sendSocket, helloPacket, false);
                    }
                    else {
                        if (receiveFile[0] == (byte) Constantes.SYNLs) {
                            System.out.println("[StartConnection]: Houve conexão, é um GET LS");

                            SendLS toSend = new SendLS(receive.getAddress(), receiveFile, flagPort);
                            new Thread(toSend).start();
                            connected = true; // a packet has been received : stop sending
                            System.out.println("[StartConnection]: Houve conexão, é um LS");
                        }
                        else {
                            System.out.println("[StartConnection]: RECEIVE, but not SYN or SynACK");
                            return null;
                        }
                    }
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
    }


    private byte[] lastTimeModified(File f){
        long lastTime = f.lastModified();
        return ByteBuffer.allocate(Long.SIZE).putLong(lastTime).array();
    }
    /**
     * When a get is received, we read the information from the packet: port from the get to where we need to send; and the Path to the file.
     * And the port to the getFile send ACK's.
     * Message:
     * AckFile | total number of packets | last time modified | port to the other side communicate (ack's).
     * @param message
     * @param destIP
     * @return
     */
    private void getReceived(byte[] message, InetAddress destIP){
        int readingThisByte = 1;

        byte[] portByte = new byte[Integer.SIZE];
        System.arraycopy(message, readingThisByte, portByte, 0, portByte.length);
        int portOtherSide = ByteBuffer.wrap(portByte).getInt();
        readingThisByte += Integer.SIZE;


        byte[] sizePathB = new byte[Integer.SIZE];
        System.arraycopy(message, readingThisByte, sizePathB, 0, sizePathB.length);
        int sizePath = ByteBuffer.wrap(sizePathB).getInt();
        readingThisByte += Integer.SIZE;


        //Acho que podemos encurtar este tamanho para 2 int's (nºpacotes + porta)  mais um long (last time modiffied)
        byte[] pathByte = new byte[sizePath];
        //A String é a mensagem toda exceto o primeiro byte e o a informação da porta
        System.arraycopy(message, readingThisByte, pathByte, 0, sizePath);
        String path = new String(pathByte);
       /*
        System.out.println("[getReceived] : Information received from get file");
        System.out.println("[getReceived] : port other side " + portOtherSide);
        System.out.println("[getReceived] : size path " + sizePath);
        System.out.println("[getReceived] : path file " + path);
*/
       // String path = pathByte.toString();
        path ="/home/banderitas/Desktop/3_ano_1_sem/CC/TransferFiles/src/rfc7231.txt";
        File f = new File(path);
        Path pathP = Paths.get(path);

        //Array to Send Ack and other information
        int currentPositionToSend = 0;
        byte[] sendACK = new byte[1 + 2 * Integer.SIZE + Long.SIZE];
        sendACK[currentPositionToSend] = (byte) Constantes.ACKFile;
        currentPositionToSend++;
        try {
            byte[] data = Files.readAllBytes(pathP);
            int numberPackets = data.length / (Constantes.maxSizePacket-Constantes.positionInPacketToSendFile);
            byte[] numberPacketsBytes = ByteBuffer.allocate(Integer.SIZE).putInt(numberPackets).array();
            System.arraycopy(numberPacketsBytes, 0, sendACK, currentPositionToSend, numberPacketsBytes.length);
            currentPositionToSend += numberPacketsBytes.length;
            byte[] lastTimeMod = lastTimeModified(f);
            System.arraycopy(lastTimeMod, 0, sendACK, currentPositionToSend, lastTimeMod.length);
            currentPositionToSend += lastTimeMod.length;

            DatagramSocket sendSocket = new DatagramSocket();
            DatagramSocket receiveSocket = new DatagramSocket();

            int thisPort = receiveSocket.getLocalPort();
            byte[] thisPortBytes = ByteBuffer.allocate(Integer.SIZE).putInt(thisPort).array();
            System.arraycopy(thisPortBytes, 0, sendACK, currentPositionToSend, thisPortBytes.length);
            currentPositionToSend += thisPortBytes.length;

            DatagramPacket infoFromFileSEND = new DatagramPacket(sendACK, sendACK.length, destIP, portOtherSide);
            byte[] receiveFile = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);

            boolean connected = false;
            sendSocket.send(infoFromFileSEND);
        /*
            System.out.println("[getReceived] : Information other side should get");
            System.out.println("[getReceived] : num pacotes " + numberPackets);
*/

            while (!connected) {

                System.out.println("[StartConnection - GET]: Envia info");
                receiveSocket.receive(receive);
                sendSocket.send(infoFromFileSEND);

                if (receiveFile[0] == (byte) Constantes.ACKFile) {
                       System.out.println("[StartConnection - GET]: Houve conexão");

                       System.out.println("[StartConnection - GET]: Information about other side:");
                       System.out.println("[StartConnection - GET]: " + receive.getPort() + " origem");

                       MainConnection info = new MainConnection(receiveSocket, receive, sendSocket, infoFromFileSEND, data);
                       SendFile oneFile = new SendFile(info, destIP);
                       new Thread(oneFile).start();

                       connected = true; // a packet has been received : stop sending
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
