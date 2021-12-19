import java.io.File;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class MainLS {
    //Esta classe será para realizar o LS. Ou seja:
    //Receber e enviar os Files em A e em B

    private HashMap<String, Long> filesAndDates = new HashMap<String, Long>();

    public static HashMap<String, Long> getMap(){
        File curDir = new File(".");
        return listOfFiles(curDir);
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    //Aqui iremos tratar de A

    public static HashMap<String, Long> listOfFiles(File dir) {
        String[] l = dir.list();
        File[] list = dir.listFiles();

        HashMap<String, Long> fAndD = new HashMap<String, Long>();

        for (int i = 0; i < list.length; i++) {
            fAndD.put(l[i], list[i].lastModified());
            System.out.println("[Main LS]: " + list[i]);
        }
        return fAndD;
    }

    //Mensagem de um único ficheiro a.txt em formato byte[]
    public static byte[] gerarMensagemA(File singleDir){
        String givenPath = singleDir.getPath();
        int currentByteTry = 0;

        //Gerar mensagem para um só FIle da lista

        byte[] sizeString = ByteBuffer.allocate(Integer.SIZE).putInt(givenPath.getBytes().length).array();
        //Guardas a String
        byte[] pathBytes = givenPath.getBytes();
        long lasTimeMod = singleDir.lastModified();
        byte[] lTimeMod = longToBytes(lasTimeMod);
        //Copias para o array total
        int sizePacket = sizeString.length + pathBytes.length + lTimeMod.length ;
        byte[] mensagem = new byte[sizePacket];

        System.arraycopy(sizeString, 0, mensagem, 0, sizeString.length);
        currentByteTry += sizeString.length;

        System.arraycopy(pathBytes, 0, mensagem, currentByteTry, pathBytes.length);
        currentByteTry += pathBytes.length;

        System.arraycopy(lTimeMod, 0, mensagem, currentByteTry, lTimeMod.length);
        currentByteTry += lTimeMod.length;

        return mensagem;
    }

    //Mensagem total da lista dos ficheiros .txt em byte[]
    public static byte[] gerarMensageTotal(File mainDir){
        File[] list = mainDir.listFiles();
        ArrayList<byte[]> mensagem = new ArrayList<>();
        int numberOfFiles = 0;

        //Para cada linha adicionas uma mensagem de cada ficheiro.
        int totalMensagem = 0;


        for (File file : list) {
            byte[] temp = gerarMensagemA(file);
            mensagem.add(temp);
            totalMensagem += temp.length;
            numberOfFiles++;
        }

        byte[] toSend = new byte[totalMensagem + Integer.SIZE];
        int keepUp = 0;

        byte[] sizeNum = ByteBuffer.allocate(Integer.SIZE).putInt(numberOfFiles).array();


        System.arraycopy(sizeNum, 0, toSend, keepUp, sizeNum.length);
        keepUp += sizeNum.length;

        for(byte[] oneFile : mensagem) {
            System.arraycopy(oneFile, 0, toSend, keepUp, oneFile.length);
            keepUp += oneFile.length;
        }
        System.out.println("[MainLS] Total bytes will be sent: "+totalMensagem);
        System.out.println("[MainLS] Data: " + new String(toSend));


        return toSend;
    }




}
