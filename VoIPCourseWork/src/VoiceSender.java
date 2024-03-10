import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.time.Instant;

public class VoiceSender implements Runnable {
    private final int socketNum;
    DatagramSocket sendingSocket;
    int port;
    InetAddress ip;
    int encryptionKey = 15;
    short authenticationKey = 10;
    boolean running = true;

    public VoiceSender(InetAddress clientIP, int clientPORT, int socketNumber) {
        this.ip = clientIP;
        this.port = clientPORT;
        this.socketNum = socketNumber;

        try {
            switch (socketNumber) {
                case 2 -> sendingSocket = new DatagramSocket2();
                case 3 -> sendingSocket = new DatagramSocket3();
                case 4 -> sendingSocket = new DatagramSocket4();
                default -> sendingSocket = new DatagramSocket();
            }
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        // Create and run the thread when class is made
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        while (running) {

            try {
                AudioRecorder recorder = new AudioRecorder(); // Creating an AudioRecorder instance
                int recordTime = 9999; // Record time in seconds
                PacketBlock packetBlock = new PacketBlock();

                for (int i = 0; i < Math.ceil(recordTime / 0.016); i++) {

                    byte[] block = recorder.getBlock();
                    byte[] encryptedPacket = encryptPacket(block);

                    if (socketNum == 3) {
                        packetBlock.addPacket(encryptedPacket);
                        if (packetBlock.getPackets().size() == 16) {

                            for (byte[] packetData : packetBlock.getPackets()) {
                                DatagramPacket packet = new DatagramPacket(packetData, packetData.length, ip, port);
                                sendingSocket.send(packet);
                            }

                            packetBlock = new PacketBlock();
                        }
                    }
                    else{
                        DatagramPacket packet = new DatagramPacket(encryptedPacket, encryptedPacket.length, ip, port);
                        sendingSocket.send(packet);
                    }
                }

            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public byte[] encryptPacket(byte[] block) {
        // Initializing ByteBuffer for encryption
        ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
        ByteBuffer plainText = ByteBuffer.wrap(block);

        for (int j = 0; j < block.length / 4; j++) {
            int fourByte = plainText.getInt();
            fourByte = fourByte ^ encryptionKey; // XOR operation with key
            unwrapEncrypt.putInt(fourByte);
        }
        byte[] encryptedBlock = unwrapEncrypt.array();

        // Creating a ByteBuffer for the voice packet
        ByteBuffer voicePacket = ByteBuffer.allocate(522);
        voicePacket.putShort(authenticationKey); // Adding authentication key
        voicePacket.putLong(Instant.now().toEpochMilli());
        voicePacket.put(encryptedBlock); // Adding encrypted audio data

        return voicePacket.array();

    }
}
