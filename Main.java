import java.net.*;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws UnknownHostException {

        if (args.length < 1) {
            System.out.println("[MAIN]: current ip -> "+InetAddress.getLocalHost());
            return;
        }
       else {
           Constantes defineBool;
           if (args.length == 1) {
               System.out.println("[Main] : 1");
               defineBool = new Constantes(true);
           }
           else {
               System.out.println("[Main] : 2");
               defineBool = new Constantes(false);
           }
               StartConnection begin = new StartConnection();
               System.out.println("[MAIN]: Args:" + args[0]);
               InetAddress destIP = InetAddress.getByName(args[0]);
               try {
                   //Este socket tem uma porta de saída aleatória
                   //Ao mandar, define o endereço de chegada :)
                   DatagramSocket toSend = new DatagramSocket();
                   System.out.println("[MAIN]: Information about socket:");
                   System.out.println("[MAIN]: " + toSend.getLocalPort() + " | " + toSend.getPort());
                   /**
                    * toSend has the port of this thread.
                    * connected has the port of the other side.
                    */
                   DatagramSocket connected = begin.startConnection(destIP, toSend, defineBool);
                   AfterConnection connection = new AfterConnection(connected.getPort());
                   System.out.println("[MAIN]: Information about connection:");
                   System.out.println("[MAIN]: this->" + toSend.getLocalPort() + " |other-> " + connected.getPort());

                   connection.generalConnection(defineBool);
               } catch (SocketException e) {
                   System.out.println("[MAIN]: Erro ao criar socket");
                   e.printStackTrace();
               }
           }


       }
    }


