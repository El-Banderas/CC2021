import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class Main {
    private static DatagramSocket getFreePort(int port) {
        try {
            return new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("[MAIN]: create send Datagram error");
            e.printStackTrace();
        }

        //Get random port to send messages
        Random rand = new Random();
        boolean found = false;
        while (!found){
            try {
                System.out.println("[MAIN]:Random port " + port);
                if (Constantes.available(port)) {
                    System.out.println("[Main]: Sucesso na porta");
                    DatagramSocket res = new DatagramSocket(port);
                    found = true;
                    return res; //found = true;

                }
            }
            catch (SocketException e){
                System.out.println("[MAIN]: Porta mal criada");
            }
        }
        System.out.println("[MAIN]: Não deve passar aqui");
        return null;

    }

private static DatagramSocket getFreePort() {
    //Get random port to send messages
    Random rand = new Random();
    int port;// = rand.nextInt(Constantes.minPort) + (Constantes.maxPort-Constantes.minPort);
    boolean found = false;
    while (!found){
        try {
            port = rand.nextInt(Constantes.minPort) + (Constantes.maxPort - Constantes.minPort);
            System.out.println("[MAIN]:Random port " + port);
            if (Constantes.available(port)) {
                System.out.println("[Main]: Sucesso na porta");
                return new DatagramSocket(port); //found = true;

            }
        }
        catch (SocketException e){
            System.out.println("[MAIN]: Porta mal criada");
            found = false;
        }
    }
    System.out.println("[MAIN]: Não deve passar aqui");
    return null;

}

    public static void main(String[] args) throws UnknownHostException {

        if (args.length < 1) return;
       if (args.length > 0 ){
           StartConnection begin = new StartConnection();
           System.out.println("[MAIN]: Args:" + args.length + "| "+args[0]+" | "+ Integer.parseInt(args[1]) + " | ");
           InetAddress destIP = InetAddress.getByName(args[0]);
           begin.startConnection(destIP, getFreePort(Integer.parseInt(args[1])));
       }
       else {
           StartConnection begin = new StartConnection();
           System.out.println("[MAIN]: Args:" + args[0]);
           InetAddress destIP = InetAddress.getByName(args[0]);
           begin.startConnection(destIP, getFreePort());
       }
    }

}
