import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class VoiceReceiver implements Runnable {
    DatagramSocket receivingSocket;
    int port;
    short authenticationKey = 10;
    boolean running = true;
    VoiceProcessor processor;

    public VoiceReceiver(int clientPORT, int socketNumber, VoiceProcessor proc) {
        this.port = clientPORT;
        this.processor = proc;

        try {
            switch (socketNumber) {
                case 2 -> receivingSocket = new DatagramSocket2(port);
                case 3 -> receivingSocket = new DatagramSocket3(port);
                case 4 -> receivingSocket = new DatagramSocket4(port);
                default -> receivingSocket = new DatagramSocket(port);
            }
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        Thread t1 = new Thread(this);
        t1.start();

        Thread t2 = new Thread(processor);
        t2.start();
    }

    @Override
    public void run() {
        while (running) {
            byte[] encryptedBlock = new byte[1034]; //changet to 1034/522
            DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length);

            try {
                receivingSocket.receive(packet);
                ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
                short authKey = buffer.getShort();
                long timestamp = buffer.getLong();

                if (authKey == authenticationKey) {
                    byte[] remainingBytes = new byte[buffer.remaining()];
                    buffer.get(remainingBytes);

                    processor.addToBuffer(timestamp, remainingBytes);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        receivingSocket.close();
    }

}
