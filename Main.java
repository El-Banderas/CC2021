import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public class Main {
    private static int numberThreads;
    private static Lock l;
    public static void main(String[] args) throws IOException {
        numberThreads = 0;

        Path path = Paths.get("/home/banderitas/Desktop/3_ano_1_sem/CC/TransferFiles/src/rfc7231.txt");

        if (args.length < 1) {
            System.out.println("[MAIN]: current ip -> "+InetAddress.getLocalHost());
            return;
        }
       else {
            Constantes defineBool;
            if (args.length == 1) {
                System.out.println("[Main] : 1");
                defineBool = new Constantes(true);
            } else {
                System.out.println("[Main] : 2");
                defineBool = new Constantes(false);
            }
            while (true) {



                StartConnection begin = new StartConnection();
                System.out.println("[MAIN]: Args:" + args[0]);
                InetAddress destIP = InetAddress.getByName(args[0]);
                //Este socket tem uma porta de saída aleatória
                //Ao mandar, define o endereço de chegada :)
                /**
                 * toSend has the port of this thread.
                 * connected has the port of the other side.
                 */

                MainConnection connected = begin.startConnection(destIP, defineBool);
                System.out.println("[MAIN]: The end of main is here");
               // System.out.println("[MAIN]: " + connected);
                //Se for uma conexão normal, sem ser um get
                //System.out.println("[MAIN]: Information about connection:");
                //System.out.println("[MAIN]: this->" + toSend.getLocalPort() + " |other-> " + connected.getPort());
            }

        }

       }
    }


