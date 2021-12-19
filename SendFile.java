import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SendFile {
    /**
     * When a "get file" message is received, we read the the Path to the file from the packet.
     * We use the address of the message to know the destination of the file.
     * The address of the other computer is managed by the "StartConnection" class.
     *
     * This function waits for a AckFile from the other side to confirm that they are ready, and stops sending "Ack File" messages.
     * Also, when the AckFile is received, this function calls the "SendArray" class to send the file.
     *
     * To acknowledge the message, we send an "AckFile" message.
     * This "AckFile" message also sends the following information, by this order.
     * Message:
     * AckFile | total size of file | last time modified
     *    1           4 bytes             8 bytes
     * @param message information received in "get file" message, the path to the file, in bytes.
     * @param destIP IP of the computer that wants the file.
     * @param otherPort Port where the other computer will receive the file.
     * @param log Class that holds the log file, and shared variables.
     */
    public static void getReceived(byte[] message, InetAddress destIP, int otherPort, LogInformation log){
        int readingThisByte = 1+Constantes.startPositionGET;

        //byte[] portByte = new byte[Integer.SIZE];
        //System.arraycopy(message, readingThisByte, portByte, 0, portByte.length);
        //int portOtherSide = ByteBuffer.wrap(portByte).getInt();
        //readingThisByte += Integer.SIZE;

        //Gets the size of the string that holds the path of the file.
        byte[] sizePathB = new byte[Integer.SIZE];
        System.arraycopy(message, readingThisByte, sizePathB, 0, sizePathB.length);
        int sizePath = ByteBuffer.wrap(sizePathB).getInt();
        readingThisByte += Integer.SIZE;

        //Convert the array of bytes to the string that holds the file path.
        byte[] pathByte = new byte[sizePath];
        System.arraycopy(message, readingThisByte, pathByte, 0, sizePath);
        String path = new String(pathByte);
        System.out.println("[StartConnection] Path of file ");
        System.out.println("[StartConnection] "+path);

        //Gets the file.
        File f = new File(path);
        Path pathP = Paths.get(path);

        //Array to store message of AckFile and other information
        int currentPositionToSend = 0;
        byte[] sendACK = new byte[1 + 2 * Integer.SIZE + Long.SIZE];
        sendACK[currentPositionToSend] = (byte) Constantes.ACKFile;
        currentPositionToSend++;
        try {
            //Read the file.
            byte[] data = Files.readAllBytes(pathP);

            int numberPackets = Constantes.calculaNumPacotes(data.length);
            //Store the size of the file to the Ack message.
            byte[] sizeFile = ByteBuffer.allocate(Integer.SIZE).putInt(data.length).array();
            System.arraycopy(sizeFile, 0, sendACK, currentPositionToSend, sizeFile.length);
            currentPositionToSend += sizeFile.length;

            //Store the last time the file was modified.
            byte[] lastTimeMod = lastTimeModified(f);
            System.arraycopy(lastTimeMod, 0, sendACK, currentPositionToSend, lastTimeMod.length);
            currentPositionToSend += lastTimeMod.length;

            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.setSoTimeout(Constantes.timeBetweenHello);

            //DatagramSocket receiveSocket = new DatagramSocket();

            // int thisPort = sendSocket.getLocalPort();
            // byte[] thisPortBytes = ByteBuffer.allocate(Integer.SIZE).putInt(thisPort).array();
            // System.arraycopy(thisPortBytes, 0, sendACK, currentPositionToSend, thisPortBytes.length);
            // currentPositionToSend += thisPortBytes.length;

            //Packet of Ack message.
            DatagramPacket infoFromFileSEND = new DatagramPacket(sendACK, sendACK.length, destIP, otherPort);
            byte[] receiveFile = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFile, receiveFile.length);

            boolean connected = false;
           // sendSocket.send(infoFromFileSEND);
            System.out.println("[getReceived - Send] : Information other side should get");
            System.out.println("[getReceived - Send] : data length " + data.length+ " num pacotes " + numberPackets);
            log.writeSYNfile(receive.getPort(), receive.getAddress(), numberPackets, path);
            //While we don't receive a "AckFile" message.
            while (!connected) {
                try {
                    System.out.println("[StartConnection - Send FILE]: Envia info");
                    System.out.println("[StartConnection - Send FILE]: Espera ACK na porta " + sendSocket.getLocalPort());
                    System.out.println("[StartConnection - Send FILE]: Espera ACK na porta " + sendSocket.getPort());
                    sendSocket.send(infoFromFileSEND);
                    sendSocket.receive(receive);

                    if (receiveFile[0] == (byte) Constantes.ACKFile) {
                        connected = true; // a packet has been received : stop sending

                        System.out.println("[StartConnection - Send FILE]: Houve conexão");

                        System.out.println("[StartConnection - Send FILE]: Information about other side:");
                        System.out.println("[StartConnection - Send FILE]: " + receive.getPort() + " origem");
                        //System.out.println("[StartConnection - GET FILE]: " + thisPort + " origem");

                        MainConnection info = new MainConnection(sendSocket, receive, sendSocket, infoFromFileSEND, data, numberPackets);
                        SendArray oneFile = new SendArray(info, log);
                        log.incrementNThreads();
                        Thread t1 = new Thread(oneFile);
                        t1.start();
                        System.out.println("[SendFile] Is this written after file is sent?");

                    } else {
                        System.out.println("[StartConnection - Send FILE]: Recebeu outra coisa");
                        System.out.println("[StartConnection - Send FILE]: Num: " + (int) receiveFile[0]);

                    }
                }
                catch(SocketTimeoutException e){
                    System.out.println("[StartConnection - Send FILE]: Não houve conexão");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[StartConnection - Send FILE]: Erro ao abrir ficheiro, acho eu");

        }
    }

    /**
     * Returns the last time the file was modified in an array of bytes.
     * @param f File analysed
     * @return Last time modified in an array of bytes.
     */
    private static byte[] lastTimeModified(File f){
        long lastTime = f.lastModified();
        return ByteBuffer.allocate(Long.SIZE).putLong(lastTime).array();
    }

}
