import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;


public class SendLS {
    private InetAddress destIP;
    private byte[] messagePacket;
    private Constantes flagPort;
    private int portOtherSide;
    private LogInformation log;

    static public int getPortFromSynLS(byte[] message){
        byte[] destPortByte = new byte[Integer.SIZE];
        System.arraycopy(message,  1+Constantes.startPositionGET, destPortByte, 0, Integer.SIZE);
        return ByteBuffer.wrap(destPortByte).getInt();

    }

    public SendLS(InetAddress destIP, byte[] message, Constantes flagPort, int portOtherSide, LogInformation log) {
        this.destIP = destIP;
        this.messagePacket = message;
        this.flagPort = flagPort;
        this.portOtherSide = portOtherSide;
        this.log = log;
    }

    /**
     * Gets the ip to send the information, and the message of get to see the port to send.
     */
    public void sendLSmain() {
        int readingThisByte = 1+Constantes.startPositionGET;

        //byte[] portByte = new byte[Integer.SIZE];
        //System.arraycopy(message, readingThisByte, portByte, 0, portByte.length);
        //int portOtherSide = ByteBuffer.wrap(portByte).getInt();
        //readingThisByte += Integer.SIZE;


        byte[] sizeFolderNameB = new byte[Integer.SIZE];
        System.arraycopy(messagePacket, readingThisByte, sizeFolderNameB, 0, sizeFolderNameB.length);
        int sizeFolderName = ByteBuffer.wrap(sizeFolderNameB).getInt();
        readingThisByte += Integer.SIZE;


        //Acho que podemos encurtar este tamanho para 2 int's (nºpacotes + porta)  mais um long (last time modiffied)
        byte[] folderB = new byte[sizeFolderName];
        //A String é a mensagem toda exceto o primeiro byte e o a informação da porta
        System.arraycopy(messagePacket, readingThisByte, folderB, 0, sizeFolderName);
        String folder = new String(folderB);
        System.out.println("[StartConnection] Path of file ");
        System.out.println("[StartConnection] " + folder);
       /*
        System.out.println("[getReceived] : Information received from get file");
        System.out.println("[getReceived] : port other side " + portOtherSide);
        System.out.println("[getReceived] : size path " + sizePath);
        System.out.println("[getReceived] : path file " + path);
*/
        // String path = pathByte.toString();
        //path ="/home/banderitas/Desktop/3_ano_1_sem/CC/TransferFiles/src/rfc7231.txt";


        //Array to Send Ack and other information
        int currentPositionToSend = 0;
        byte[] sendACK = new byte[1 + Integer.SIZE ];
        sendACK[currentPositionToSend] = (byte) Constantes.ACKLs;
        currentPositionToSend++;
        try {
            //String folder1 ="/share/src";
            byte[] data = AfterConnection.currentLS(folder,-1);
            int numberPackets = Constantes.calculaNumPacotes(data.length);
            byte[] sizeLS = ByteBuffer.allocate(Integer.SIZE).putInt(data.length).array();
            System.arraycopy(sizeLS, 0, sendACK, currentPositionToSend, sizeLS.length);
            currentPositionToSend += sizeLS.length;


            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.setSoTimeout(Constantes.timeBetweenHello);

            //DatagramSocket receiveSocket = new DatagramSocket();

            DatagramPacket infoFromFileSEND = new DatagramPacket(sendACK, sendACK.length, destIP, portOtherSide);
            byte[] receiveFile = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);

            boolean connected = false;
            sendSocket.send(infoFromFileSEND);
            System.out.println("[sendLS] : Information other side should get");
            System.out.println("[sendLS] : data length " + data.length+ " num pacotes " + numberPackets);

            while (!connected) {
                try {
                    System.out.println("[sendLS]: Envia info");
                    System.out.println("[sendLS]: Espera ACK na porta " + sendSocket.getLocalPort());
                    System.out.println("[sendLS]: Espera ACK na porta " + sendSocket.getPort());
                    sendSocket.send(infoFromFileSEND);
                    sendSocket.receive(receive);

                    if (receiveFile[0] == (byte) Constantes.ACKLs) {
                        connected = true; // a packet has been received : stop sending

                        System.out.println("[sendLS]: Houve conexão");

                        System.out.println("[sendLS]: Information about other side:");
                        System.out.println("[sendLS]: " + receive.getPort() + " origem");
                        //System.out.println("[StartConnection - GET FILE]: " + thisPort + " origem");

                        MainConnection info = new MainConnection(sendSocket, receive, sendSocket, infoFromFileSEND, data, numberPackets);
                        System.out.println("[sendLS] Data: ");
                        System.out.println("[sendLS] "+data);
                        SendArray oneFile = new SendArray(info, log);
                        log.incrementNThreads();
                        new Thread(oneFile).start();

                    } else {
                        System.out.println("[sendLS]: Recebeu outra coisa");
                        System.out.println("[sendLS]: Num: " + (int) receiveFile[0]);

                    }
                }
                catch(SocketTimeoutException e){
                    System.out.println("[sendLS]: Não houve conexão");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[sendLS]: Erro ao abrir ficheiro, acho eu");

        }


    }

    /**
     * Envia para o outro lado o fin. Envia 10 vezes para o caso de não dar  o primeiro.
     * Este fim de conexão não é importante, porque o outro lado já tem o que precisa.
     * @param sendSocket
     * @param receiveSocket
     * @param destIP
     * @throws IOException
     */
    private static void endConnection(DatagramSocket sendSocket, DatagramSocket receiveSocket, InetAddress destIP, int portOtherSide) throws IOException {
       int attempts = 10;
        boolean receiveFin = false;
        byte[] finACKByte = new byte[1];
        finACKByte[0] = (byte) Constantes.FynLS;
        //byte[] lastFinB = new byte[1];
        DatagramPacket finPacket = new DatagramPacket(finACKByte, finACKByte.length,destIP, portOtherSide);

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

