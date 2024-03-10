import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.PriorityBlockingQueue;

public class VoiceProcessor implements Runnable {

    private final PriorityBlockingQueue<PacketWrapper> packetBuffer = new PriorityBlockingQueue<>();
    private AudioPlayer player;
    private int socketNum;


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

        while (true) {
            // Process
            if (packetBuffer.size() >= 2){
                try {
                    PacketWrapper firstPacket =  packetBuffer.take();
                    processAudio(firstPacket);

                } catch (IOException | InterruptedException e) {
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
            int encryptionKey = 15;
            fourByte = fourByte ^ encryptionKey; // XOR decrypt
            unwrapDecrypt.putInt(fourByte);
        }
        return unwrapDecrypt.array();
    }

    private void processAudio(PacketWrapper packetWrapper) throws IOException {
        byte[] decryptedPacket =  decryptAudio(packetWrapper.getData());
        player.playBlock(decryptedPacket);


        if (socketNum == 2) {
            PacketWrapper secondPacket = packetBuffer.peek();

            assert secondPacket != null;
            long timeDiff = secondPacket.getTimestamp() - packetWrapper.getTimestamp();

            if (timeDiff > 32) {
                player.playBlock(decryptedPacket);
            }
        }


    }


}
