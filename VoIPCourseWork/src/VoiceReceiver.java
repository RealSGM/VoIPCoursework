import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class VoiceReceiver implements Runnable {
    private final PacketWrapper dummyPacket;
    private DatagramSocket receivingSocket;
    private final VoiceProcessor processor;
    private final Thread processorThread;

    public VoiceReceiver(int clientPORT, int socketNumber, VoiceProcessor processorInstance, PacketWrapper dummy) {
        this.processor = processorInstance;
        this.dummyPacket = dummy;

        try {
            switch (socketNumber) {
                case 2 -> receivingSocket = new DatagramSocket2(clientPORT);
                case 3 -> receivingSocket = new DatagramSocket3(clientPORT);
                case 4 -> receivingSocket = new DatagramSocket4(clientPORT);
                default -> receivingSocket = new DatagramSocket(clientPORT);
            }
        } catch (SocketException e) {
            System.out.println("ERROR: VoiceReceiver: Could not open UDP socket to receive.");
            System.exit(0);
        }

        Thread receiverThread = new Thread(this);
        receiverThread.start();

        processorThread = new Thread(processor);
        processorThread.start();
    }

    @Override
    public void run() {
        boolean running = true;
        int packetSize = dummyPacket.calculatePacketSize();

        while (running) {
            byte[] encryptedBlock = new byte[packetSize];
            DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length);

            try {
                receivingSocket.receive(packet);
                ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
                decodeBuffer(buffer);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        receivingSocket.close();
        processorThread.interrupt();
    }

    private void decodeBuffer(ByteBuffer buffer){
        short authKey = buffer.getShort();
        int sequenceNumber = buffer.getInt();
        long timestamp = buffer.getLong();
        HeaderWrapper headerWrapper = new HeaderWrapper(timestamp, sequenceNumber);

        if (authKey == headerWrapper.getAuthenticationNumber()) {
            byte[] remainingBytes = new byte[buffer.remaining()];
            buffer.get(remainingBytes);
            PacketWrapper packetWrapper = new PacketWrapper(headerWrapper, remainingBytes);
            processor.addToBuffer(packetWrapper);
        }
    }
}
