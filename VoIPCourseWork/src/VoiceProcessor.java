import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class represents a voice processor that processes incoming voice packets.
 */
public class VoiceProcessor implements Runnable {

    private final PriorityBlockingQueue<PacketWrapper> packetBuffer = new PriorityBlockingQueue<>();
    private AudioPlayer player;
    private final int socketNum;

    /**
     * Constructs a VoiceProcessor object with the specified socket number.
     *
     * @param socket The socket number for the processor.
     */
    public VoiceProcessor(int socket) {
        this.socketNum = socket;
    }

    /**
     * Starts the voice processing thread.
     */
    @Override
    public void run() {
        try {
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        // Continuous processing loop
        while (true) {
            // Process packets if there are at least two packets in the buffer
            if (packetBuffer.size() >= 2) {
                try {
                    PacketWrapper firstPacket = packetBuffer.take(); // Take the first packet from the buffer
                    processAudio(firstPacket); // Process the first packet
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Adds a packet to the processing buffer.
     *
     * @param timestamp The timestamp of the packet.
     * @param packet    The packet data.
     */
    public void addToBuffer(long timestamp, byte[] packet) {
        PacketWrapper packetWrapper = new PacketWrapper(timestamp, packet);
        packetBuffer.add(packetWrapper);
    }

    /**
     * Decrypts the audio data from an encrypted packet.
     *
     * @param encryptedData The encrypted audio data.
     * @return The decrypted audio data.
     */
    private byte[] decryptAudio(byte[] encryptedData) {
        ByteBuffer unwrapDecrypt = ByteBuffer.allocate(encryptedData.length);
        ByteBuffer cipherText = ByteBuffer.wrap(encryptedData);

        for (int j = 0; j < encryptedData.length / 4; j++) {
            int fourByte = cipherText.getInt();
            int encryptionKey = 15;
            fourByte = fourByte ^ encryptionKey; // XOR decrypt
            unwrapDecrypt.putInt(fourByte);
        }
        return unwrapDecrypt.array();
    }

    /**
     * Processes the audio data from a packet.
     *
     * @param packetWrapper The packet wrapper containing the audio data.
     * @throws IOException If an I/O error occurs during audio processing.
     */
    private void processAudio(PacketWrapper packetWrapper) throws IOException {
        byte[] decryptedPacket = decryptAudio(packetWrapper.getData());
        player.playBlock(decryptedPacket);

        // Check if the socket number is 2
        if (socketNum == 2) {
            PacketWrapper secondPacket = packetBuffer.peek(); // Peek the second packet without removing it from the buffer

            // Ensure the second packet exists
            assert secondPacket != null;

            // Calculate the time difference between the first and second packet timestamps
            long timeDiff = secondPacket.getTimestamp() - packetWrapper.getTimestamp();

            // If the time difference is greater than 32 milliseconds, play the first packet again
            if (timeDiff > 32) {
                player.playBlock(decryptedPacket);
            }
        }
    }
}
