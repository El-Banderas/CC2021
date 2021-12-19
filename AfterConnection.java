import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Here, we handle the synchronization of files, comparing the files in the other folder with the current folder.
 * We make a request to the other computer from time to time, to know if it got some different files.
 * If that happens, we make a request for that file.
 */
public class AfterConnection implements Runnable {
    private MainConnection portDestination;
    private InetAddress destIP;
    //Depois eliminar isto
    private final Constantes flagPort;
    private LogInformation log;
    private String folder;


    /**
     * Constructor of the AfterConnection class.
     * @param portDestination Port of the AfterConnection of the other side.
     * @param log Object that holds the log file, and shared variables.
     * @param destIP IP of the computer we are synchronizing.
     * @param flag
     */
    public AfterConnection(MainConnection portDestination, LogInformation log, InetAddress destIP, Constantes flag, String folder) {
        this.portDestination = portDestination;
        this.destIP = destIP;
        this.flagPort = flag;
        this.log = log;
        this.folder = folder;
    }

    /**
     * Central function of AfterConnection.
     * Here, we ask for a list of files from the other computer, and compare with the files of this folder.
     *
     */
    public void run() {
        System.out.println("----------------------------------------------");
        System.out.println("[AfterConnection] LS part");
        System.out.println("----------------------------------------------");
        System.out.println("[AfterConnection] Send/ReceiveFiles");
        /**
         * Se calhar é preciso saber quantos o outro vai precisar ou quantos este vai enviar?
         * Guarda informação sobre o outro
         * Existe um contador para os dois valores, necessário neste ciclo
         * while (Há para enviar) {
         * Envia um;
         * if (há para receber) Recebe um;
         * }
         * while (Há para receber){
         * recebe
         * }
         */



        //Test ls
        try {
            if (flagPort.whatPort) {
                String folder1 = "/share/src";
                byte[] lsOtherSide = getLS.getLS(destIP, flagPort,folder , log);
                System.out.println("[AfterConnection] Get other side was received");
                interpretarLSOtherSide(lsOtherSide);
              //  String responseSendLS = convertByteString(lsOtherSide);
              //  System.out.println("[AfterConnection] : Test ls, is correct? ");
              //  System.out.println("[AfterConnection] : " + responseSendLS);

            }
        } catch (NotConnectedExcpetion notConnectedExcpetion) {
            notConnectedExcpetion.printStackTrace();
            System.out.println("[AfterConnection] : erro ao obter o get");
        }
        /*
            if (flagPort.whatPort) {
                //String GivenPath = "/share/tp2-folder2/Chapter_3_v8.0.pptx";
                String GivenPath = "/share/tp2-folder2/rfc7231.txt";
                //String GivenPath = "/share/LOGXXXX";
                GetFile test = new GetFile(destIP, flagPort, GivenPath, log);
                Thread t1 = new Thread(test);
                t1.start();
                try {
                    t1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("[AfterConnection] : Erro no join");

                }

                System.out.println("[AfterConnection] : Deve imprimir isto após receber o ficheiro");

            }
            else System.out.println("[AfterConnection] : Não acontece nada deste lado. Deve aparecer um GET");

         */
    }

    private void interpretarLSOtherSide(byte[] lsOtherSide) {
        int keepUp = 0;

        byte[] numFileB = new byte[Integer.SIZE];
        System.arraycopy(lsOtherSide,  keepUp, numFileB, 0, numFileB.length);
        keepUp += numFileB.length;
        int numFileI = ByteBuffer.wrap(numFileB).getInt();

        System.out.println("[AfterConnection]: Número de Ficheiros " + numFileI);
        System.out.println("[MainLS] Total bytes will be received: "+lsOtherSide.length);

        for(int i = 0; i < numFileI; i++){
            byte[] sizeStringB = new byte[Integer.SIZE];
            System.arraycopy(lsOtherSide, keepUp, sizeStringB, 0, sizeStringB.length);
            int sizeStringI = ByteBuffer.wrap(sizeStringB).getInt();
            keepUp += sizeStringB.length;


            byte[] stringB = new byte[sizeStringI];
            System.arraycopy(lsOtherSide, keepUp, stringB, 0, stringB.length);
            String stringI = new String(stringB);
            keepUp += stringB.length;

            byte[] longB = new byte[Long.BYTES];
            System.arraycopy(lsOtherSide,  keepUp, longB, 0, longB.length);
            long longI = ByteBuffer.wrap(longB).getLong();
            keepUp += longB.length;

            System.out.println("[After Conection]: File other side: " + stringI);
            System.out.println("[After Conection]: File other side (ltm) : " + longI);
            if(!MainLS.getMap().containsKey(stringI) || !MainLS.getMap().get(stringI).equals(longI)){
                GetFile test = new GetFile(destIP, flagPort, stringI, log);
                Thread t1 = new Thread(test);
                t1.start();
            }
        }
    }


