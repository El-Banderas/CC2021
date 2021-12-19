import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LogInformation {
    private BufferedOutputStream file;
    private int nThreads = 0;
    Lock l = new ReentrantLock();
    private boolean write;

    /**
     * The next three functions keeps the number of current threads active.
     * This will be used in the status part of the HTTP request.
     */
    public void incrementNThreads(){
        l.lock();
        try {
            nThreads++;
        }
        finally {
            l.unlock();
        }
    }
    public void decrementNThreads(){
        l.lock();
        try {
            nThreads--;
        }
        finally {
            l.unlock();
        }
    }
    public int getNThreads(){
        return nThreads;
    }
    public LogInformation(boolean write) {
        if (write) {
            this.write = write;
            LocalDateTime date = LocalDateTime.now();
            long seconds = Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).getSeconds();

            //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            //LocalDateTime now = LocalDateTime.now();
            String pathFile = "LOG" + seconds;
            //String pathFile = "LOG";
            // Creates a FileOutputStream
            try {

                FileOutputStream foutst = new FileOutputStream(pathFile);
                file = new BufferedOutputStream(foutst);
                file.write("Ola\n".getBytes());
                file.flush();
                // Creates a BufferedOutputStream
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public LogInformation(InetAddress ip) {
        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        //LocalDateTime now = LocalDateTime.now();
        //String pathFile = "LOG"+now.toString();
        String pathFile = "LOG"+ip.toString();
        // Creates a FileOutputStream
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(pathFile, true);
            file.write("Ola".getBytes());
            file.flush();
            // Creates a BufferedOutputStream
            BufferedOutputStream buffer = new BufferedOutputStream(file);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close() throws Exception {
        System.out.println("[LOG Information]: Fim de programa");
        file.write("----End of connection-----\n".getBytes());
        file.flush();
        file.close();
    }

    private void writeToFile(String str){
        if (write) {
            try {
                file.write(str.getBytes());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[LogInformation] Error in write to file");
            }
        }

    }
    public void writeSYNreceived(int port, InetAddress address) {
        String toWrite = "Received SYN from other computer\nOrigin: "+address+" and port: "+port+"\nConnection will start\n";
        writeToFile(toWrite);

    }

    public void writeSYNfile(int port, InetAddress address, int numberPackets, String path) {
        String toWrite = "Received GET FILE from other computer\nOrigin: "+address+" and port: "+port+"\n" +
                "Will be sent "+numberPackets + " packets from the "+ path+" file.\n";
        writeToFile(toWrite);
    }

    public void writeGetFile(String givenPath, InetAddress destIP, int portOtherSide) {
        String toWrite = "[GetFile] Start of getting "+givenPath + "file\n".
                  concat("[GetFile] From this IP: " +destIP + " . And this port:" +  portOtherSide);

        writeToFile(toWrite);
    }

    public void writeDurationFileTransfer(long l, String givenPath, int sizeFile) {
        String toWrite = "[GetFile] File "+givenPath + "was received.\n".
                concat("[GetFile] And took "+ l+ " miliseconds to transfer "+sizeFile+" bytes.\n")
                .concat("[GetFile] So the ");
        writeToFile(toWrite);
    }


    public void writeGetArrayEnd(long l, int tryesNotAck, boolean isFile, String givenPath, int numPackets, InetAddress destIP) {
        if (isFile) {
            String toWrite = "[GetArray] File " + givenPath + "was received from "+destIP+".\n".
                    concat("[GetFile] It took " + numPackets + " packets to transfer and " + tryesNotAck + " ACK's packets weren't confirmed.\n")
                    .concat("[GetFile] The bytes transfet took " + l + " miliseconds.\n");
            writeToFile(toWrite);
        }
        else {
            String toWrite = "[GetArray] List os files from other side was received.\n".
                    concat("[GetFile] It took " + numPackets + " packets to transfer and " + tryesNotAck + " ACK's packets weren't confirmed.\n")
                    .concat("[GetFile] The bytes transfet took " + l + " miliseconds.\n");
            writeToFile(toWrite);     }
    }

    public void writeSendArray(InetAddress address, int port, int numPackets, int repeteadACksLog, long l, int length, boolean isFile) {
        String toWrite = "[SendArray] " + length + " are going to be sent to " + address + ".\n".
                concat("[SendArray] It took " + numPackets + " packets to transfer, and " + repeteadACksLog + " ACK's packets weren't confirmed.\n")
                .concat("[SendArray] The bytes transfet took " + l + " miliseconds.\n")
                .concat("[SendArray] So the efficiency was " + length / l + " bytes/seconds.\n");
        writeToFile(toWrite);
    }

    public void beginningSide(InetAddress destIP) {
        String toWrite = "[StartConnection] The communication with " + destIP + " will begin.\n";
        writeToFile(toWrite);

    }

    public void writeStartHTTP() {
        String toWrite = "[HTTP connection] Computer start accepting http connections.\n";
        writeToFile(toWrite);

    }
}
