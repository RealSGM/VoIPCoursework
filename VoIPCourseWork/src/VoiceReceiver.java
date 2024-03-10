import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * This class represents a voice receiver that listens for incoming voice packets.
 */
public class VoiceReceiver implements Runnable {
    private DatagramSocket receivingSocket;
    private final VoiceProcessor processor;

    /**
     * Constructs a VoiceReceiver object.
     *
     * @param clientPORT    The port number to listen on.
     * @param socketNumber  The socket number.
     * @param processorInstance          The VoiceProcessor instance to process incoming packets.
     */
    public VoiceReceiver(int clientPORT, int socketNumber, VoiceProcessor processorInstance) {
        this.processor = processorInstance;

        try {
            switch (socketNumber) {
                case 2 -> receivingSocket = new DatagramSocket2(clientPORT);
                case 3 -> receivingSocket = new DatagramSocket3(clientPORT);
                case 4 -> receivingSocket = new DatagramSocket4(clientPORT);
                default -> receivingSocket = new DatagramSocket(clientPORT);
            }
        } catch (SocketException e) {
            System.out.println("ERROR: VoiceReceiver: Could not open UDP socket to receive.");
            e.printStackTrace();
            System.exit(0);
        }

        // Start the receiver thread
        Thread receiverThread = new Thread(this);
        receiverThread.start();

        // Start the processor thread
        Thread processorThread = new Thread(processor);
        processorThread.start();
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            byte[] encryptedBlock = new byte[526];
            DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length);

            try {
                // Receive the packet
                receivingSocket.receive(packet);
                ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
                short authKey = buffer.getShort();
                int sequenceNumber = buffer.getInt();
                long timestamp = buffer.getLong();

                // Check authentication key
                short authenticationKey = 10;
                if (authKey == authenticationKey) {
                    byte[] remainingBytes = new byte[buffer.remaining()];
                    buffer.get(remainingBytes);

                    // Add packet to processor buffer
                    processor.addToBuffer(timestamp, remainingBytes, sequenceNumber);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        receivingSocket.close();
    }
}
