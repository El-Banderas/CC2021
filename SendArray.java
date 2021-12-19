import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * This class is used to send an array of bytes to a given address (IP and port).
 * In the main connection we hold the following information:
 * > IP and Port to send files;
 * > Socket to send messages;
 * > Socket to receive ACK's messages.
 */
public class SendArray implements Runnable {
    private MainConnection mainConnection;
    private LogInformation log;
    final private int attemptsConst = 20;

    /**
     * @param mainConnection Object that holds information about the connection
     * @param log Class that holds the log file, and shared variables.
     */
    public SendArray(MainConnection mainConnection, LogInformation log) {
        System.out.println("[SendArray]: " + mainConnection.here.getLocalPort() + " e envia para " + mainConnection.otherSide.getPort());
        this.log = log;
        this.mainConnection = mainConnection;
    }


    /**
     * Run method of the Thread.
     * This way, we can send arrays simultaneously.
     */
    public void run() {
            System.out.println("[SendArray] --------------------");
            System.out.println("[SendArray] Vai enviar para:");
            System.out.println("[SendArray] " + mainConnection.herePacket.getPort());
            System.out.println("[SendArray] Vai receber em:");
            System.out.println("[SendArray] " + mainConnection.otherSide.getLocalPort());
            sendFileMain();
    }

    /**
     * Main method to send one array.
     * The protocol of transmission is described in the report, but there are some particularities in the implementation.
     * Because we don't have the type of the communication in the header of the message, for efficiency, we can't distinguish whether the message is an SYN or ACK message.
     * So, we found out that those two types of message were equivalent to the numbers 1280 and 768.
     * Those numbers could be sequence numbers, so we have a flag that marks if we pass the 200th packet acknowledge.
     * If that happens, we can receive those two sequence numbers, otherwise, they are other type of message.
     */
    private void sendFileMain() {
        int numPackets = mainConnection.numPackets;
        int lastAckSeq = 0;
        int positionInFile = 0;
        int thisSeqNum = 0;
        int repeatedACksLog = 0;
        boolean past50 = false;
        int currentAttempts = attemptsConst;
        byte[] acksB = new byte[2];
        DatagramPacket receiveP = new DatagramPacket(acksB, acksB.length);
        int sizeCopiedFromFile = Constantes.maxSizePacket - Constantes.starFilePosition;
        byte[] file = new byte[Constantes.maxSizePacket];
        DatagramPacket sendPacket = new DatagramPacket(file, file.length, mainConnection.herePacket.getAddress(), mainConnection.herePacket.getPort());
        int previousSeq = 0;
        int previousPositionFile = 0;
        long startLog = System.currentTimeMillis();

        try {
            mainConnection.here.setSoTimeout(Constantes.timeBetweenHello);
                while (lastAckSeq <= numPackets && thisSeqNum <= numPackets && currentAttempts >= 0) {
                    if (thisSeqNum >= 200) past50 = true;
                    try {
                        System.out.println("[SendArray] " + lastAckSeq + " / " + numPackets);
                        previousSeq = thisSeqNum;
                        previousPositionFile = positionInFile;
                       // previousCurrentPacket = currentPacket;
                        for (int i = 0; i < Constantes.windowSize && thisSeqNum <= numPackets; i++) {
                            if (thisSeqNum != numPackets) System.arraycopy(mainConnection.file, positionInFile, file, Constantes.starFilePosition, sizeCopiedFromFile);
                            else {
                                System.out.println("[  SendArray  ] max read " + (positionInFile + sizeCopiedFromFile)+ " / will copy " + (int) (mainConnection.file.length - positionInFile)
                                            + " but " + file.length);

                                System.arraycopy(mainConnection.file, positionInFile, file, Constantes.starFilePosition,
                                        mainConnection.file.length - positionInFile);
                            }

                            byte[] numSeq = ByteBuffer.allocate(2).putChar((char) thisSeqNum).array();
                            System.arraycopy(numSeq, 0, file, 0, numSeq.length);
                            mainConnection.here.send(sendPacket);
                            System.out.println("[SendArray] send " + thisSeqNum);
                            positionInFile += sizeCopiedFromFile;
                            thisSeqNum++;

                        }
                        mainConnection.otherSide.receive(receiveP);
                        currentAttempts = attemptsConst;
                        int numAck = GetArray.getSeqNumber(acksB);
                        System.out.println("[SendArray] Recebeu ack nº " + numAck);
                        if (!past50 && (numAck == 1280 || numAck == 768)) System.out.println("[SendArray] Recebeu um ACK");
                        else {
                            lastAckSeq = numAck;
                        }
                        //A packet was lost.
                        //We receive a Ack number lower than expected.
                        if (numAck < thisSeqNum) {
                                repeatedACksLog++;
                                thisSeqNum = numAck;
                                positionInFile = numAck * sizeCopiedFromFile;
                        }
                        //if the sequence number is bigger that the number we can represent.
                        if (thisSeqNum >= Constantes.maxNumberSeq) {
                            thisSeqNum = thisSeqNum % Constantes.maxNumberSeq;
                            //System.out.println("[SendArray] Corta número Seq ");

                        }

                    } catch (SocketTimeoutException e) {
                        repeatedACksLog++;
                        thisSeqNum = previousSeq;
                        positionInFile = previousPositionFile;
                        System.out.println("[SendArray] Esperou muito pelo ACK");
                        currentAttempts--;
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    mainConnection.close();
    long endLog = System.currentTimeMillis();
    log.writeSendArray(mainConnection.herePacket.getAddress(), mainConnection.herePacket.getPort(), mainConnection.numPackets, repeatedACksLog, endLog-startLog, mainConnection.file.length, true);
    log.decrementNThreads();
    }

}