import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class VoiceReceiver implements Runnable {
    DatagramSocket receivingSocket;
    int port;
    int encryptionKey = 15;
    short authenticationKey = 10;
    boolean running = true;

    public VoiceReceiver(int clientPORT) {
        this.port = clientPORT;

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        // Initialize AudioPlayer
        AudioPlayer player;
        try {
            player = new AudioPlayer();

        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        // Initialize DatagramSocket for receiving voice data
        try {
            receivingSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            System.exit(0);
        }

        while (running) {
            // Receive a DatagramPacket
            byte[] encryptedBlock = new byte[514];
            DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length);

            try {
                receivingSocket.receive(packet);

                // Extract authentication key from received packet
                ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
                short authKey = buffer.getShort();

                if (authKey == authenticationKey) {

                    byte[] remainingBytes = new byte[buffer.remaining()];
                    buffer.get(remainingBytes);

                    // Initialize ByteBuffer for decryption
                    ByteBuffer unwrapDecrypt = ByteBuffer.allocate(remainingBytes.length);
                    ByteBuffer cipherText = ByteBuffer.wrap(remainingBytes);

                    // Decrypting the received audio block using XOR operation with the encryption key
                    for (int j = 0; j < remainingBytes.length / 4; j++) {
                        int fourByte = cipherText.getInt();
                        fourByte = fourByte ^ encryptionKey; // XOR decrypt
                        unwrapDecrypt.putInt(fourByte);
                    }
                    byte[] decryptedBlock = unwrapDecrypt.array();

                    // Play the decrypted audio block
                    player.playBlock(decryptedBlock);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        receivingSocket.close();
    }
}