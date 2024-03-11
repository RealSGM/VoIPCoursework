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

public class VoiceSender implements Runnable {

    private final int socketNum;
    int port;
    InetAddress ip;
    DatagramSocket sendingSocket;

    int sequenceNumber = 0;
    
    double tempTime = 20;
    long elapsedTime = System.currentTimeMillis();
    
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

    @Override
    public void run() {
        PacketBlock packetBlock = new PacketBlock();
        long startTime = System.currentTimeMillis();
        boolean running = true;
        CyclicRedundancyCheck encoder = new CyclicRedundancyCheck();
        try {
            AudioRecorder recorder = new AudioRecorder();

            while (running) {
                
                // Network Analysis
                elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > tempTime * 1000) {
                    running = false;
                }

                // Packet Creation
                byte[] block = recorder.getBlock();
                byte[] encodedData = encoder.encode(block);
                byte[] encryptedData = encryptData(encodedData);
                byte[] encryptedPacket = createPacket(encryptedData);

                if (socketNum != 3) {
                    sendPacket(encryptedPacket);
                } else {
                    if (packetBlock.addPacketToBlock(encryptedPacket)){
                        sendPacketBlock(packetBlock);
                        packetBlock = new PacketBlock();
                    }
                }
            }

            System.out.printf("Packets Sent: %d. %n", sequenceNumber);
            System.out.println("Bitrate: " + (sequenceNumber * 612L / (elapsedTime / 1000)));

        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public byte[] encryptData(byte[] block) {
        // Initializing ByteBuffer for encryption
        int encryptionKey = 15;

        ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
        ByteBuffer plainText = ByteBuffer.wrap(block);

        for (int j = 0; j < block.length / 4; j++) {
            int fourByte = plainText.getInt();
            fourByte = fourByte ^ encryptionKey; // XOR operation with key
            unwrapEncrypt.putInt(fourByte);
        }

        return unwrapEncrypt.array();
    }
    
    private byte[] createPacket(byte[] encryptedData){
        long timestamp = System.currentTimeMillis();
        HeaderWrapper headerWrapper = new HeaderWrapper(timestamp, sequenceNumber);
        PacketWrapper packetWrapper = new PacketWrapper(headerWrapper, encryptedData);

        int size = packetWrapper.calculatePacketSize();
        // Creating a ByteBuffer for the voice packet
        ByteBuffer voicePacket = ByteBuffer.allocate(size);

        voicePacket.putShort(headerWrapper.getAuthenticationNumber());
        voicePacket.putInt(headerWrapper.getSequenceNumber());
        voicePacket.putLong(timestamp);
        voicePacket.put(encryptedData);
        sequenceNumber++;

        return voicePacket.array();
    }
    
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
