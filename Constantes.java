import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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

    static public int maxSizePacket = 1000;


    /**
     * Constants about getFile Part
     * maxNumberSeq is the maximum number of sequence (16 bits).
     * starFilePosition is the position in packet to send file.
     */
    static public int windowSize = 30;
    static public int maxNumberSeq = (int) Character.MAX_VALUE;
    static public int starFilePosition = 2;
    static public int calculaNumPacotes(int size){
        return (size/(maxSizePacket-starFilePosition));
    }
    static public int numberAttempts = 25;
    static public int numAttemptsSendNotAck = 15;
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

    /**
     * Information about authenticity and integrity of packets send.
     */
    static private String KEY = "CC2021";
    static public byte[] keyWordB = keyWord();
    static public int startPositionGET = keyWordB.length;

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static byte[] keyWord(){
        try {

        SecretKeySpec sigingKey = new SecretKeySpec(KEY.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = null;
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(sigingKey);
        return mac.doFinal(KEY.getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            System.out.println("[CONSTANTES] : Erro ao gerar keys");
        }
        byte[] x = new byte[1];
        x[0] = (byte) 0;
        return x;
    }

    /**
     * Acho que não é necessária */
    private static String convertToHex(byte[] bytes){
        StringBuffer hash = new StringBuffer();
        for(int i = 0; i < bytes.length; i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1){
                hash.append('0');
            }
            hash.append(hex);
        }
        return hash.toString();
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
