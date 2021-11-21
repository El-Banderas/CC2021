import java.net.DatagramPacket;

public class AfterConnection {
    private int portDestination;

    /**
     * We only need to store the port where we are going to send information.
     * We don't need the current port, it will be generated.
     * @param portDestination
     */
    public AfterConnection(int portDestination) {
        this.portDestination = portDestination;
    }

    /**
     * General function of Sending files. To-dos':
     * Manda para a porta de destino uma lista com os ficheiros que tem, e data da última alteração.
     *          Pode ter de ser em vários pacotes.
     * Recebe a lista do outro lado.
     * Compara com o que tem nesta pasta.
     * Para cada ficheiro que é para mandar:
        * Chama a minha função com o PATH de cada ficheiro que é para mandar.
        *      A minha função precisa de saber qual o destino inicial da mensagem, nome, e a data que foi modificado.
     * Num while(receive) com timeout, da mesma forma que o StartConnection, recebe os vários nomes de ficheiros
     *     e chama o receiveFile para os ler (Diogo).
     */
    public void generalConnection(Constantes flagPort){
        System.out.println("----------------------------------------------");
        System.out.println("[AfterConnection]");


    }
}
