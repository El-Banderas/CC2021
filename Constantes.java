import java.io.IOException;
import java.net.Socket;

public class Constantes {
    static public int OfficialPort = 8888;
    static public int SYN = 0;
    static public int SynACK = 0;

    static public int minPort = 100;
    static public int maxPort = 900;


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
    public int officialPort1 = 8886;
    public int officialPort2 = 8887;
    public int WhatPort(){
        if (whatPort) return officialPort1;
        return officialPort2;
    }
    public int WhatPortToSend(){
        if (whatPort) return officialPort2;
        return officialPort1;
    }
}
