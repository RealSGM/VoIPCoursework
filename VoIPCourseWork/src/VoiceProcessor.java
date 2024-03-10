import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.PriorityBlockingQueue;

public class VoiceProcessor implements Runnable {

    private PriorityBlockingQueue<PacketWrapper> packetBuffer = new PriorityBlockingQueue<>();
    private AudioPlayer player;
    private int socketNum;
    private int encryptionKey = 15;
    private boolean running = true;


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

        while (running) {
            if (packetBuffer.size() >= 2) {
                PacketWrapper firstPacket = packetBuffer.poll();


                byte[] decryptedPacket = decryptAudio(firstPacket.getData());

                try {
                    processAudio(firstPacket.getTimestamp(), decryptedPacket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public void addToBuffer(long timestamp, byte[] packet) {
        PacketWrapper packetWrapper = new PacketWrapper(timestamp, packet);
        packetBuffer.add(packetWrapper);
    }

    private byte[] decryptAudio(byte[] encryptedData) {
        ByteBuffer unwrapDecrypt = ByteBuffer.allocate(encryptedData.length);
        ByteBuffer cipherText = ByteBuffer.wrap(encryptedData);

        for (int j = 0; j < encryptedData.length / 4; j++) {
            int fourByte = cipherText.getInt();
            fourByte = fourByte ^ encryptionKey; // XOR decrypt
            unwrapDecrypt.putInt(fourByte);
        }
        return unwrapDecrypt.array();
    }

    private void processAudio(long timestamp, byte[] data) throws IOException {
        player.playBlock(data);
    }

}
