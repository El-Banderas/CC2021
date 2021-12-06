import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AfterConnection implements Runnable {
    //Este serve para receber coisas, acho. Porque tem a porta desta thread, está "listening"
    //E este para mandar.
            //Porque tem o endereço do outro.
    private MainConnection portDestination;
    private InetAddress destIP;
    //Depois eliminar isto
    private final Constantes flagPort;
    /**
     * We only need to store the port where we are going to send information.
     * We don't need the current port, it will be generated.
     * @param portDestination
     * Depois é preciso remover o bool, só serve para testar envio de ficheiros
     */
    public AfterConnection( MainConnection portDestination, InetAddress destIP, Constantes flag) {
        this.portDestination = portDestination;
        this.destIP = destIP;
        this.flagPort = flag;
    }

    /**
     * General function of Sending files. To-dos':
     * Manda para a porta de destino uma lista com os ficheiros que tem, e data da última alteração.
     *          Pode ter de ser em vários pacotes.
     * Recebe a lista do outro lado.
     * Compara com o que tem nesta pasta.
     * Para cada ficheiro que é para mandar:
        * Chama a minha função com o PATH de cada ficheiro que é para mandar.
        *      A minha função precisa de saber qual o destino inicial da mensagem.
     * Num while(receive) com timeout, da mesma forma que o StartConnection, recebe os vários nomes de ficheiros
     *     e chama o receiveFile para os ler (Diogo).
     */
    public void run(){
        System.out.println("----------------------------------------------");
        System.out.println("[AfterConnection] LS part");
        System.out.println("----------------------------------------------");
        System.out.println("[AfterConnection] Send/ReceiveFiles" );
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
                byte[] lsOtherSide = getLS.getLS(destIP, flagPort);
                String responseSendLS = convertByteString(lsOtherSide);
                System.out.println("[AfterConnection] : Test ls, is correct? ");
                System.out.println("[AfterConnection] : " + responseSendLS);
            }
        } catch (NotConnectedExcpetion notConnectedExcpetion) {
            notConnectedExcpetion.printStackTrace();
            System.out.println("[AfetConnection] : erro ao obter o get");
        }




/*
            if (flagPort.whatPort) {
                String GivenPath = "/home/banderitas/Desktop/3_ano_1_sem/CC/TransferFiles/src/rfc7231.txt";
                GetFile test = new GetFile(destIP, flagPort, GivenPath);
                new Thread(test).start();
            }
            else System.out.println("[AfterConnection] : Não acontece nada deste lado. Deve aparecer um GET");

 */
        }

        //Now this is not necessary, but got good code. Don't delete
    private DatagramSocket esperaConexao(){
        boolean connected = false;
        byte[] receiveFile = new byte[100]; // Where we store the data of datagram of the name
        DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);
        try {
        System.out.println("[AfterConnection] Informations (here) -> " + portDestination.here.getPort() + "  "+ portDestination.here.getLocalPort());
        System.out.println("[AfterConnection] Informations (packet) -> " + portDestination.herePacket.getPort() );
        System.out.println("[AfterConnection] Informations (otherside) -> " + portDestination.otherSide.getPort() + "  "+ portDestination.otherSide.getLocalPort());
        System.out.println("[AfterConnection] Informations (packet) -> " + portDestination.otherPacket.getPort() );

            DatagramSocket receiveS = new DatagramSocket();
            System.out.println("[AfterConnection]: Informations to create Listening socket");
            int temp = portDestination.here.getLocalPort();
            System.out.println("[AfterConnection]: ip: " + destIP + " porta  " +temp );
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
                    System.out.println("[ReceiveFile]: Vou receber "+numberPackets + " pacotes");
                    byte[] tryConnection = new byte[5];
                    tryConnection[0] = (byte) Constantes.SynACK;
                    DatagramPacket helloPacket = new DatagramPacket(tryConnection, tryConnection.length, destIP, receive.getPort());
                    DatagramSocket toReceive = new DatagramSocket();
                    toReceive.send(helloPacket);
                    return toReceive;
                }
                else {
                    System.out.println("[ReceiveFile]: Não sei o que recebi");
                }

            } catch (SocketTimeoutException e) {
                System.out.println("[AfterConnection] : Estou à espera");
            }
            catch (IOException e) {
                System.out.println("[AfterConnection] : IOException");
            }
        }
        } catch (SocketException e) {
            System.out.println("[AfterConnection] Não criou o socket para receber ");
        }

        return null;
    }


    private String convertByteString(byte[] message){

//        byte[] sizeSrting = new byte[Integer.SIZE]; //testLS.getBytes();
//        System.arraycopy(message, 1, sizeSrting, 0, Integer.SIZE);
//        int sizeString = ByteBuffer.wrap(sizeSrting).getInt();


        byte[] string = new byte[message.length-1]; //testLS.getBytes();
        System.arraycopy(message, 1, string, 0, string.length);
        return new String(string);
    }

    static public byte[] currentLS(int port){
        System.out.println("[AfterConnection] Converting LS ");
        System.out.println("[AfterConnection] This port " + port);

        String testLS = "Olá, estou correto?";
        byte[] toSend = testLS.getBytes();

        byte[] res = new byte[toSend.length+1+Integer.SIZE];
        res[0] = (byte) Constantes.ACKLs;

      //  byte[] sizeString = ByteBuffer.allocate(Integer.SIZE).putInt(toSend.length).array();
        byte[] portToReceive = ByteBuffer.allocate(Integer.SIZE).putInt(port).array();
        System.arraycopy(portToReceive, 0, res, 1, Integer.SIZE);


        //System.arraycopy(sizeString, 0, res, 1, Integer.SIZE);
        System.arraycopy(toSend, 0, res, 1+Integer.SIZE, toSend.length);
        //byte[] lsOtherSide = SendLS.sendLS(testLS.getBytes(), portDestination, flagPort);
        return res;

    }
}
