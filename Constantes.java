import java.io.IOException;
import java.net.Socket;

public class Constantes {
    static public int timeBetweenHello = 1000;
    static public int OfficialPort = 8888;
    static public int SYN = 0;
    static public int SynACK = 1;
    static public int SYNFile = 2;
    static public int ACKFile = 3;
    /**
     * To end connections
     */
    static public int SYNLs = 4;
    static public int ACKLs = 5;
    static public int FynLS = 6;
    static public int FynAck = 7;

    static public int minPort = 100;
    static public int maxPort = 900;

    static public int maxSizePacket = 1500;

    /**
     * Constants about getFile Part
     * maxNumberSeq is the maximum number of sequence (16 bits).
     * starFilePosition is the position in packet to send file.
     */
    static public int maxNumberSeq = 65535;
    static public int starFilePosition = 2;


    /**
     * byte 0 to say it's a packet about a file.
     * byte 1 and 2 to SeqNumber
     */
    static public int positionInPacketToSendFile = 3;


    public static boolean available(int port){
        try(Socket ignored = new Socket("localhost", port)){
            return false;
        }
        catch (IOException ignored)
        {
            return true;
        }
    }
//A partir daqui é só para treinar coisas

    public Constantes(boolean whatPort) {
        this.whatPort = whatPort;
    }

    /**
     * Como estou a testar:
     * Em vez de serem ip's diferentes, eu uso portas diferentes.
     * True tenta conectar com False. Usam portas oficiais diferentes
     */

    public boolean whatPort;
    public int officialPort1 = OfficialPort;
//    public int officialPort2 = 8888;
//    public int officialPort1 = 8888;
    public int officialPort2 = OfficialPort;
    public int WhatPort(){
        if (whatPort) return officialPort1;
        return officialPort2;
    }
    public int WhatPortToSend(){
        if (whatPort) return officialPort2;
        return officialPort1;
    }
}
