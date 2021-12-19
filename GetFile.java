import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * This class is used to get a file from other computer, by the IP adress.
 */
public class GetFile implements Runnable{
    private DatagramSocket sendSocket;
    private InetAddress destIP;
    //Depois eliminar
    private Constantes flagPort;
    private String givenPath;
    private LogInformation log;

    //This information is completed after the "Ack file" message is received.
    private int numberPackets;
    private int sizeFile;
    private long lastTimeMod;
    private int portOtherSide;


    /**
     * Constructor
     * @param destIP IP from where we want the file
     * @param flagPort
     * @param givenPath Path to the file we want.
     * @param log Class that holds the log file, and shared variables.
     */
    public GetFile(InetAddress destIP, Constantes flagPort, String givenPath, LogInformation log) {
        this.destIP = destIP;
        this.flagPort = flagPort;
        this.givenPath = givenPath;
        this.log = log;
    }



    /**
     * Starts connection to receive file.
     * @return Information about the connection.
     */
    private MainConnection startGetOneFile() {
        boolean connected = false;
        try {

            this.sendSocket = new DatagramSocket();
            DatagramPacket synFile = GetArray.packetOfSYN(true, givenPath, destIP, flagPort);
            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);


            sendSocket.setSoTimeout(Constantes.timeBetweenHello);
          //  DatagramPacket receivePacket = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);
            //sendSocket.send(synFile);
            //Enquanto não estiver connectado
            while (!connected){
                System.out.println("[Files.GetFile] O outro lado deve mostrar:");
                System.out.println("[Files.GetFile] porta deste lado " + sendSocket.getLocalPort());
                System.out.println("[Files.GetFile] path " + givenPath);
                //Envia uma tentativa de conexão
                sendSocket.send(synFile);
                System.out.println("[getFile]: Envia Get");
                //Tenta receber qualquer coisa
                try{
                    sendSocket.receive(receive);
                 //   sendSocket.send(synFile);

                    if (receiveFileInfo[0] == (byte) Constantes.ACKFile) {
                        //Envia ACK de que recebeu tamanho do ficheiro

                        connected = true; // a packet has been received : stop sending
                        System.out.println("[getFile]: Houve conexão");
                        System.out.println("[getFile]: Information about other side:");
                        System.out.println("[getFile]: "+receive.getPort()+" origem");
                        handleACK(receiveFileInfo);
                        this.portOtherSide = receive.getPort();
                        //O MainConnection receberá  o Path

                      //  sendSocket.send(ackFile);
                      //  sendSocket.send(ackFile);
                      //  sendSocket.send(ackFile);
                        return new MainConnection(sendSocket, receive, sendSocket, synFile, true);
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

    /**
     * This function handles the information received by the first ACK packet.
     * Stores in this object the size of the file, the number of packets we are going to receive, and the last time modified of the file received.
     * @param messageACK Content of the Ack message received.
     */
    private void handleACK(byte[] messageACK){
        byte[] arrayBytes = new byte[Integer.SIZE];
        System.arraycopy(messageACK,  1, arrayBytes, 0, Integer.SIZE);
        this.sizeFile = ByteBuffer.wrap(arrayBytes).getInt();

        this.numberPackets = Constantes.calculaNumPacotes(sizeFile);
        byte[] ltmB = new byte[Long.SIZE];
        System.arraycopy(messageACK,  1+Integer.SIZE, ltmB, 0, Long.SIZE);
        this.lastTimeMod = ByteBuffer.wrap(ltmB).getLong();

        //System.arraycopy(messageACK,  1+Integer.SIZE+Long.SIZE, arrayBytes, 0, Integer.SIZE);
        //this.portOtherSide = ByteBuffer.wrap(arrayBytes).getInt();

        System.out.println("[Files.GetFile] Read this information from Sender");
        System.out.println("[Files.GetFile] num pack: " + numberPackets);
        System.out.println("[Files.GetFile] last time M " + lastTimeMod);
        System.out.println("[Files.GetFile] port to send " + portOtherSide);

    }

    /**
     * Main method of this class.
     * First, it tries to get a connection with the computer that holds the file.
     * Then, starts receiving the file.
     * Also, this function changes the date the file was modified to the original same date.
     */
    public void run () {
        log.incrementNThreads();
        log.writeGetFile(givenPath, destIP, portOtherSide);
        long start = System.currentTimeMillis();
      MainConnection connection = startGetOneFile();
      receiveFile(connection);
        long finish = System.currentTimeMillis();
        log.writeDurationFileTransfer(finish-start, givenPath, sizeFile);
        log.decrementNThreads();
    }

    /**
     * After the creation of two new ports to transmit the file, this function will call the "GetArray" class to receive the file.
     * @param connection Information about the connection.
     */
    private void receiveFile(MainConnection connection) {
        System.out.println("[GETFILE] -------------------------------");
        System.out.println("[GETFILE]       Agora recebe pacote");
        System.out.println("[GETFILE] Recebe em " + sendSocket.getLocalPort());
        System.out.println("[GETFILE] Envia para " + portOtherSide);
        //GetArray toGet = new GetArray(sendSocket, destIP, flagPort, numberPackets, sizeFile, portOtherSide, givenPath, log);
        GetArray toGet = new GetArray(connection, log, numberPackets, sizeFile, givenPath, lastTimeMod);
        toGet.receiveFile(true);
    }





        /*
        byte[] numSeq = new byte[Constantes.starFilePosition - 1];
        System.arraycopy(receive, 0, numSeq, 0, numSeq.length);
        try {
            int x = ByteBuffer.wrap(numSeq).getChar();

            System.out.println("[Files.GetFile] Recebeu um pacote estranho");
            System.out.println("[Files.GetFile] Começa por " + x);

            return x;
        }
        catch (Exception e){
            return 0;
        }
        */
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