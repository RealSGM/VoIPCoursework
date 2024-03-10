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

//This class represents a voice sender that sends audio data over UDP.
public class VoiceSender implements Runnable {

    private final int socketNum;
    int port;
    InetAddress ip;
    DatagramSocket sendingSocket;

    int encryptionKey = 15;
    short authenticationKey = 10;
    int sequenceNumber = 0;
    double breakTime = 5;
    int packetsPerSecond = 128;
    long elapsedTime = System.currentTimeMillis();

    /**
     * Constructs a VoiceSender object with the specified parameters.
     *
     * @param clientIP     The destination IP address.
     * @param clientPORT   The destination port number.
     * @param socketNumber The socket number.
     */
    public VoiceSender(InetAddress clientIP, int clientPORT, int socketNumber) {
        this.ip = clientIP;
        this.port = clientPORT;
        this.socketNum = socketNumber;

        try {
            // Initialize the DatagramSocket based on the socket number
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

    /**
     * Starts the sender thread to send audio data.
     */
    @Override
    public void run() {
        PacketBlock packetBlock = new PacketBlock();
        long startTime = System.currentTimeMillis();
        boolean running = true;

        try {
            AudioRecorder recorder = new AudioRecorder();

            while (running) {
                elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > breakTime * 1000) {
                    running = false;
                }

                byte[] block = recorder.getBlock();
                byte[] encryptedPacket = encryptPacket(block);

                if (socketNum != 3) {
                    sendPacket(encryptedPacket);
                } else {
                    packetBlock.addPacket(encryptedPacket);
                    if (packetBlock.getPackets().size() == 16) {
                        sendPacketBlock(packetBlock);
                        packetBlock = new PacketBlock();
                    }
                }
            }

            System.out.printf("Packets Sent: %d. %n", sequenceNumber);
            System.out.println("Bitrate: " + (sequenceNumber * 526 / (elapsedTime / 1000)));
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypts a block of audio data.
     *
     * @param block The audio data block to be encrypted.
     * @return The encrypted audio data block.
     */
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
        ByteBuffer voicePacket = ByteBuffer.allocate(526);
        voicePacket.putShort(authenticationKey); // Adding authentication key
        voicePacket.putInt(sequenceNumber);
        voicePacket.putLong(Instant.now().toEpochMilli());
        voicePacket.put(encryptedBlock); // Adding encrypted audio data
        sequenceNumber++;

        return voicePacket.array();
    }

    /**
     * Sends a packet block containing multiple packets.
     *
     * @param packetBlock The packet block to be sent.
     * @throws IOException If an I/O error occurs while sending the packet block.
     */
    private void sendPacketBlock(PacketBlock packetBlock) throws IOException {
        for (byte[] packetData : packetBlock.getPackets()) {
            sendPacket(packetData);
        }
    }

    private void sendPacket(byte[] packetData) throws IOException {
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, ip, port);
        sendingSocket.send(packet);
    }
}
