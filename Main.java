import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.locks.Lock;
/**
 * In the main function we start the two forms of communication with this computer, and check the number of arguments.
 * The TCP connection (with HTTP protocol), and UDP connection, that share files.
 * Those two connections occur simultaneously, in two parallel threads.
 * Also, here we create the LogInformation object to share variables and write information to the log file.
 */

public class Main {
    private static Lock l;
    public static void main(String[] args) throws IOException {
        
        Path path = Paths.get("/home/banderitas/Desktop/3_ano_1_sem/CC/TransferFiles/src/rfc7231.txt");
        LogInformation log = new LogInformation(false);
        HTTPconnection http = new HTTPconnection(log);
        new Thread(http).start();
        //LogInformation log = new LogInformation(InetAddress.getLocalHost());

        if (args.length < 1) {
            System.out.println("[MAIN]: current ip -> "+InetAddress.getLocalHost());
            return;
        }
        else {
            if (args.length < 2) {
                System.out.println("[MAIN]: Falta a pasta");
                return;
            }
            Constantes defineBool;
           // if (args.length == 1) {
                System.out.println("[Main] : 1");
                defineBool = new Constantes(true);
            //} else {
            //    System.out.println("[Main] : 2");
               // defineBool = new Constantes(true);
            //}
            //while (true) {

                StartConnection begin = new StartConnection(log);
                System.out.println("[MAIN]: Args:" + args[0]);
                InetAddress destIP = InetAddress.getByName(args[0]);
                //Este socket tem uma porta de saída aleatória
                //Ao mandar, define o endereço de chegada :)
                /**
                 * toSend has the port of this thread.
                 * connected has the port of the other side.
                 */
                begin.startConnection(destIP, defineBool, args[1]);
                System.out.println("[MAIN]: The end of main is here");
           // }

        }

    }
}


