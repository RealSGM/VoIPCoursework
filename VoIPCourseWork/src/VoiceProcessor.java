import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.PriorityBlockingQueue;

public class VoiceProcessor implements Runnable {

    private final PriorityBlockingQueue<PacketWrapper> packetBuffer = new PriorityBlockingQueue<>();
    private final int socketNum;
    private AudioPlayer player;

    private final DiffieHellman dh;
    private long shared_key;

    public VoiceProcessor(int socket, DiffieHellman dh) {
        this.socketNum = socket;
        this.dh = dh;
    }

    @Override
    public void run() {
        try {
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        CyclicRedundancyCheck decoder = new CyclicRedundancyCheck();
        // Continuous processing loop
         while (true) {
             // Process packets if there are at least two packets in the buffer
             if (!packetBuffer.isEmpty()) {
                 try {
                     PacketWrapper firstPacket = packetBuffer.take(); // Take the first packet from the buffer
                     byte[] decryptedPacket = decryptAudio(firstPacket.data());
                     byte[] decodedPacket = decoder.decode(decryptedPacket);
                     processAudio(decodedPacket); // Process the first packet
                 } catch (IOException | InterruptedException e) {
                     throw new RuntimeException(e);
                 }
             }
         }

    }

    public void addToBuffer(PacketWrapper packetWrapper) {
        packetBuffer.add(packetWrapper);
    }

    private byte[] decryptAudio(byte[] encryptedData) {
        ByteBuffer unwrapDecrypt = ByteBuffer.allocate(encryptedData.length);
        ByteBuffer cipherText = ByteBuffer.wrap(encryptedData);

        for (int j = 0; j < encryptedData.length / 4; j++) {
            int fourByte = cipherText.getInt();
            int encryptionKey = (int) this.getShared_key();
            fourByte = fourByte ^ encryptionKey; // XOR decrypt
            unwrapDecrypt.putInt(fourByte);
        }
        return unwrapDecrypt.array();
    }

    private void processAudio(byte[] packet) throws IOException {
        try {
            player.playBlock(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long sendPublicKey() {
        return this.dh.getPublic_key();
    }

    public void receivePublicKey(long otherPublicKey) {
        this.shared_key = this.dh.generateSecretKey(otherPublicKey);
    }

    public long getShared_key() {
        return this.shared_key;
    }
}
