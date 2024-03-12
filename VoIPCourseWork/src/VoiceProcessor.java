import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class VoiceProcessor implements Runnable {

    private final int socketNum;
    private AudioPlayer player;
    private final ConcurrentSkipListMap<Integer, PacketWrapper> packetBuffer = new ConcurrentSkipListMap<>(); // Use ConcurrentSkipListMap

    public VoiceProcessor(int socket) {
        this.socketNum = socket;
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
             if (packetBuffer.size() >= 16) {
                 try {
                     Map.Entry<Integer, PacketWrapper> firstEntry = packetBuffer.pollFirstEntry(); // Take and remove the first packet from the buffer
                     PacketWrapper firstPacket = firstEntry.getValue(); // Take the first packet from the buffer
                     decodePacket(firstPacket, decoder);
                     
                     if (socketNum == 2){
                         Map.Entry<Integer, PacketWrapper> secondEntry = packetBuffer.firstEntry(); // Take and remove the first packet from the buffer
                         PacketWrapper secondPacket = secondEntry.getValue();
                         if (secondPacket.getHeader().getTimestamp() - firstPacket.getHeader().getTimestamp() > 32){
                             decodePacket(secondPacket, decoder);
                         }
                     }

                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
    }

    public void addToBuffer(PacketWrapper packetWrapper) {
        packetBuffer.put(packetWrapper.getHeader().getSequenceNumber(), packetWrapper);
    }

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

    private void decodePacket(PacketWrapper packet, CyclicRedundancyCheck decoder) throws IOException {
        byte[] decryptedPacket = decryptAudio(packet.data());
        byte[] decodedPacket = decoder.decode(decryptedPacket);
        processAudio(decodedPacket); // Process the first packet
    }

    private void processAudio(byte[] packet) throws IOException {
        try {
            player.playBlock(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
