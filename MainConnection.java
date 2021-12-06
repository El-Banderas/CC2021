import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Path;

public class MainConnection {
    public DatagramSocket here;
    public DatagramPacket herePacket;
    public DatagramSocket otherSide;
    public DatagramPacket otherPacket;
    public boolean getFile;
    //Caso seja um get, pode guardar já o path.
    public byte[] file;

    /**
     * General connections
     * @param here
     * @param herePacket
     * @param otherSide
     * @param otherPacket
     * @param getFile
     */
    public MainConnection(DatagramSocket here, DatagramPacket herePacket, DatagramSocket otherSide, DatagramPacket otherPacket, boolean getFile) {
        this.here = here;
        this.herePacket = herePacket;
        this.otherSide = otherSide;
        this.otherPacket = otherPacket;
        this.getFile = getFile;
        this.file = null;
    }



    /**
     *   Este construtor é usado quando é feito um getFile no servidor.
     *   Assim, ele já tem o path, recebido pelo pacote SYN.
     * @param here
     * @param herePacket
     * @param otherSide
     * @param otherPacket
     * @param file
     */
    public MainConnection(DatagramSocket here, DatagramPacket herePacket, DatagramSocket otherSide, DatagramPacket otherPacket, byte[] file) {
        this.here = here;
        this.herePacket = herePacket;
        this.otherSide = otherSide;
        this.otherPacket = otherPacket;
        this.getFile = true;
        this.file = file;
    }

    public void close (){
        here.close();
        otherSide.close();
    }
}