    //Now this is not necessary, but got good code. Don't delete
    private DatagramSocket esperaConexao() {
        boolean connected = false;
        byte[] receiveFile = new byte[100]; // Where we store the data of datagram of the name
        DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);
        try {
            System.out.println("[AfterConnection] Informations (here) -> " + portDestination.here.getPort() + "  " + portDestination.here.getLocalPort());
            System.out.println("[AfterConnection] Informations (packet) -> " + portDestination.herePacket.getPort());
            System.out.println("[AfterConnection] Informations (otherside) -> " + portDestination.otherSide.getPort() + "  " + portDestination.otherSide.getLocalPort());
            System.out.println("[AfterConnection] Informations (packet) -> " + portDestination.otherPacket.getPort());

            DatagramSocket receiveS = new DatagramSocket();
            System.out.println("[AfterConnection]: Informations to create Listening socket");
            int temp = portDestination.here.getLocalPort();
            System.out.println("[AfterConnection]: ip: " + destIP + " porta  " + temp);
            receiveS.connect(destIP, temp);
            while (!connected) {

                //Tenta receber qualquer coisa
                try {

                    portDestination.here.receive(receive);
                    if (receiveFile[0] == (byte) Constantes.SYNFile || receiveFile[0] == (byte) Constantes.ACKFile) {
                        connected = true; // a packet has been received : stop sending
                        System.out.println("[ReceiveFile]: Houve conexão");
                        byte[] x = new byte[Long.SIZE];
                        System.arraycopy(receiveFile, 1, x, 0, x.length);

                        long numberPackets = ByteBuffer.wrap(x).getLong();
                        System.out.println("[ReceiveFile]: Vou receber " + numberPackets + " pacotes");
                        byte[] tryConnection = new byte[5];
                        tryConnection[0] = (byte) Constantes.SynACK;
                        DatagramPacket helloPacket = new DatagramPacket(tryConnection, tryConnection.length, destIP, receive.getPort());
                        DatagramSocket toReceive = new DatagramSocket();
                        toReceive.send(helloPacket);
                        return toReceive;
                    } else {
                        System.out.println("[ReceiveFile]: Não sei o que recebi");
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("[AfterConnection] : Estou à espera");
                } catch (IOException e) {
                    System.out.println("[AfterConnection] : IOException");
                }
            }
        } catch (SocketException e) {
            System.out.println("[AfterConnection] Não criou o socket para receber ");
        }

        return null;
    }


    private String convertByteString(byte[] message) {

//        byte[] sizeSrting = new byte[Integer.SIZE]; //testLS.getBytes();
//        System.arraycopy(message, 1, sizeSrting, 0, Integer.SIZE);
//        int sizeString = ByteBuffer.wrap(sizeSrting).getInt();


        byte[] string = new byte[message.length - 1 - Integer.SIZE]; //testLS.getBytes();
        System.arraycopy(message, 1 + Integer.SIZE, string, 0, string.length);
        return new String(string);
    }

    private String convertBS(byte[] message) {

//        byte[] sizeSrting = new byte[Integer.SIZE]; //testLS.getBytes();
//        System.arraycopy(message, 1, sizeSrting, 0, Integer.SIZE);
//        int sizeString = ByteBuffer.wrap(sizeSrting).getInt();


        byte[] string = new byte[message.length - 1 - Integer.SIZE]; //testLS.getBytes();
        System.arraycopy(message, 1 + Integer.SIZE, string, 0, string.length);
        return new String(string);
    }

    static public byte[] currentLS(String folder, int port) {
        System.out.println("[AfterConnection] Converting LS ");
        System.out.println("[AfterConnection] This port " + port);

        /*
        String testLS = "/share/tp2-folder2/rfc7231.txt";
        System.out.println("[AfterConnection] : String sent");
        System.out.println("[AfterConnection] : " + testLS);
        File f = new File(testLS);
        Path pathP = Paths.get(testLS);

        byte[] test = testLS.getBytes();
        byte[] res = new byte[test.length + 1 + Integer.SIZE];
        res[0] = (byte) Constantes.ACKLs;
        byte[] sizeString = ByteBuffer.allocate(Integer.SIZE).putInt(test.length).array();
        System.arraycopy(sizeString, 0, res, 1, Integer.SIZE);
        System.arraycopy(test, 0, res, 1+Integer.SIZE, test.length);
        byte[] toSend = testLS.getBytes();
       */


            File newFolder = new File(folder);
            String[] l = newFolder.list();
            byte[] toSend = MainLS.gerarMensageTotal(newFolder);

            //  byte[] sizeString = ByteBuffer.allocate(Integer.SIZE).putInt(toSend.length).array();
            //byte[] portToReceive = ByteBuffer.allocate(Integer.SIZE).putInt(port).array();
            //System.arraycopy(portToReceive, 0, res, 1, Integer.SIZE);

            //System.arraycopy(sizeString, 0, res, 1, Integer.SIZE);
            //byte[] lsOtherSide = SendLS.sendLS(testLS.getBytes(), portDestination, flagPort);
            return toSend;

        //return res;
    }
    // catch (Exception e) {
    //     System.out.println("[AfteConnection]: teste");
    // }
    // return null;
}
