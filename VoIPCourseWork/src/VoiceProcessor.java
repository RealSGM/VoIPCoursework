import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class VoiceProcessor implements Runnable {

    private final int socketNum;
    private final PacketWrapper dummyPacket;
    private final int packetSize;
    private final int minPacketCount;
    private AudioPlayer player;
    private final ConcurrentSkipListMap<Integer, PacketWrapper> packetBuffer = new ConcurrentSkipListMap<>();

    private final DiffieHellman dh;
    private long shared_key;

    public VoiceProcessor(int socket, DiffieHellman dh) {
        this.socketNum = socket;
        this.dh = dh;
    }

    @Override
    public void run() {
        player = initializeAudioPlayer();
        CyclicRedundancyCheck decoder = new CyclicRedundancyCheck();

         while (true) {
             if (packetBuffer.size() >= minPacketCount) {
                 try {
                     Map.Entry<Integer, PacketWrapper> firstEntry = packetBuffer.pollFirstEntry();
                     PacketWrapper firstPacket = firstEntry.getValue();
                     decodePacket(firstPacket, decoder);
                     
                     if (socketNum == 2){
                         Map.Entry<Integer, PacketWrapper> secondEntry = packetBuffer.firstEntry();
                         PacketWrapper secondPacket = secondEntry.getValue();
                         if (secondPacket.header().getTimestamp() - firstPacket.header().getTimestamp() > 100){
                             byte[] interpolatedData = interpolatePacketData(firstPacket, secondPacket);
                             PacketWrapper interpolatedPacket = new PacketWrapper(secondPacket.header(), interpolatedData);
                             decodePacket(interpolatedPacket, decoder);
                         }
                     }

                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
    }

    private AudioPlayer initializeAudioPlayer() {
        try {
            return new AudioPlayer();
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Error initializing AudioPlayer", e);
        }
    }

    /**
     * Interpolates the data between two packet wrappers.
     * @param firstPacket  The first PacketWrapper object.
     * @param secondPacket The second PacketWrapper object.
     * @return The interpolated data as a byte array.
     */
    private byte[] interpolatePacketData(PacketWrapper firstPacket, PacketWrapper secondPacket) {
        byte[] firstData = firstPacket.data();
        byte[] secondData = secondPacket.data();
        byte[] interpolatedData = new byte[dummyPacket.data().length];

        for (int i = 0; i < dummyPacket.data().length; i++) {
            double ratio = (double) (i + 1) / packetSize;
            interpolatedData[i] = (byte) ((1 - ratio) * firstData[i] + ratio * secondData[i]);
        }

        return interpolatedData;
    }

    public void addToBuffer(PacketWrapper packetWrapper) {
        packetBuffer.put(packetWrapper.header().getSequenceNumber(), packetWrapper);
    }

    /**
     * Decrypts the audio data.
     * @param encryptedData The encrypted audio data as a byte array.
     * @return The decrypted audio data as a byte array.
     */
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

    /**
     * Decodes a packet using a cyclic redundancy check.
     * @param packet  The PacketWrapper object to decode.
     * @param decoder The CyclicRedundancyCheck object used for decoding.
     * @throws IOException If an I/O error occurs.
     */
    private void decodePacket(PacketWrapper packet, CyclicRedundancyCheck decoder) throws IOException {
        byte[] decryptedPacket = decryptAudio(packet.data());
        byte[] decodedPacket = decoder.decode(decryptedPacket);
        processAudio(decodedPacket);
    }

    /**
     * Processes the audio data by playing it using the AudioPlayer.
     * @param packet The audio data to process as a byte array.
     * @throws IOException If an I/O error occurs.
     */
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
