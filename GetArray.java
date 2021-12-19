import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GetArray {
    private DatagramSocket sendSocket;
    private InetAddress destIP;
    //Depois eliminar

    //Informação que só chega depois do ACK
    private int numberPackets;
    private int sizeFile;
    private int portOtherSide;
    private  String givenPath;
    private boolean isFile;
    private LogInformation log;
    private long lastTimeModified;

    /**
     * Constructor -> Because it receives a path, we know a file is going to be received.
     * @param connection Information about the connection
     * @param log Class that holds the log file, and shared variables.
     * @param numberPackets Number of packets that will be sent.
     * @param sizeFile Size of the file, necessary for the last packet.
     * @param givenPath Path of the file.
     * @param lastTimeM Last time the file was modified.
     */
    public GetArray(MainConnection connection, LogInformation log, int numberPackets, int sizeFile, String givenPath, long lastTimeM) {
        this.sendSocket = connection.here;
        this.destIP = connection.herePacket.getAddress();
        this.log = log;
        this.portOtherSide = connection.herePacket.getPort();
        this.numberPackets = numberPackets;
        this.sizeFile = sizeFile;
        this.givenPath = givenPath;
        isFile = true;
        this.lastTimeModified = lastTimeM;
    }

    public GetArray(MainConnection connection, LogInformation log, int numberPackets, int sizeFile) {
        this.sendSocket = connection.here;
        this.destIP = connection.herePacket.getAddress();
        this.log = log;
        this.portOtherSide = connection.herePacket.getPort();
        this.numberPackets = numberPackets;
        this.sizeFile = sizeFile;
        isFile = false;
    }

    public GetArray(DatagramSocket sendSocket, InetAddress destIP,
                    int numberPackets, int sizeFile, int portOtherSide, String givenPath, LogInformation log) {
        this.sendSocket = sendSocket;
        this.destIP = destIP;
        this.numberPackets = numberPackets;
        this.sizeFile = sizeFile;
        this.portOtherSide = portOtherSide;
        this.givenPath = givenPath;
        isFile = true;
        this.log = log;
    }
    public GetArray(DatagramSocket sendSocket, InetAddress destIP,
                    int numberPackets, int sizeFile, int portOtherSide, LogInformation log) {
        this.sendSocket = sendSocket;
        this.destIP = destIP;
        this.numberPackets = numberPackets;
        this.sizeFile = sizeFile;
        this.portOtherSide = portOtherSide;
        this.givenPath = null;
        isFile = false;
        this.log = log;
    }

    /**
     * This function is used to create packets that start connections both to "GetLS" and "GetFiles".
     * This packets have the same structure, so we can use the same function.
     * @param isFile Defines the byte that says if we want a file or the list of files in the other computer.
     * @param toWrite Name of the file or current folder
     * @param destIP IP of the computer where we want the file
     * @param flagPort Temp
     * @return Packet ready to be sent.
     */
    public static DatagramPacket packetOfSYN(boolean isFile, String toWrite, InetAddress destIP, Constantes flagPort){
        //Bytes para enviar
        int currentByteArray = 0;


        //Size of the string we want to send.
        byte[] sizeString = ByteBuffer.allocate(Integer.SIZE).putInt(toWrite.getBytes().length).array();

        //String we want to send.
        byte[] pathBytes = toWrite.getBytes();
        //The extra byte we allocate is for the type of the message.
        int sizePacket = sizeString.length + pathBytes.length + 1 + Constantes.startPositionGET;
        byte[] tryConnection = new byte[sizePacket];
        System.arraycopy(Constantes.keyWordB, 0, tryConnection, currentByteArray, Constantes.startPositionGET);
        currentByteArray+= Constantes.startPositionGET;


        if (isFile) tryConnection[currentByteArray] = (byte) Constantes.SYNFile;
        else  tryConnection[currentByteArray] = (byte) Constantes.SYNLs;
        currentByteArray++;


        System.arraycopy(sizeString, 0, tryConnection, currentByteArray, sizeString.length);
        currentByteArray += sizeString.length;

        System.arraycopy(pathBytes, 0, tryConnection, currentByteArray, pathBytes.length);
        currentByteArray += pathBytes.length;

        DatagramPacket synFile = new DatagramPacket(tryConnection, currentByteArray,
                destIP, flagPort.WhatPortToSend());
        return synFile;
    }



    public byte[] receiveFile(boolean isFile) {

        boolean whileNotReceiveInfro = false;
        int totalBytesRead = 0;
        boolean past50 = false;
        DatagramPacket ackFile = packetOfACKFile(portOtherSide, isFile);

        try {
            long start = System.currentTimeMillis();
            int tryesNotAck = 0;
            byte[] receiveFileInfo = new byte[Constantes.maxSizePacket]; // Where we store the data of datagram of the name
            DatagramPacket receive = new DatagramPacket(receiveFileInfo, receiveFileInfo.length);
            sendSocket.setSoTimeout(Constantes.timeBetweenHello);
            List<byte[]> receivedBytes = new ArrayList<>(numberPackets);
            int nPacketsReceived = 0;
            int lastSeqReceived = 0;
            //Pode haver erro aqui
            while (lastSeqReceived <= numberPackets) {

                for (int i = 0; i < Constantes.windowSize && lastSeqReceived <= numberPackets; i++) {
                    System.out.println("[GetArray] " +nPacketsReceived+ " / " + numberPackets);
                    try {
                        if (!whileNotReceiveInfro) {
                            System.out.println("[GetArray]: Envia ACKFile para a porta "+ackFile.getPort());
                            sendSocket.send(ackFile);
                        }
                        sendSocket.receive(receive);
                        int thisSeqNum = getSeqNumber(receiveFileInfo);
                        System.out.println("[GetArray] Seq Nº " +thisSeqNum);
                        // System.out.println("[GETFILE] recebeu o pacote nº " + thisSeqNum);
                        //Recebeu o pacote correto
                        if (thisSeqNum == lastSeqReceived) {
                            whileNotReceiveInfro = true;
                            past50 = true;
                            if (thisSeqNum != numberPackets) {
                                System.out.println("[GetArray] Armazenou pacote " + thisSeqNum);
                                byte[] thisPacket = new byte[Constantes.maxSizePacket - Constantes.starFilePosition];
                                System.arraycopy(receiveFileInfo, Constantes.starFilePosition, thisPacket, 0, thisPacket.length);
                                receivedBytes.add(thisPacket);
                                totalBytesRead+=thisPacket.length;
                                //Se chegar ao máximo, volta a 0.
                                if (lastSeqReceived >= Constantes.maxNumberSeq)
                                    lastSeqReceived = lastSeqReceived % Constantes.maxNumberSeq;
                            }
                            else {
                                byte[] thisPacket = new byte[sizeFile - (numberPackets*(Constantes.maxSizePacket - Constantes.starFilePosition))];
                                System.out.println("[GetArray] Espera receber no último " + thisPacket.length);

                                System.arraycopy(receiveFileInfo, Constantes.starFilePosition, thisPacket, 0, thisPacket.length);
                                receivedBytes.add(thisPacket);
                                totalBytesRead+=thisPacket.length;

                            }
                            nPacketsReceived++;
                            lastSeqReceived++;

                        } else //Envia o último número de SEQ recebido
                        {
                            if (!past50 && (thisSeqNum == 1280 ||  thisSeqNum == 5620))
                                sendSocket.send(ackFile);
                            else {
                                System.out.println("[GetArray] Não armazenou pacote ");
                                System.out.println("[GetArray] Espera " + lastSeqReceived + " e recebeu " + thisSeqNum);
                                System.out.println("[GetArray] Envia ack 3 " + lastSeqReceived);
                                sendAck(lastSeqReceived);
                                i = 0;
                                tryesNotAck++;
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        //if (lastSeqReceived ==  numberPackets) break;
                        System.out.println("[GetArray] Esperou muito pelos pacotes");
                        System.out.println("[GetArray] Envia ack 1 " + lastSeqReceived );

                        sendAck(lastSeqReceived);
                        tryesNotAck++;

                    }
                }
                sendAck(lastSeqReceived);
                System.out.println("[GetArray] Envia ack 2 " + lastSeqReceived );

            }

            //Escreve no ficheiro
            sendSocket.close();
            System.out.println("[GetArray] : Agora tratar do FIN");
            System.out.println("[GetArray] : Leu (bytes) "+totalBytesRead);
            long finish = System.currentTimeMillis();
            log.writeGetArrayEnd(finish-start, tryesNotAck, isFile, givenPath, numberPackets, destIP);
            if (isFile) {
                    File f = new File(givenPath); // Creating the file

                FileOutputStream outToFile = new FileOutputStream(f); // Creating the stream through which we write the file content
                for (int i = 0; i <= numberPackets; i++) {
                    //System.out.println("[GETFILE] Envia ack 3 " + lastSeqReceived);
                    outToFile.write(receivedBytes.get(i));
                }
                f.setLastModified(lastTimeModified);
                System.out.println("[GetFile] Test last time mod changed: "+f.lastModified() );
                System.out.println("[GetFile] File name : "+f.getName() );

                outToFile.flush();
                outToFile.close();
                return null;
            }
            else{
                ByteBuffer temp = ByteBuffer.allocate(Constantes.maxSizePacket*(numberPackets+1));
                    for (int i = 0; i <= numberPackets; i++) {
                        System.out.println("[getArray] Message: " + new String(receivedBytes.get(i)));
                        temp.put(receivedBytes.get(i));
                    }
                return temp.array();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("[GetArray] : Erro ao criar ficheiro.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[GetArray] : Erro ao criar socket.");

        }
    return null;
    }


    private void sendAck(int lastSeqReceived) throws IOException {
        byte[] numSeq = ByteBuffer.allocate(2).putChar((char) lastSeqReceived).array();
        //byte[] numSeq = ByteBuffer.allocate(Character.SIZE).putChar((char) 1).array();
        DatagramPacket sendACK = new DatagramPacket(numSeq, numSeq.length, destIP, portOtherSide);
        // System.out.println("[GETFILE] Envia ack " + getSeqNumber(numSeq) );

        this.sendSocket.send(sendACK);
    }

    public static int getSeqNumber(byte[] receive) {
        ByteBuffer test = ByteBuffer.wrap(receive);
        int x = test.getChar();
        System.out.println("[GetArray] SeqNumber " + x);
        return x;
    }
    private DatagramPacket packetOfACKFile(int destPort, boolean isFile){
        //Bytes para enviar
        int currentByteTry = 0;

        byte[] tryConnection = new byte[1];
       if (isFile) tryConnection[0] = (byte) Constantes.ACKFile;
       else tryConnection[0] = (byte) Constantes.ACKLs;
        DatagramPacket synFile = new DatagramPacket(tryConnection, 1,
                destIP, destPort);
        return synFile;
    }

}
