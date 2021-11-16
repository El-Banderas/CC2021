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
}
