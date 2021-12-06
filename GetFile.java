import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GetFile implements Runnable{
    private DatagramSocket otherSide;
    private InetAddress destIP;
    //Depois eliminar
    private Constantes flagPort;

    //Informação que só chega depois do ACK
    private int numberPackets;
    private long lastTimeMod;
    private int portOtherSide;
    private String givenPath;


    /**
     * Construtor
     *
     */
    public GetFile(InetAddress destIP, Constantes flagPort, String givenPath) {
        this.destIP = destIP;
        this.flagPort = flagPort;
        this.givenPath = givenPath;
        try {
            otherSide = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("[getFile]: Erro ao criar socket");
        }
    }

    /**
     * Fills the packet of SYNFILE with information about the request.
     * It sends the port to receive, the size of a string with the path of the file, and the path of the file.
     * @param localPortToReceive Port to receive information
     * @return
     */
    private DatagramPacket packetOfSYNFile(int localPortToReceive){
        //Bytes para enviar
        int currentByteTry = 0;

        //Assim o outro lado sabe para onde mandar
        byte[] portToReceive = ByteBuffer.allocate(Integer.SIZE).putInt(localPortToReceive).array();

        //Manda também o comprimento da String
         byte[] sizeString = ByteBuffer.allocate(Integer.SIZE).putInt(givenPath.getBytes().length).array();
       // System.out.println("[GETFILE] Size of path " + givenPath.getBytes().length);
       // System.out.println("[GETFILE] Path " + givenPath);
         //Assim, quando faz o pedido de getFile, manda já o Path e a porta pela qual espera receber
        byte[] pathBytes = givenPath.getBytes();
        //O +1 é para o tipo de mensagem
        int sizePacket = portToReceive.length + sizeString.length + pathBytes.length + 1;
        byte[] tryConnection = new byte[sizePacket];


        tryConnection[currentByteTry] = (byte) Constantes.SYNFile;
        currentByteTry++;

        System.arraycopy(portToReceive, 0, tryConnection, currentByteTry, portToReceive.length);
        currentByteTry+=portToReceive.length;

        System.arraycopy(sizeString, 0, tryConnection, currentByteTry, sizeString.length);
        currentByteTry += sizeString.length;

        System.arraycopy(pathBytes, 0, tryConnection, currentByteTry, pathBytes.length);
        currentByteTry += pathBytes.length;

        DatagramPacket synFile = new DatagramPacket(tryConnection, currentByteTry,
                destIP, flagPort.WhatPortToSend());
        return synFile;
    }

    /**
     * Starts connection to receive file.
     */
    private MainConnection startGetOneFile() {
        boolean connected = false;
        try {
            int timeBetweenHellos = 1000;
            //Bytes para receber

            //Define qual a porta em que recebe coisas
// Talvez seja preciso meter um número no receive
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramSocket receiveSocket = new DatagramSocket();
            DatagramPacket synFile = packetOfSYNFile(receiveSocket.getLocalPort());
            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);


            receiveSocket.setSoTimeout(timeBetweenHellos);
          //  DatagramPacket receivePacket = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);
            sendSocket.send(synFile);
            //Enquanto não estiver connectado
            while (!connected){
                System.out.println("[GetFile] O outro lado deve mostrar:");
                System.out.println("[GetFile] porta deste lado " + receiveSocket.getLocalPort());
                System.out.println("[GetFile] path " + givenPath);
                //Envia uma tentativa de conexão
                sendSocket.send(synFile);
                System.out.println("[getFile]: Envia Get");
                //Tenta receber qualquer coisa
                try{
                    receiveSocket.receive(receive);
                    sendSocket.send(synFile);
                    if (receiveFileInfo[0] == (byte) Constantes.ACKFile) {
                        connected = true; // a packet has been received : stop sending
                        System.out.println("[getFile]: Houve conexão");
                        System.out.println("[getFile]: Information about other side:");
                        System.out.println("[getFile]: "+receive.getPort()+" origem");
                        handleACK(receiveFileInfo);
                        //O MainConnection receberá  o Path
                        return new MainConnection(receiveSocket, receive, sendSocket, synFile, true);
                    }
                    else
                    {
                        System.out.println("[getFile]: RECEIVE, but not SYN or SynACK");
                        return null;
                    }
                }
                catch (SocketTimeoutException e) {
                    System.out.println("[getFile]: Não houve conexão");
                }

            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            System.out.println("[getFile]: Erro no socket");

            return null;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("[getFile]: Erro no socket");
            return null;
        }
        return null;
    }

    private void handleACK(byte[] messageACK){
        byte[] arrayBytes = new byte[Integer.SIZE];
        System.arraycopy(messageACK,  1, arrayBytes, 0, Integer.SIZE);
        this.numberPackets = ByteBuffer.wrap(arrayBytes).getInt();

        byte[] ltmB = new byte[Long.SIZE];
        System.arraycopy(messageACK,  1+Integer.SIZE, ltmB, 0, Long.SIZE);
        this.lastTimeMod = ByteBuffer.wrap(ltmB).getLong();

        System.arraycopy(messageACK,  1+Integer.SIZE+Long.SIZE, arrayBytes, 0, Integer.SIZE);
        this.portOtherSide = ByteBuffer.wrap(arrayBytes).getInt();
/*
        System.out.println("[GetFile] Read this information from Sender");
        System.out.println("[GetFile] num pack: " + numberPackets);
        System.out.println("[GetFile] last time M " + lastTimeMod);
        System.out.println("[GetFile] port to send " + portOtherSide);
*/
    }


    public void run () {
      MainConnection connection = startGetOneFile();
      receiveFile(connection);
    }

    /**
     * After the creation of two new ports to transmit the file, this function will receive the file and send the ACK's to the other side.
     * @param connection Information about the connection.
     */
    private void receiveFile(MainConnection connection) {
        System.out.println("[GETFILE] -------------------------------");
        System.out.println("[GETFILE]       Agora recebe pacote");
        try {
            File f = new File(givenPath); // Creating the file
            FileOutputStream outToFile = new FileOutputStream(f); // Creating the stream through which we write the file content
            int nPacketsReceived = 0;
            //Pode haver erro aqui
            while (nPacketsReceived < numberPackets) {

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("[GetFile] : Erro ao criar ficheiro.");
        }


    }


}


  /*
    private void startConnection(int numberPackets) {
        boolean connected = false;
        try {
            int timeBetweenHellos = 1000;
            //Bytes para enviar
            byte[] tryConnection = new byte[100];
            System.out.println("[SendFile]: "+numberPackets + " are going to be send.");
            tryConnection[0] = (byte) Constantes.SYNFile;
            byte[] x = ByteBuffer.allocate(Long.SIZE).putLong(numberPackets).array();

            System.arraycopy(x, 0, tryConnection, 1, x.length);

            //Bytes para receber
            byte[] receiveFile = new byte[10]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(tryConnection, tryConnection.length);

            //Define qual a porta em que recebe coisas
            here.setSoTimeout(timeBetweenHellos);
            System.out.println("[SendFile] Informations (here) -> " + otherSide.here.getPort() + "  "+ otherSide.here.getLocalPort());
            System.out.println("[SendFile] Informations (packet) -> " + otherSide.herePacket.getPort() );
            System.out.println("[SendFile] Informations (there) -> " + otherSide.otherSide.getPort() + "  "+ otherSide.otherSide.getLocalPort());
            System.out.println("[SendFile] Informations (packet) -> " + otherSide.otherPacket.getPort() );
            System.out.println("[SendFile] ---------------------------------------------------------" );
            DatagramSocket sendS = new DatagramSocket();

            sendS.connect(destIP, otherSide.otherPacket.getPort());
            System.out.println("[SendFile] send size ip -> " + destIP );
            System.out.println("[SendFile] send size (outra porta)-> " + otherSide.otherPacket.getPort() );

            //Enquanto não estiver connectado
            while (!connected){
                DatagramPacket sizeFile = new DatagramPacket(tryConnection, tryConnection.length);
                //Envia uma tentativa de conexão

                //otherSide.here.send(sizeFile);
                sendS.send(sizeFile);
                System.out.println("[SendFile]: Envia Hello");
                //Tenta receber qualquer coisa
                try{
                    otherSide.here.receive(receive);
                    here.send(sizeFile);
                    if (receiveFile[0] == (byte) Constantes.SynACK || receiveFile[0] == (byte) Constantes.SYN) {
                        connected = true; // a packet has been received : stop sending
                        System.out.println("[SendFile]: Houve conexão");
                        //    System.out.println("[StartConnection]: Information about other side:");
                        //    System.out.println("[StartConnection]: "+receive.getPort()+" origem");

                    }
                    else
                    {
                        System.out.println("[SendFile]: RECEIVE, but not SYN or SynACK");
                        return ;
                    }
                }
                catch (SocketTimeoutException e) {
                    System.out.println("[SendFile]: Não houve conexão");
                }

            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            System.out.println("[SendFile]: Erro no socket");

        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("[SendFile]: Erro no socket");
        }
        System.out.println("[SendFile]: Conexão feita");
    }
*/